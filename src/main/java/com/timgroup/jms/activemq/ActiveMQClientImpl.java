package com.timgroup.jms.activemq;

import java.net.URI;
import java.net.URISyntaxException;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.timgroup.jms.JMSClient;
import com.timgroup.jms.JMSUtil;
import com.timgroup.util.Utils;

public class ActiveMQClientImpl extends JMSClient {
    
    public static class Factory implements JMSClient.Factory {
        @Override
        public JMSClient create(URI uri) throws JMSException {
            return new ActiveMQClientImpl(uri);
        }
    }
    
    private final ActiveMQConnectionFactory connectionFactory;
    
    public ActiveMQClientImpl(URI uri) throws JMSException {
        String host = uri.getHost();
        int port = Utils.defaulting(uri.getPort(), -1, 61616);
        
        URI brokerURL;
        try {
            brokerURL = new URI("tcp", null, host, port, null, null, null);
        } catch (URISyntaxException e) {
            throw JMSUtil.newJMSException("barely plausible error constructing broker URL", e);
        }
        connectionFactory = new ActiveMQConnectionFactory(brokerURL);
    }
    
    @Override
    public void createQueue(String queueName) throws JMSException {
        QueueConnection connection = createConnection();
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            try {
                session.createQueue(queueName);
            } finally {
                session.close();
            }
        } finally {
            connection.close();
        }
    }
    
    @Override
    public void createTransientQueue(String queueName) throws JMSException {
        createQueue(queueName);
    }
    
    @Override
    public QueueConnection createConnection() throws JMSException {
        return connectionFactory.createQueueConnection();
    }
    
}
