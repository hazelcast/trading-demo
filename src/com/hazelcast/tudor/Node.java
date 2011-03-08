package com.hazelcast.tudor;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class Node {
    final HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(null);
    final ITopic<StockPriceUpdate> topicFeed = hazelcast.getTopic("feed");
    final AtomicLong countReceivedStockUpdates = new AtomicLong();
    final Logger logger = Logger.getLogger("PricerNode");
    final ITopic<String> topicLogs = hazelcast.getTopic("logs");
    final ConcurrentMap<String, Double> mapStockPrices = new ConcurrentHashMap<String, Double>(8000);
    final String memberString = hazelcast.getCluster().getLocalMember().toString();

    public static void main(String[] args) {
        new Node().init();
    }

    void init() {
        topicFeed.addMessageListener(new StockStreamListener());
        startStreamer();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        long updates = countReceivedStockUpdates.getAndSet(0) / 5;
                        log("ReceivedStocks " + updates);
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
                    topicFeed.publish(new StockPriceUpdate(StockDatabase.randomPick(), price));
                }
            }
        }, 0, 1000);
    }

    class StockStreamListener implements MessageListener<StockPriceUpdate> {
        public void onMessage(StockPriceUpdate stockPriceUpdate) {
            countReceivedStockUpdates.incrementAndGet();
            String symbol = stockPriceUpdate.getSymbol();
            mapStockPrices.put(symbol, stockPriceUpdate.getPrice());
        }
    }

    void log(String msg) {
        if (msg != null) {
            logger.info(msg);
            topicLogs.publish(memberString + ": " + msg);
        }
    }
}
