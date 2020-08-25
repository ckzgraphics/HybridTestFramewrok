package com.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Sender {

    private static final String QUEUE_NAME = "jms/queue";
    private static final String BROKER_URL = "amqp://<username>:<password>@<host>:<port>";

    private ConnectionFactory factory = new ConnectionFactory();

    public void sendMessage(String text) throws Exception {
        factory.setUri(BROKER_URL);
        factory.useSslProtocol();

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicPublish("", QUEUE_NAME, null, text.getBytes());
    }
}
