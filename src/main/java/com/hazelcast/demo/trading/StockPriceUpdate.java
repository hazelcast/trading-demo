package com.hazelcast.demo.trading;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class StockPriceUpdate implements DataSerializable {
    int instrumentId;
    double price;

    public StockPriceUpdate() {
    }

    public StockPriceUpdate(Integer instrumentId, double price) {
        this.price = price;
        this.instrumentId = instrumentId;
    }

    public double getPrice() {
        return price;
    }

    public int getInstrumentId() {
        return instrumentId;
    }

    public void readData(ObjectDataInput in) throws IOException {
        instrumentId = in.readInt();
        price = in.readDouble();
    }

    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(instrumentId);
        out.writeDouble(price);
    }

    @Override
    public String toString() {
        return "StockPriceUpdate{" +
                "instrumentId='" + instrumentId + '\'' +
                ", price=" + price +
                '}';
    }
}
