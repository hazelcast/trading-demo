package com.hazelcast.tudor;

import com.hazelcast.client.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class TraderDesktop {
    final HazelcastClient hazelcastClient;
    volatile boolean running = true;

    public static void main(String args[]) {
        String hostname = (args != null && args.length > 0) ? args[0] : "localhost";
        TraderDesktop td = new TraderDesktop(hostname);
        td.init();
    }

    public TraderDesktop(String hostname) {
        ClientConfig config = new ClientConfig();
        config.addAddress(hostname);
        hazelcastClient = HazelcastClient.newHazelcastClient(config);
    }

    void init() {
        JFrame frame = new JFrame("Trader Desktop");
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        // This is an empty content area in the frame
        final JLabel label = new JLabel("PortfolioManagerId");
        final JTextField txtId = new JTextField("18");
        final JButton btOpen = new JButton("Open");
        btOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openPMWindow(txtId.getText().trim());
            }
        });
        frame.setLayout(new FlowLayout());
        frame.add(label);
        frame.add(txtId);
        frame.add(btOpen);
        frame.pack();
        frame.setVisible(true);
    }

    void openPMWindow(String id) {
        JFrame frame = new PMWindow("PM " + id, Integer.parseInt(id));
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                running = false;
                hazelcastClient.shutdown();
                System.exit(0);
            }
        });
        frame.pack();
        frame.setVisible(true);
    }

    class PMWindow extends JFrame {
        final PMTableModel tableModel = new PMTableModel();
        final JTable table = new JTable(tableModel);
        final ITopic topic;
        final java.util.List<Integer> lsInstrumentIds = new ArrayList<Integer>();
        final ConcurrentMap<Integer, PositionView> positions = new ConcurrentHashMap<Integer, PositionView>(100);
        final JLabel lblCount = new JLabel("0");
        final java.util.Queue<Integer> updatedRows = new ConcurrentLinkedQueue<Integer>();

        PMWindow(String title, int pmId) throws HeadlessException {
            super(title);
            init();
            topic = hazelcastClient.getTopic("pm_" + pmId);
            new Thread() {
                @Override
                public void run() {
                    while (running) {
                        try {
                            Thread.sleep(500);
                            Integer row = updatedRows.poll();
                            while (row != null) {
                                table.removeRowSelectionInterval(row, row);
                                row = updatedRows.poll();
                            }
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
            }.start();
            topic.addMessageListener(new MessageListener<PositionView>() {
                int count = 0;

                public void onMessage(Message<PositionView> pv) {
                    lblCount.setText(String.valueOf(count++));
                    Integer instrumentId = pv.getMessageObject().getInstrumentId();
                    if (positions.put(instrumentId, pv.getMessageObject()) == null) {
                        lsInstrumentIds.add(instrumentId);
                        int row = lsInstrumentIds.indexOf(instrumentId);
                        tableModel.fireTableRowsInserted(row, row);
                    } else {
                        int row = lsInstrumentIds.indexOf(instrumentId);
                        tableModel.fireTableRowsUpdated(row, row);
                        updatedRows.add(row);
                        table.addRowSelectionInterval(row, row);
                    }
                }
            });
        }

        void init() {
            table.setPreferredScrollableViewportSize(new Dimension(300, 400));
            table.setFillsViewportHeight(true);
            JScrollPane scrollPane = new JScrollPane(table);
            setLayout(new BorderLayout());
            add(lblCount, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
        }

        class PMTableModel extends AbstractTableModel {
            private String[] columnNames = {"#", "Instrument", "Quantity", "CurrentPrice", "P&L"};
            final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

            public int getColumnCount() {
                return columnNames.length;
            }

            public int getRowCount() {
                return lsInstrumentIds.size();
            }

            public String getColumnName(int col) {
                return columnNames[col];
            }

            public Object getValueAt(int row, int col) {
                PositionView pw = positions.get(lsInstrumentIds.get(row));
                Instrument instrument = LookupDatabase.getInstrumentById(pw.getInstrumentId());
                if (col == 0)
                    return row + 1;
                else if (col == 1)
                    return instrument.symbol;
                else if (col == 2)
                    return pw.getQuantity();
                else if (col == 3)
                    return pw.getLastPrice();
                else if (col == 4)
                    return currencyFormat.format(pw.getProfitOrLoss());
                return "ERROR";
            }

            public Class getColumnClass(int c) {
                return String.class;
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        }
    }
}
