package com.larryink.marketconsole;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class MainApp extends Application {
    
    private final TableView<DataRow> table = new TableView<>();
    private final ObservableList<DataRow> data
            = FXCollections.observableArrayList();
    private TableColumn bidQtyCol;
    private TableColumn bidCol;
    private TableColumn askCol;
    private TableColumn askQtyCol;

    private final Executor executor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        }
    });
    private boolean stopped = false;

    @Override
    public void start(Stage primaryStage) {
        table.setItems(data);

        bidQtyCol = new TableColumn("BidQty");
        bidQtyCol.setMinWidth(80);
        bidQtyCol.setCellValueFactory(new PropertyValueFactory<>("bidQty"));

        bidCol = new TableColumn("Bid");
        bidCol.setMinWidth(80);
        bidCol.setCellValueFactory(new PropertyValueFactory<>("bid"));

        askCol = new TableColumn("Ask");
        askCol.setMinWidth(80);
        askCol.setCellValueFactory(new PropertyValueFactory<>("ask"));

        askQtyCol = new TableColumn("AskQty");
        askQtyCol.setMinWidth(80);
        askQtyCol.setCellValueFactory(new PropertyValueFactory<>("askQty"));

        table.getColumns().addAll(bidQtyCol, bidCol, askCol, askQtyCol);

        StackPane root = new StackPane();
        root.getChildren().add(table);
        primaryStage.setTitle("AAPL");
        primaryStage.setScene(new Scene(root, 400, 250));
        primaryStage.show();

        JmsReceiver mdReciever = new JmsReceiver("tcp://localhost:61616", data);
        mdReciever.init();
        executor.execute(mdReciever);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
