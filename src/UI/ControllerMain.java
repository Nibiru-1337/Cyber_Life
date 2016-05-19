package UI;

import game.Game;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import presets.FileProcessor;
import rules.RuleQuantifier;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import javafx.stage.FileChooser;

public class ControllerMain implements Initializable {
    private int N = 5;
    private int BLOCKSIZE = 10;
    private GraphicsContext gc;
    private Game g;
    private FileProcessor fp;
    private FileChooser fc;
    Thread t;
    private CanvasRedrawTask<HashMap> task;
    ObservableList<Map.Entry> currentRulesList;
    @FXML private Stage root;
    @FXML private Canvas c;
    @FXML private ListView lvCurrentRules;
    @FXML private Label lGeneration;

    public ControllerMain(Game game, Stage r){
        this.g = game;
        this.root = r;
    }

    @Override public void initialize(URL location, ResourceBundle resources) {
        fc = new FileChooser();
        //Set extension filter
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));
        fp = new FileProcessor();
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
        currentRulesList = FXCollections.observableArrayList();
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
        lGeneration.setText("Generation: 0");
    }

    @FXML private void simpleArrowPreset(ActionEvent event){
        g.resetGame();
        gc.clearRect(0, 0, c.getWidth(), c.getHeight());
        fp.loadFromFile("src/presets/SimpleArrow.txt", g);
        GraphicsContext gc = c.getGraphicsContext2D();
        drawBoard(gc);
        putCurrentRules();
    }

    @FXML private void canvasClicked(MouseEvent me){
        int x = (int) (me.getX() - (int)me.getX()%BLOCKSIZE);
        int y = (int) (me.getY() - (int)me.getY()%BLOCKSIZE);
        Point p = new Point();
        p.setLocation(x,y);
        Point hmXY = translateToHashMapPos(p, BLOCKSIZE);
        if (!g.board.containsKey(hmXY)) {
            drawCell(gc, p, BLOCKSIZE, eState.ALIVE);
            g.board.put(hmXY, true);
            g.addPointAsActive(hmXY);
        }
        //if an alive cell was clicked, made it dead
        else if (g.board.get(hmXY) == true) {
            drawCell(gc, p, BLOCKSIZE, eState.DEAD);
            g.board.put(hmXY, false);
            g.addPointAsActive(hmXY);
        }
        //if a dead cell was clicked, make it empty
        else if (g.board.get(hmXY) == false){
            drawCell(gc, p, BLOCKSIZE, eState.EMPTY);
            g.board.remove(hmXY);
            g.addPointAsActive(hmXY);
        }
        //System.out.println("x:" + p.x);
        //System.out.println("y:" + p.y);

    }

    protected void putCurrentRules(){
        //fpCurrentRules.getChildren().clear();
        ObservableList<Map.Entry> list = FXCollections.observableArrayList();
        Iterator it = g.getDiscreteRuleIterator();
        int i = 1;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String name = "Discrete Rule #" + Integer.toString(i);
            list.add(pair);
            i++;
        }
        it = g.getQuantifierRuleIterator();
        while (it.hasNext()) {
            RuleQuantifier rq = (RuleQuantifier)it.next();
            //put rule on display
            Label r = new Label();
            r.setPrefWidth(290.0);
            String name = "Quantifier Rule #" + Integer.toString(i);
            r.setText(name);
            //fpCurrentRules.getChildren().add(r);
            i++;
        }
        lvCurrentRules.setItems(list);
        lvCurrentRules.setCellFactory(new Callback<ListView<Map.Entry>, ListCell<Map.Entry>>() {
            @Override
            public ListCell<Map.Entry> call(ListView<Map.Entry> param) {
                return new DiscreteRuleListCell(list, g);
            }
        });
    }

    @FXML private void save(){
        File file = fc.showSaveDialog(root);
        if (file != null) {
            fp.saveToFile(file.getAbsolutePath(), g.board.entrySet().iterator(), g.getDiscreteRuleIterator());
        }
    }

    @FXML private void load(){
        File file = fc.showOpenDialog(root);
        if (file != null) {
            g.resetGame();
            gc.clearRect(0, 0, c.getWidth(), c.getHeight());
            fp.loadFromFile(file.getAbsolutePath(), g);
            GraphicsContext gc = c.getGraphicsContext2D();
            drawBoard(gc);
            putCurrentRules();
        }
    }

    private void drawBoard(GraphicsContext gc){
        Iterator<Map.Entry<Point, Boolean>> it = g.board.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Point, Boolean> pair = it.next();
            //if cell is alive or dead draw it
            if (pair.getValue())
                drawCell(gc, translateToCanvasPos(pair.getKey(), BLOCKSIZE), BLOCKSIZE, eState.ALIVE);
            else
                drawCell(gc, translateToCanvasPos(pair.getKey(), BLOCKSIZE), BLOCKSIZE, eState.DEAD);
        }
    }

    private void drawCell(GraphicsContext gc, Point xy, int blocksize, eState s){
        if (s == eState.ALIVE)
            gc.setFill(Color.BLACK);
        else if (s == eState.DEAD)
            gc.setFill(Color.LIGHTCORAL);
        else if (s == eState.EMPTY)
            gc.setFill(Color.WHITE);
        gc.fillRect(xy.x, xy.y, blocksize, blocksize);
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
