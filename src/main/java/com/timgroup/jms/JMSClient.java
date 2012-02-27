package com.timgroup.jms;

import java.net.URI;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JMSClient {
    
    public static interface Factory {
        public JMSClient create(URI uri) throws JMSException;
    }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JMSClient.class);
    
    public abstract void createQueue(String queueName) throws JMSException;
    
    public abstract void createTransientQueue(String queueName) throws JMSException;
    
    public void sendShortTextMessage(String queueName, String text) throws JMSException {
        Queue queue = getQueue(queueName);
        QueueConnection connection = createConnection();
        try {
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);
            TextMessage message = session.createTextMessage(text);
            sender.send(message);
        }
        finally {
            closeQuietly(connection);
        }
    }
    
    private void closeQuietly(Connection connection) {
        try {
            connection.close();
        }
        catch (JMSException e) {
            LOGGER.error("error closing connection " + connection, e);
        }
    }
    
    protected abstract Queue getQueue(String queueName) throws JMSException;
    
    protected abstract QueueConnection createConnection() throws JMSException;
    
    public String receiveMessage(String queueName) throws JMSException {
        Queue queue = getQueue(queueName);
        QueueConnection connection = createConnection();
        try {
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
            QueueReceiver receiver = session.createReceiver(queue);
            Message message = receiver.receive();
            return toString(message);
        }
        finally {
            closeQuietly(connection);
        }
    }
    
    private String toString(Message message) throws JMSException {
        String text;
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            text = textMessage.getText();
        }
        else {
            text = message.toString();
        }
        return text;
    }
    
}
