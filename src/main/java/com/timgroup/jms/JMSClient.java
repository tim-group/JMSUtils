package com.timgroup.jms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
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

import com.timgroup.reflection.ConvertibleType;
import com.timgroup.reflection.WrapperUtil;

public abstract class JMSClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JMSClient.class);
    
    public static interface Factory {
        
        public JMSClient create(URI uri) throws JMSException;
        
    }
    
    public abstract void createQueue(String queueName) throws JMSException;
    
    public abstract void createTransientQueue(String queueName) throws JMSException;
    
    public void sendShortTextMessage(String queueName, final String text) throws JMSException {
        performSendAction(queueName, new SendAction() {
            @Override
            public void perform(QueueSession session, QueueSender sender) throws JMSException {
                TextMessage message = session.createTextMessage(text);
                send(sender, message);
            }
        });
    }
    
    public void sendShortTextMessageRepeatedly(String queueName, final String text, final int repeats) throws JMSException {
        performSendAction(queueName, new SendAction() {
            @Override
            public void perform(QueueSession session, QueueSender sender) throws JMSException {
                for (int i = 0; i < repeats; i++) {
                    TextMessage message = session.createTextMessage(text);
                    send(sender, message);
                }
            }
        });
    }
    
    public void sendTextMessage(String queueName) throws JMSException {
        String text;
        try {
            text = readFully(System.in);
        } catch (IOException e) {
            throw JMSUtil.newJMSException("error reading from standard input", e);
        }
        sendShortTextMessage(queueName, text);
    }
    
    private String readFully(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public void sendMapMessage(String queueName) throws JMSException {
        final Map<String, Object> entries;
        try {
            entries = readFullyAsMap();
        } catch (IOException e) {
            throw JMSUtil.newJMSException("error reading from standard input", e);
        }
        
        performSendAction(queueName, new SendAction() {
            @Override
            public void perform(QueueSession session, QueueSender sender) throws JMSException {
                MapMessage message = session.createMapMessage();
                for (Entry<String, Object> entry : entries.entrySet()) {
                    message.setObject(entry.getKey(), entry.getValue());
                }
                send(sender, message);
            }
        });
    }
    
    private HashMap<String, Object> readFullyAsMap() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        HashMap<String, Object> map = new LinkedHashMap<String, Object>();
        String line;
        while ((line = in.readLine()) != null) {
            int equals = line.indexOf('=');
            if (equals == -1) throw new IOException("bad line: " + line);
            String key = line.substring(0, equals);
            String valueStr = line.substring(equals + 1);
            int colon = key.indexOf(":");
            String tag;
            if (colon != -1) {
                tag = key.substring(colon + 1);
                key = key.substring(0, colon);
            } else {
                tag = String.class.getName();
            }
            Class<?> type = WrapperUtil.forName(tag);
            Object value = ConvertibleType.convert(valueStr, type);
            map.put(key, value);
        }
        return map;
    }
    
    private static interface SendAction {
        
        public void perform(QueueSession session, QueueSender sender) throws JMSException;
        
    }
    
    private void performSendAction(String queueName, SendAction sendAction) throws JMSException {
        QueueConnection connection = createConnection();
        try {
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueName);
            QueueSender sender = session.createSender(queue);
            sendAction.perform(session, sender);
        } finally {
            closeQuietly(connection);
        }
    }
    
    private void send(QueueSender sender, Message message) throws JMSException {
        long t0 = System.nanoTime();
        sender.send(message);
        long t1 = System.nanoTime();
        LOGGER.info("sent message in {} ns", t1 - t0);
    }
    
    private void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (JMSException e) {
            LOGGER.error("error closing connection " + connection, e);
        }
    }
    
    protected abstract QueueConnection createConnection() throws JMSException;
    
    public String receiveMessage(String queueName) throws JMSException {
        QueueConnection connection = createConnection();
        try {
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
            Queue queue = session.createQueue(queueName);
            QueueReceiver receiver = session.createReceiver(queue);
            Message message = receiver.receive();
            return toString(message);
        } finally {
            closeQuietly(connection);
        }
    }
    
    private String toString(Message message) throws JMSException {
        String text;
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            text = textMessage.getText();
        } else if (message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            StringBuilder sb = new StringBuilder();
            @SuppressWarnings("unchecked")
            ArrayList<String> keys = Collections.list(mapMessage.getMapNames());
            for (String key : keys) {
                Object value = mapMessage.getObject(key);
                sb.append(key);
                sb.append(':');
                sb.append(WrapperUtil.getName(value.getClass()));
                sb.append('=');
                sb.append(value);
                sb.append('\n');
            }
            text = sb.toString();
        } else {
            text = message.toString();
        }
        return text;
    }
    
}
