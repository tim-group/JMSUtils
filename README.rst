This is a SUPER WICKED package for doing simple things with JMS message queues.

To build::

    gradle clean installApp

(note that Gradle should be M9, so say ``gradle-m9`` if you're at TIM Group)

This will build the code and generate some start scripts. To run, say::

    build/install/JMSUtils/bin/jms

You should see some helpful command-line help.

URLs identify brokers (not queues), and are of the form::

    protocol://host:port

Where protocol is one of:

* activemq
* hornetq

You can prefix the protocol with ``jms:`` if you like. That will be ignored. We'd like to add rabbitmq support soon.

Commands are simply methods on the class ``com.timgroup.jms.JMSClient``. Arguments to those commands correspond to the arguments to the methods. The strings given on the command line are converted to the right type by reflective smartarsery.

Some commands you might particularly enjoy:

createQueue queueName
	create a durable queue on the broker

sendShortTextMessage queueName text
	send a message with the given text to the given queue

sendTextMessage queueName
	send a message with text read from standard input to the given queue

receiveMessage queueName
	receive a message from the given queue and write it to standard output

There are some commands which make use of the idea of 'heavy messages' for testing. A heavy message's text consists of some number of full stops, followed by a label, usually a number. Each dot stands for one second's processing work for the receiver.

sendHeavyMessages queueName repeats
	send the given number (`repeats`) of heavy messages to the given queue; uses a single session for all the messages

receiveAndProcessHeavyMessages queueName
	forever receives messages from a queue and pretends to process them by sleeping for a second per leading full stop; uses a single session for all the messages
