package com.astgspeech.fasteagi.command;

import org.asteriskjava.fastagi.command.AbstractAgiCommand;

public class GetAudioSample extends AbstractAgiCommand {
	    /**
	     * Serial version identifier.
	     */
	    private static final long serialVersionUID = 376273443229053753L;

	    /**
	     * Creates a new AnswerCommand.
	     */
	    public GetAudioSample()
	    {
	        super();
	    }

	    @Override
	   public String buildCommand()
	    {
	        return "GETAUDIOSAMPLE";
	    }
	}
