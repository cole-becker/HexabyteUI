package com.example;

public class Product {
    public String name;
    public double price;
    public int quantity;
    public int id;
    public String origin;

    public Product(int id, String n, double p, int q, String o) {
        this.id = id;
        this.name = n;
        this.price = p;
        this.quantity = q;
        this.origin = o;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public double getPrice(){
        return price;
    }

    public int getQuantity(){
        return quantity;
    }

    public void setQuantity(int q){
        quantity = q;
    }

    public void setPrice(double p){
        price = p;
    }

    public String getOrigin() { return origin; }
}