package com.hazelcast.tudor;

import java.io.Serializable;

public class Instrument implements Serializable {
    Integer id;
    String symbol;
    String name;

    Instrument(Integer _id, String _symbol, String _name) {
        this.id = _id;
        this.symbol = _symbol;
        this.name = _name;
    }
}
