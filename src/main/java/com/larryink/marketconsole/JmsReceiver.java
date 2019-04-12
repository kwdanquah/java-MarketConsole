/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.larryink.marketconsole;

import javafx.collections.ObservableList;
import javax.jms.Connection;
import javax.jms.Queue;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author kyere
 */
public class JmsReceiver implements Runnable{
    
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Queue queue;
    private MessageConsumer consumer;
    private ObservableList<DataRow>  data;
        
    public JmsReceiver(final String url, ObservableList<DataRow> data){
        this.connectionFactory = new ActiveMQConnectionFactory(url);
        this.data = data;
    }
        
    public void init(){
        try{
            connection = connectionFactory.createConnection();
            session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            queue = session.createQueue("MKT_DATA");
            consumer = session.createConsumer(queue);
            connection.start();
        }catch(Exception e){
            e.printStackTrace();
        }                                
    }
        
    @Override
    public void run() {
        boolean stopped = false;
        while (!stopped) {
            try{
                TextMessage textMsg = (TextMessage) consumer.receive();
                System.out.println(textMsg);
                System.out.println("Received: " + textMsg.getText());
                data.add(new DataRow("100", "12.04", "13.63", "200"));
                if (textMsg.getText().equals("END")) {
                    break;
                }
            }catch(Exception e){
                e.printStackTrace();
                stopped=true;
            }
        }
    }
}
