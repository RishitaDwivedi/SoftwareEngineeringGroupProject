package uk.ac.soton.seg15.view.scenes;

import java.util.*;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.text.Text;

import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.seg15.App;
import uk.ac.soton.seg15.model.Calculate;
import uk.ac.soton.seg15.model.ColorBlindness;
import uk.ac.soton.seg15.model.Obstacle;
import uk.ac.soton.seg15.model.Runway;
import uk.ac.soton.seg15.view.View;
import uk.ac.soton.seg15.view.components.*;

import javax.swing.*;
import java.util.stream.Collectors;


public class MainScene extends BaseScene{

    private InputPanel inputPanel;
    private BorderPane bp;
    private final BooleanProperty toggleBreakdown = new SimpleBooleanProperty(false);

    private StringProperty calcType = new SimpleStringProperty();

    private static final Logger logger = LogManager.getLogger(MainScene.class);

    private RunwaySettingsPanel settingsPanel;
    private ImportExport ie;
    private Notis notis;

    private StackPane viewStackPane;
    protected OutputPanelbd outputPanelbd;

    private ObjectProperty<Runway> curRunway = new SimpleObjectProperty<>();

    private ObjectProperty<Obstacle> curObstacle = new SimpleObjectProperty<>();

    private SideOnView sideView;
    private TopDownView topView;

    private BorderPane viewBP;

    private BorderPane top;

    private ScrollPane scrollInput;
    private int direction = 1;

    /**
     * true if individual view, false if simultaneously
     */
    private Boolean individualView;

    /**
     * True if on side view, false if on top down
     */
    private Boolean sideViewBool;

    private ComboBox viewType;
    private CompassView compassView;
    private TextField field;
    private ComboBox<String> runwayCB;
    private ObservableList<Runway> runwayList = new SimpleListProperty<>();

    private SimpleDoubleProperty stageWidth = new SimpleDoubleProperty();

    private SimpleDoubleProperty stageHeight = new SimpleDoubleProperty();
    private CheckBox rotateTopDown;
    private Runway reciprocal = new Runway();

    private Button switchDirection;


    public MainScene(StackPane root, Color color, View view, ObjectProperty runway, ObjectProperty obstacle, Stage stage) {
        super(root,color, view, stage);

            individualView = false;
            sideViewBool = true;

            bp = new BorderPane();
            root.getChildren().add(bp);

            curRunway.bindBidirectional(runway);
            curObstacle.bindBidirectional(obstacle);

        //stage.minHeightProperty().bind(this.heightProperty());
        //stage.minWidthProperty().bind(this.widthProperty());

        stageWidth.bind(view.getStage().widthProperty());
        stageHeight.bind(view.getStage().heightProperty());

        stageWidth.addListener(newVal ->{
            if(sideView != null && topView != null && scrollInput != null && top != null){
                resize();
            }
        });
        stageHeight.addListener(newVal ->{
            if(sideView != null && topView != null && scrollInput != null && top != null){
                resize();
            }
        });

    }
    private void resize(){
        var viewWidth = (stageWidth.getValue()*15)/20;
        var viewHeight= (stageHeight.getValue()*8)/10;

        if(individualView == true){
            sideView.resize(viewWidth,viewHeight);
            topView.resize(viewWidth,viewHeight);
        }else{
            sideView.resize(viewWidth,viewHeight/2);
            topView.resize(viewWidth,viewHeight/2);
        }

        scrollInput.setPrefWidth(stageWidth.getValue()*5/20);
        scrollInput.setPrefHeight((stageHeight.getValue()*8)/10);

        outputPanelbd.setPrefWidth(stageWidth.getValue()*5/20);
        outputPanelbd.setPrefHeight((stageHeight.getValue()*8)/10);

//        logger.info(stageWidth.getValue() + " " + stageHeight.getValue());

        //top.setPrefHeight((stageHeight.getValue()*2)/10);
        //top.setPrefWidth(stageWidth.getValue()/5);

    }

