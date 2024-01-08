package uk.ac.soton.seg15.view.components;

import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.seg15.view.View;
import uk.ac.soton.seg15.view.events.CalculateButtonListener;
import uk.ac.soton.seg15.view.events.SubmitEditRunwayListener;

public class RunwaySettingsPanel extends VBox {

    private static final Logger logger = LogManager.getLogger(RunwaySettingsPanel.class);
    private View view;
    private ParameterField lda;
    private ParameterField asda;
    private ParameterField tora;
    private ParameterField toda;
    private ParameterField threshold;
    private ParameterField resa;
    private ParameterField heading;
    private ComboBox<String> position;
    private SubmitEditRunwayListener submitEditRunwayListener;

    private Button submit;
    private String prevName;

    public RunwaySettingsPanel(View view){
        this.view = view;

        this.setSpacing(9);
        this.setPadding(new Insets(5,5,5,5));

        this.setMaxSize(300,300);

        this.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        buildInputBoxes();
        buildButton();
        if(view.getRunway() != null)
            updateFields();
    }

    /**
     * Builds and sets the prompt text for the input boxes
     */
    private void buildInputBoxes(){
        lda = new ParameterField("LDA", "m", 0, Double.MAX_VALUE);

        asda = new ParameterField("ASDA","m", 0, Double.MAX_VALUE);

        tora = new ParameterField("TORA","m", 0, Double.MAX_VALUE);

        toda = new ParameterField("TODA","m", 0, Double.MAX_VALUE);

        threshold = new ParameterField("Threshold Displacement","m", -Double.MAX_VALUE, Double.MAX_VALUE);

        resa = new ParameterField("RESA","m", 0, Double.MAX_VALUE);
        resa.setText("240");

        heading = new ParameterField("Heading", "", 1, 36);

        var list = new String[]{"L","C","R"};
        position = new ComboBox<>(FXCollections.observableList(Arrays.stream(list).collect(Collectors.toList())));

        this.getChildren().addAll(lda,asda,tora,toda,threshold,resa,heading,position);

    }

    public void updateFields(){
        lda.setText(Double.toString(view.getRunway().getParameters().getLda()));
        asda.setText(Double.toString(view.getRunway().getParameters().getAsda()));
        tora.setText(Double.toString(view.getRunway().getParameters().getTora()));
        toda.setText(Double.toString(view.getRunway().getParameters().getToda()));
        threshold.setText(Double.toString(view.getRunway().getThreshold()));
        heading.setText(Integer.toString(view.getRunway().getHeading()));
        position.setValue(view.getRunway().getPosition());
    }


    private void buildButton(){
        submit = new Button("Submit changes");
        submit.setOnAction(event -> {
            submitChanges();
            buttonClicked(event);
        });
        this.getChildren().add(submit);
    }

    /**
     * Called when button is pressed. Sends text field inputs if not null to view.
     */
    private void submitChanges(){
        if(!check()) return;
        view.finalNotiUpdate("Values Changed: ");
        if (!lda.getText().isEmpty()){
            //send width update via view
            view.ldaUpdate(lda.getText());
        }
        if (!asda.getText().isEmpty()){
            //send width update via view
            view.asdaUpdate(asda.getText());
        }
        if (!tora.getText().isEmpty()){
            //send width update via view
            view.toraUpdate(tora.getText());
        }
        if (!toda.getText().isEmpty()){
            //send width update via view
            view.todaUpdate(toda.getText());
        }
        if (!threshold.getText().isEmpty()){
            //send width update via view
            view.threshUpdate(threshold.getText());
        }
        if (!resa.getText().isEmpty()){
            //send width update via view
            view.resaUpdate(resa.getText());
        }
        if (!heading.getText().isEmpty()){
            //send width update via view
            view.headingUpdate(heading.getText());
        }
        if (!position.getValue().isEmpty()){
            //send width update via view
            view.positionUpdate(position.getValue());
        }
        logger.info(view.getRunway().getPosition());
        view.notiCollect();
        if (view.getFinalNoti() != "Values Changed: ") {
            view.showNotification(view.getFinalNoti());
        }
    }

    /**
     * Input handling
     * @return
     */
    private boolean check(){
        prevName = view.getRunway().getHeading() + view.getRunway().getPosition();
        for(var runway : view.getAirport().getRunways()) {
            var name = runway.getHeading() + runway.getPosition();
            if (name.equals(heading.getText() + position.getValue()) && !prevName.equals(heading.getText() + position.getValue())){
                view.showNotification("Name of runway already exists");
                return false;
            }
        }

        if(Double.parseDouble(asda.getText()) < Double.parseDouble(tora.getText())) {
            view.showNotification("ASDA must be greater/equal to TORA");
            return false;
        } else if (Double.parseDouble(toda.getText()) < Double.parseDouble(tora.getText())) {
            view.showNotification("TODA must be greater/equal to TORA");
            return  false;
        }

        return true;
    }

    public void setOnButtonClicked(SubmitEditRunwayListener listener) {
        this.submitEditRunwayListener = listener;
    }

    private void buttonClicked(ActionEvent event) {
        if (submitEditRunwayListener != null) {
            submitEditRunwayListener.submitEditRunway(event, prevName);
        }
    }


}
