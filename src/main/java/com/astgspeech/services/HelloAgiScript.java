package com.astgspeech.services;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.astgspeech.fasteagi.command.GetAudioSample;

public class HelloAgiScript extends BaseAgiScript
{
    public void service(AgiRequest request, AgiChannel channel)
            throws AgiException
    {
        // Answer the channel...
        answer();
        
        sendCommand(new GetAudioSample());
                
        // ...say hello...
        streamFile("welcome");
                
        // ...and hangup.
        hangup();
    }
}