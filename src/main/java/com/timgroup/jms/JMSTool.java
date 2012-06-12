package com.timgroup.jms;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.JMSException;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.timgroup.jms.JMSClient.Factory;
import com.timgroup.jms.activemq.ActiveMQClientImpl;
import com.timgroup.jms.hornetq.HornetQClientImpl;
import com.timgroup.reflection.ReflectedObject;
import com.timgroup.util.Utils;

@CommandLineInterface(application = "jms")
public class JMSTool {
    
    public static void main(String[] args) {
        try {
            JMSToolOptions options = parseOptions(args);
            List<String> arguments = options.getArguments();
            try {
                URI uri = parseURI(arguments.remove(0));
                List<InetSocketAddress> alternates = parseAlternates(options.getAlternateBrokers());
                String command = !arguments.isEmpty() ? arguments.remove(0) : null;
                handleCommand(uri, alternates, options, command, arguments);
            } catch (URISyntaxException e) {
                System.err.println("invalid JMS URL: " + e.getMessage());
                if (options.isVerbose()) e.printStackTrace();
            } catch (JMSException e) {
                System.err.println("error connecting to JMS server: " + e.getMessage());
                if (options.isVerbose()) e.printStackTrace();
            } catch (IllegalArgumentException e) {
                System.err.println("illegal argument: " + e.getMessage());
                if (options.isVerbose()) e.printStackTrace();
            } catch (InvocationTargetException e) {
                System.err.println("error invoking command: " + e.getCause().getMessage());
                if (options.isVerbose()) e.getCause().printStackTrace();
            }
        } catch (ArgumentValidationException e) {
            System.err.println(e.getMessage());
        }
    }
    
    private static JMSToolOptions parseOptions(String[] args) throws ArgumentValidationException {
        Class<JMSToolOptions> optionsClass = JMSToolOptions.class;
        JMSToolOptions options = CliFactory.parseArguments(optionsClass, args);
        if (options.getArguments().isEmpty()) CliFactory.parseArguments(optionsClass, "--help");
        return options;
    }
    
    private static URI parseURI(String uriStr) throws URISyntaxException {
        return new URI(Utils.stripPrefix(uriStr, "jms:"));
    }
    
    private static List<InetSocketAddress> parseAlternates(List<String> alternateStrings) {
        if (alternateStrings == null) return Collections.emptyList();
        ArrayList<InetSocketAddress> alternates = new ArrayList<InetSocketAddress>();
        for (String alternateString : alternateStrings) {
            String host;
            int port;
            int colon = alternateString.indexOf(':');
            if (colon != -1) {
                host = alternateString.substring(0, colon);
                port = Integer.parseInt(alternateString.substring(colon + 1));
            } else {
                host = alternateString;
                port = -1;
            }
            alternates.add(new InetSocketAddress(host, port));
        }
        return alternates;
    }
    
    private static void handleCommand(URI uri, List<InetSocketAddress> alternates, JMSToolOptions options, String command,
            List<String> arguments) throws JMSException, IllegalArgumentException, InvocationTargetException {
        Factory factory = getClientFactoryForScheme(uri.getScheme());
        JMSClient client = factory.create(uri, alternates);
        if (command != null) {
            handleCommand(client, options, command, arguments);
        }
    }
    
    private static Factory getClientFactoryForScheme(String scheme) {
        if (scheme.equals("hornetq")) return new HornetQClientImpl.Factory();
        else if (scheme.equals("activemq")) return new ActiveMQClientImpl.Factory();
        else throw new IllegalArgumentException("unknown scheme: " + scheme);
        // TODO: handle the 'jndi' scheme, which does a JNDI lookup
        // http://www.ietf.org/rfc/rfc6167.txt
    }
    
    private static void handleCommand(JMSClient client, JMSToolOptions options, String command, List<String> arguments)
            throws IllegalArgumentException, InvocationTargetException {
        Object result = new ReflectedObject(client).invokeMethod(command, arguments);
        if (result != null) System.out.println(result);
    }
    
}
