package com.hazelcast.tudor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PMOrder extends Deal {
    int instrumentId;
    int quantity;
    double price;

    public PMOrder(int instrumentId, int quantity, double price) {
        this.instrumentId = instrumentId;
        this.price = price;
        this.quantity = quantity;
    }

    public PMOrder() {
    }

    public void readData(DataInput in) throws IOException {
        instrumentId = in.readInt();
        quantity = in.readInt();
        price = in.readDouble();
    }

    public void writeData(DataOutput out) throws IOException {
        out.writeInt(instrumentId);
        out.writeInt(quantity);
        out.writeDouble(price);
    }
}
