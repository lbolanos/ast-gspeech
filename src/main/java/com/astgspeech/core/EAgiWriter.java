package com.astgspeech.core;


import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.command.AgiCommand;


/**
 * Default implementation of the AGIWriter interface.
 * 
 * @author srt
 * @version $Id: FastAgiWriter.java 1015 2008-04-04 21:56:36Z srt $
 */
class EAgiWriter {
    EAgiWriter()    {
    }

    public void sendCommand(AgiCommand command) throws AgiException {
       	System.out.println(command.buildCommand());
    }
}
