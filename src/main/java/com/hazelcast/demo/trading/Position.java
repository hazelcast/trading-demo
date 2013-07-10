package com.hazelcast.demo.trading;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Position implements DataSerializable {
    Integer instrumentId;
    int quantity = 0;
    List<Deal> lsDeals;

    public Position(Integer instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Position() {
    }

    public void addDeal(Deal deal) {
        if (lsDeals == null) {
            lsDeals = new ArrayList<Deal>();
        }
        this.quantity += deal.quantity;
        this.lsDeals.add(deal);
    }

    public void readData(ObjectDataInput in) throws IOException {
        instrumentId = in.readInt();
        quantity = in.readInt();
        int size = in.readInt();
        lsDeals = new ArrayList<Deal>(size);
        for (int i = 0; i < size; i++) {
            Deal deal = new Deal();
            deal.readData(in);
            lsDeals.add(deal);
        }
    }

    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(instrumentId);
        out.writeInt(quantity);
        out.writeInt(lsDeals.size());
        for (Deal deal : lsDeals) {
            deal.writeData(out);
        }
    }

    public double calculateProfitLoss(double lastPrice) {
        double profitOrLoss = 0;
        if (lsDeals != null) {
            for (Deal deal : lsDeals) {
                profitOrLoss += deal.quantity * (lastPrice - deal.price);
            }
        }
        return profitOrLoss;
    }

    public int getDealSize() {
        return (lsDeals == null) ? 0 : lsDeals.size();
    }
}
