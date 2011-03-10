package com.hazelcast.tudor;

import java.util.HashMap;
import java.util.Map;

public class Portfolio {
    int pmId;
    Map<String, Position> mapPositions = new HashMap<String, Position>(100);
}
