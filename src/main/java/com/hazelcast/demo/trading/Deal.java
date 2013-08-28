package com.hazelcast.demo.trading;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

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

    public void readData(ObjectDataInput in) throws IOException {
        quantity = in.readInt();
        price = in.readDouble();
    }

    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(quantity);
        out.writeDouble(price);
    }
}
