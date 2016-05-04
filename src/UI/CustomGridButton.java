package UI;

import javafx.scene.control.Button;

/**
 * Created by Nibiru on 2016-05-02.
 */
public class CustomGridButton extends Button {
    private int state = 0;

    public void switchState(){
        state = (state+1)%3;
        if (state==0)
            this.setStyle("-fx-base: #F9F8F8;");
        if (state==1)
            this.setStyle("-fx-base: #7A9F35;");
        if (state==2)
            this.setStyle("-fx-base: #ff002b;");
    }
    public int getState(){
        return state;
    }
}
