package com.hazelcast.tudor;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InstrumentInfo {
    volatile double lastPrice;
    final ConcurrentMap<Integer, Portfolio> mapRelatedPortfolios = new ConcurrentHashMap<Integer, Portfolio>(100);

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    Portfolio getPortfolio(int pmId) {
        return mapRelatedPortfolios.get(pmId);
    }

    public void addPortfolio(Portfolio portfolio) {
        mapRelatedPortfolios.putIfAbsent(portfolio.pmId, portfolio);
    }

    public void removePortfolio(int pmId) {
        mapRelatedPortfolios.remove(pmId);
    }

    public Collection<Portfolio> getPortfolios() {
        return mapRelatedPortfolios.values();
    }
}
