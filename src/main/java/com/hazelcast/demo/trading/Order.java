package com.hazelcast.demo.trading;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Order extends PMOrder {
    int id;
    int portfolioManagerId;
    List<Integer> lsAccounts;

    public Order(int id, int instrumentId, int quantity, double price, int portfolioManagerId, List<Integer> lsAccounts) {
        super(instrumentId, quantity, price);
        this.id = id;
        this.portfolioManagerId = portfolioManagerId;
        this.lsAccounts = lsAccounts;
    }

    public Order() {
    }

    @Override
    public void readData(DataInput in) throws IOException {
        super.readData(in);
        id = in.readInt();
        portfolioManagerId = in.readInt();
        int size = in.readInt();
        lsAccounts = new ArrayList<Integer>(size);
        for (int i = 0; i < size; i++) {
            lsAccounts.add(in.readInt());
        }
    }

    @Override
    public void writeData(DataOutput out) throws IOException {
        super.writeData(out);
        out.writeInt(id);
        out.writeInt(portfolioManagerId);
        out.writeInt(lsAccounts.size());
        for (int i = 0; i < lsAccounts.size(); i++) {
            out.writeInt(lsAccounts.get(i));
        }
    }
}
