package uk.ac.soton.seg15.view.components;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;

import javafx.scene.text.Font;

import javafx.geometry.Insets;
import javafx.scene.control.Button;

import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.seg15.model.Obstacle;
import uk.ac.soton.seg15.model.Runway;
import uk.ac.soton.seg15.view.View;


public class OutputPanelbd extends VBox{
    private final Logger logger = LogManager.getLogger(OutputPanelbd.class);
    private View view;
    private Text TORA;
    private Text TORACalc;
    private Text TORAOriginal;
    private Text ASDA;
    private Text ASDACalc;
    private Text ASDAOriginal;
    private Text TODA;
    private Text TODACalc;
    private Text TODAOriginal;
    private Text LDA;
    private Text LDACalc;
    private Text LDAOriginal;
    private TableView<RunwayParam> table;
    private TableColumn <RunwayParam, String> column;
    private TableColumn <RunwayParam, Double> column2;
    private TableColumn <RunwayParam, Double> column3;


    private BooleanProperty toggleBreakdown = new SimpleBooleanProperty();

    public OutputPanelbd(View view, double width, double height){
        this.view = view;
        this.setSpacing(30);
        this.setPadding(new Insets(5,5,5,5));
        setPrefWidth(width);
        setPrefHeight(height);


        Button back = new Button("Back to Input Panel");
        back.setOnAction(x -> toggleBreakdown.set(false));
        table = new TableView<>();


        this.getChildren().addAll(back, table);

        widthProperty().addListener(((observableValue, number, t1) -> {
            for (var col : table.getColumns()) {
                col.setPrefWidth(t1.doubleValue()/3);
            }
        }));

        calculationOutput();
    }

    private void calculationOutput(){
        this.TORACalc = new Text("TORA Calculation: \n");
        TORACalc.getStyleClass().add("breakdownText");
        this.ASDACalc = new Text("ASDA Calculation: \n");
        ASDACalc.getStyleClass().add("breakdownText");
        this.TODACalc = new Text("TODA Calculation: \n");
        TODACalc.getStyleClass().add("breakdownText");
        this.LDACalc = new Text("LDA Calculation: \n");
        LDACalc.getStyleClass().add("breakdownText");


        var showNewStage = new Button("Display breakdown");
        showNewStage.setOnAction(e -> {
            display(TORACalc, ASDACalc, TODACalc, LDACalc);
        });
        this.getChildren().add(showNewStage);
    }

    private void display(Text toraCalc, Text asdaCalc, Text todaCalc, Text ldaCalc) {
        var stage = new Stage();
        var root = new BorderPane();
        var scene = new Scene(root, 550, 200);
        //root.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY,CornerRadii.EMPTY,new Insets(5,5,5,5))));


        TextFlow outputBreakdown = new TextFlow();
        outputBreakdown.setTabSize(100);
        outputBreakdown.setLineSpacing(20);
        outputBreakdown.getChildren().addAll(toraCalc, asdaCalc, todaCalc, ldaCalc);

        stage.setScene(scene);
        root.getChildren().add(outputBreakdown);
        stage.show();
    }

