package com.astgspeech.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.asteriskjava.fastagi.reply.AgiReply;
import org.asteriskjava.util.Log;
import org.asteriskjava.util.LogFactory;

import com.astgspeech.fasteagi.command.GetAudioSample;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

public class HelloAgiScript extends BaseAgiScript {
	
	private final Log logger = LogFactory.getLog(HelloAgiScript.class);

    public void service(AgiRequest request, AgiChannel channel)
            throws AgiException
    {
        // Answer the channel...
        answer();
        
        File file = new File("/var/lib/asterisk/agi-bin/ast-gspeech/log/audioB64.raw");
		FileOutputStream fop;
		try {
			fop = new FileOutputStream(file);        
			while( true ) {
				logger.info("GetAudioSample:"  );
				sendCommand(new GetAudioSample());
		        AgiReply lastReply = getLastReply();
		        String firstLine = lastReply.getFirstLine();
		        byte[] buffer = Base64.decodeBase64(firstLine);
		        
		        if( fop != null ) {
					fop.write(buffer);
				}
		        logger.info("writing:" + buffer.length );
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
                
        // ...say hello...
        streamFile("welcome");
                
        // ...and hangup.
        hangup();
    }
}