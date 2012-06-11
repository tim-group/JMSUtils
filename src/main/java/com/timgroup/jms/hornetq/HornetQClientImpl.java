package com.timgroup.jms.hornetq;

import java.net.URI;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.QueueConnection;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;

import com.timgroup.jms.JMSClient;
import com.timgroup.jms.JMSUtil;
import com.timgroup.util.Utils;

public class HornetQClientImpl extends JMSClient {
    
    private static final String JMS_QUEUE_PREFIX = "jms.queue.";
    
    public static class Factory implements JMSClient.Factory {
        @Override
        public JMSClient create(URI uri) throws JMSException {
            return new HornetQClientImpl(uri);
        }
    }
    
    private final ClientSession session;
    private final TransportConfiguration transportConfiguration;
    
    public HornetQClientImpl(String host, int port, String username, String password, String queueName) throws JMSException {
        super(queueName);
        transportConfiguration = makeTransportConfiguration(host, port);
        session = createSession(transportConfiguration, username, password);
    }
    
    public HornetQClientImpl(String host, int port, String queueName) throws JMSException {
        this(host, port, null, null, queueName);
    }
    
    public HornetQClientImpl(URI uri) throws JMSException {
        this(uri.getHost(), Utils.defaulting(uri.getPort(), -1, 5445), Utils.username(uri), Utils.password(uri), uri.getPath());
    }
    
    private TransportConfiguration makeTransportConfiguration(String host, int port) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(TransportConstants.HOST_PROP_NAME, host);
        params.put(TransportConstants.PORT_PROP_NAME, port);
        return new TransportConfiguration(NettyConnectorFactory.class.getName(), params);
    }
    
    private ClientSession createSession(TransportConfiguration transportConfiguration, String username, String password)
            throws JMSException {
        boolean ha = false;
        ServerLocator locator = createServerLocator(transportConfiguration, ha);
        ClientSessionFactory factory = createSessionFactory(locator);
        
        // these defaults are copied from org.hornetq.core.client.impl.ClientSessionFactoryImpl
        boolean xa = false;
        boolean autoCommitSends = true;
        boolean autoCommitAcks = true;
        boolean preAcknowledge = locator.isPreAcknowledge();
        int ackBatchSize = locator.getAckBatchSize();
        
        return createSession(factory, username, password, xa, autoCommitSends, autoCommitAcks, preAcknowledge, ackBatchSize);
    }
    
    private ClientSession createSession(ClientSessionFactory factory,
                                        String username,
                                        String password,
                                        boolean xa,
                                        boolean autoCommitSends,
                                        boolean autoCommitAcks,
                                        boolean preAcknowledge,
                                        int ackBatchSize) throws JMSException {
        try {
            return factory.createSession(username, password, xa, autoCommitSends, autoCommitAcks, preAcknowledge, ackBatchSize);
        }
        catch (HornetQException e) {
            throw JMSUtil.newJMSException("error creating session for " + username + ":" + password + "@" + factory, e);
        }
    }
    
    private ClientSessionFactory createSessionFactory(ServerLocator locator) throws JMSException {
        try {
            return locator.createSessionFactory();
        }
        catch (Exception e) {
            throw JMSUtil.newJMSException("error creating session factory from locator " + locator, e);
        }
    }
    
    private ServerLocator createServerLocator(TransportConfiguration transportConfiguration, boolean ha) {
        ServerLocator locator;
        if (ha) {
            locator = HornetQClient.createServerLocatorWithHA(transportConfiguration);
        }
        else {
            locator = HornetQClient.createServerLocatorWithoutHA(transportConfiguration);
        }
        return locator;
    }
    
    @Override
    public void createQueue() throws JMSException {
        createQueue(true);
    }
    
    @Override
    public void createTransientQueue() throws JMSException {
        createQueue(false);
    }
    
    private void createQueue(boolean durable) throws JMSException {
        try {
            String coreQueueName = JMS_QUEUE_PREFIX + queueName;
            session.createQueue(JMS_QUEUE_PREFIX + queueName, coreQueueName, durable);
        }
        catch (HornetQException e) {
            throw JMSUtil.newJMSException(e);
        }
    }
    
    @Override
    public QueueConnection createConnection() throws JMSException {
        HornetQConnectionFactory cf = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
        return cf.createQueueConnection();
    }
    
}
