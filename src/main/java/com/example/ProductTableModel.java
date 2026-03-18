package com.example;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ProductTableModel extends AbstractTableModel {

    private List<Product> products;
    private String[] columns = {"ID", "Name", "Price ($)", "Quantity", "Origin"};

    public ProductTableModel(List<Product> products) {
        this.products = products;
    }

    @Override
    public int getRowCount() {
        return products.size();
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
        Product p = products.get(rowIndex);

        switch (columnIndex) {
            case 0: return p.getId();
            case 1: return p.getName();
            case 2: return p.getPrice();
            case 3: return p.getQuantity();
            case 4: return p.getOrigin();
            default: return null;
        }
    }
}