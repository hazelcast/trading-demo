package com.hazelcast.tudor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

public class StockDatabase {
    static List<String> lsStocks = new ArrayList<String>(8000);
    static Random random = new Random(System.currentTimeMillis());

    static {
        load();
    }

    private static void load() {
        String stocksFile = System.getProperty("stocks.file");
        if (stocksFile == null) {
            stocksFile = "/apps/projects/hazelcast/space/hztudor/trunk/stocks.txt";
        }
        String stocks = readFile(stocksFile);
        System.out.println(stocks);
        StringTokenizer st = new StringTokenizer(stocks, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            System.out.println(token);
            lsStocks.add(token);
        }
    }

    private static String readFile(String filename) {
        File file = new File(filename);
        StringBuffer contents = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
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

    public static String randomPick() {
        return lsStocks.get(random.nextInt(lsStocks.size()));
    }

    public static void main(String[] args) {
        System.out.println("random " + StockDatabase.randomPick());
    }
}
