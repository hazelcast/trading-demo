package com.hazelcast.demo.trading;

import com.hazelcast.nio.DataSerializable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Deal implements DataSerializable {
    int quantity;
    double price;

    public Deal(int quantity, double price) {
        this.price = price;
        this.quantity = quantity;
    }

    public Deal() {
    }

    public void readData(DataInput in) throws IOException {
        quantity = in.readInt();
        price = in.readDouble();
    }

    public void writeData(DataOutput out) throws IOException {
        out.writeInt(quantity);
        out.writeDouble(price);
    }
}
