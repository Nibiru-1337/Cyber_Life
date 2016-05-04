package UI;

import game.Game;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import rules.eState;

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
    @FXML private ChoiceBox cbStateCenter;
    @FXML private ChoiceBox cbComparison;
    @FXML private ChoiceBox cbStateCount;
    @FXML private ChoiceBox cbStateResult;
    @FXML private ChoiceBox cbWhere;

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
                "exactly", "less then", "less then or equal", "more then", "more then or equal"));
        cbWhere.setItems(FXCollections.observableArrayList(
                "1st row", "2nd row", "3rd row", "4th row", "5th row",
                "1st column", "2nd column", "3rd column","4th column","5th column",
                "neighborhood", "inner border", "outer border"));
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
        s = result.getState();
        if (s == 0)
            g.addDiscreteRule(n, eState.EMPTY);
        else if (s == 1)
            g.addDiscreteRule(n, eState.ALIVE);
        else
            g.addDiscreteRule(n, eState.DEAD);
        mainWindow.putCurrentRules();
    }
}
