package com.hazelcast.tudor;

import java.io.Serializable;

public class PortfolioManager implements Serializable {
    Integer id;
    String name;

    PortfolioManager(Integer _id, String _name) {
        this.id = _id;
        this.name = _name;
    }
}
