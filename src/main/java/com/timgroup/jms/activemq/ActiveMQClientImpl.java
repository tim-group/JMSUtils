package com.timgroup.jms.activemq;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timgroup.jms.JMSClient;
import com.timgroup.jms.JMSUtil;
import com.timgroup.util.Utils;

public class ActiveMQClientImpl extends JMSClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveMQClientImpl.class);

    public static class Factory implements JMSClient.Factory {
        
        @Override
        public JMSClient create(URI uri, List<InetSocketAddress> alternates) throws JMSException {
            return new ActiveMQClientImpl(uri, alternates);
        }
    }
    
    private final ActiveMQConnectionFactory connectionFactory;
    
    public ActiveMQClientImpl(URI uri, List<InetSocketAddress> alternates) throws JMSException {
        super(uri.getPath());
        String host = uri.getHost();
        int port = port(uri.getPort());
        
        URI brokerURL;
        if (alternates.isEmpty()) {
            brokerURL = newTCPURL(host, port, "jms.prefetchPolicy.all=1");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("failover:(");
            sb.append(newTCPURL(host, port, null));
            for (InetSocketAddress inetSocketAddress : alternates) {
                sb.append(",");
                sb.append(newTCPURL(inetSocketAddress.getHostName(), port(inetSocketAddress.getPort()), null));
            }
            sb.append(")");
            sb.append("?randomize=false&jms.prefetchPolicy.all=1");
            try {
                brokerURL = new URI(sb.toString());
            } catch (URISyntaxException e) {
                throw JMSUtil.newJMSException("all-too plausible error constructing broker URL", e);
            }
        }
        LOGGER.info("connecting with broker URL {}", brokerURL);
        connectionFactory = new ActiveMQConnectionFactory(brokerURL);
    }
    
    private Integer port(int rawPort) {
        return Utils.defaulting(rawPort, -1, 61616);
    }
    
    private URI newTCPURL(String host, int port, String options) throws JMSException {
        try {
            return new URI("tcp", null, host, port, null, options, null);
        } catch (URISyntaxException e) {
            throw JMSUtil.newJMSException("barely plausible error constructing broker URL", e);
        }
    }
    
    @Override
    public void createQueue() throws JMSException {
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
    public void createTransientQueue() throws JMSException {
        createQueue();
    }
    
    @Override
    public QueueConnection createConnection() throws JMSException {
        return connectionFactory.createQueueConnection();
    }
    
}
