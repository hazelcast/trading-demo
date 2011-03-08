package com.hazelcast.tudor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Position implements Serializable {
    Integer instrumentId;
    double quantity = 0;

    private List<Integer> orderIdList = new ArrayList<Integer>();

    public synchronized void addOrder(Order order) throws Exception {
        this.quantity += order.quantity;
        this.orderIdList.add(order.id);
    }
}
