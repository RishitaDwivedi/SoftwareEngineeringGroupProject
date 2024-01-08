package uk.ac.soton.seg15.view.components;

import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.seg15.model.Parameters;
import uk.ac.soton.seg15.model.Runway;
import uk.ac.soton.seg15.view.View;

/**
 * A window that is displayed when the user wants to add a new runway to the airport
 */
public class NewRunway extends Stage {

  private static Logger logger = LogManager.getLogger(NewRunway.class);
  private final StackPane root;
  private final Scene scene;
  private View view;
  private VBox main;
  private ComboBox<String> position;
  private ParameterField heading;
  private ParameterField asda;
  private ParameterField tora;
  private ParameterField toda;

  public NewRunway(double width, double height, View view) {
    this.view = view;
    setResizable(false);
    initOwner(view.getStage());
    initStyle(StageStyle.UNDECORATED);

    root = new StackPane();
    scene = new Scene(root, width, height);

    scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
    setScene(scene);
    build();
    this.show();
    sizeToScene();
  }

  private void build(){
    main = new VBox();
    root.getChildren().add(main);

    main.setSpacing(5);
    main.setAlignment(Pos.CENTER);
    main.setPadding(new Insets(5,5,5,5));

    HBox name = new HBox();
    name.setAlignment(Pos.BOTTOM_CENTER);
    heading = new ParameterField("Heading", "", 1, 36);
    var list = new String[]{"L","C","R"};
    position = new ComboBox<String>(FXCollections.observableList(Arrays.asList(list)));
    position.setPromptText("Position");
    name.getChildren().addAll(heading, position);
    main.getChildren().addAll(name);

    var lda = new ParameterField("LDA", "m", 0, Double.MAX_VALUE);

    asda = new ParameterField("ASDA","m", 0, Double.MAX_VALUE);

    tora = new ParameterField("TORA","m", 0, Double.MAX_VALUE);

    toda = new ParameterField("TODA","m", 0, Double.MAX_VALUE);

    var threshold = new ParameterField("Threshold Displacement","m", -Double.MAX_VALUE, Double.MAX_VALUE);

    var resa = new ParameterField("RESA","m", 0, Double.MAX_VALUE);
    resa.setText("240");

    main.getChildren().addAll(lda, asda, tora, toda, threshold, resa);

    HBox buttonsBox = new HBox();
    buttonsBox.setAlignment(Pos.CENTER);
    buttonsBox.setSpacing(10);
    var create = new Button("Create");
    var cancel = new Button("Cancel");
    buttonsBox.getChildren().addAll(create,cancel);
    main.getChildren().addAll(buttonsBox);

    create.setOnAction(e -> {
      if(!check()) return;
      logger.info(resa.getValue());
      Runway run = new Runway(Integer.parseInt(heading.getText()), threshold.getValue(), position.getValue(),
          new Parameters(tora.getValue(), toda.getValue(), asda.getValue(), lda.getValue(), Double.parseDouble(resa.getText())));
      view.addRunway(run);
      view.setRunway(run);
      view.showNotification("Created new runway: " + heading.getText() + position.getValue());
      close();
    });

    Alert alert = new Alert(AlertType.CONFIRMATION, "Cancelling will remove changes. \nAre you sure you want to cancel?");
    cancel.setOnAction(e -> {
      alert.showAndWait()
          .filter(response -> response == ButtonType.OK)
          .ifPresent(response -> close());
    });
  }

  /**
   * input handling
   * @return
   */
  private boolean check(){
    //Empty check
    for (var node: main.getChildren()){
      if (node instanceof ParameterField){
        var field = (ParameterField) node;
        if (field.getText().isEmpty()) {
          view.showNotification("Enter value for: " + field.getName());
          return false;
        }
      }
    }

    if(position.getValue() == null){
      view.showNotification("Enter value for: " + position.getPromptText());
      return false;
    }

    //Existing name check
    if(view.getAirport().getRunways().stream()
        .anyMatch(x -> {
          var name = x.getHeading() + x.getPosition();
          return name.equals(heading.getText() + position.getValue());
        })){
      view.showNotification("Runway name already exists");
      return false;
    }

    //Stop way and clear way check
    if(Double.parseDouble(asda.getText()) < Double.parseDouble(tora.getText())) {
      view.showNotification("ASDA must be greater/equal to TORA");
      return false;
    } else if (Double.parseDouble(toda.getText()) < Double.parseDouble(tora.getText())) {
      view.showNotification("TODA must be greater/equal to TORA");
      return  false;
    }

    return true;
  }


}
