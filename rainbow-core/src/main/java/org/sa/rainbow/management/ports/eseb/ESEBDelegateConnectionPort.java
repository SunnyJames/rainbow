package org.sa.rainbow.management.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.RainbowConstants;
import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.management.ports.AbstractDelegateConnectionPort;
import org.sa.rainbow.management.ports.DisconnectedRainbowManagementPort;
import org.sa.rainbow.management.ports.IRainbowManagementPort;
import org.sa.rainbow.management.ports.RainbowManagementPortFactory;
import org.sa.rainbow.management.ports.eseb.ESEBConnector.IESEBListener;

public class ESEBDelegateConnectionPort extends AbstractDelegateConnectionPort {
    static Logger                  LOGGER = Logger.getLogger (ESEBDelegateConnectionPort.class);
    private ESEBConnector   m_connectionRole;
    private IRainbowManagementPort m_deploymentPort;

    public ESEBDelegateConnectionPort (RainbowDelegate delegate) throws IOException {
        super (delegate);
        String port = Rainbow.properties ().getProperty (RainbowConstants.PROPKEY_MASTER_CONNECTION_PORT);
        if (port == null) {
            port = Rainbow.properties ().getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION_PORT);
            if (port == null) {
                port = "1234";
            }
        }
        short p = Short.valueOf (port);
        m_connectionRole = new ESEBConnector (Rainbow.properties ().getProperty (
                RainbowConstants.PROPKEY_MASTER_LOCATION), p);
        m_connectionRole.addListener (new IESEBListener() {

            @Override
            public void receive (Map<String, String> msg) {
                String type = msg.get (ESEBConstants.MSG_TYPE_KEY);
                switch (type) {
                case ESEBConstants.MSG_TYPE_CONNECT_DELEGATE: {
                    if (m_delegate.getId ().equals (msg.get (ESEBConstants.TARGET))) {
                        m_delegate.disconnectFromMaster ();
                    }
                }
                }
            }
        });
    }

    @Override
    public IRainbowManagementPort connectDelegate (String delegateID, Properties connectionProperties) throws RainbowConnectionException {
        /*
         * connectionProperties should contain the following information: 
         * PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT, PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST: 
         *    Information regarding how to connect to the deployment connector on
         *    the delegate. Note that the delegate needs to create a server that gets passed to the master, 
         *    who becomes a client of the this server. 
         * PROPKEY_ESEB_DELEGATE_CONNECTION_PORT: The port of the delegate connection port
         * server, which will be which will be used by the master to reply to this connection port.
         */
        Map<String, String> msg = m_connectionRole.encodePropertiesAsMap (connectionProperties);
        short deploymentPortNum;
        String port = connectionProperties.getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT, Rainbow
                .properties ().getProperty (Rainbow.PROPKEY_MASTER_DEPLOYMENT_PORT, "1234"));
        msg.put (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT, port);
        deploymentPortNum = Short.valueOf (port);
        String host = connectionProperties.getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST, Rainbow
                .properties ().getProperty (Rainbow.PROPKEY_DEPLOYMENT_LOCATION, "localhost"));
        msg.put (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST, host);
        msg.put (ESEBConstants.MSG_DELEGATE_ID_KEY, delegateID);
        msg.put (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_CONNECT_DELEGATE);

        m_deploymentPort = null;

        m_connectionRole.blockingSendAndReceive (msg, new IESEBListener () {
            @Override
            public void receive (Map<String, String> msgRcvd) {
                String reply = msgRcvd.get (ESEBConstants.MSG_CONNECT_REPLY);
                if (!ESEBConstants.MSG_REPLY_OK.equals (reply)) {
                    LOGGER.error (MessageFormat.format (
                            "Delegate {0}: connectDelegate received the following reply: {1}", m_delegate.getId (),
                            reply));
                }
                else {
                    try {
                        m_deploymentPort = RainbowManagementPortFactory.createDelegateDeploymentPort (m_delegate,
                                m_delegate.getId ());
                    }
                    catch (RainbowConnectionException e) {

                    }
                }
            }
        }, 10000);
        if (m_deploymentPort == null) {
            LOGGER.error ("The call to connectDelegate timed out without returning a deployment port...");
            // REVIEW: Throw an exception instead
            m_deploymentPort = DisconnectedRainbowManagementPort.instance ();
        }
        return m_deploymentPort;
    }

    @Override
    public void disconnectDelegate (String delegateId) {
        Map<String, String> msg = new HashMap<> ();
        msg.put (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_DISCONNECT_DELEGATE);
        msg.put (ESEBConstants.MSG_DELEGATE_ID_KEY, delegateId);
        m_connectionRole.publish (msg);
    }

    @Override
    public void dispose () {
        try {
            m_connectionRole.close ();
        }
        catch (IOException e) {
            LOGGER.warn (MessageFormat.format ("Could not close the connection port for delegate {0}",
                    m_delegate.getId ()));
        }
    }

}