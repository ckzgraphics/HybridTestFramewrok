package com.mq;

import com.rabbitmq.client.*;

import java.io.IOException;

public class Receiver {

    private static final String BROKER_URI = "amqp://<username>:<password>@<host>:<port>";
    private final static String QUEUE_NAME = "jms/queue";
    private SimpleCache cache;

    public Receiver(SimpleCache cache) {
        this.cache = cache;
    }

    public void receive() throws Exception {
        // let's setup evrything and start listening
        ConnectionFactory factory = createConnectionFactory();

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicConsume(QUEUE_NAME, true, newConsumer(channel));
    }

    protected ConnectionFactory createConnectionFactory() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(BROKER_URI);
        factory.useSslProtocol(); // Note this, we'll get back to it soon...
        return factory;
    }

    private DefaultConsumer newConsumer(Channel channel) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                cache.update(new String(body));  // put each message into the cache
            }
        };
    }
}