    /**
     * Screen sections as fractions (width height):
     * Left(5/20,8/10)
     * Centre(15/20,8/10)
     * Top(1,2/10)
     */
    @Override
    public void build(){
        //Top side: Back, Display settings, Import/Export,Runway settings
        buildSettingsPanel();
        buildImpExpPanel();
        buildnotipanel();
        buildTopHBox();

        //Left side: Input and output
        inputPanel = new InputPanel(view);
        inputPanel.setAlignment(Pos.CENTER);
        inputPanel.getToggleBreakdown().bindBidirectional(toggleBreakdown);
        scrollInput = new ScrollPane(inputPanel);
        scrollInput.setPadding(new Insets(10,10,10,10));
        scrollInput.setPrefWidth(stageWidth.getValue()*5/20);
        scrollInput.setPrefHeight((stageHeight.getValue()*8)/10);

        bp.setLeft(scrollInput);

        outputPanelbd = new OutputPanelbd(view, stageWidth.getValue()*5/20, stageHeight.getValue()*8/10);
        outputPanelbd.getToggleBreakdown().bindBidirectional(toggleBreakdown);
//        outputPanelbd.setPrefWidth(stageWidth.getValue()*5/20);
//        outputPanelbd.setPrefHeight((stageHeight.getValue()*8)/10);

        toggleBreakdown.addListener(x -> toggleBreakdownCalculations());

        //Centre: Display
        buildViewPanel();

        //Listeners and event handlers
        curRunway.addListener(((observable, oldValue, newValue) -> {
            updateViews();
            newValues();
        } ));

        curObstacle.addListener(((observable, oldValue, newValue) -> {
            updateViews();
            newValues();
        } ));

        inputPanel.setOnButtonClicked(event -> {
            updateViews();
            newValues();
        });

        calcType.bindBidirectional(inputPanel.getScenarioType());
        calcType.addListener((observableValue, s, t1) -> {
            updateViews();
            newValues();
        });

        settingsPanel.setOnButtonClicked((event, prevName) -> {
            var curName = curRunway.get().getHeading() + curRunway.get().getPosition();
            if(!prevName.equals(curName)){
                runwayCB.getItems().add(curName);
                runwayCB.getItems().remove(prevName);
                runwayCB.getItems().remove("Add new runway...");
                runwayCB.getItems().add("Add new runway...");
                runwayCB.setValue(curName);
            }
            updateViews();
            newValues();
        });

        /**
        if(!runwayCB.getItems().isEmpty()) {
            runwayCB.setValue(runwayCB.getItems().get(0));
            runwayCB.getOnAction().handle(new ActionEvent());
        }*/
    }


    private void newValues() {
//        logger.info("toggle breakdown: " + toggleBreakdown.get());
        if(toggleBreakdown.get()) {
            this.outputPanelbd.updateValues(calcType.get());
            bp.setLeft(outputPanelbd);
        } else {
            bp.setLeft(scrollInput);}
    }

    /**
     * Initialises the Settings panel
     */
    private void buildSettingsPanel(){
        settingsPanel = new RunwaySettingsPanel(view);
        settingsPanel.setPadding(new Insets(5,5,5,5));

        HBox.setHgrow(settingsPanel,Priority.ALWAYS);
    }

    private void buildViewPanel(){
        var width = (stageWidth.getValue()*15)/20;
        var height= (stageHeight.getValue()*8)/10;
        viewStackPane = new StackPane();
        bp.setCenter(viewStackPane);

        //---Display accessories---
        //Colour keys
        KeyBox keyBox = new KeyBox(200, 10);

        //Compass
        compassView = new CompassView(width/10, width/10, view.getRunway().getHeading() * 10);

        //---Main View---
        viewBP = new BorderPane();
        viewStackPane.getChildren().add(viewBP);
        BackgroundFill fill = new BackgroundFill(ColorBlindness.daltonizeCorrect(Color.LIGHTGREEN), CornerRadii.EMPTY, new Insets(0,0,0,0));
        viewBP.setBackground(new Background(fill));

        if(individualView == false){
            sideView = new SideOnView(width,height/2,inputPanel.getScenarioType(),view);
            topView = new TopDownView(width,height/2,inputPanel.getScenarioType(),view);
            viewBP.setTop(topView);
            viewBP.setBottom(sideView);

            BorderPane.setMargin(topView, new Insets(0,0,10,0));
            StackPane.setAlignment(compassView, Pos.CENTER_RIGHT);

        } else {
            if(sideViewBool == true){
                sideView = new SideOnView(width,height,inputPanel.getScenarioType(),view);
                viewBP.setTop(sideView);
                BorderPane.setMargin(sideView, new Insets(0,0,10,0));
            } else {
                topView = new TopDownView(width,height,inputPanel.getScenarioType(),view);
                viewBP.setTop(topView);
                BorderPane.setMargin(topView, new Insets(0,0,10,0));
            }
            StackPane.setAlignment(compassView, Pos.BOTTOM_RIGHT);
        }


        if(runwayCB.getValue() != null) checkRunwayDesignator();
        if(calcType.get() != null){
            newValues();
            updateViews();
        }

        viewStackPane.getChildren().addAll(keyBox, compassView);
        StackPane.setAlignment(keyBox, Pos.TOP_RIGHT);

    }

