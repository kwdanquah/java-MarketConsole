package com.larryink.marketconsole;

import com.larryink.marketplacetypes.BookEntry;
import com.larryink.marketplacetypes.JmsMarketDataProvider;
import com.larryink.marketplacetypes.MarketDataListener;
import com.larryink.marketplacetypes.MarketDepth;
import com.larryink.marketplacetypes.MarketTrade;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class MainApp extends Application implements MarketDataListener {
    
    private final TableView<BookDataRow> bookTable = new TableView<>();
    private final ObservableList<BookDataRow> bookData = FXCollections.observableArrayList();
    
    private final TableView<TradeDataRow> tradesTable = new TableView<>();
    private final ObservableList<TradeDataRow> tradesData = FXCollections.observableArrayList();
    
    private int depth = 10;

    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        }
    });
    private boolean stopped = false;
    
    private JmsMarketDataProvider mdReciever;
    
    private void initBookTable(){
        bookTable.setItems(bookData);
        
        TableColumn bidWhoCol = new TableColumn("BidWho");
        bidWhoCol.setMinWidth(60);
        bidWhoCol.setCellValueFactory(new PropertyValueFactory<>("bidWho"));

        TableColumn bidQtyCol = new TableColumn("BidQty");
        bidQtyCol.setMinWidth(60);
        bidQtyCol.setCellValueFactory(new PropertyValueFactory<>("bidQty"));

        TableColumn bidCol = new TableColumn("Bid");
        bidCol.setMinWidth(60);
        bidCol.setCellValueFactory(new PropertyValueFactory<>("bid"));

        TableColumn askCol = new TableColumn("Ask");
        askCol.setMinWidth(60);
        askCol.setCellValueFactory(new PropertyValueFactory<>("ask"));

        TableColumn askQtyCol = new TableColumn("AskQty");
        askQtyCol.setMinWidth(60);
        askQtyCol.setCellValueFactory(new PropertyValueFactory<>("askQty"));
        
        TableColumn askWhoCol = new TableColumn("AskWho");
        askWhoCol.setMinWidth(60);
        askWhoCol.setCellValueFactory(new PropertyValueFactory<>("askWho"));

        bookTable.getColumns().addAll(bidWhoCol, bidQtyCol, bidCol, askCol, askQtyCol, askWhoCol);
                
        for(int idx = 0; idx < depth; ++idx){
            bookData.add(new BookDataRow("-", "-", "-", "-", "-", "-"));
        }
        
    }
    
    private void initTradesTable(){
        tradesTable.setItems(tradesData);
        
        TableColumn timeCol = new TableColumn("Time");
        timeCol.setMinWidth(60);
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));

        TableColumn buyerCol = new TableColumn("Buyer");
        buyerCol.setMinWidth(60);
        buyerCol.setCellValueFactory(new PropertyValueFactory<>("buyer"));

        TableColumn sellerCol = new TableColumn("Seller");
        sellerCol.setMinWidth(60);
        sellerCol.setCellValueFactory(new PropertyValueFactory<>("seller"));

        TableColumn priceCol = new TableColumn("Price");
        priceCol.setMinWidth(60);
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn qtyCol = new TableColumn("Qty");
        qtyCol.setMinWidth(60);
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
        
        tradesTable.getColumns().addAll(timeCol, buyerCol, sellerCol, priceCol, qtyCol);
    }

    @Override
    public void start(Stage primaryStage) {
        initBookTable();
        initTradesTable();
       
        StackPane root = new StackPane();
        root.getChildren().add(bookTable);
        //root.getChildren().add(tradesTable);
        primaryStage.setTitle("AAPL");
        primaryStage.setScene(new Scene(root, 400, 250));
        primaryStage.show();

        mdReciever = new JmsMarketDataProvider("tcp://localhost:61616", this);
        mdReciever.init();
    }
    
    @Override
    public void stop(){
        mdReciever.close();
        executor.shutdown();        
    }
    public static void main(String[] args) {
        launch(args);
        System.out.println("Application Exiting...");
    }

    @Override
    public void bookUpdate(MarketDepth md) {
        ArrayList<BookDataRow> newData = new ArrayList();

        newData.addAll(bookData);
        List<BookEntry> bids = md.getBuyDepth();
        int idx=0;
        int endIdx = min(bids.size(),depth);
        for(; idx<endIdx; ++idx){
            newData.get(idx).setBid(bids.get(idx).getPrice());
            newData.get(idx).setBidQty(bids.get(idx).getShares());
            newData.get(idx).setBidWho(bids.get(idx).getWho());
        }
        for(idx=endIdx;idx<depth; ++idx){
            newData.get(idx).setBid("-");
            newData.get(idx).setBidQty("-");
            newData.get(idx).setBidWho("-");
        }
        
        List<BookEntry> asks = md.getSellDepth();
        idx = 0;
        endIdx = min(asks.size(),depth);
        for(; idx<endIdx; ++idx){
            newData.get(idx).setAsk(asks.get(idx).getPrice());
            newData.get(idx).setAskQty(asks.get(idx).getShares());
            newData.get(idx).setAskWho(asks.get(idx).getWho());
        }
        for(idx=endIdx;idx<depth; ++idx){
            newData.get(idx).setAsk("-");
            newData.get(idx).setAskQty("-");
            newData.get(idx).setAskWho("-");
        }
        
        bookData.clear();
        bookData.addAll(newData);
        
    }

    @Override
    public void tradeUpdate(MarketTrade trade) {
       tradesData.add(new TradeDataRow(trade.getDate().toString(), trade.getSymbol(), trade.getBuyer(), 
               trade.getSeller(), trade.getQuantity(), trade.getPrice()));
    }

}
