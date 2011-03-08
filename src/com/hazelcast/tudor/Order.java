package com.hazelcast.tudor;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    Integer id;
    Instrument instrument;
    PortfolioManager portfolioManager;
    Double quantity;
    Double price;
    List<Account> lsAccounts;

    Order(Integer _id, Instrument _instrument, PortfolioManager _portfolioManager, Double _quantity, Double _price) {
        this.id = _id;
        this.instrument = _instrument;
        this.portfolioManager = _portfolioManager;
        this.quantity = _quantity;
        this.price = _price;
    }
}
