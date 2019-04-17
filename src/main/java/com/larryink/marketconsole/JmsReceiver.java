/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.larryink.marketconsole;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.larryink.marketplacetypes.BookEntry;
import com.larryink.marketplacetypes.MarketDepth;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.collections.ObservableList;
import javax.jms.Connection;
import javax.jms.Topic;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

/**
 *
 * @author kyere
 */
public class JmsReceiver implements Runnable {

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Topic topic;
    private MessageConsumer consumer;
    private ObservableList<DataRow> data;
    private Gson gson;
    private int depth;

    public JmsReceiver(final String url, ObservableList<DataRow> data, int depth) {
        this.connectionFactory = new ActiveMQConnectionFactory(url);
        this.data = data;  
        this.depth = depth;
    }

    public void init() {
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = session.createTopic("MKT_DATA");
            consumer = session.createConsumer(topic);
            connection.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement jsonElement, java.lang.reflect.Type type, JsonDeserializationContext jdc) throws JsonParseException {
                return new Date(jsonElement.getAsJsonPrimitive().getAsLong());
            }
        }).create();
        
        for(int idx = 0; idx < depth; ++idx){
            data.add(new DataRow("-", "-", "-", "-"));
        }
    }

    @Override
    public void run() {
        boolean stopped = false;
        while (!stopped) {
            try {
                Message message = consumer.receive();
                ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;
                String json = textMessage.getText();
                System.out.println(json);
                MarketDepth md = gson.fromJson(textMessage.getText(), MarketDepth.class);
                updateData(md);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        try {
            connection.close();
        } catch (JMSException e) {
        }    
    }
    
    private void updateData(MarketDepth md){
        ArrayList<DataRow> newData = new ArrayList();
        //newData.addAll(data);
        newData.addAll(data);
        List<BookEntry> bids = md.getBuyDepth();
        int idx=0;
        int endIdx = min(bids.size(),depth);
        for(; idx<endIdx; ++idx){
            newData.get(idx).setBid(bids.get(idx).getPrice());
            newData.get(idx).setBidQty(bids.get(idx).getShares());
        }
        for(idx=endIdx;idx<depth; ++idx){
            newData.get(idx).setBid("-");
            newData.get(idx).setBidQty("-");
        }
        
        List<BookEntry> asks = md.getSellDepth();
        idx = 0;
        endIdx = min(asks.size(),depth);
        for(; idx<endIdx; ++idx){
            newData.get(idx).setAsk(asks.get(idx).getPrice());
            newData.get(idx).setAskQty(asks.get(idx).getShares());
        }
        for(idx=endIdx;idx<depth; ++idx){
            newData.get(idx).setAsk("-");
            newData.get(idx).setAskQty("-");
        }
        
        data.clear();
        data.addAll(newData);
    }
    
}
