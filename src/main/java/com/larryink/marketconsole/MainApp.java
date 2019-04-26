package com.larryink.marketconsole;

import com.larryink.marketplace.models.MarketMaker;
import com.larryink.marketplace.models.PriceModel;
import com.larryink.marketplace.types.BookEntry;
import com.larryink.marketplace.types.JmsMarketDataProvider;
import com.larryink.marketplace.types.MarketDataListener;
import com.larryink.marketplace.types.MarketDepth;
import com.larryink.marketplace.types.MarketTrade;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application implements MarketDataListener {

    private TableView<BookDataRow> bookTable;
    private final ObservableList<BookDataRow> bookData = FXCollections.observableArrayList();
    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());

    private TableView<TradeDataRow> tradesTable = new TableView<>();
    private final ObservableList<TradeDataRow> tradesData = FXCollections.observableArrayList();

    private double last = 100;
    private int lastTimeInt = 0;
    LineChart<Number, Double> stockChart;
    XYChart.Series series;

    private int depth = 10;

    private ExecutorService mdExecutor;
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        }
    });
    private boolean stopped = true;

    private JmsMarketDataProvider mdReciever;

    private MarketMaker marketMaker;
    private PriceModel priceModel;
    private String name;

    static final String MQ_SERVER_URL = "tcp://localhost:61616";
    static final String MARKET_SERVER_ENDPOINT = "http://localhost:8081/";

    private void initBookTable() {
        bookTable.setItems(bookData);
        ObservableList<?> cols = bookTable.getColumns();

        ((TableColumn) cols.get(0)).setCellValueFactory(new PropertyValueFactory<>("bidWho"));
        ((TableColumn) cols.get(1)).setCellValueFactory(new PropertyValueFactory<>("bidQty"));
        ((TableColumn) cols.get(2)).setCellValueFactory(new PropertyValueFactory<>("bid"));
        ((TableColumn) cols.get(3)).setCellValueFactory(new PropertyValueFactory<>("ask"));
        ((TableColumn) cols.get(4)).setCellValueFactory(new PropertyValueFactory<>("askQty"));
        ((TableColumn) cols.get(5)).setCellValueFactory(new PropertyValueFactory<>("askWho"));

        for (int idx = 0; idx < depth; ++idx) {
            bookData.add(new BookDataRow("-", "-", "-", "-", "-", "-"));
        }

    }

    private void initTradesTable() {
        tradesTable.setItems(tradesData);
        ObservableList<?> cols = tradesTable.getColumns();

        ((TableColumn) cols.get(0)).setCellValueFactory(new PropertyValueFactory<>("time"));
        ((TableColumn) cols.get(1)).setCellValueFactory(new PropertyValueFactory<>("symbol"));
        ((TableColumn) cols.get(2)).setCellValueFactory(new PropertyValueFactory<>("buyer"));
        ((TableColumn) cols.get(3)).setCellValueFactory(new PropertyValueFactory<>("seller"));
        ((TableColumn) cols.get(4)).setCellValueFactory(new PropertyValueFactory<>("qty"));
        ((TableColumn) cols.get(5)).setCellValueFactory(new PropertyValueFactory<>("price"));
    }

    private void initStockChart() {
        series = new XYChart.Series();
        series.setName("AAPL");
        stockChart.getData().add(series);
        lastTimeInt = (int) (new Date().getTime() / 1000);
        series.getData().add(new XYChart.Data(0, last));
    }

    @Override
    public void start(Stage stage) throws Exception {
        AnchorPane root = FXMLLoader.load(getClass().getResource("/fxml/FXML.fxml"));
        ObservableList<Node> Nodes = (ObservableList<Node>) root.getChildren();

        bookTable = (TableView<BookDataRow>) Nodes.get(0);
        initBookTable();

        tradesTable = (TableView<TradeDataRow>) Nodes.get(1);
        initTradesTable();

        stockChart = (LineChart<Number, Double>) Nodes.get(2);
        initStockChart();

        Button resume = (Button) Nodes.get(3);
        resume.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                marketMaker.resume();
            }
        });

        Button pause = (Button) Nodes.get(4);
        pause.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                marketMaker.pause();
            }
        });

        this.mdExecutor = Executors.newSingleThreadExecutor();

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();

        mdReciever = new JmsMarketDataProvider(MQ_SERVER_URL, this, 0);
        mdReciever.init();

        name = "Kyere";
        priceModel = new PriceModel(last);
        marketMaker = new MarketMaker(name, "AAPL", MQ_SERVER_URL, MARKET_SERVER_ENDPOINT, priceModel);
        executor.execute(marketMaker);
    }

    @Override
    public void stop() {
        mdReciever.close();
        executor.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
        System.out.println("Application Exiting...");
    }

    @Override
    public void bookUpdate(final MarketDepth md) {
        LOG.info("Incoming:" + md.toString());
        mdExecutor.execute(new Runnable() {
            public void run(){
                int maxIdx = md.getMaxDepth();
                bookData.clear();
                for(int idx = 0;idx<maxIdx;idx++){
                    bookData.add(new BookDataRow());
                }
                                
                Set<String> currentOrders = marketMaker.getOrders();

                List<BookEntry> bids = md.getBuyDepth();
                int idx = 0;
                int endIdx = min(bids.size(), depth);
                for (; idx < endIdx; ++idx) {
                    BookDataRow newRow = bookData.get(idx);
                    newRow.setBid(bids.get(idx).getPrice());
                    newRow.setBidQty(bids.get(idx).getShares());
                    if (currentOrders.contains(bids.get(idx).getId())) {
                        newRow.setBidWho("X");
                    } else {
                        newRow.setBidWho("");
                    }

                }

                List<BookEntry> asks = md.getSellDepth();
                idx = 0;
                endIdx = min(asks.size(), depth);
                for (; idx < endIdx; ++idx) {
                      BookDataRow newRow = bookData.get(idx);
                    newRow.setAsk(asks.get(idx).getPrice());
                    newRow.setAskQty(asks.get(idx).getShares());
                    if (currentOrders.contains(asks.get(idx).getId())) {
                        newRow.setAskWho("X");
                    } else {
                        newRow.setAskWho("");
                    }
                }

            }
        });
    }

    @Override
    public void tradeUpdate(final MarketTrade trade) {
        tradesData.add(new TradeDataRow(trade.getDate().toString(), trade.getSymbol(), trade.getBuyer(),
                trade.getSeller(), trade.getQuantity(), trade.getPrice()));

        int tradeTimeInt = (int) (trade.getDate().getTime() / 1000);
        series.getData().add(new XYChart.Data(tradeTimeInt - lastTimeInt, trade.getPrice()));
    }

}
