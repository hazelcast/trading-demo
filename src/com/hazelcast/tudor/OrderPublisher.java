package com.hazelcast.tudor;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.ITopic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class OrderPublisher {
    final Timer timer = new Timer();
    volatile PublishTask publishTask = null;
    final HazelcastClient hazelcastClient;
    final ITopic<Order> topicOrders;

    public static void main(String[] args) throws Exception {
        String host = (args != null && args[0] != null) ? args[0] : "localhost";
        HazelcastClient client = HazelcastClient.newHazelcastClient("tudor", "tudor-pass", host);
        OrderPublisher op = new OrderPublisher(client);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("OrderPublisher > ");
            String command = in.readLine();
            if ("stop".equals(command)) {
                op.stop();
            } else if ("start".equals(command)) {
                op.start();
            }
        }
    }

    public OrderPublisher(HazelcastClient client) {
        this.hazelcastClient = client;
        this.topicOrders = client.getTopic("orders");
    }

    private void start() {
        if (publishTask == null) {
            publishTask = new PublishTask();
            timer.scheduleAtFixedRate(publishTask, 0, 1000);
        }
    }

    private void stop() {
        publishTask.cancel();
        publishTask = null;
    }

    private class PublishTask extends TimerTask {
        @Override
        public void run() {
            for (int i = 0; i < 1000; i++) {
                double price = (int) (Math.random() * 50) + 1;
//                topicOrders.publish(new Order());
            }
        }
    }
}
