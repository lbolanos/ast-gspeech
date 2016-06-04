package com.astgspeech.services;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;

import com.astgspeech.BaseAgiRecoScript;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.RecognizeResponse.EndpointerEvent;
import com.google.cloud.speech.v1.SpeechRecognitionResult;

public class AnsweringMachine extends BaseAgiRecoScript {
	
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
			if( transcript.contains("mensaje después del tono" ) ) {
				setVariable( "MACHINE" ,"TRUE" );
				hangup();				
				return false;
			}
			if( transcript.contains("aló" ) ) {
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
