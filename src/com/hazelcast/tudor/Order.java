package com.hazelcast.tudor;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    int id;
    String symbol;
    int portfolioManagerId;
    int quantity;
    double price;
    List<Integer> lsAccounts;

    public Order(int id, String symbol, int quantity, double price, int portfolioManagerId, List<Integer> lsAccounts) {
        this.id = id;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.portfolioManagerId = portfolioManagerId;
        this.lsAccounts = lsAccounts;
    }
}
