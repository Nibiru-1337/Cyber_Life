package UI;

import game.Game;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import rules.RuleQuantifier;
import rules.eState;

import java.util.List;
import java.util.Map;

/**
 * Created by Nibiru on 2016-05-02.
 */
public class QuantifierRuleListCell extends ListCell<RuleQuantifier> {

    HBox hbox = new HBox();
    Pane pane = new Pane();
    Button button = new Button("X");
    Label lText = new Label();
    RuleQuantifier lastItem;
    ObservableList<RuleQuantifier> currentRulesList;
    Game g;
    private int N = 5;

    public QuantifierRuleListCell(ObservableList<RuleQuantifier> list, Game g, ListView lv) {
        super();
        this.prefWidthProperty().bind(lv.widthProperty().subtract(2));
        this.setMaxWidth(Control.USE_PREF_SIZE);
        lText.setWrapText(true);
        hbox.getChildren().addAll(lText,pane, button);
        HBox.setHgrow(pane, Priority.ALWAYS);
        currentRulesList = list;
        this.g = g;
    }

    @Override
    protected void updateItem(RuleQuantifier item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);  // No text in label of super class
        if (empty) {
            lastItem = null;
            setGraphic(null);
        } else {
            lastItem = item;
            lText.setText(lastItem.getText());
            setGraphic(hbox);
            //remove element if pressed
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    //System.out.println(lastItem + " : " + event);
                    g.removeQuantifierRule(lastItem);
                    currentRulesList.remove(lastItem);
                }
            });
        }
    }
}
