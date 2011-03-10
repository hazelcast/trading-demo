package com.hazelcast.tudor;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.IQueue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderPublisher {
    final static Map<Integer, List<Instrument>> mapPMInstruments = new HashMap<Integer, List<Instrument>>(1000);
    final Timer timer = new Timer();
    volatile PublishTask publishTask = null;
    final HazelcastClient hazelcastClient;
    final IQueue<Order> qOrders;
    final Random random = new Random();
    final AtomicInteger orderIds = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        String host = (args != null && args.length > 0) ? args[0] : "localhost";
        HazelcastClient client = HazelcastClient.newHazelcastClient("dev", "dev-pass", host);
        OrderPublisher op = new OrderPublisher(client);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        op.start();
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
        this.qOrders = client.getQueue("orders");
        for (int i = 8; i < 1000; i++) {
            List<Instrument> lsInstruments = new ArrayList<Instrument>(100);
            for (int a = 0; a < 100; a++) {
                lsInstruments.add(LookupDatabase.randomPickInstrument());
            }
            mapPMInstruments.put(i, lsInstruments);
        }
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
            List<Integer> lsAccounts = new ArrayList<Integer>(8);
            for (int i = 0; i < 8; i++) {
                lsAccounts.add(i);
            }
            for (int i = 0; i < 100; i++) {
                double price = random.nextInt(50) + 1;
                int quantity = 8 * (random.nextInt(100) + 10);
                int pmId = -1;
                while (pmId < 8) {
                    pmId = random.nextInt(LookupDatabase.PMCount);
                }
                List<Instrument> lsInstruments = mapPMInstruments.get(pmId);
                Instrument randomInstrument = lsInstruments.get(random.nextInt(lsInstruments.size()));
                int orderId = orderIds.incrementAndGet();
                Order order = new Order(orderId, randomInstrument.id, quantity, price, pmId, lsAccounts);
                try {
                    qOrders.put(order);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
