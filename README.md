# ast-gspeech
Asterisk Connection with Google Cloud Speech API

You have to follow the instructions to get google speech api working

https://cloud.google.com/speech/docs/

This project use eagi to send the audio throw java grpc to google and return the text.

```sh
wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
yum install apache-maven
```

You have to follow the instructions from https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/speech/grpc

Visit the [Cloud Console](https://console.developers.google.com), and navigate to:
`API Manager > Credentials > Create credentials >
Service account key > New service account`.
Create a new service account, and download the json credentials file.

Then, set
the `GOOGLE_APPLICATION_CREDENTIALS` /var/lib/asterisk/agi-bin/ast-gspeech/bin/run.sh variable to point to your
downloaded service account credentials before running this example:

    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/credentials-key.json

Download Code:
cd /var/lib/asterisk/agi-bin
git clone https://github.com/lbolanos/ast-gspeech.git

Write your service in /var/lib/asterisk/agi-bin/ast-gspeech/src/main/java/com/astgspeech/services


Then, build the program:

```sh
chmod 755 /var/lib/asterisk/agi-bin/ast-gspeech/bin/run.sh
dos2unix /var/lib/asterisk/agi-bin/ast-gspeech/bin/run.sh
cd /var/lib/asterisk/agi-bin/ast-gspeech/
$ mvn package
```

##HelloWorld
```
package com.astgspeech.services;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;

import com.astgspeech.BaseAgiRecoScript;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.RecognizeResponse.EndpointerEvent;
import com.google.cloud.speech.v1.SpeechRecognitionResult;

public class HelloWorld extends BaseAgiRecoScript {
	
	private String lastTranscript = "";

	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		answer();
		streamFile("beep");
		/*
        // ...say hello...
        streamFile("welcome");
        streamFile("tt-monkeys");
        // ...and hangup.
        hangup();
        */
		super.service(request, channel);
	}

	@Override
	public void onError(Throwable error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCompleted() {
		// TODO Auto-generated method stub		
	}

	@Override
	public boolean onEvent(EndpointerEvent endpoint) {
		switch( endpoint.getNumber() ) {
			case EndpointerEvent.END_OF_SPEECH_VALUE:
				if( lastTranscript.length() > 30 ) {
					return false;
				}
				return true;
		}
		return true;
	}

	@Override
	public boolean onNext(String transcript, float confidence, SpeechRecognitionResult speechRecognitionResult,
			RecognizeResponse response) {
		if( confidence > 0.8 ) {
			lastTranscript = transcript;
		}
		try {
			setVariable( "transcript" ,transcript );
		} catch (AgiException e) {
			e.printStackTrace(System.err);
		}
		return true;
	}

	@Override
	public boolean onFinal(String transcript, float confidence, SpeechRecognitionResult speechRecognitionResult,
			RecognizeResponse response) {
		try {
			setVariable( "transcript" ,transcript );
		} catch (AgiException e) {
			e.printStackTrace(System.err);
		}
		return false;
	}



}
```
##AnsweringMachine
```
package com.astgspeech.services;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.util.Log;
import org.asteriskjava.util.LogFactory;

import java.text.Normalizer;
import com.astgspeech.BaseAgiRecoScript;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.RecognizeResponse.EndpointerEvent;
import com.google.cloud.speech.v1.SpeechRecognitionResult;

public class AnsweringMachine extends BaseAgiRecoScript {
	
	private final Log logger = LogFactory.getLog(AnsweringMachine.class);
	
	private String lastTranscript = "";

	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {		
		/*
        // ...say hello...
        streamFile("welcome");
        streamFile("tt-monkeys");
        // ...and hangup.
        hangup();
        */
		super.service(request, channel);
	}

	@Override
	public void onError(Throwable error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCompleted() {
		// TODO Auto-generated method stub		
	}

	@Override
	public boolean onEvent(EndpointerEvent endpoint) {
		switch( endpoint.getNumber() ) {
			case EndpointerEvent.END_OF_SPEECH_VALUE:
				if( lastTranscript.length() > 30 ) {
					return false;
				}
				return true;
		}
		return true;
	}

	@Override
	public boolean onNext(String transcript, float confidence, SpeechRecognitionResult speechRecognitionResult,
			RecognizeResponse response) {
		try{
			if( confidence > 0.8 ) {
				lastTranscript = transcript;
			}
			setVariable( "transcript" ,transcript );
			
			String tol = Normalizer.normalize(transcript, Normalizer.Form.NFD).toLowerCase()
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			logger.info("onNext:" + tol + " confidence:" + confidence );
			if( confidence > 0.6 && 
				( tol.contains("grabe su mensaje" )  ||
					tol.contains("mensaje despues del tono" )  ||
					tol.contains( "lleno y no puede recibir" ) ||
					tol.contains( "buzon" ) 
					) ) {
				setVariable( "MACHINE" ,"TRUE" );
				hangup();				
				return false;
			}
			//logger.info("onNext: not found" );
			if( confidence > 0.6 && 
				( tol.contains("alo" ) ) ){
				setVariable( "MACHINE" ,"FALSE" );
				return false;
			}
		} catch (AgiException e) {
			e.printStackTrace(System.err);
		}
		return true;
	}

	@Override
	public boolean onFinal(String transcript, float confidence, SpeechRecognitionResult speechRecognitionResult,
			RecognizeResponse response) {
		try {
			setVariable( "transcript" ,transcript );
		} catch (AgiException e) {
			e.printStackTrace(System.err);
		}
		return false;
	}



}

```


Modify /etc/asterisk/extensions.conf
```
Example 1:
exten => 127,1,eagi(./ast-gspeech/bin/run.sh,com.astgspeech.services.HelloWorld)
exten => 127,n,Verbose(1,The text you just said is: ${transcript})
exten => 127,n,Playback(hello-world)
exten => 127,n,Hangup()
```

```
Example 2:
exten => 126,1,Answer()
exten => 126,n,Playback(vm-savemessage)
exten => 126,n,eagi(./ast-gspeech/bin/run.sh)
;exten => 126,n,Festival(${transcript})
exten => 126,n,Verbose(1,The text you just said is: ${transcript})
exten => 126,n,Playback(hello-world)
exten => 126,n,Hangup()
```
Output:
```
Received response: results {
  alternatives {
    transcript: "el d\303\255a de entrega de notas recibida la orden particular quienes hayan aprobado todas las materias iespien a paz y salvo"
    confidence: 0.80237895
  }
  is_final: true
}

SET VARIABLE "transcript" "el día de entrega de notas recibida la orden particular quienes hayan aprobado todas las materias y estén a paz y salvo" returned:200 result=1
Sent 332800 bytes from audio file:
Received response: endpoint: END_OF_AUDIO

recognize completed.
Jun 02, 2016 6:25:06 PM io.grpc.internal.TransportSet$TransportListener transportShutdown
INFO: Transport io.grpc.netty.NettyClientTransport@457b01e5(speech.googleapis.com/216.58.218.10:443) for speech.googleapis.com/216.58.218.10:443 is being shutdown
Jun 02, 2016 6:25:06 PM io.grpc.internal.TransportSet$TransportListener transportTerminated
INFO: Transport io.grpc.netty.NettyClientTransport@457b01e5(speech.googleapis.com/216.58.218.10:443) for speech.googleapis.com/216.58.218.10:443 is terminated
    -- <SIP/6001-00000003>AGI Script ./astexample/bin/run.sh completed, returning 0
    -- Executing [126@from-internal:4] Verbose("SIP/6001-00000003", "1,The text you just said is: el día de entrega de notas recibida la orden particular quienes hayan aprobado todas las materias y estén a paz y salvo") in new stack
 The text you just said is: el día de entrega de notas recibida la orden particular quienes hayan aprobado todas las materias y estén a paz y salvo
    -- Executing [126@from-internal:5] Playback("SIP/6001-00000003", "hello-world") in new stack
    -- <SIP/6001-00000003> Playing 'hello-world.gsm' (language 'en')
    -- Executing [126@from-internal:6] Hangup("SIP/6001-00000003", "") in new stack
  == Spawn extension (from-internal, 126, 6) exited non-zero on 'SIP/6001-00000003'
```


