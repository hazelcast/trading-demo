package com.hazelcast.tudor;

import com.hazelcast.core.*;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class Node {
    final HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(null);
    final ITopic<StockPriceUpdate> topicFeed = hazelcast.getTopic("feed");
    final IQueue<Order> qOrders = hazelcast.getQueue("orders");
    final IMap<String, Position> mapPositions = hazelcast.getMap("positions"); // pmId,instrumentId
    final AtomicLong countReceivedStockUpdates = new AtomicLong();
    final AtomicLong countOrdersProcessed = new AtomicLong();
    final Logger logger = Logger.getLogger("Node");
    final ITopic<String> topicLogs = hazelcast.getTopic("logs");
    final ConcurrentMap<Integer, Double> mapStockPrices = new ConcurrentHashMap<Integer, Double>(8000);
    final String memberString = hazelcast.getCluster().getLocalMember().toString();
    final int threads = 40;
    final ExecutorService threadExecutor = Executors.newFixedThreadPool(threads);

    public static void main(String[] args) {
//        System.setProperty("hazelcast.initial.min.cluster.size", "5");
        new Node().init();
    }

    void init() {
        for (int i = 0; i < threads; i++) {
            threadExecutor.execute(new PositionQueueSlurper());
        }
        topicFeed.addMessageListener(new StockStreamListener());
        startStreamer();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        long updates = countReceivedStockUpdates.getAndSet(0) / 5;
                        log("ReceivedStocks:" + updates + ", OrdersProcessed: " + countOrdersProcessed.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    void startStreamer() {
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < 500; i++) {
                    double price = (int) (Math.random() * 50) + 1;
                    topicFeed.publish(new StockPriceUpdate(LookupDatabase.randomPickInstrument().id, price));
                }
            }
        }, 0, 1000);
    }

    class StockStreamListener implements MessageListener<StockPriceUpdate> {
        public void onMessage(StockPriceUpdate stockPriceUpdate) {
            countReceivedStockUpdates.incrementAndGet();
            mapStockPrices.put(stockPriceUpdate.getInstrumentId(), stockPriceUpdate.getPrice());
        }
    }

    void log(String msg) {
        if (msg != null) {
            logger.info(msg);
            topicLogs.publish(memberString + ": " + msg);
        }
    }

    public class PositionQueueSlurper implements Runnable {

        public void run() {
            while (true) {
                try {
                    Transaction txn = hazelcast.getTransaction();
                    txn.begin();
                    try {
                        Order order = qOrders.take();
                        logger.info("Processing " + order);
                        List<Integer> lsAccounts = order.lsAccounts;
                        int accountQuantity = order.quantity / lsAccounts.size();
                        for (Integer account : lsAccounts) {
                            String key = account + "," + order.instrumentId;
                            updatePosition(key, order.instrumentId, new Deal(accountQuantity, order.price));
                        }
                        String key = order.portfolioManagerId + "," + order.instrumentId;
                        updatePosition(key, order.instrumentId, new Deal(order.quantity, order.price));
                        logger.info("qOrders size " + qOrders.size());
                        txn.commit();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        txn.rollback();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void updatePosition(String key, int instrumentId, Deal deal) {
            Position position = mapPositions.get(key);
            if (position == null) {
                position = new Position(instrumentId);
            } else if (position.getDealSize() > 100) {
                for (int i = 0; i < 10; i++) {
                    position.lsDeals.remove(0);
                }
            }
            position.addDeal(deal);
            mapPositions.put(key, position);
            logger.info("size " + mapPositions.size());
        }
    }
}
