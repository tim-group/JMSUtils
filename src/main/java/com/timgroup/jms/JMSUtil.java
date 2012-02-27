package com.timgroup.jms;

import javax.jms.JMSException;

public class JMSUtil {
    
    public static JMSException newJMSException(String reason, Exception cause) {
        JMSException e = new JMSException(reason);
        e.initCause(cause);
        return e;
    }
    
    public static JMSException newJMSException(Exception cause) {
        return newJMSException(cause.getMessage(), cause);
    }
    
    private JMSUtil() {}
    
}
