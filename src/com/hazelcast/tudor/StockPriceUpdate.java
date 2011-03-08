package com.hazelcast.tudor;

import com.hazelcast.nio.DataSerializable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StockPriceUpdate implements DataSerializable {
    String symbol;
    double price;

    public StockPriceUpdate() {
    }

    public StockPriceUpdate(String symbol, double price) {
        this.price = price;
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public String getSymbol() {
        return symbol;
    }

    public void readData(DataInput in) throws IOException {
        symbol = in.readUTF();
        price = in.readDouble();
    }

    public void writeData(DataOutput out) throws IOException {
        out.writeUTF(symbol);
        out.writeDouble(price);
    }

    @Override
    public String toString() {
        return "StockPriceUpdate{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                '}';
    }
}
