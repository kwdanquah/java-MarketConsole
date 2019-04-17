/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.larryink.marketconsole;

import javafx.beans.property.SimpleStringProperty;

public class DataRow {

    private SimpleStringProperty bidQty;
    private SimpleStringProperty bid;
    private SimpleStringProperty ask;
    private SimpleStringProperty askQty;

    public DataRow(String bidQty, String bid, String ask, String askQty) {
        this.bidQty = new SimpleStringProperty(bidQty);
        this.bid = new SimpleStringProperty(bid);
        this.ask = new SimpleStringProperty(ask);
        this.askQty = new SimpleStringProperty(askQty);
    }

    public void setBidQty(SimpleStringProperty bidQty) {
        this.bidQty = bidQty;
    }
    
    public void setBidQty(long bidQty) {
        this.bidQty = new SimpleStringProperty(String.format("%d",bidQty));
    }
    
    public void setBidQty(String bidQty) {
        this.bidQty = new SimpleStringProperty(bidQty);
    }

    public void setBid(SimpleStringProperty bid) {
        this.bid = bid;
    }
    
    public void setBid(double bid) {
        this.bid = new SimpleStringProperty(String.format("%.2f",bid));
    }
    
    public void setBid(String bid) {
        this.bid = new SimpleStringProperty(bid);
    }

    public void setAsk(SimpleStringProperty ask) {
        this.ask = ask;
    }
    
    public void setAsk(double ask) {
        this.ask = new SimpleStringProperty(String.format("%.2f",ask));
    }
    
    public void setAsk(String ask) {
        this.ask = new SimpleStringProperty(ask);
    }

    public void setAskQty(SimpleStringProperty askQty) {
        this.askQty = askQty;
    }
    
    public void setAskQty(long askQty) {
        this.askQty = new SimpleStringProperty(String.format("%d",askQty));
    }
    
    public void setAskQty(String askQty) {
        this.askQty = new SimpleStringProperty(askQty);
    }
    
    public String getBidQty() {
        return bidQty.get();
    }

    public String getBid() {
        return bid.get();
    }
    
    public String getAsk() {
        return ask.get();
    }
    
    public String getAskQty() {
        return askQty.get();
    }

}
