package com.example;

import java.util.*;

import javax.swing.JTextField;

public class Backend {

    public static void addProduct(List<Product> products, Product p){
        boolean found = false;
        for (Product p2 : products){
            if (p2.getName().replace(" ", "").toLowerCase().equals(p.getName().replace(" ", "").toLowerCase())){
                p2.setQuantity(p2.getQuantity() + p.getQuantity());
                found = true;
                break;
            }
        }
        if (!found){
            products.add(p);
        }
    }

    public static void removeProduct(List<Product> products, JTextField n){
        for (Product p : products){
            if (p.getName().replace(" ","").toLowerCase().equals(n.getText().replace(" ", "").toLowerCase())){
                products.remove(p);
                break;
            }
        }
    }

    public static void removeProductshort(List<Product> products, String n){
        for (Product p : products){
            if (p.getName().replace(" ","").toLowerCase().equals(n.replace(" ", "").toLowerCase())){
                products.remove(p);
                break;
            }
        }
    }
    
    public static void editQuantity(List<Product> products, JTextField nametxt, JTextField pricetxt){
        for (Product p : products){
            if (p.getName().replace(" ", "").toLowerCase().equals(nametxt.getText().replace(" ", "").toLowerCase())){
                p.setQuantity(Integer.parseInt(pricetxt.getText()));
                break;
            }
        }
    }

    public static void editPrice(List<Product> products, JTextField nametxt, JTextField pricetxt){
        for (Product p : products){
            if (p.getName().replace(" ", "").toLowerCase().equals(nametxt.getText().replace(" ", "").toLowerCase())){
                p.setPrice(Double.parseDouble(pricetxt.getText()));
                break;
            }
        }
    }
} 