    private void buildImpExpPanel(){
        ie = new ImportExport(view);
        ie.setPadding(new Insets(5,5,5,5));
        HBox.setHgrow(ie,Priority.ALWAYS);

    }

    private void buildnotipanel(){
        notis = new Notis(view);
        notis.setPadding(new Insets(5,5,5,5));
        HBox.setHgrow(notis,Priority.ALWAYS);
    }

    /**
     * Builds the topHBox that stores the back button, the choose runway button and the runway settings button.
     */
    private void buildTopHBox(){
        Button backButton = new Button("<-");
        backButton.setOnAction(x -> goBack());

        //Runway manipulation
        HBox runways = new HBox();
        runways.setAlignment(Pos.CENTER);
        runways.setSpacing(5);

        Button notii = new Button("Notifications");
        notii.setOnAction(event -> toggleNotiSettings());
        runways.getChildren().addAll(notii);

        //Importing and Exporting
        Button impexp = new Button("Import and Export");
        impexp.setOnAction(event -> toggleImpSettings());
        runways.getChildren().addAll(impexp);

        //Runway ComboBox
        runwayList = FXCollections.observableList(view.getAirport() == null ?
                Arrays.stream(Runway.runwayArray()).collect(Collectors.toList()) :
                view.getAirport().getRunways());
        runwayList.addListener((ListChangeListener<? super Runway>)  e -> {
                    runwayCB.setItems(FXCollections.observableList(runwayList.stream()
                            .map(x -> x.getHeading() + x.getPosition())
                            .collect(Collectors.toList())));
                }
        );


        runwayCB = new ComboBox<>(FXCollections.observableList(runwayList.stream()
            .map(x -> x.getHeading() + x.getPosition())
            .collect(Collectors.toList()))
        );
        runwayCB.getItems().add("Add new runway...");

        runwayCB.setPromptText("Choose Runway");
        runwayCB.setOnAction(e -> {
            handleRunwaySelection();
            if(rotateTopDown.isSelected()){
                topView.rotateToCompass(view.getRunway().getHeading());
                updateViews();
            }
        });


        runways.getChildren().add(runwayCB);

        //Runway Settings Button
        Button settings = new Button("Runway Settings");
        settings.setOnAction(event -> toggleRunwaySettings());
        runways.getChildren().addAll(settings);


        //Display Settings
        HBox displaySettings = new HBox();
        displaySettings.setAlignment(Pos.CENTER);
        displaySettings.setSpacing(5);
        //Rotating to compass
        rotateTopDown = new CheckBox("Rotate to Compass");
        rotateTopDown.setOnAction(event -> {
            topView.toggleCompassRotation();
            topView.rotateToCompass(view.getRunway().getHeading());
            updateViews();
            view.showNotification("Rotating top down to compass");
        });


        //Display isolation

        String[] viewTypes = {"Side-On","Top-Down","Simultaneous"};
        viewType = new ComboBox(FXCollections.observableArrayList(viewTypes));
        viewType.getSelectionModel().select(2);
        individualView = false;


        viewType.setOnAction((EventHandler<ActionEvent>) event -> {
            switch (viewType.getValue().toString()) {
                case "Side-On":
                    individualView = true;
                    sideViewBool = true;
                    rotateTopDown.setDisable(true);
                    break;
                case "Top-Down":
                    individualView = true;
                    sideViewBool = false;
                    rotateTopDown.setDisable(false);
                    break;
                case "Simultaneous":
                    individualView = false;
                    rotateTopDown.setDisable(false);
                    break;
            }
//            direction = 1;
            rotateTopDown.setSelected(false);
            buildViewPanel();
            bp.setCenter(viewStackPane);
        });

        displaySettings.getChildren().addAll(rotateTopDown, viewType);

        //Top pane configurations
        top = new BorderPane();
        top.setPadding(new Insets(5,5,5,5));
        //top.setPrefHeight(root.getHeight()*1/12);


        Text airportName = new Text();
        airportName.getStyleClass().add(".airport-name");
        if(view.getAirport() != null){
            airportName.setText(view.getAirport().getName());
        } else {
            airportName.setText("Test Airport");
        }

        HBox leftHbox = new HBox();
        leftHbox.setSpacing(20);
        leftHbox.setAlignment(Pos.CENTER);
        leftHbox.setPadding(new Insets(5,5,5,5));
        leftHbox.getChildren().addAll(backButton, airportName);

        top.setLeft(leftHbox);
        top.setAlignment(leftHbox, Pos.CENTER);
        top.setRight(runways);
        top.setAlignment(runways, Pos.CENTER);
        top.setCenter(displaySettings);

        bp.setTop(top);
        bp.setAlignment(top,Pos.BOTTOM_CENTER);
    }

