package com.hazelcast.tudor;

import com.hazelcast.nio.DataSerializable;

import java.io.DataInput;
import java.io.DataOutput;
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

    public void readData(DataInput in) throws IOException {
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

    public void writeData(DataOutput out) throws IOException {
        out.writeInt(instrumentId);
        out.writeInt(quantity);
        out.writeInt(lsDeals.size());
        for (Deal deal : lsDeals) {
            deal.writeData(out);
        }
    }

    public int getDealSize() {
        return (lsDeals == null) ? 0 : lsDeals.size();
    }
}
