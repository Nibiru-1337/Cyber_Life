package UI;

import game.Game;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import rules.eState;

import java.util.List;
import java.util.Map;

/**
 * Created by Nibiru on 2016-05-02.
 */
public class DiscreteRuleListCell extends ListCell<Map.Entry> {
    HBox hbox = new HBox();
    GridPane gPane= new GridPane();
    Label lResult = new Label("Result:");
    Rectangle rResult = new Rectangle(15,15);
    Pane pane = new Pane();
    Button button = new Button("X");
    Map.Entry lastItem;
    ObservableList<Map.Entry> currentRulesList;
    Game g;
    private int N = 5;

    public DiscreteRuleListCell(ObservableList<Map.Entry> list, Game g) {
        super();
        hbox.getChildren().addAll(gPane, lResult, rResult,pane, button);
        HBox.setHgrow(pane, Priority.ALWAYS);
        currentRulesList = list;
        this.g = g;
    }

    @Override
    protected void updateItem(Map.Entry item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);  // No text in label of super class
        if (empty) {
            lastItem = null;
            setGraphic(null);
        } else {
            lastItem = item;

            gPane.setGridLinesVisible(true);
            gPane.setVgap(2);
            gPane.setHgap(2);
            gPane.setPrefWidth(50);
            gPane.setPrefHeight(50);
            // set neighborhod rectangles in rules display
            List<List<eState>> n = (List<List<eState>>) item.getKey();
            for(int x = 0; x < N; x++) {
                for (int y = 0; y < N; y++) {
                    if (n.get(x).get(y) == eState.EMPTY)
                        gPane.add(new Rectangle(15,15,Color.LIGHTGREY),x,y);
                    else if (n.get(x).get(y) == eState.ALIVE)
                        gPane.add(new Rectangle(15,15,Color.LAWNGREEN),x,y);
                    else if (n.get(x).get(y) == eState.DEAD)
                        gPane.add(new Rectangle(15,15,Color.LIGHTCORAL),x,y);
                }
            }
            // set result rectangle
            if (item.getValue() == eState.ALIVE)
                rResult.setFill(Color.LIGHTGREEN);
            else if (item.getValue() == eState.DEAD)
                rResult.setFill(Color.LIGHTCORAL);
            else if (item.getValue() == eState.EMPTY)
                rResult.setFill(Color.LIGHTGREY);
            setGraphic(hbox);
            //remove element if pressed
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    //System.out.println(lastItem + " : " + event);
                    g.removeDiscreteRule((List<List<eState>>) lastItem.getKey());
                    currentRulesList.remove(lastItem);
                }
            });
        }
    }
}
