package com.example;

import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.awt.FlowLayout;

public class Frontend {
    
    public static void viewList(List<Product> products){
        System.out.println();
        for (Product p : products){
            System.out.println(p.id + " " + p.name + " " + p.price + " " + p.quantity);
        }
        System.out.println();
    }

    public static void main(String[] args) throws SQLException {
        boolean owner = false;
        boolean manager = false;
        boolean employee = false;
        InventorySystem system = new InventorySystem();

        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (Exception e) {
            
        }

        // Add login menu
        JPanel loginpane = new JPanel();
        loginpane.setLayout(new BoxLayout(loginpane, BoxLayout.Y_AXIS));

        JPanel userpane = new JPanel();
        userpane.add(new JLabel("Username: "));
        JTextField userLogin = new JTextField(15);
        userpane.add(userLogin);

        JPanel passpane = new JPanel();
        passpane.add(new JLabel("Password: "));
        JPasswordField passwordLogin = new JPasswordField(15);
        passpane.add(passwordLogin);

        loginpane.add(userpane);
        loginpane.add(passpane);

        // While wrong login allow constant attempts instead of closing
        String role = null;
        while (role == null){
            int result = JOptionPane.showConfirmDialog(
                null,
                loginpane,
                "login",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );
            if (result == JOptionPane.CANCEL_OPTION) return;
            if (result == JOptionPane.OK_OPTION){
                String username = userLogin.getText();
                char[] passChar = passwordLogin.getPassword();
                String password = new String(passChar);

                role = system.login(username, password);
                
                if (role == null){
                    passwordLogin.setText("");
                }
        }
            if (role != null){
                if (role.toLowerCase().equals("owner")){
                    owner = true;
                }
                if (role.toLowerCase().equals("manager")){
                    manager = true;
                }
                if (role.toLowerCase().equals("employee")){
                    employee = true;
                }
            } 
        }

        if (employee || owner || manager){
            JFrame frame = new JFrame("Hexabyte Inventory Manager");
            frame.setLayout(new BorderLayout());
            frame.setSize(1920,1080);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JTabbedPane tabpane = new JTabbedPane();
            frame.add(tabpane, BorderLayout.CENTER);
        
            Scanner myInput = new Scanner(System.in);
            
            List<Product> Data = new ArrayList<>();
            system.loadInventory(Data);
            ProductTableModel model = new ProductTableModel(Data);
            JTable table = new JTable(model);
            Font font = new Font("Arial", Font.PLAIN, 15);
            table.setFont(font);
            ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
            table.setRowHeight(16);
            table.getTableHeader().setFont(font);
            JScrollPane scrl = new JScrollPane(table);
            tabpane.add("Stock", scrl);
            table.setRowHeight(20);

            List<Log> logs = new ArrayList<>();
            system.loadLog(logs);
            LogTableModel logmodel = new LogTableModel(logs);
            JTable logtable = new JTable(logmodel);
            logtable.setFont(font);
            ((DefaultTableCellRenderer) logtable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
            logtable.setRowHeight(16);
            logtable.getTableHeader().setFont(font);
            JScrollPane scrllog = new JScrollPane(logtable);
            tabpane.add("Logs", scrllog);
            logtable.setRowHeight(20);
            tabpane.setTabPlacement(JTabbedPane.LEFT);

            if (manager || owner){
                table.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyChar() == '=') {
                            try {
                                system.updateQuantity(Data.get(table.getSelectedRow()).getName(), Data.get(table.getSelectedRow()).getQuantity() + 1);
                                system.loadInventory(Data);
                                system.loadLog(logs);
                                logmodel.fireTableDataChanged();
                                model.fireTableDataChanged();   
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                            }
                        }
                        if (e.getKeyChar() == '\b') {
                            try {
                                int result = JOptionPane.showConfirmDialog(
                                frame,
                                "Are you sure you want to delete " + Data.get(table.getSelectedRow()).getName(),
                                "Confirmation Dialog",
                                JOptionPane.YES_OPTION
                                );
                                if (result == JOptionPane.YES_OPTION){
                                    system.removeItem(Data.get(table.getSelectedRow()).getName());
                                    system.loadInventory(Data);
                                    system.loadLog(logs);
                                    logmodel.fireTableDataChanged();
                                    model.fireTableDataChanged(); 
                                } 
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                            }
                        }
                        if (e.getKeyChar() == 'p') {
                            try {
                                String newPrice = JOptionPane.showInputDialog(
                                null,
                                "Enter new price for " + Data.get(table.getSelectedRow()).getName() + ":"
                                );
                                if (newPrice == null) {return;}
                                    system.updatePrice(Data.get(table.getSelectedRow()).getName(), Double.parseDouble(newPrice));
                                    system.loadInventory(Data);
                                    system.loadLog(logs);
                                    logmodel.fireTableDataChanged();
                                    model.fireTableDataChanged();   
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                            }
                        }
                    }
                });
            }

            table.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyChar() == '-') {
                        try {
                            system.updateQuantity(Data.get(table.getSelectedRow()).getName(), Data.get(table.getSelectedRow()).getQuantity() - 1);
                            system.loadInventory(Data);
                            system.loadLog(logs);
                            logmodel.fireTableDataChanged();
                            model.fireTableDataChanged();   
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                        }
                    }
                    if (e.getKeyChar() == 'q') {
                        try {
                            String newQuant = JOptionPane.showInputDialog(
                                null,
                                "Enter new quantity for " + Data.get(table.getSelectedRow()).getName() + ":"
                            );
                            if (newQuant == null) {return;}
                            system.updateQuantity(Data.get(table.getSelectedRow()).getName(), Integer.parseInt(newQuant));
                            system.loadInventory(Data);
                            system.loadLog(logs);
                            logmodel.fireTableDataChanged();
                            model.fireTableDataChanged();   
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Error: Please enter a valid integer.");
                        } catch (Exception ea) {
                            JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                        }
                    }
                }
            });

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
            JButton addButt = new JButton("Add");
            top.add(addButt);
            JButton removeButt = new JButton("Remove");
            top.add(removeButt);
            JButton priceButt = new JButton("Change Price");
            top.add(priceButt);
            JButton quantityButt = new JButton("Change Quantity");
            top.add(quantityButt);

            top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

            JPanel rightpane = new JPanel();
            frame.add(rightpane, BorderLayout.LINE_START);

            addButt.addActionListener(e -> {
                JPanel addPane = new JPanel();
                addPane.setLayout(new BoxLayout(addPane, BoxLayout.Y_AXIS));

                JPanel idAdd = new JPanel();
                idAdd.add(new JLabel("ID: "));
                JTextField idProduct = new JTextField(15);
                idAdd.add(idProduct);

                JPanel nameAdd = new JPanel();
                nameAdd.add(new JLabel("Product Name: "));
                JTextField nameProduct = new JTextField(15);
                nameAdd.add(nameProduct);

                JPanel priceAdd = new JPanel();
                priceAdd.add(new JLabel("Product Price: "));
                JTextField priceProduct = new JTextField(15);
                priceAdd.add(priceProduct);

                JPanel quantAdd = new JPanel();
                quantAdd.add(new JLabel("Product Quantity: "));
                JTextField quantProduct = new JTextField(15);
                quantAdd.add(quantProduct);

                JPanel originAdd = new JPanel();
                originAdd.add(new JLabel("Origin: "));
                JTextField originProduct = new JTextField(15);
                originAdd.add(originProduct);

                addPane.add(idAdd);
                addPane.add(nameAdd);
                addPane.add(priceAdd);
                addPane.add(quantAdd);
                addPane.add(originAdd);

                int addProduct = JOptionPane.showConfirmDialog(
                    null,
                    addPane,
                    "Add Product",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                );
                if (addProduct == JOptionPane.CANCEL_OPTION) {return;}
                if (addProduct == JOptionPane.OK_OPTION){
                    try {
                        system.addItem(Integer.parseInt(idProduct.getText()), nameProduct.getText(), Double.parseDouble(priceProduct.getText()), Integer.parseInt(quantProduct.getText()), originProduct.getText());
                        system.loadInventory(Data);
                        system.loadLog(logs);
                        logmodel.fireTableDataChanged();
                        model.fireTableDataChanged();
                    } catch (Exception er) {
                        
                    }
                }
            });

            removeButt.addActionListener(e -> {
                try {
                    int result1 = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to delete " + Data.get(table.getSelectedRow()).getName(),
                    "Confirmation Dialog",
                    JOptionPane.YES_OPTION
                    );
                    if (result1 == JOptionPane.YES_OPTION){
                        system.removeItem(Data.get(table.getSelectedRow()).getName());
                        system.loadInventory(Data);
                        system.loadLog(logs);
                        logmodel.fireTableDataChanged();
                        model.fireTableDataChanged(); 
                    } 
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                }
            });

            priceButt.addActionListener(e -> {
                try {
                    String newPrice = JOptionPane.showInputDialog(
                        null,
                        "Enter new price for " + Data.get(table.getSelectedRow()).getName() + ":"
                    );
                    if (newPrice == null) {
                        return;
                    }
                        system.updatePrice(Data.get(table.getSelectedRow()).getName(), Double.parseDouble(newPrice));
                        system.loadInventory(Data);
                        system.loadLog(logs);
                        logmodel.fireTableDataChanged();
                        model.fireTableDataChanged();   
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                }
            });

            quantityButt.addActionListener(e -> {
                try {
                    String newQuant = JOptionPane.showInputDialog(
                        null,
                        "Enter new quantity for " + Data.get(table.getSelectedRow()).getName() + ":"
                    );
                    if (newQuant == null) {
                        return;
                    }
                        system.updateQuantity(Data.get(table.getSelectedRow()).getName(), Integer.parseInt(newQuant));
                        system.loadInventory(Data);
                        system.loadLog(logs);
                        logmodel.fireTableDataChanged();
                        model.fireTableDataChanged();   
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Error: Please enter a valid integer.");
                } catch (Exception ea) {
                    JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                }
            });

            if (!(owner || manager)){
                priceButt.setVisible(false);
                removeButt.setVisible(false);
                addButt.setVisible(false);
                tabpane.remove(1);
            }

            frame.add(top, BorderLayout.PAGE_START);
            
            frame.setVisible(true);
            myInput.close();
        }
        else {
            JOptionPane.showMessageDialog(null, "Error: Invalid User.");
        }
    }
}
