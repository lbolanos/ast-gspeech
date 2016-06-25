package com.astgspeech.core;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.asteriskjava.fastagi.AbstractAgiServer;
import org.asteriskjava.fastagi.AgiScript;
import org.asteriskjava.fastagi.AgiServer;
import org.asteriskjava.fastagi.ClassNameMappingStrategy;
import org.asteriskjava.fastagi.CompositeMappingStrategy;
import org.asteriskjava.fastagi.MappingStrategy;
import org.asteriskjava.fastagi.ResourceBundleMappingStrategy;
import org.asteriskjava.fastagi.StaticMappingStrategy;
import org.asteriskjava.util.Log;
import org.asteriskjava.util.LogFactory;
import org.asteriskjava.util.ReflectionUtil;
import org.asteriskjava.util.ServerSocketFacade;
import org.asteriskjava.util.internal.ServerSocketFacadeImpl;

/**
 * Default implementation of the {@link org.asteriskjava.fastagi.AgiServer} interface for FastAGI.
 *
 * @author srt
 * @version $Id: DefaultEAgiServer.java 1304 2009-05-12 22:51:12Z srt $
 */
public class DefaultEAgiServer extends AbstractAgiServer implements AgiServer
{
    private final Log logger = LogFactory.getLog(getClass());

    /**
     * The default name of the resource bundle that contains the config.
     */
    private static final String DEFAULT_CONFIG_RESOURCE_BUNDLE_NAME = "fastagi";

    /**
     * The default bind port.
     */
    private static final int DEFAULT_BIND_PORT = 4573;

    private ServerSocketFacade serverSocket;
    private String configResourceBundleName = DEFAULT_CONFIG_RESOURCE_BUNDLE_NAME;
    private int port = DEFAULT_BIND_PORT;

    /**
     * Creates a new DefaultEAgiServer.
     */
    public DefaultEAgiServer()
    {
        this(null, null);
    }

    /**
     * Creates a new DefaultEAgiServer and loads its configuration from an alternative resource bundle.
     *
     * @param configResourceBundleName the name of the conifiguration resource bundle (default is "fastagi").
     */
    public DefaultEAgiServer(String configResourceBundleName)
    {
        this(configResourceBundleName, null);
    }

    /**
     * Creates a new DefaultEAgiServer that uses the given {@link MappingStrategy}.
     *
     * @param mappingStrategy the MappingStrategy to use to determine the AgiScript to run.
     * @since 1.0.0
     */
    public DefaultEAgiServer(MappingStrategy mappingStrategy)
    {
        this(null, mappingStrategy);
    }

    /**
     * Creates a new DefaultEAgiServer that runs the given {@link AgiScript} for all requests.
     *
     * @param agiScript the AgiScript to run.
     * @since 1.0.0
     */
    public DefaultEAgiServer(AgiScript agiScript)
    {
        this(null, new StaticMappingStrategy(agiScript));
    }

    /**
     * Creates a new DefaultEAgiServer and loads its configuration from an alternative resource bundle and
     * uses the given {@link MappingStrategy}.
     *
     * @param configResourceBundleName the name of the conifiguration resource bundle (default is "fastagi").
     * @param mappingStrategy          the MappingStrategy to use to determine the AgiScript to run.
     * @since 1.0.0
     */
    public DefaultEAgiServer(String configResourceBundleName, MappingStrategy mappingStrategy)
    {
        super();
        if (mappingStrategy == null)
        {
            final CompositeMappingStrategy compositeMappingStrategy = new CompositeMappingStrategy();

            compositeMappingStrategy.addStrategy(new ResourceBundleMappingStrategy());
            compositeMappingStrategy.addStrategy(new ClassNameMappingStrategy());
            if (ReflectionUtil.isClassAvailable("javax.script.ScriptEngineManager"))
            {
                MappingStrategy scriptEngineMappingStrategy =
                        (MappingStrategy) ReflectionUtil.newInstance("org.asteriskjava.fastagi.ScriptEngineMappingStrategy");
                if (scriptEngineMappingStrategy != null)
                {
                    compositeMappingStrategy.addStrategy(scriptEngineMappingStrategy);
                }
            }
            else
            {
                logger.warn("ScriptEngine support disabled: It is only availble when running at least Java 6");
            }

            setMappingStrategy(compositeMappingStrategy);
        }
        else
        {
            setMappingStrategy(mappingStrategy);
        }

        if (configResourceBundleName != null)
        {
            this.configResourceBundleName = configResourceBundleName;
        }

        loadConfig();
    }


    /**
     * Sets the TCP port to listen on for new connections.
     * <p/>
     * The default port is 4573.
     *
     * @param bindPort the port to bind to.
     * @deprecated use {@see #setPort(int)} instead
     */
    public void setBindPort(int bindPort)
    {
        this.port = bindPort;
    }

    /**
     * Sets the TCP port to listen on for new connections.
     * <p/>
     * The default port is 4573.
     *
     * @param port the port to bind to.
     * @since 0.2
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * Returns the TCP port this server is configured to bind to.
     *
     * @return the TCP port this server is configured to bind to.
     * @since 1.0.0
     */
    public int getPort()
    {
        return port;
    }

    private void loadConfig()
    {
        final ResourceBundle resourceBundle;

        try
        {
            resourceBundle = ResourceBundle.getBundle(configResourceBundleName);
        }
        catch (MissingResourceException e)
        {
            return;
        }

        try
        {
            String portString;

            try
            {
                portString = resourceBundle.getString("port");
            }
            catch (MissingResourceException e)
            {
                // for backward compatibility only
                portString = resourceBundle.getString("bindPort");
            }
            port = Integer.parseInt(portString);
        }
        catch (Exception e) // NOPMD
        {
            // swallow
        }

        try
        {
            setPoolSize(Integer.parseInt(resourceBundle.getString("poolSize")));
        }
        catch (Exception e) // NOPMD
        {
            // swallow
        }

        try
        {
            setMaximumPoolSize(Integer.parseInt(resourceBundle.getString("maximumPoolSize")));
        }
        catch (Exception e) // NOPMD
        {
            // swallow
        }
    }

    protected ServerSocketFacade createServerSocket() throws IOException
    {
        return new ServerSocketFacadeImpl(port, 0, null);
    }

    public void startup() throws IOException, IllegalStateException {
        EAgiConnectionHandler connectionHandler;

       	LineNumberReader in = new LineNumberReader(new InputStreamReader(System.in));
		connectionHandler = new EAgiConnectionHandler(getMappingStrategy(), in);
		connectionHandler.run();

    }

    public void run()
    {
        try
        {
            startup();
        }
        catch (IOException e) // NOPMD
        {
            // nothing we can do about that and exceptions have already been logged
            // by startup().
        }
    }

    @Override
    public void shutdown() throws IllegalStateException
    {
        // setting the death flag causes the accept() loop to exit when a
        // SocketException occurs.
        super.shutdown();

        if (serverSocket != null)
        {
            try
            {
                // closes the server socket and throws a SocketException on
                // Threads waiting in accept()
                serverSocket.close();
            }
            catch (IOException e)
            {
                logger.warn("IOException while closing server socket.", e);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();

        if (serverSocket != null)
        {
            try
            {
                serverSocket.close();
            }
            catch (IOException e) // NOPMD
            {
                // swallow
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        final AgiServer server;

        server = new DefaultEAgiServer();
        server.startup();
    }
}
