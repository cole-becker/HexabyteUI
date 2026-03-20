package com.example;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class LogTableModel extends AbstractTableModel {

    private List<Log> logs;
    private String[] columns = {"Idlog", "User", "Entries", "Timestamp"};

    public LogTableModel(List<Log> logs) {
        this.logs = logs;
    }

    @Override
    public int getRowCount() {
        return logs.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Log l = logs.get(rowIndex);

        switch (columnIndex) {
            case 0: return l.getIdlog();
            case 1: return l.getUser();
            case 2: return l.getEntries();
            case 3: return l.getTimestamp();
            default: return null;
        }
    }
}