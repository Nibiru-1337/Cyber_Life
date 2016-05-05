package UI;

import game.Game;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Game g = new Game();

        FXMLLoader firstLoader = new FXMLLoader(getClass().getResource("WindowMain.fxml"));
        firstLoader.setController(new ControllerMain(g));
        Parent root = firstLoader.load();

        primaryStage.setTitle("Cyber_Life Simulation");
        Scene scene = new Scene(root, 1200, 900);
        primaryStage.setScene(scene);
        //primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
