package com.hazelcast.tudor;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OldNode {
/*
**  A Position holds a collection of order transactions (i.e. buys and sells).
**  Positions can be viewed at various aggregations. For example,
**  take two portfolio managers trading for one entity.  In this case,
**  we need to manage three different Position objects (i.e. one for each portfolio manager,
**  and an additional Position for the trading entity.
**
**  If we have the following activity:
**      pm1 buys 10,000 shares of IBM  -- portfolio manger pm1 Position is long 10,000, entity position is long 10,000
**      pm2 shorts 8,000 shares of IBM -- portfolio manager pm2 Position is short 8,000, entity position is long 2,000
**      pm1 sells 4,000 shares of IBM  -- portfolio manager pm1 Position is long 6,000, entity position is short 2,000
**      pm2 covers 5,000 shares of IBM -- portfolio manager pm2 Position is 0, entity position is long 3,000
*/

    public static class PositionQueueSlurper implements Runnable {

        BlockingQueue<Order> positionSlurperQueue = Hazelcast.getQueue("positionSlurperQueue");
        Map portfolioManagerPositionMap = Hazelcast.getMap("portfolioManagerPositionMap");
        Map entityPositionMap = Hazelcast.getMap("entityPositionMap");

        public void run() {
            while (true) {
                try {
                    Transaction txn = Hazelcast.getTransaction();
                    txn.begin();
                    try {
                        Order order = positionSlurperQueue.take();
//                        List<Account> lsAccounts = order.lsAccounts;
//                        for (Account account : lsAccounts) {
//                            if ((position = (PositionEntity) entityPositionMap.get(order.instrument.id)) == null) {
//                                position = new PositionEntity();
//                                position.instrumentId = order.instrument.id;
//                            }
//                            position.addOrder(order);
//                            entityPositionMap.put(position.instrumentId, position);
//                        }
                        PositionPortfolioManager position = null;
                        String key = order.instrument.id + "," + order.portfolioManager.id;
                        if ((position = (PositionPortfolioManager) portfolioManagerPositionMap.get(key)) == null) {
                            position = new PositionPortfolioManager(order.instrument.id, order.portfolioManager.id);
                        }
                        position.addOrder(order);
                        portfolioManagerPositionMap.put(position.getKey(), position);
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
    }

    public static void main(String[] args) throws Exception {
        /**
         1. 8 entity (account) each with 50 PMs (sub account) each PM will have
         100 positions for symbols
         2. Iterate 7000 symbols + PMs

         while (true) {
         Order(symbol, amount, PM, price, List<account>)
         }

         Split the amount into the accounts for that PM.

         1000 orders per second.  One dedicated Node will generate 1000 orders per second
         push into the cluster.
         Update the PM's positions and 8 entities positions.
         Simulate the realization.. so that we can evict the orders... per
         symbol keep 100 orders.
         entity * pm * symbol (ave. 100)  = 8 * 50 * 100  = 40 K orders
         pm * symbol (100) = 50 * 100 = 5K orders

         each PM's portfolio will contain around 100 symbols.

         Show : symbol + amount + last_price + (P & L == profit and loss ...
         try to calculate this == (lastTick - order.price) * quantity) JTable.

         simulate the symbol + last+price feed.  5000 per second. each node
         will publish (5000 / # of nodes)

         success point:
         1. throughput
         2. open three windows: one for entity 2 diff. PMs. clients.
         3. run it 10 nodes
         4. creation of the order and show it on the GUI.. average at least.
         5. show how queues/maps/topics are doing.


         Deadline :  March 10
         */
        BlockingQueue positionSlurperQueue = Hazelcast.getQueue("positionSlurperQueue");
        Map orderMap = Hazelcast.getMap("orderMap");
        Map instrumentMap = Hazelcast.getMap("instrumentMap");
        Map portfolioManagerMap = Hazelcast.getMap("portfolioManagerMap");
        int threads = 2;
        ExecutorService threadExecutor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            threadExecutor.execute(new PositionQueueSlurper());
            System.out.println("Started thread " + i);
        }
        PortfolioManager pm1 = new PortfolioManager(1, "joe");
        PortfolioManager pm2 = new PortfolioManager(2, "john");
        portfolioManagerMap.put(pm1.id, pm1);
        portfolioManagerMap.put(pm2.id, pm1);
        Instrument stock = new Instrument(1, "IBM", "International Business Machines");
        instrumentMap.put(stock.id, stock);
        List<Order> orders = new ArrayList<Order>();
        int orderId = 1;
        orders.add(new Order(orderId++, stock, pm1, 10000D, 101.00));
        orders.add(new Order(orderId++, stock, pm2, -8000D, 101.50));
        orders.add(new Order(orderId++, stock, pm1, -4000D, 102.00));
        orders.add(new Order(orderId++, stock, pm2, 5000D, 101.00));
        for (Order order : orders) {
            Transaction txn = Hazelcast.getTransaction();
            txn.begin();
            try {
                orderMap.put(order.id, order);
                positionSlurperQueue.put(order);
                txn.commit();
            } catch (Throwable t) {
                txn.rollback();
            }
        }
        Thread.sleep(2000);
        Map entityPositionMap = Hazelcast.getMap("entityPositionMap");
        Position p = (Position) entityPositionMap.get(1);
        Instrument i = (Instrument) instrumentMap.get(p.instrumentId);
        System.out.println(i.symbol + " " + p.quantity);
        Map portfolioManagerPositionMap = Hazelcast.getMap("portfolioManagerPositionMap");
        p = (Position) portfolioManagerPositionMap.get("1,1");
        i = (Instrument) instrumentMap.get(p.instrumentId);
        System.out.println(i.symbol + " " + p.quantity);
        p = (Position) portfolioManagerPositionMap.get("1,2");
        i = (Instrument) instrumentMap.get(p.instrumentId);
        System.out.println(i.symbol + " " + p.quantity);
    }
}
