package com.hazelcast.tudor;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

public class LookupDatabase {
    final static Map<Integer, Instrument> mapInstruments = new HashMap<Integer, Instrument>(8000);
    final static Random random = new Random(System.currentTimeMillis());

    static {
        load();
    }

    private static void load() {
        InputStream in = LookupDatabase.class.getResourceAsStream("/stocks.txt");
        String stocks = readFile(in);
        System.out.println(stocks);
        StringTokenizer st = new StringTokenizer(stocks, ",");
        int id = 0;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            System.out.println(token);
            Instrument instrument = new Instrument(id++, token, token);
            mapInstruments.put(instrument.id, instrument);
        }
    }

    private static String readFile(InputStream in) {
        StringBuffer contents = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String text = null;
            while ((text = reader.readLine()) != null) {
                contents.append(text)
                        .append(System.getProperty(
                                "line.separator"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contents.toString();
    }

    public static Instrument getInstrumentById(int instrumentId) {
        return mapInstruments.get(instrumentId);
    }

    public static Instrument randomPickInstrument() {
        return mapInstruments.get(random.nextInt(mapInstruments.size()));
    }

    public static void main(String[] args) {
        System.out.println("random " + LookupDatabase.randomPickInstrument());
    }
}
