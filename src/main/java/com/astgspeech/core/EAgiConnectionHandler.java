package com.astgspeech.core;

import java.io.IOException;
import java.io.LineNumberReader;

import org.asteriskjava.fastagi.MappingStrategy;
import org.asteriskjava.util.SocketConnectionFacade;


public class EAgiConnectionHandler extends CoreAgiConnectionHandler
{

    private LineNumberReader socket;

	/**
     * Creates a new EAGIConnectionHandler to handle the given EAGI socket connection.
     *
     * @param mappingStrategy the strategy to use to determine which script to run.
     * @param socket the socket connection to handle.
     */
    public EAgiConnectionHandler(MappingStrategy mappingStrategy, LineNumberReader socket )
    {
        super(mappingStrategy);
        this.socket = socket;
    }

    @Override
    protected EAgiReader createReader()    {
        return new EAgiReader(socket);
    }

    @Override
    protected EAgiWriter createWriter() {
        return new EAgiWriter();
    }

    @Override
    protected void release()
    {
        try
        {
            socket.close();
        }
        catch (IOException e) // NOPMD
        {
            // swallow
        }
    }
}
