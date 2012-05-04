package com.hazelcast.demo.trading;

import java.io.Serializable;

public class PositionPortfolioManager extends Position implements Serializable {

    Integer portfolioManagerId;

    PositionPortfolioManager(Integer _instrumentId, Integer _portfolioManagerId) {
        this.instrumentId = _instrumentId;
        this.portfolioManagerId = _portfolioManagerId;
    }

    String getKey() {
        return this.instrumentId + "," + this.portfolioManagerId;
    }
}