    private Runway getRunway(String name){
        Iterator<Runway> it = runwayList.iterator();
        while(it.hasNext()){
            Runway r = it.next();
            if((r.getHeading()+ r.getPosition()).equals(name)){
                return r;
            }
        }
        return null;
    }

    private Runway getOppRunway(Runway run){
        String oppPos;
        int oppHeading;
        if(run.getPosition().equals("R")){
            oppPos = "L";
        }else if(run.getPosition().equals("L")){
            oppPos = "R";
        }else{
            oppPos = "C";
        }

        if(run.getHeading() > 18){
            oppHeading = run.getHeading() - 18;
        }else{
            oppHeading = run.getHeading() + 18;
        }
        Runway val;
        try{
            val = getRunway(oppHeading+oppPos);
        }catch(Exception e){
            val = null;
        }

        return val;
    }

    private void handleRunwaySelection(){
        if(runwayCB.getValue() == null || runwayCB.getValue().isEmpty()) return;
        if (runwayCB.getValue().equals("Add new runway...")){
            createNewRunway();
            return;
        }

        /*
        if(getOppRunway(getRunway(runwayCB.getValue())) == null){
            switchDirection.setDisable(true);
        }else{
            switchDirection.setDisable(false);
        }*/

        checkRunwayDesignator();
    }

    private void checkRunwayDesignator(){
        Runway val = getRunway(runwayCB.getValue());
        if(val == null){
            view.showNotification("Could not find runway");
            return;
        }
        int curRunwayHeading = val.getHeading();
        int reciprocalHeading = curRunwayHeading > 18 ? curRunwayHeading - 18 : curRunwayHeading + 18;
        String reciprocalPos = "";
        switch(val.getPosition()){
            case "L":
                reciprocalPos = "R";
                break;
            case "C":
                reciprocalPos = "C";
                break;
            case "R":
                reciprocalPos = "L";
                break;
        }

        String finalReciprocalPos = reciprocalPos;
        String curRunwayDesignator = curRunwayHeading + "\n" + val.getPosition();
        try {
            reciprocal = runwayCB.getItems().stream()
                .map(x -> runwayList.stream()
                    .filter(runway -> x.equals(runway.getHeading() + runway.getPosition()))
                    .findFirst().get()
                )
                .filter(i -> reciprocalHeading == i.getHeading() && finalReciprocalPos.equals(
                    i.getPosition()))
                .findFirst().get();

            String reciprocalDesignator = reciprocalHeading + "\n" + reciprocalPos;
            if(val.getHeading() <= reciprocal.getHeading()) {
                topView.setDesignators(curRunwayDesignator, reciprocalDesignator);
                topView.setDegreeOffset(0);
                sideView.setDesignators(curRunwayDesignator, reciprocalDesignator);
                direction = 1;
            } else {
                topView.setDesignators(reciprocalDesignator, curRunwayDesignator);
                topView.setDegreeOffset(180);
                sideView.setDesignators(reciprocalDesignator, curRunwayDesignator);
            }
        } catch (NoSuchElementException exception) {
            topView.setDesignators(curRunwayDesignator, "");
            sideView.setDesignators(curRunwayDesignator, "");
            reciprocal = new Runway();
            direction = 1;
        }

        //topView.switchScenarioDirection(direction);
        //sideView.switchDirection(direction);


        curRunway.set(val);
        view.asdaUpdate(Double.toString(val.getParameters().getAsda()));
        view.todaUpdate(Double.toString(val.getParameters().getToda()));
        view.toraUpdate(Double.toString(val.getParameters().getTora()));
        view.ldaUpdate(Double.toString(val.getParameters().getLda()));
        view.resaUpdate(Double.toString(val.getParameters().getResa()));
        view.headingUpdate(Integer.toString(val.getHeading()));
        view.positionUpdate(val.getPosition());
        view.threshUpdate(Double.toString(val.getThreshold()));
        view.showNotification(val.getHeading() + val.getPosition() + " selected");

        settingsPanel.updateFields();
        newValues();
    }

