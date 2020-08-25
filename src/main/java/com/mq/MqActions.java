package com.mq;

import com.rabbitmq.client.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class MqActions {

    private static final String EXCHANGE = "foo.domain";
    private static final String EXCHANGE_DLX = "foo.domain.dlx";
    private static final String QUEUE1 = "foo.domain.queue1";
    private static final String QUEUE2 = "foo.domain.queue2";
    private static final String QUEUE_DLX = "foo.domain.dlq";
    private static final String[] TEST_MESSAGES = {"foo", "bar", "baz"};
    private static final String ROUTING_KEY1 = "rk1";
    private static final String ROUTING_KEY2 = "rk2";
    private static final String[] ROUTING_KEYS = {ROUTING_KEY1, ROUTING_KEY2, ROUTING_KEY1};

    @Before
    public void init() throws IOException, TimeoutException {
        Connection connection = connectionFactory().newConnection();
        Channel channel = connection.createChannel();
        // Cleanup from previous test
        channel.queueDelete(QUEUE_DLX);
        channel.queueDelete(QUEUE2);
        channel.queueDelete(QUEUE1);
        channel.exchangeDelete(EXCHANGE_DLX);
        channel.exchangeDelete(EXCHANGE);
        // EXCHANGE/QUEUEs
        channel.exchangeDeclare(EXCHANGE, "topic");
        Map<String, Object> queueArgs = new HashMap<>();
        queueArgs.put("x-message-ttl", 10 * 1000);
        queueArgs.put("x-dead-letter-exchange", EXCHANGE_DLX);
        channel.queueDeclare(QUEUE1, true, false, false, queueArgs);
        channel.queueBind(QUEUE1, EXCHANGE, ROUTING_KEY1);
        channel.queueDeclare(QUEUE2, true, false, false, queueArgs);
        channel.queueBind(QUEUE2, EXCHANGE, ROUTING_KEY2);
        // DLX/DLQ
        channel.exchangeDeclare(EXCHANGE_DLX, "topic");
        channel.queueDeclare(QUEUE_DLX, true, false, false, null);
        channel.queueBind(QUEUE_DLX, EXCHANGE_DLX, "#");
        // Send test messages to DLQ
        for (int i = 0; i < TEST_MESSAGES.length; i++) {
            channel.basicPublish(EXCHANGE_DLX, ROUTING_KEYS[i], null, TEST_MESSAGES[i].getBytes());
        }
        channel.close();
        connection.close();
    }

    private static void verifyMessages(String queue, String routingKey, String... messages) throws IOException, TimeoutException {
        Connection connection = connectionFactory().newConnection();
        Channel channel = connection.createChannel();

        List<String> messagesRead = new ArrayList<>();
        while (true) {
            GetResponse response = channel.basicGet(queue, true);
            if (response == null) {
                break;
            }
            Envelope envelope = response.getEnvelope();

            Assert.assertEquals(envelope.getRoutingKey(), routingKey);
            messagesRead.add(new String(response.getBody()));
        }
        channel.close();
        connection.close();
        Assert.assertEquals(messagesRead, Arrays.asList(messages));
    }

    private static ConnectionFactory connectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        return factory;
    }

    @Test
    public void moveAllMessagesToExchange() throws Exception {
        verifyMessages(QUEUE1, ROUTING_KEY1, "foo", "baz");
        verifyMessages(QUEUE2, ROUTING_KEY2, "bar");
        verifyMessages(QUEUE_DLX, null);
    }
}
