package com.hazelcast.tudor;

import com.hazelcast.nio.DataSerializable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PositionView implements DataSerializable {
    int pmId;
    int instrumentId;
    int quantity;
    double lastPrice;
    double profitOrLoss;

    public PositionView(int pmId, int instrumentId, int quantity, double lastPrice, double profitOrLoss) {
        this.pmId = pmId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.lastPrice = lastPrice;
        this.profitOrLoss = profitOrLoss;
    }

    public void readData(DataInput in) throws IOException {
        pmId = in.readInt();
        instrumentId = in.readInt();
        quantity = in.readInt();
        lastPrice = in.readDouble();
        profitOrLoss = in.readDouble();
    }

    public void writeData(DataOutput out) throws IOException {
        out.writeInt(pmId);
        out.writeInt(instrumentId);
        out.writeInt(quantity);
        out.writeDouble(lastPrice);
        out.writeDouble(profitOrLoss);
    }

    public int getInstrumentId() {
        return instrumentId;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public int getPmId() {
        return pmId;
    }

    public double getProfitOrLoss() {
        return profitOrLoss;
    }

    public int getQuantity() {
        return quantity;
    }
}