    /**
     * Updates the values of each label when values are available
     */
    public TableView<RunwayParam> updateValues(String scenarioType) {

        Runway runway = view.getRunway();
        Obstacle obstacle = view.getObstacle();

        if(scenarioType.equals("Landing Over")){
            this.TORACalc.setText("");
            this.ASDACalc.setText("");
            this.TODACalc.setText("");
            if (runway.getParameters().getResa() > (obstacle.getHeight() * 50)) {
                if (runway.getParameters().getResa() + 60 < 300) {
                    this.LDACalc.setText("LDA = LDA - Distance from Threshold - Blast Protection \n" + runway.getParameters().getLda() + "-" + obstacle.getDistanceFromThreshold() + "- 300 -" + "=" + runway.getNewParameters().getLda());
                } else {
                    this.LDACalc.setText("LDA = LDA - Distance from Threshold - RESA - StripEnd \n" + runway.getParameters().getLda() + "-" + obstacle.getDistanceFromThreshold() + "- 60 - " + runway.getParameters().getResa() + "=" + runway.getNewParameters().getLda());
                }

            } else {
                if ((obstacle.getHeight() * 50) + 60 < 300) {
                    this.LDACalc.setText("LDA = LDA - Distance from Threshold - Blast Protection \n" + runway.getParameters().getLda() + "-" + obstacle.getDistanceFromThreshold() + "- 300 -" + "=" + runway.getNewParameters().getLda());
                } else {
                    this.LDACalc.setText("LDA = LDA - Distance from Threshold - StripEnd - Slope Calculation \n" + runway.getParameters().getLda() + "-" + obstacle.getDistanceFromThreshold() + "- 60 - " + (obstacle.getHeight() * 50) + "=" + runway.getNewParameters().getLda());
                }
            }

        } else if (scenarioType.equals("Landing Toward")){
            this.TORACalc.setText("");
            this.ASDACalc.setText("");
            this.TODACalc.setText("");
            this.LDACalc.setText("LDA = Distance from Threshold - RESA - StripEnd \nLDA = " + obstacle.getDistanceFromThreshold() + " - " + runway.getParameters().getResa() + " - 60 = " + runway.getNewParameters().getLda() + "\n");

        } else if (scenarioType.equals("TakeOff Toward")){
            if (runway.getParameters().getResa() > (obstacle.getHeight() * 50)) {
                this.TORACalc.setText("TORA = Displaced Threshold + Distance from Threshold - RESA - StripEnd \nTORA = " + runway.getThreshold() + " + " + obstacle.getDistanceFromThreshold() + " - " + runway.getParameters().getResa() + " - 60 = " + runway.getNewParameters().getTora() + "\n");
            }
            else{
                this.TORACalc.setText("TORA = Displaced Threshold + Distance from Threshold - Slope Calculation - StripEnd \nTORA = " + runway.getThreshold() + " + " + obstacle.getDistanceFromThreshold() + " - " + (obstacle.getHeight() * 50) + " - 60 = " + runway.getNewParameters().getTora() + "\n");
            }
            this.ASDACalc.setText("ASDA = (R) TORA \nASDA = " + runway.getNewParameters().getTora() + "\n");
            this.TODACalc.setText("TODA = (R) TORA \nTODA = " + runway.getNewParameters().getTora() + "\n");
            this.LDACalc.setText("");

        } else if (scenarioType.equals(("TakeOff Away"))){
            this.TORACalc.setText("TORA = TORA - Blast Distance - Distance from Threshold - Threshold Displacement  \n TORA = " + runway.getParameters().getTora() + "- 300 - 60 - " + obstacle.getDistanceFromThreshold() + "=" + runway.getNewParameters().getTora() + "\n");
            this.TODACalc.setText("TODA = (R) TORA + Clearway \nTODA = " + runway.getNewParameters().getTora() + " + " + runway.getParameters().getClearway() + " = " + runway.getNewParameters().getToda() + "\n");
            this.ASDACalc.setText("ASDA = (R) TORA + Stopway \nASDA = " + runway.getNewParameters().getTora() + " + " + runway.getParameters().getStopway() + " = " + runway.getNewParameters().getAsda() + "\n");
            this.LDACalc.setText("");

        }
        table.setPrefHeight(190);
        table.setStyle("-fx-font-size:16");

        column = new TableColumn<>("Name");
        column.setCellValueFactory(new PropertyValueFactory<>("name"));

        column2 = new TableColumn<>("OriginalValues");
        column2.setCellValueFactory(new PropertyValueFactory<>("originalVal"));

        column3 = new TableColumn<>("RecalculatedValues");
        column3.setCellValueFactory(new PropertyValueFactory<>("recalcVal"));

        table.getColumns().clear();
        table.getColumns().addAll(column, column2, column3);

        if (scenarioType.equals("Landing Over") || scenarioType.equals("Landing Toward")){
            ObservableList<RunwayParam> data = FXCollections.observableArrayList(
                    new RunwayParam("ASDA", runway.getParameters().getAsda(), runway.getParameters().getAsda()),
                    new RunwayParam("TORA", runway.getParameters().getTora(), runway.getParameters().getTora()),
                    new RunwayParam("TODA", runway.getParameters().getToda(), runway.getParameters().getToda()),
                    new RunwayParam("LDA", runway.getParameters().getLda(), runway.getNewParameters().getLda()));
            table.setItems(data);
        }
        if (scenarioType.equals("TakeOff Away") || scenarioType.equals("TakeOff Toward")){
            ObservableList<RunwayParam> data = FXCollections.observableArrayList(
                    new RunwayParam("ASDA", runway.getParameters().getAsda(), runway.getNewParameters().getAsda()),
                    new RunwayParam("TORA", runway.getParameters().getTora(), runway.getNewParameters().getTora()),
                    new RunwayParam("TODA", runway.getParameters().getToda(), runway.getNewParameters().getToda()),
                    new RunwayParam("LDA", runway.getParameters().getLda(), runway.getParameters().getLda()));
            table.setItems(data);
        }

        
        table.autosize();
        return table;
    }

    public BooleanProperty getToggleBreakdown(){return toggleBreakdown;}
}
