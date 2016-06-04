package com.astgspeech.services;

import java.io.IOException;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;

import com.astgspeech.BaseAgiRecoScript;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.RecognizeResponse.EndpointerEvent;

public class HelloWorld extends BaseAgiRecoScript {

	public HelloWorld() throws IOException {
		super();
	}

	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		/*
		answer();
        // ...say hello...
        streamFile("welcome");
        streamFile("tt-monkeys");
        // ...and hangup.
        hangup();
        */
		super.service(request, channel);
	}

	@Override
	public boolean onNext(String transcript, RecognizeResponse response) {
		//execute( new SetVariableCommand( "transcript" ,transcript  ) );
		return true;
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
		// TODO Auto-generated method stub
		return true;
	}



}
