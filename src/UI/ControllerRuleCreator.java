package UI;

import game.Game;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import presets.FileProcessor;
import rules.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ControllerRuleCreator implements Initializable {
    private int N = 5;
    private CustomGridButton[][] buttons;
    private CustomGridButton result;
    private Game g;
    private ControllerMain mainWindow;
    @FXML private GridPane g5x5;
    @FXML private StackPane resultPane;
    @FXML private ChoiceBox<String> cbStateCenter;
    @FXML private ChoiceBox<String> cbComparison;
    @FXML private ChoiceBox<String> cbStateCount;
    @FXML private ChoiceBox<String> cbStateResult;
    @FXML private ChoiceBox<String> cbWhere;
    @FXML private FlowPane fpFirstRuleSet;
    @FXML private FlowPane fpQuantifier;
    @FXML private Label lEnd;

    public ControllerRuleCreator(Game game, ControllerMain cm){
        this.g = game;
        this.mainWindow = cm;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //create 5x5 grid of buttons, last one for rule output
        buttons = new CustomGridButton[N][N];
        for ( int x = 0; x < N; x++){
            for ( int y = 0; y < N; y++){
                CustomGridButton b = new CustomGridButton();
                b.setText("");
                b.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent ae) {
                        CustomGridButton b = (CustomGridButton) ae.getSource();
                        b.switchState();
                    }
                });
                buttons[x][y]= b;
                g5x5.add(b,x,y);
            }
        }
        CustomGridButton b = new CustomGridButton();
        b.setText("");
        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                CustomGridButton b = (CustomGridButton) ae.getSource();
                b.switchState();
            }
        });
        resultPane.getChildren().add(b);
        result = b;
        //initialize 2nd method choices
        cbStateCenter.setItems(FXCollections.observableArrayList("alive", "dead", "empty"));
        cbStateCount.setItems(FXCollections.observableArrayList("alive", "dead", "empty"));
        cbStateResult.setItems(FXCollections.observableArrayList("live", "die", "disappear"));
        cbComparison.setItems(FXCollections.observableArrayList(
                "exactly", "less than", "less than or equal", "more than", "more than or equal"));
        cbWhere.setItems(FXCollections.observableArrayList(
                "in 1st row", "in 2nd row", "in 3rd row", "in 4th row", "in 5th row",
                "in 1st column", "in 2nd column", "in 3rd column","in 4th column","in 5th column",
                "in neighborhood", "in inner border", "in outer border", "above", "below",
                "to the left", "to the right"));
    }

    @FXML private void bDiscreteSave(ActionEvent event) {
        List<List<eState>> n = new ArrayList<>(N);
        int s;
        for (int x = 0; x < N; x++) {
            n.add(x, new ArrayList<>(N));
            for (int y = 0; y < N; y++) {
                s = buttons[x][y].getState();
                if (s == 0)
                    n.get(x).add(y, eState.EMPTY);
                else if (s == 1)
                    n.get(x).add(y, eState.ALIVE);
                else
                    n.get(x).add(y, eState.DEAD);
            }
        }
        //check if rule is not stupid
        if (n.equals(g.getEmptyNeighborhood())){
            displayMessage("Cannot create a rule for empty neighborhood.");
            return;
        }

        s = result.getState();
        //add rule to container
        if (s == 0)
            g.addDiscreteRule(n, eState.EMPTY);
        else if (s == 1)
            g.addDiscreteRule(n, eState.ALIVE);
        else
            g.addDiscreteRule(n, eState.DEAD);
        mainWindow.putCurrentRules();
    }

    @FXML private void bQuantifierSave(ActionEvent event) {
        try{
            final ObservableList<Node> children = fpQuantifier.getChildren();
            //String values[] = new String[5];
            int i = 0;
            int howMany = 0;
            String text = "";
            //iterate over all children of pane
            for (Node node : children) {
                if (node instanceof FlowPane){
                    for (Node child : ((FlowPane) node).getChildren()){
                        if (child instanceof Label) {
                            Label l = (Label) child;
                            text = text + l.getText();
                        }
                        if (child instanceof ChoiceBox) {
                            ChoiceBox cb = (ChoiceBox) child;
                            String str = cb.getValue().toString();
                            text = text + str;
                            //values[i] = str;
                            i++;
                        }
                        if (child instanceof TextField) {
                            TextField tf = (TextField) child;
                            String str = tf.getText();
                            howMany = Integer.parseInt(str);
                            text = text + " " + str + " ";
                        }
                    }
                }
            }
            RuleQuantifier qr = FileProcessor.getQRuleFromString(text);
            g.addQuantifierRule(qr);
            mainWindow.putCurrentRules();
        } catch (NullPointerException | NumberFormatException e){
            displayMessage("Cannot create such a rule");
        }
    }

    @FXML private void makeQuantifierComposite(ActionEvent event){
        FlowPane fp = new FlowPane();
        ChoiceBox<String> n1 = new ChoiceBox<String>(FXCollections.observableArrayList(" AND", " OR"));
        Label n2 = new Label(" there are ");
        ChoiceBox<String> n3 = new ChoiceBox<String>(FXCollections.observableArrayList(FXCollections.observableArrayList(
                "exactly", "less than", "less than or equal", "more than", "more than or equal")));
        TextField n4 = new TextField("");
        n4.setPrefWidth(41);
        ChoiceBox<String> n5 = new ChoiceBox<String>(FXCollections.observableArrayList("alive", "dead", "empty"));
        Label n6 = new Label(" cells ");
        ChoiceBox<String> n7 = new ChoiceBox<String>(FXCollections.observableArrayList(
                "in 1st row", "in 2nd row", "in 3rd row", "in 4th row", "in 5th row",
                "in 1st column", "in 2nd column", "in 3rd column","in 4th column","in 5th column",
                "in neighborhood", "in inner border", "in outer border", "above", "below",
                "to the left", "to the right"));
        fpFirstRuleSet.getChildren().remove(lEnd);
        fpFirstRuleSet.getChildren().remove(cbStateResult);
        fp.getChildren().addAll(n1,n2,n3,n4,n5,n6,n7,lEnd,cbStateResult);
        fp.setAlignment(Pos.CENTER);
        fpQuantifier.getChildren().add(fp);
        //lEnd and cbStateResult
    }

    @FXML static void displayMessage(String msg){
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        Button b = new Button("OK");
        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialogStage.hide();
            }
        });
        VBox vb = new VBox();
        vb.getChildren().addAll(new Text(msg), b);
        vb.setAlignment(Pos.CENTER);
        vb.setPadding(new Insets(5));
        dialogStage.setScene(new Scene(vb));
        dialogStage.show();
    }
}
