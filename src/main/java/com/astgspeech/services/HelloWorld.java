package com.astgspeech.services;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;

import com.astgspeech.BaseAgiRecoScript;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.RecognizeResponse.EndpointerEvent;
import com.google.cloud.speech.v1.SpeechRecognitionResult;

public class HelloWorld extends BaseAgiRecoScript {

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
				return false;
		}
		return true;
	}

	@Override
	public boolean onNext(String transcript, float confidence, SpeechRecognitionResult speechRecognitionResult,
			RecognizeResponse response) {
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
