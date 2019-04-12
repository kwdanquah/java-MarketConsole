/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.larryink.marketconsole;

import javafx.beans.property.SimpleStringProperty;

public class DataRow {

    private final SimpleStringProperty bidQty;
    private final SimpleStringProperty bid;
    private final SimpleStringProperty ask;
    private final SimpleStringProperty askQty;

    public DataRow(String bidQty, String bid, String ask, String askQty) {
        this.bidQty = new SimpleStringProperty(bidQty);
        this.bid = new SimpleStringProperty(bid);
        this.ask = new SimpleStringProperty(ask);
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
