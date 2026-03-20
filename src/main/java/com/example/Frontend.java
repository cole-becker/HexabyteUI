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
        boolean firststart = false;
        String role = null;

        try {
            InventorySystem system = new InventorySystem();
            
            if (system.isTableEmpty("users")){
                firststart = true;
            }

            // Create SQL Tables for the user if not already there
            system.initializeDatabases();

            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
            } catch (Exception e) {
                System.out.println();
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

            // If no users detected in databse create owner login
            if (firststart) {
                int result = JOptionPane.showConfirmDialog(
                    null,
                    loginpane,
                    "create login for owner",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                );
                if (result == JOptionPane.CANCEL_OPTION) return;
                if (result == JOptionPane.OK_OPTION){
                    String username = userLogin.getText();
                    char[] passChar = passwordLogin.getPassword();
                    String password = new String(passChar);

                    system.addUser(username, password, "Owner", "System");
                    role = "Owner";
                    owner = true;
                }
            }
            
            // Loop through login attempts 
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
            final boolean isOwner = owner;
            final boolean isManager = manager;
            final String currentUser = userLogin.getText();

            // If valid login role then open UI
            if (employee || owner || manager){
                JFrame frame = new JFrame("Hexabyte Inventory Manager - " + role);
                frame.setLayout(new BorderLayout());
                frame.setSize(1920,1080);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JTabbedPane tabpane = new JTabbedPane();
                frame.add(tabpane, BorderLayout.CENTER);
            
                Scanner myInput = new Scanner(System.in);
                
                // Create tablemodel for Products and set the tab
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

                // Create tablemodel for logs and set the tab
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
                    // KeyBind shortcuts for manager and owner for removing and changing quantity and prices
                    table.addKeyListener(new KeyAdapter() {
                        public void keyPressed(KeyEvent e) {
                            if (e.getKeyChar() == '=') {
                                try {
                                    system.updateQuantity(Data.get(table.getSelectedRow()).getName(), Data.get(table.getSelectedRow()).getQuantity() + 1, currentUser);
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
                                        system.removeItem(Data.get(table.getSelectedRow()).getName(), currentUser);
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
                                    if (Double.parseDouble(newPrice) >= 0.0){
                                        system.updatePrice(Data.get(table.getSelectedRow()).getName(), Double.parseDouble(newPrice), currentUser);
                                        system.loadInventory(Data);
                                        system.loadLog(logs);
                                        logmodel.fireTableDataChanged();
                                        model.fireTableDataChanged();   
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Cannot make price less than 0.0.");
                                    }
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                                }
                            }
                        }
                    });
                }

                // KeyBind shortcuts changing quantity
                table.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyChar() == '-') {
                            try {
                                if ((Data.get(table.getSelectedRow()).getQuantity() - 1) >= 0){
                                    system.updateQuantity(Data.get(table.getSelectedRow()).getName(), Data.get(table.getSelectedRow()).getQuantity() - 1, currentUser);
                                    system.loadInventory(Data);
                                    system.loadLog(logs);
                                    logmodel.fireTableDataChanged();
                                    model.fireTableDataChanged(); 
                                }
                                else {
                                    JOptionPane.showMessageDialog(null, "Cannot make quantity less than 0.");
                                }  
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
                                if (Integer.parseInt(newQuant) >= 0){
                                    system.updateQuantity(Data.get(table.getSelectedRow()).getName(), Integer.parseInt(newQuant), currentUser);
                                    system.loadInventory(Data);
                                    system.loadLog(logs);
                                    logmodel.fireTableDataChanged();
                                    model.fireTableDataChanged(); 
                                }else {
                                    JOptionPane.showMessageDialog(null, "Cannot make quantity less than 0.");
                                }  
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Error: Please enter a valid integer.");
                            } catch (Exception ea) {
                                JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                            }
                        }
                    }
                });

                // Create and add buttons to JPanel
                JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
                JButton addButt = new JButton("Add");
                top.add(addButt);
                JButton removeButt = new JButton("Remove");
                top.add(removeButt);
                JButton priceButt = new JButton("Change Price");
                top.add(priceButt);
                JButton quantityButt = new JButton("Change Quantity");
                top.add(quantityButt);

                // JMenubar for adding and removing users from database
                JMenuBar menubar = new JMenuBar();
                JMenu menu = new JMenu("User");
                JMenuItem addUser = new JMenuItem("Add New User");
                JMenuItem removeUser = new JMenuItem("Remove User");
                menu.add(addUser);
                menu.add(removeUser);
                menubar.add(menu);

                frame.setJMenuBar(menubar);

                // Pop up to add new user to database, Managers can only add employees
                addUser.addActionListener(e -> {
                    JPanel addUserPane = new JPanel();
                    addUserPane.setLayout(new BoxLayout(addUserPane, BoxLayout.Y_AXIS));

                    JPanel usernamePane = new JPanel();
                    usernamePane.add(new JLabel("Username: "));
                    JTextField usernameInput = new JTextField(15);
                    usernamePane.add(usernameInput);

                    JPanel passwordPane = new JPanel();
                    passwordPane.add(new JLabel("Password: "));
                    JPasswordField passwordInput = new JPasswordField(15);
                    passwordPane.add(passwordInput);

                    JPanel rolePane = new JPanel();
                    rolePane.add(new JLabel("Role: "));
                    String [] roles = new String[] {"Employee", "Manager"};
                    JComboBox<String> roleInput = new JComboBox<>(roles);
                    rolePane.add(roleInput);

                    addUserPane.add(usernamePane);
                    addUserPane.add(passwordPane);
                    addUserPane.add(rolePane);

                    int result = JOptionPane.showConfirmDialog(
                    null,
                    addUserPane,
                    "create new user",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                    );

                    if (result == JOptionPane.CANCEL_OPTION) {return;}
                    
                    if (result == JOptionPane.OK_OPTION){
                        char[] passChar = passwordInput.getPassword();
                        String addPass = new String(passChar);
                        if (isOwner){
                            if (!addPass.isEmpty() && !usernameInput.getText().isEmpty()){
                                system.addUser(usernameInput.getText(), addPass, roleInput.getSelectedItem().toString(), currentUser);
                                JOptionPane.showMessageDialog(null, "Successfully created user: " + usernameInput.getText());
                                system.loadLog(logs);
                                logmodel.fireTableDataChanged();
                            } else {
                                JOptionPane.showMessageDialog(null, "Invalid User");
                            }
                        }
                        if (isManager){
                            if (roleInput.getSelectedItem().toString().toLowerCase().equals("employee")){
                                if (!addPass.isEmpty() && !usernameInput.getText().isEmpty()){
                                    system.addUser(usernameInput.getText(), addPass, roleInput.getSelectedItem().toString(), currentUser);
                                    JOptionPane.showMessageDialog(null, "Successfully created user: " + usernameInput.getText());
                                    system.loadLog(logs);
                                    logmodel.fireTableDataChanged();
                                }
                            } 
                            if (roleInput.getSelectedItem().toString().toLowerCase().equals("manger")) {
                                JOptionPane.showMessageDialog(null, "Insuffiecient Perimissions");
                            }
                        }
                    }   
                });

                // Pop up to remove users from database with role awareness
                removeUser.addActionListener(e -> {
                    JPanel removePane = new JPanel();
                    removePane.add(new JLabel("Username: "));
                    JTextField userRemove = new JTextField(15);
                    removePane.add(userRemove);
                    
                    int result = JOptionPane.showConfirmDialog(
                    null,
                    removePane,
                    "Remove user",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                    );

                    if (result == JOptionPane.CANCEL_OPTION){return;}
                    if (result == JOptionPane.OK_OPTION){
                        // Manager cannot delete owner
                        if (isManager) {
                            if (!system.getRole(userRemove.getText()).toLowerCase().equals("owner")){
                                system.removeUser(userRemove.getText(), currentUser, userRemove.getText());
                                JOptionPane.showMessageDialog(null, "User successfully deleted");
                                system.loadLog(logs);
                                logmodel.fireTableDataChanged();
                            }
                        // Owner can delete anyone (even himself to reset login)
                        } else {
                            system.removeUser(userRemove.getText(), currentUser, userRemove.getText());
                            JOptionPane.showMessageDialog(null, "User successfully deleted");
                            system.loadLog(logs);
                            logmodel.fireTableDataChanged();
                        }
                    }
                });

                // Space the top bar buttons
                top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
                top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

                // Create right pane for looks
                JPanel rightpane = new JPanel();
                frame.add(rightpane, BorderLayout.LINE_START);

                // Pop up for adding a new Product to the system
                addButt.addActionListener(e -> {
                    JPanel addPane = new JPanel();
                    addPane.setLayout(new BoxLayout(addPane, BoxLayout.Y_AXIS));

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
                            if (Integer.parseInt(quantProduct.getText()) >= 0 && Double.parseDouble(priceProduct.getText()) >= 0){
                                system.addItem(nameProduct.getText(), Double.parseDouble(priceProduct.getText()), Integer.parseInt(quantProduct.getText()), originProduct.getText(), currentUser);
                                system.loadInventory(Data);
                                system.loadLog(logs);
                                logmodel.fireTableDataChanged();
                                model.fireTableDataChanged();
                            } else {
                                JOptionPane.showMessageDialog(null, "Error: Product not added because either the price or quantity was less than 0.");
                            }
                        } catch (Exception er) {
                            
                        }
                    }
                });

                // Remove an Product from the system after selecting it in the Table
                removeButt.addActionListener(e -> {
                    try {
                        int result1 = JOptionPane.showConfirmDialog(
                        frame,
                        "Are you sure you want to delete " + Data.get(table.getSelectedRow()).getName(),
                        "Confirmation Dialog",
                        JOptionPane.YES_OPTION
                        );
                        if (result1 == JOptionPane.YES_OPTION){
                            system.removeItem(Data.get(table.getSelectedRow()).getName(), currentUser);
                            system.loadInventory(Data);
                            system.loadLog(logs);
                            logmodel.fireTableDataChanged();
                            model.fireTableDataChanged(); 
                        } 
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                    }
                });

                // Pop up to change price of selected product in table
                priceButt.addActionListener(e -> {
                    try {
                        String newPrice = JOptionPane.showInputDialog(
                            null,
                            "Enter new price for " + Data.get(table.getSelectedRow()).getName() + ":"
                        );
                        if (newPrice == null) {
                            return;
                        }
                        if (Double.parseDouble(newPrice) >= 0.0){
                            system.updatePrice(Data.get(table.getSelectedRow()).getName(), Double.parseDouble(newPrice), currentUser);
                            system.loadInventory(Data);
                            system.loadLog(logs);
                            logmodel.fireTableDataChanged();
                            model.fireTableDataChanged();   
                        } else {
                            JOptionPane.showMessageDialog(null, "Cannot make price less than 0.0.");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                    }
                });

                // Pop up to change quantity of selected product in table
                quantityButt.addActionListener(e -> {
                    try {
                        String newQuant = JOptionPane.showInputDialog(
                            null,
                            "Enter new quantity for " + Data.get(table.getSelectedRow()).getName() + ":"
                        );
                        if (newQuant == null) {
                            return;
                        }
                        if (Integer.parseInt(newQuant) >= 0){
                            system.updateQuantity(Data.get(table.getSelectedRow()).getName(), Integer.parseInt(newQuant), currentUser);
                            system.loadInventory(Data);
                            system.loadLog(logs);
                            logmodel.fireTableDataChanged();
                            model.fireTableDataChanged();   
                        } else {
                            JOptionPane.showMessageDialog(null, "Cannot make quantity less than 0.");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Error: Please enter a valid integer.");
                    } catch (Exception ea) {
                        JOptionPane.showMessageDialog(null, "Error: Select a row in the stock table.");
                    }
                });

                // Disable buttons and menu so that employees don't have permissions
                if (!(owner || manager)){
                    priceButt.setVisible(false);
                    removeButt.setVisible(false);
                    addButt.setVisible(false);
                    tabpane.remove(1);
                    menu.setVisible(false);
                }

                frame.add(top, BorderLayout.PAGE_START);
                
                frame.setVisible(true);
                myInput.close();
            }

        } catch (Exception er) {
            JOptionPane.showMessageDialog(null, "SQL Server not running or failure to connect.");
        }
    }
}
