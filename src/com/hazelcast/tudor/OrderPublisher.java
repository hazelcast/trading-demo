package com.hazelcast.tudor;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.IQueue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderPublisher {
    final ConcurrentMap<Integer, List<Instrument>> mapPMInstruments = new ConcurrentHashMap<Integer, List<Instrument>>(1000);
    final Timer timer = new Timer();
    volatile PublishTask publishTask = null;
    final HazelcastClient hazelcastClient;
    final IQueue<Order> qOrders;
    final Random random = new Random();
    final AtomicInteger orderIds = new AtomicInteger();
    volatile int rate = 1000;
    final BlockingQueue<Order> localQueue = new LinkedBlockingQueue<Order>(5000);
    final ExecutorService es;

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
            } else if (command.startsWith("rate")) {
                if (command.length() > 5) {
                    op.rate = Integer.parseInt(command.substring(5).trim());
                }
                System.out.println("rate is now " + op.rate);
            } else if (command.startsWith("status")) {
                System.out.println("rate " + op.rate + "  waiting " + op.localQueue.size());
            } else if (command.startsWith("pm")) {
                if (command.length() > 3) {
                    op.updatePMs(Integer.parseInt(command.substring(3).trim()));
                }
                System.out.println("pm count is now " + op.mapPMInstruments.size());
            }
        }
    }

    private void updatePMs(int pmCount) {
        for (int i = 0; i < pmCount; i++) {
            List<Instrument> lsInstruments = new ArrayList<Instrument>(100);
            for (int a = 0; a < 100; a++) {
                lsInstruments.add(LookupDatabase.randomPickInstrument());
            }
            mapPMInstruments.putIfAbsent(i, lsInstruments);
        }
    }

    public OrderPublisher(HazelcastClient client) {
        es = Executors.newFixedThreadPool(40);
        for (int i = 0; i < 40; i++) {
            es.execute(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Order order = localQueue.take();
                            qOrders.put(order);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
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
            for (int i = 0; i < rate; i++) {
                double price = random.nextInt(50) + 1;
                int quantity = 8 * (random.nextInt(100) + 10);
                int pmId = -1;
                while (pmId < 8) {
                    pmId = random.nextInt(mapPMInstruments.size());
                }
                List<Instrument> lsInstruments = mapPMInstruments.get(pmId);
                Instrument randomInstrument = lsInstruments.get(random.nextInt(lsInstruments.size()));
                int orderId = orderIds.incrementAndGet();
                localQueue.offer(new Order(orderId, randomInstrument.id, quantity, price, pmId, lsAccounts));
            }
        }
    }
}
