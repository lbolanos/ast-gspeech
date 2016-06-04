package com.astgspeech;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.asteriskjava.fastagi.command.AgiCommand;
import org.asteriskjava.fastagi.command.ChannelStatusCommand;
import org.asteriskjava.fastagi.command.SayDigitsCommand;
import org.asteriskjava.fastagi.command.SetVariableCommand;
import org.asteriskjava.fastagi.command.StreamFileCommand;
import org.asteriskjava.fastagi.command.VerboseCommand;
import org.asteriskjava.manager.action.PlayDtmfAction;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.AudioRequest;
import com.google.cloud.speech.v1.InitialRecognizeRequest;
import com.google.cloud.speech.v1.InitialRecognizeRequest.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeRequest;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechGrpc;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import com.google.protobuf.TextFormat;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.auth.ClientAuthInterceptor;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

/**
 * Hello world!
 *
 */
public class Recognize {

	private static LineNumberReader in = new LineNumberReader(new InputStreamReader(System.in));

	private static String agi_channel;

	private final ManagedChannel channel;

	private final SpeechGrpc.SpeechStub stub;

	private int samplingRate;

	private static final List<String> OAUTH2_SCOPES = Arrays.asList("https://www.googleapis.com/auth/cloud-platform");

	public Recognize() throws IOException {
		String host = "speech.googleapis.com";
		Integer port = 443;
		this.samplingRate = 8000;
		GoogleCredentials creds = GoogleCredentials.getApplicationDefault();
		creds = creds.createScoped(OAUTH2_SCOPES);
		channel = NettyChannelBuilder.forAddress(host, port).negotiationType(NegotiationType.TLS)
				.intercept(new ClientAuthInterceptor(creds, Executors.newSingleThreadExecutor())).build();
		stub = SpeechGrpc.newStub(channel);
		System.err.println("Created stub for " + host + ":" + port);
	}

	public static String execute(AgiCommand command) throws IOException {
		String buildCommand = command.buildCommand();
		System.out.println(buildCommand);
		String checkresults = checkresults();
		System.err.println( buildCommand + " returned:" + checkresults);
		return checkresults;
	}
	
	public static String getCode( String linea ) {
		return linea.substring(0, 3);
	}
	
	public static String getResult( String linea ) {
		return linea.substring(linea.lastIndexOf("=") + 1);
	}

	public static void readAGIEnv() throws IOException {
		String linea;
		do { // read AGI environment
			linea = in.readLine();
			System.err.println(linea);
			if( linea.contains("agi_channel") ) {
				agi_channel = linea.substring(linea.lastIndexOf(":") + 2);
			}
		} while (linea.length() > 0);
	}

	public static String checkresults() throws IOException {
		String linea = in.readLine();		
		return linea;
	}

	public static void readMedia() throws IOException {
		FileInputStream fin = getFIS();

		String strDestinationFile = "filedata.gsm";
		// create FileOutputStream object for destination file
		FileOutputStream fout = new FileOutputStream(strDestinationFile);

		byte[] b = new byte[1024];
		int noOfBytes = 0;
		int total = 0;

		// System.out.println("Copying file using streams");

		// read bytes from source file and write to destination file
		while ((noOfBytes = fin.read(b)) != -1 && total <= 800000) {
			fout.write(b, 0, noOfBytes);
			total += noOfBytes;
		}

		// System.out.println("File copied!");

		// close the streams
		fin.close();
		fout.close();
	}

