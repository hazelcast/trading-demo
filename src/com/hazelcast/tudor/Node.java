package com.hazelcast.tudor;

import com.hazelcast.core.*;

import java.util.Collection;
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
    final IMap<String, Position> mapPositions = hazelcast.getMap("positions"); // <pmId,instrumentId, Position>
    final IMap<Integer, Integer> mapNewOrders = hazelcast.getMap("neworders");  // <pmId, instrumentId>
    final AtomicLong countReceivedStockUpdates = new AtomicLong();
    final AtomicLong countOrdersProcessed = new AtomicLong();
    final AtomicLong countNewOrderEvents = new AtomicLong();
    final AtomicLong countPositionViews = new AtomicLong();
    final Logger logger = Logger.getLogger("Node");
    final ITopic<String> topicLogs = hazelcast.getTopic("logs");
    final ConcurrentMap<Integer, Double> mapStockPrices = new ConcurrentHashMap<Integer, Double>(8000);
    final String memberString = hazelcast.getCluster().getLocalMember().toString();
    final int threads = 40;
    final ExecutorService esOrderConsumer = Executors.newFixedThreadPool(threads);
    final ExecutorService esEventProcessor = Executors.newFixedThreadPool(10);
    final ConcurrentMap<Integer, Portfolio> localPMPositions = new ConcurrentHashMap<Integer, Portfolio>();
    final ConcurrentMap<Integer, InstrumentInfo> mapInstrumentInfos = new ConcurrentHashMap<Integer, InstrumentInfo>();

    public static void main(String[] args) {
//        System.setProperty("hazelcast.initial.min.cluster.size", "5");
        new Node().init();
    }

    void init() {
        for (int i = 0; i < threads; i++) {
            esOrderConsumer.execute(new PositionQueueSlurper());
        }
        topicFeed.addMessageListener(new StockStreamListener());
        mapNewOrders.addLocalEntryListener(new NewOrderListener());
        startStreamer();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        long feeds = countReceivedStockUpdates.getAndSet(0) / 5;
                        long orders = countOrdersProcessed.getAndSet(0) / 5;
                        long events = countNewOrderEvents.getAndSet(0) / 5;
                        long views = countPositionViews.getAndSet(0) / 5;
                        log("Feeds:" + feeds + ", OrdersProcessed:" + orders + ", newOrderEvents:" + events + ", Views:" + views);
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
        public void onMessage(final StockPriceUpdate stockPriceUpdate) {
            esEventProcessor.execute(new Runnable() {
                public void run() {
                    countReceivedStockUpdates.incrementAndGet();
                    int instrumentId = stockPriceUpdate.getInstrumentId();
                    mapStockPrices.put(instrumentId, stockPriceUpdate.getPrice());
                    InstrumentInfo instrumentInfo = createOrGetInstrumentInfo(instrumentId);
                    Collection<Portfolio> relatedPortfolios = instrumentInfo.getPortfolios();
                    for (Portfolio relatedPortfolio : relatedPortfolios) {
                        firePositionViewChanged(createPositionView(relatedPortfolio, instrumentId));
                    }
                }
            });
        }
    }

    private void firePositionViewChanged(PositionView positionView) {
        if (positionView == null) return;
        countPositionViews.incrementAndGet();
        ITopic topicPM = hazelcast.getTopic("pm_" + positionView.pmId);
        topicPM.publish(positionView);
    }

    public PositionView createPositionView(Portfolio portfolio, Integer instrumentId) {
        Double lastPrice = mapStockPrices.get(instrumentId);
        if (lastPrice == null) return null;
        Position position = portfolio.getPosition(instrumentId);
        return new PositionView(portfolio.pmId, instrumentId, position.quantity, lastPrice, portfolio.calculateProfitOrLoss());
    }

    private Portfolio createOrGetPortfolio(int pmId) {
        Portfolio portfolio = localPMPositions.get(pmId);
        if (portfolio == null) {
            portfolio = new Portfolio(pmId);
            Portfolio existing = localPMPositions.putIfAbsent(pmId, portfolio);
            if (existing != null) {
                portfolio = existing;
            }
        }
        return portfolio;
    }

    private InstrumentInfo createOrGetInstrumentInfo(int instrumentId) {
        InstrumentInfo instrumentInfo = mapInstrumentInfos.get(instrumentId);
        if (instrumentInfo == null) {
            instrumentInfo = new InstrumentInfo();
            InstrumentInfo existing = mapInstrumentInfos.putIfAbsent(instrumentId, instrumentInfo);
            if (existing != null) {
                instrumentInfo = existing;
            }
        }
        return instrumentInfo;
    }

    class NewOrderListener implements EntryListener<Integer, Integer> {
        public void entryAdded(final EntryEvent<Integer, Integer> entryEvent) {
            countNewOrderEvents.incrementAndGet();
            esEventProcessor.execute(new Runnable() {
                public void run() {
                    Integer pmId = entryEvent.getKey();
                    Integer instrumentId = entryEvent.getValue();
                    //load all positions for this pm
                    //and create the Portfolio
                }
            });
        }

        public void entryRemoved(final EntryEvent<Integer, Integer> entryEvent) {
        }

        public void entryUpdated(final EntryEvent<Integer, Integer> entryEvent) {
            countNewOrderEvents.incrementAndGet();
            esEventProcessor.execute(new Runnable() {
                public void run() {
                    try {
                        Integer pmId = entryEvent.getKey();
                        Integer instrumentId = entryEvent.getValue();
                        Position position = mapPositions.get(pmId + "," + instrumentId);
                        if (position != null) {
                            Portfolio portfolio = createOrGetPortfolio(pmId);
                            InstrumentInfo instrumentInfo = createOrGetInstrumentInfo(instrumentId);
                            instrumentInfo.addPortfolio(portfolio);
                            portfolio.update(position);
                            PositionView positionView = createPositionView(portfolio, instrumentId);
                            firePositionViewChanged(positionView);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
            );
        }

        public void entryEvicted(final EntryEvent<Integer, Integer> entryEvent) {
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
                Transaction txn = hazelcast.getTransaction();
                txn.begin();
                try {
                    Order order = qOrders.take();
                    List<Integer> lsAccounts = order.lsAccounts;
                    int accountQuantity = order.quantity / lsAccounts.size();
                    for (Integer account : lsAccounts) {
                        String key = account + "," + order.instrumentId;
                        updatePosition(key, order, account, accountQuantity);
                    }
                    String key = order.portfolioManagerId + "," + order.instrumentId;
                    updatePosition(key, order, order.portfolioManagerId, order.quantity);
                    txn.commit();
                } catch (Throwable t) {
                    t.printStackTrace();
                    txn.rollback();
                }
            }
        }

        public void updatePosition(String key, Order order, int pmId, int quantity) {
            Deal deal = new Deal(quantity, order.price);
            Position position = mapPositions.get(key);
            if (position == null) {
                position = new Position(order.instrumentId);
            } else if (position.getDealSize() > 100) {
                for (int i = 0; i < 10; i++) {
                    position.lsDeals.remove(0);
                }
            }
            position.addDeal(deal);
            mapPositions.put(key, position);
            mapNewOrders.put(pmId, order.instrumentId);
        }
    }
}