    private void createNewRunway(){
        var runwayCreation = new NewRunway(300, 500, view);
        runwayCreation.setOnHidden(e -> {
            runwayCB.setValue(runwayCB.getItems().get(runwayCB.getItems().size() - 2));
        });
    }

    /**
     * Switches the scene to the menu scene
     */
    private void goBack(){
        view.switchToMenu();
    }

    /**
     * Toggles whether the runway settings are being displayed or not
     */
    private void toggleRunwaySettings(){

        logger.info("toggle runway settings");

        if(viewStackPane.getChildren().contains(settingsPanel)){
            viewStackPane.getChildren().remove(settingsPanel);
        } else {
            StackPane.setAlignment(settingsPanel,Pos.TOP_RIGHT);
            viewStackPane.getChildren().add(settingsPanel);
            if(viewStackPane.getChildren().contains(ie)){
                viewStackPane.getChildren().remove(ie);
            }
            if(viewStackPane.getChildren().contains(notis)){
                viewStackPane.getChildren().remove(notis);
            }
        }

    }

    private void toggleImpSettings(){

        logger.info("toggle import export settings");

        if(viewStackPane.getChildren().contains(ie)){
            viewStackPane.getChildren().remove(ie);
        } else {
            StackPane.setAlignment(ie,Pos.TOP_RIGHT);
            viewStackPane.getChildren().add(ie);
            if(viewStackPane.getChildren().contains(settingsPanel)){
                viewStackPane.getChildren().remove(settingsPanel);}
            if(viewStackPane.getChildren().contains(notis)){
                viewStackPane.getChildren().remove(notis);
            }
        }

    }

    private void toggleNotiSettings(){

        logger.info("toggle noti settings");
        notis.update();

        if(viewStackPane.getChildren().contains(notis)){
            viewStackPane.getChildren().remove(notis);
        } else {
            StackPane.setAlignment(notis,Pos.TOP_RIGHT);
            viewStackPane.getChildren().add(notis);
            if(viewStackPane.getChildren().contains(settingsPanel)){
                viewStackPane.getChildren().remove(settingsPanel);}
            if(viewStackPane.getChildren().contains(ie)){
                viewStackPane.getChildren().remove(ie);}
        }

    }

    public void toggleBreakdownCalculations(){
        if(toggleBreakdown.get()) {
            this.outputPanelbd.updateValues(calcType.get());
            bp.setLeft(outputPanelbd);
        } else {
            bp.setLeft(scrollInput);}
    }

    public void updateViews(){
        view.calculate(calcType.get(), direction);
        sideView.updateView();
        topView.update();
        compassView.update(view.getRunway().getHeading());
    }


    public void updateRunwayList(Runway runway){
        var val = runway.getHeading() + runway.getPosition();
        if(!runwayList.contains(runway)) runwayList.add(runway);
        runwayCB.getItems().remove("Add new runway...");
        if(!runwayCB.getItems().contains(val)) runwayCB.getItems().add(val);
        runwayCB.getItems().add("Add new runway...");
        runwayCB.setValue(runwayCB.getItems().get((runwayCB.getItems().size() - 2)));
    }

    public void updateObstacleList(Obstacle obstacle) {
        inputPanel.updateObstacleList(obstacle);
    }


}
