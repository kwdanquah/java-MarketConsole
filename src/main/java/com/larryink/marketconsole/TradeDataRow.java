/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.larryink.marketconsole;

import javafx.beans.property.SimpleStringProperty;

public class TradeDataRow {

    private SimpleStringProperty time;
    private SimpleStringProperty buyer;
    private SimpleStringProperty seller;
    private SimpleStringProperty qty;
    private SimpleStringProperty price;
    private SimpleStringProperty symbol;

    public TradeDataRow(String time, String symbol, String buyer, String seller, long qty, double price) {
        this.time = new SimpleStringProperty(time);
        this.symbol = new SimpleStringProperty(symbol);
        this.buyer = new SimpleStringProperty(buyer);
        this.seller = new SimpleStringProperty(seller);
        this.qty = new SimpleStringProperty(String.format("%d",qty));
        this.price = new SimpleStringProperty(String.format("%.2f",price));
    }

    public void setTime(SimpleStringProperty time) {
        this.time = time;
    }
    
    public void setTime(String time) {
        this.time = new SimpleStringProperty(time);
    }

    public void setSymbol(SimpleStringProperty symbol) {
        this.symbol = symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = new SimpleStringProperty(symbol);
    }
    
    public void setBuyer(SimpleStringProperty buyer) {
        this.buyer = buyer;
    }
       
    public void setBuyer(String buyer) {
        this.buyer = new SimpleStringProperty(buyer);
    }
    
    public void setSeller(SimpleStringProperty seller) {
        this.seller = seller;
    }
       
    public void setSeller(String seller) {
        this.seller = new SimpleStringProperty(seller);
    }
    
    public void setQty(SimpleStringProperty qty) {
        this.qty = qty;
    }
    
    public void setQty(String qty) {
        this.qty = new SimpleStringProperty(qty);
    }
    
    public void setQty(long qty) {
        this.qty = new SimpleStringProperty(String.format("%d",qty));
    }

    public void setPrice(SimpleStringProperty price) {
        this.price = price;
    }
    
    public void setPrice(double price) {
        this.price = new SimpleStringProperty(String.format("%.2f",price));
    }
    
    public void setPrice(String price) {
        this.price = new SimpleStringProperty(price);
    }

    public String getTime() {
        return time.get();
    }

    public String getBuyer() {
        return buyer.get();
    }

    public String getSeller() {
        return seller.get();
    }

    public String getQty() {
        return qty.get();
    }

    public String getPrice() {
        return price.get();
    }

}
