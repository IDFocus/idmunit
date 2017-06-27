/*
 * IDMUnit Connector for local or remote command execution. 
 * Code created from IDMUnit source. 
 */
package org.idmunit.connector;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.idmunit.IdMUnitException;
import org.idmunit.connector.BasicConnector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * Implements an IdMUnit connector which executes commands locally or remotely over SSH. 
 * @author Mark van Reijn, idfocus
 * @version 1.0
 */
public class ExecuteConnector extends BasicConnector {
    protected Map<String, String> config;

    private static final String BIN_DIR = "bin-dir";
    private static final String SHELL = "shell";
    // Max length of the commandline.
	private static final int CMD_BUFFER = 256;

    private static Log log = LogFactory.getLog(ExecuteConnector.class);
    //protected String m_driverInputFilePath;
    protected String m_commandShell;

    /*
     *     <connection>
     *         <name>Execute</name>
     *         <description>Connector for executing local commands</description>
     *         <type>org.idmunit.ExecuteConnector</type>
     *         <server/>
     *         <shell>/bin/bash</shell>
     *         <multiplier/>
     *         <substitutions/>
     *         <data-injections/>
     *     </connection>
     */
    public void setup(Map<String, String> config) throws IdMUnitException {
    	this.config = config;
    	
        if (config.get(BIN_DIR) == null) {
            throw new IdMUnitException("'" + BIN_DIR + "' not configured");
        }

        m_commandShell = config.get(SHELL);
        if (m_commandShell == null) {
            throw new IdMUnitException("'" + SHELL + "' not configured");
        }

        log.info("##### Command Shell: " + m_commandShell);
    }

	private String buildCmdLine( Map<String, Collection<String>> data ) 
	{
		// FIXME use the values from config here
		Set<String> keySet = data.keySet();
		StringBuffer cmdData = new StringBuffer( CMD_BUFFER );
		for ( Iterator<String> iter = keySet.iterator(); iter.hasNext(); ) 
		{
			String attrName = iter.next();
			log.info("Processing key: ");
			log.info( attrName );

			// FIXME this will append all data in the order from the sheet. Should check for command and arguments only
			Collection<String> attrVal = data.get( attrName );
			//Append the data to the data entry here
			for ( Iterator<String> iter2 = attrVal.iterator(); iter2.hasNext(); ) 
			{
				cmdData.append( iter2.next().trim() );
				if ( iter2.hasNext() )
				{
					cmdData.append( " " );
				}
			}
			if ( iter.hasNext() )
			{
				cmdData.append( " " );
			}
		}
		return cmdData.toString();
	}

	public void opLocalCmd( Map<String, Collection<String>> data ) throws IdMUnitException
	{
		String cmd = buildCmdLine( data );
		// FIXME Hardcoded. Should be a cwd.
		String cmdLine = m_commandShell + " " + cmd;
		log.info( "...executing commandline: " );
		log.info( cmdLine );
		Runtime run = Runtime.getRuntime() ;
		try 
		{
			Process pr = run.exec( cmdLine ) ;
			int exitStatus = pr.waitFor() ;
			log.info( "Command finished with exit status " + exitStatus + ", output: " );

			BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
			String line;
			while ( ( line = buf.readLine() ) != null )
			{
				log.info( line ) ;
			}
			
			if ( exitStatus > 0 )
			{
				throw new IdMUnitException( "Command returned error status (" + exitStatus + ")" );
			}
		} catch ( Exception e ) {
			throw new IdMUnitException( "Failed to execute " + cmd + ". Error: " + e.getMessage() );
		}
		log.info( "...SUCCESS" );
		
	}

	public void opRemoteCmd( Map<String, Collection<String>> data ) throws IdMUnitException
	{
		String cmd = buildCmdLine( data );
		// FIXME Hardcoded. Should be a cwd.
		String cmdLine = m_commandShell + " " + cmd;
		log.info( "...executing commandline: " );
		log.info( cmdLine );
		JSch jsch = new JSch();  

		try 
		{
			// FIXME port is not configurable
			Session session = jsch.getSession( config.get( CONFIG_USER ), config.get( CONFIG_SERVER ), 22 );

			java.util.Properties sshconfig = new java.util.Properties(); 
			sshconfig.put( "StrictHostKeyChecking" , "no" );
			session.setConfig( sshconfig );

			session.setPassword( (String)config.get( CONFIG_PASSWORD ) );
			session.connect();

			Channel channel=session.openChannel( "exec" );
			( (ChannelExec) channel ).setCommand( cmdLine );

			channel.setInputStream( null );
			( (ChannelExec) channel ).setErrStream( System.err );
			InputStream in=channel.getInputStream();

			channel.connect();

			int exitStatus;
			String output = "";

			byte[] tmp=new byte[1024];
			while ( true )
			{
				while ( in.available() > 0 )
				{
					int i = in.read( tmp, 0, 1024 );
					if ( i < 0 ) break;
					output += new String ( tmp, 0, i );
				}
				if ( channel.isClosed() )
				{
					exitStatus = channel.getExitStatus();
					break;
				}
				try { Thread.sleep( 1000 ); } catch ( Exception ee ){}
			}
			channel.disconnect();
			session.disconnect();
			log.info( output );
			log.info( "Command finished with exit status " + exitStatus + ", output: " );
			System.out.print( "output: " + output );

			if ( exitStatus > 0 )
			{
				throw new IdMUnitException( "Command returned error status (" + exitStatus + ")" );
			}
		} catch ( Exception e ) {
			throw new IdMUnitException( "Failed to execute " + cmd + ". Error: " + e.getMessage() );
		}
		log.info( "...SUCCESS" );
		
	}

}