package com.hazelcast.demo.trading;

import java.io.Serializable;

public class PositionEntity extends Position implements Serializable {
    String accountId;
}
