package UI;

import game.Game;
import javafx.scene.input.MouseEvent;
import rules.eState;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class ControllerMain implements Initializable {
    private int N = 5;
    private int BLOCKSIZE = 10;
    private GraphicsContext gc;
    private Game g;
    Thread t;
    private CanvasRedrawTask<HashMap> task;
    @FXML private Canvas c;
    @FXML private FlowPane fpCurrentRules;
    @FXML private Label lGeneration;

    public ControllerMain(Game game){
        this.g = game;
    }

    @Override public void initialize(URL location, ResourceBundle resources) {
        gc = c.getGraphicsContext2D();
        task = new CanvasRedrawTask<HashMap>(c, lGeneration) {
            @Override
            protected void redraw(GraphicsContext context, HashMap data, double w, double h, Label lGeneration) {
                context.clearRect(0,0,w,h);
                drawBoard(context);
                lGeneration.setText("Generation: " + g.getGeneration());
            }
        };
        g.setTask(task);
        /*ObservableList<String> list = FXCollections.observableArrayList(
                "Empty", "Glider", "Preset 3", "Preset 4", "Preset 5", "Preset 6");
        ListView<String> lv = new ListView<>(list);
        lv.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new CustomPresetButton();
            }
        });
        spPresets.getChildren().add(lv);
        //Button pb = new Button();
        //Button pb2 = new Button();
        //spPresets.getChildren().add(pb);
        //spPresets.getChildren().add(pb2);*/
    }

    @FXML private void bPlay_Pressed(ActionEvent ae){
        //http://stackoverflow.com/questions/22772379/updating-ui-from-different-threads-in-javafx
        g.work = true;
        t = new Thread(g);
        t.setDaemon(true);
        t.start();
    }

    @FXML private void bPause_Pressed(ActionEvent ae){
        g.work = false;
    }

    @FXML private void bStep_Pressed(ActionEvent ae){
        g.getNextGeneration();
        task.requestRedraw(g.board);
    }

    @FXML private void bRuleCreator_Pressed(ActionEvent ae) {
        Parent root;
        try {
            FXMLLoader secondLoader = new FXMLLoader(getClass().getResource("WindowRuleCreator.fxml"));
            secondLoader.setController(new ControllerRuleCreator(g, this));
            root = secondLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Rule Creator");
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void emptyPreset(ActionEvent event){
        g.resetGame();
        gc.clearRect(0, 0, c.getWidth(), c.getHeight());
        putCurrentRules();
    }

    @FXML private void gliderPreset(ActionEvent event){
        g.resetGame();
        gc.clearRect(0,0,c.getWidth(),c.getHeight());

        g.board.put(new Point(-44,-38), true);
        g.board.put(new Point(-43,-38), true);
        g.board.put(new Point(-42,-38), true);
        g.board.put(new Point(-41,-38), true);
        g.board.put(new Point(-42,-39), true);
        g.board.put(new Point(-42,-37), true);
        GraphicsContext gc = c.getGraphicsContext2D();
        drawBoard(gc);

        List<List<eState>> n;
        n = g.getEmptyNeighborhood();
        n.get(0).set(1, eState.ALIVE); //1
        n.get(0).set(2, eState.ALIVE);
        n.get(0).set(3, eState.ALIVE);
        n.get(1).set(2, eState.ALIVE);
        g.addDiscreteRule(n, eState.ALIVE);

        n = g.getEmptyNeighborhood();
        n.get(0).set(3, eState.ALIVE); //2
        n.get(1).set(2, eState.ALIVE);
        n.get(1).set(3, eState.ALIVE);
        n.get(1).set(4, eState.ALIVE);
        n.get(2).set(3, eState.ALIVE);
        g.addDiscreteRule(n, eState.ALIVE);

        n = g.getEmptyNeighborhood();
        n.get(0).set(1, eState.ALIVE); //3
        n.get(1).set(0, eState.ALIVE);
        n.get(1).set(1, eState.ALIVE);
        n.get(1).set(2, eState.ALIVE);
        n.get(2).set(1, eState.ALIVE);
        g.addDiscreteRule(n, eState.ALIVE);

        n = g.getEmptyNeighborhood();
        n.get(0).set(3, eState.ALIVE); //4
        n.get(1).set(3, eState.ALIVE);
        n.get(2).set(3, eState.ALIVE);
        n.get(3).set(3, eState.ALIVE);
        n.get(2).set(4, eState.ALIVE);
        n.get(2).set(2, eState.ALIVE);
        g.addDiscreteRule(n, eState.EMPTY);

        n = g.getEmptyNeighborhood();
        n.get(0).set(1, eState.ALIVE); //5
        n.get(1).set(1, eState.ALIVE);
        n.get(2).set(1, eState.ALIVE);
        n.get(3).set(1, eState.ALIVE);
        n.get(2).set(0, eState.ALIVE);
        n.get(2).set(2, eState.ALIVE);
        g.addDiscreteRule(n, eState.EMPTY);

        n = g.getEmptyNeighborhood();
        n.get(2).set(2, eState.ALIVE); //6
        n.get(3).set(2, eState.ALIVE);
        n.get(4).set(2, eState.ALIVE);
        g.addDiscreteRule(n, eState.EMPTY);

        putCurrentRules();
    }

    @FXML private void canvasClicked(MouseEvent me){
        int x = (int) (me.getX() - (int)me.getX()%BLOCKSIZE);
        int y = (int) (me.getY() - (int)me.getY()%BLOCKSIZE);
        Point p = new Point();
        p.setLocation(x,y);
        drawCell(gc,p,BLOCKSIZE);
        p = translateToHashMapPos(p, BLOCKSIZE);
        //System.out.println("x:" + p.x);
        //System.out.println("y:" + p.y);
        g.board.put(p, true);
    }

    protected void putCurrentRules(){
        fpCurrentRules.getChildren().clear();

        Iterator it = g.getDiscreteRuleIterator();
        int i = 1;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            //put rule on display
            Label r = new Label();
            r.setPrefWidth(290.0);
            String name = "Rule #" + Integer.toString(i);
            r.setText(name);
            fpCurrentRules.getChildren().add(r);
            i++;
        }
    }

    private void drawBoard(GraphicsContext gc){
        Iterator it = g.board.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            //if cell is alive draw it
            if ((boolean)pair.getValue()){
                drawCell(gc, translateToCanvasPos((Point)pair.getKey(), BLOCKSIZE), BLOCKSIZE);
            }
        }
    }

    private void drawCell(GraphicsContext gc, Point xy, int blocksize){
        gc.setFill(Color.BLACK);
        gc.fillRect(xy.x,xy.y,blocksize,blocksize );
    }

    private Point translateToCanvasPos(Point xy, int blocksize) {
        //TODO:assuming canvas is 900x800
        Point out = new Point();
        out.x = 450 + (xy.x * blocksize);
        out.y = 400 + (xy.y * blocksize);
        return out;
    }

    private Point translateToHashMapPos(Point xy, int blocksize){
        //TODO:assuming canvas is 900x800
        Point out = new Point();
        out.x = (xy.x - 450)/ blocksize;
        out.y = (xy.y - 400)/ blocksize;
        return out;
    }
}
