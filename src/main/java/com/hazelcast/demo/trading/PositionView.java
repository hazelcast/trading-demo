package com.hazelcast.demo.trading;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

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

    public PositionView() {
    }

    public void readData(ObjectDataInput in) throws IOException {
        pmId = in.readInt();
        instrumentId = in.readInt();
        quantity = in.readInt();
        lastPrice = in.readDouble();
        profitOrLoss = in.readDouble();
    }

    public void writeData(ObjectDataOutput out) throws IOException {
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

    @Override
    public String toString() {
        return "PositionView{" +
                "pmId=" + pmId +
                ", instrumentId=" + instrumentId +
                ", quantity=" + quantity +
                ", lastPrice=" + lastPrice +
                ", profitOrLoss=" + profitOrLoss +
                '}';
    }
}
