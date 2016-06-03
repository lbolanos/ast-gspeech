# ast-gspeech
Asterisk Connection with Google Cloud Speech API

You have to follow the instructions to get google speech api working

https://cloud.google.com/speech/docs/

This project use eagi to send the audio throw java grpc to google and return the text.

yum install apache-maven

You have to follow the instructions from https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/speech/grpc

Visit the [Cloud Console](https://console.developers.google.com), and navigate to:
`API Manager > Credentials > Create credentials >
Service account key > New service account`.
Create a new service account, and download the json credentials file.

Then, set
the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to point to your
downloaded service account credentials before running this example:

    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/credentials-key.json

Download Code:
cd /var/lib/asterisk/agi-bin
git clone https://github.com/lbolanos/ast-gspeech.git
	
Then, build the program:

```sh
$ mvn package
```

Modify /etc/asterisk/extensions.conf

Example:
exten => 126,1,Answer()
exten => 126,n,Playback(vm-savemessage)
exten => 126,n,eagi(./ast-gspeech/bin/run.sh)
;exten => 126,n,Festival(${transcript})
exten => 126,n,Verbose(1,The text you just said is: ${transcript})
exten => 126,n,Playback(hello-world)
exten => 126,n,Hangup()

Output:
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



