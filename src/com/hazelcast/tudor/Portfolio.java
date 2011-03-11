package com.hazelcast.tudor;

import java.util.HashMap;
import java.util.Map;

public class Portfolio {
    final int pmId;
    final Map<Integer, Position> mapPositions = new HashMap<Integer, Position>(100); //instrumentId, Position

    public Portfolio(int pmId) {
        this.pmId = pmId;
    }

    public void update(Position position) {
        mapPositions.put(position.instrumentId, position);
    }

    public double calculateProfitOrLoss() {
        return 0;
    }

    public Position getPosition(int instrumentId) {
        return mapPositions.get(instrumentId);
    }
}