	@SuppressWarnings("restriction")
	private static FileInputStream getFIS() {
		FileDescriptor fd = new FileDescriptor();
		sun.misc.SharedSecrets.getJavaIOFileDescriptorAccess().set(fd, 3);
		FileInputStream fin = new FileInputStream(fd);
		return fin;
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	/** Send streaming recognize requests to server. */
	public void recognize() throws InterruptedException, IOException {
		final CountDownLatch finishLatch = new CountDownLatch(1);
		final InfiniteLoop infinite = new InfiniteLoop( true );
		StreamObserver<RecognizeResponse> responseObserver = new StreamObserver<RecognizeResponse>() {
			@Override
			public void onNext(RecognizeResponse response) {
				System.err.println("Received response: " + TextFormat.printToString(response));
				try {
					//	execute( new SayDigitsCommand(Long.toString(finishLatch.getCount() ) ) );
					for (SpeechRecognitionResult speechRecognitionResult : response.getResultsList()) {
						if( speechRecognitionResult.getIsFinal() ) {
							for (SpeechRecognitionAlternative speechRecognitionAlternative : speechRecognitionResult.getAlternativesList()) {
								String transcript = speechRecognitionAlternative.getTranscript();
								execute( new SetVariableCommand( "transcript" ,transcript  ) );
							}
							infinite.setInfinite( false );
						}
					}
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}

			@Override
			public void onError(Throwable error) {
				Status status = Status.fromThrowable(error);
				System.err.println("recognize failed: {0}" + status);
				finishLatch.countDown();
			}

			@Override
			public void onCompleted() {
				System.err.println("recognize completed.");
				finishLatch.countDown();
			}
		};
		//execute( new SayDigitsCommand("1") );
		//execute( new StreamFileCommand("beep") );
		
		StreamObserver<RecognizeRequest> requestObserver = stub.recognize(responseObserver);
		try {
			// Build and send a RecognizeRequest containing the parameters for
			// processing the audio.
			InitialRecognizeRequest initial = InitialRecognizeRequest.newBuilder()
					.setEncoding(AudioEncoding.LINEAR16)
					.setSampleRate(samplingRate)
					.setLanguageCode("es-CO")
					.setContinuous(true)
					.setEnableEndpointerEvents(true)
					.setInterimResults(true)
					.build();
			RecognizeRequest firstRequest = RecognizeRequest.newBuilder().setInitialRequest(initial).build();
			requestObserver.onNext(firstRequest);

			// Open audio file. Read and send sequential buffers of audio as
			// additional RecognizeRequests.
			FileInputStream inFIS = getFIS();//new FileInputStream(new File(file));
			DataInputStream in = new DataInputStream( inFIS );
			// For LINEAR16 at 16000 Hz sample rate, 3200 bytes corresponds to
			// 100 milliseconds of audio.
			byte[] buffer = new byte[3200];
			int bytesRead;
			int totalBytes = 0;
			//while ((bytesRead = in.read(buffer)) != -1) {
			Date now = new Date();
			Date before = now;
			while(infinite.isInfinite()){
				try{					
					int available = in.available();
					//System.err.println("Data available:" + available );					
					if( available >= 3200 ) {
						in.readFully(buffer);
						bytesRead = buffer.length;
						int delay = 3;
						now = new Date();
						long timeNow = now.getTime();
						long elapsed = timeNow - before.getTime();					
						System.err.println("Sent " + bytesRead + " elapsed:" + elapsed + " msec:" + timeNow );
						before = now;
						if( elapsed < 180 ) {
							//continue;
							delay = 20;
						}
						
						totalBytes += bytesRead;
						AudioRequest audio = AudioRequest.newBuilder().setContent(ByteString.copyFrom(buffer, 0, bytesRead))
								.build();
						RecognizeRequest request = RecognizeRequest.newBuilder().setAudioRequest(audio).build();
						requestObserver.onNext(request);
						// To simulate real-time audio, sleep after sending each audio
						// buffer.
						// For 16000 Hz sample rate, sleep 100 milliseconds.
						Thread.sleep( ( samplingRate / 40) -  delay );
					} else {
						String execute = execute( new ChannelStatusCommand(agi_channel) );
						String result = getCode( execute );
						if( result.equals("511")) {
							break;
						}
						result = getResult( execute );
						if( result.equals("0")) {
							break;
						}
						Thread.sleep( ( samplingRate / 40) -  3 );
						now = new Date();
						long timeNow = now.getTime();
						long elapsed = timeNow - before.getTime();					
						System.err.println("No DATA elapsed:" + elapsed + " msec:" + timeNow );
						if( elapsed > 3000 ) {
							break;
						}
					}
					
				} catch ( EOFException eof ) {
					System.err.println("EOF " + totalBytes );
					break;
				}
			}
			System.err.println("Sent " + totalBytes + " bytes from audio file: ");
		} catch (RuntimeException e) {
			// Cancel RPC.
			requestObserver.onError(e);
			throw e;
		}
		// Mark the end of requests.
		requestObserver.onCompleted();

		// Receiving happens asynchronously.
		finishLatch.await(1, TimeUnit.MINUTES);
	}

	public static void main(String[] args) {
		try {
			readAGIEnv();
			execute(new VerboseCommand("TestApp 3",	1));			
			//execute( new StreamFileCommand("filedata"));
			//execute( new SayDigitsCommand("12354") );
			//execute( new VerboseCommand("Recording", 1) );
			//execute( new RecordFileCommand("testwavgsm","gsm","#", 5000 ) );
			//execute( new VerboseCommand("Playing", 1) );
			//execute( new StreamFileCommand("testwavgsm") );
			//readMedia();
				

			Recognize client = new Recognize();
			//execute( new SayDigitsCommand("12354") );
			try {
				client.recognize();
			} finally {
				client.shutdown();
			}

		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		}
	}
}
