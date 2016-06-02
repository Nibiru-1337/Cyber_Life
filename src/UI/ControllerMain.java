package UI;

import game.Game;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import file_stuff.FileProcessor;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javafx.stage.FileChooser;

public class ControllerMain implements Initializable {
    private int N = 5;
    private int blocksize = 10;
    private int xOffset = 0;
    private int yOffset = 0;
    private GraphicsContext gc;
    private Game g;
    private FileProcessor fp;
    private FileChooser fc;
    Thread t;
    private CanvasRedrawTask<ConcurrentHashMap> task;
    ObservableList<Map.Entry> currentRulesList;
    @FXML private Stage root;
    @FXML private Canvas c;
    @FXML private Canvas cGrid;
    @FXML private ListView lvDiscreteRules;
    @FXML private ListView<RuleQuantifier> lvQuantifierRules;
    @FXML private Label lGeneration;
    @FXML private Spinner<Integer> sFastForward;
    @FXML private Button bFastForward;
    @FXML private Spinner sZoom;
    @FXML private Button bUp; @FXML private Button bDown;
    @FXML private Button bLeft; @FXML private Button bRight;
    @FXML private Button bEmpty; @FXML private Button bArrow;
    @FXML private Button bDragons; @FXML private Button bOscillators;
    @FXML private Button bBomb; @FXML private Button bPattern;

    public ControllerMain(Game game, Stage r){
        this.g = game;
        this.root = r;
    }

    @Override public void initialize(URL location, ResourceBundle resources) {
        fc = new FileChooser();
        fp = new FileProcessor();
        gc = c.getGraphicsContext2D();
        //Set extension filter
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));
        sFastForward.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,100,1));
        bFastForward.setOnAction((val) -> {
            t = new Thread(g);
            t.setDaemon(true);
            g.generationIterations.set(sFastForward.getValue());
            t.start();
        });
        sZoom.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3,50,10));
        sZoom.valueProperty().addListener((obs, oldValue, newValue) -> {
                    blocksize = (int) newValue;
                    drawBoardGrid(cGrid);
                    gc.clearRect(0, 0, c.getWidth(), c.getHeight());
                    drawBoard(gc);
                });
        task = new CanvasRedrawTask<ConcurrentHashMap>(c, lGeneration) {
            @Override
            protected void redraw(GraphicsContext context, ConcurrentHashMap data, double w, double h, Label lGeneration) {
                context.clearRect(0,0,w,h);
                drawBoard(context);
                lGeneration.setText("Generation: " + g.getGeneration());
            }
        };
        g.setTask(task);
        drawBoardGrid(cGrid);
        currentRulesList = FXCollections.observableArrayList();

    }

    @FXML private void bPlay_Pressed(ActionEvent ae){
        //http://stackoverflow.com/questions/22772379/updating-ui-from-different-threads-in-javafx
        //Set up thread for calculating generations
        t = new Thread(g);
        t.setDaemon(true);
        g.work = true;
        t.start();
    }

    @FXML private void bPause_Pressed(ActionEvent ae){
        g.work = false;
    }

    @FXML private void bStep_Pressed(ActionEvent ae){
        g.work = false;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t = new Thread(g);
        t.setDaemon(true);
        g.generationIterations.set(1);
        t.start();
    }

    @FXML private void navigation_Pressed(ActionEvent ae){
        if (ae.getSource() == bUp)
            yOffset += 10;
        else if (ae.getSource() == bDown)
            yOffset -= 10;
        else if (ae.getSource() == bLeft)
            xOffset += 10;
        else
            xOffset -= 10;
        gc.clearRect(0, 0, c.getWidth(), c.getHeight());
        drawBoard(gc);
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

    @FXML private void preset_Pressed(ActionEvent ae){
        g.work = false;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Button pressed = (Button) ae.getSource();
        if (pressed == bEmpty){
            resetPositioning();
            g.resetGame();
            gc.clearRect(0, 0, c.getWidth(), c.getHeight());
            putCurrentRules();
            lGeneration.setText("Generation: 0");
        }
        else if (pressed == bArrow)
            loadPreset("R/presetArrowShips.txt");
        else if (pressed == bDragons)
            loadPreset("R/presetDragons.txt");
        else if (pressed == bOscillators)
            loadPreset("R/presetOscillators.txt");
        else if (pressed == bBomb)
            loadPreset("R/presetAtomBomb.txt");
        else if (pressed == bPattern)
            loadPreset("R/presetCoolPattern.txt");
    }

    @FXML private void canvasClicked(MouseEvent me){
        int x = (int) (me.getX() - (int)me.getX()% blocksize);
        int y = (int) (me.getY() - (int)me.getY()% blocksize);
        Point p = new Point();
        p.setLocation(x,y);
        Point hmXY = translateToHashMapPos(p, blocksize, xOffset, yOffset);
        if (!g.board.containsKey(hmXY)) {
            drawCell(gc, p, blocksize, eState.ALIVE);
            g.board.put(hmXY, true);
            g.addPointAsActive(hmXY);
        }
        //if an alive cell was clicked, made it dead
        else if (g.board.get(hmXY) == true) {
            drawCell(gc, p, blocksize, eState.DEAD);
            g.board.put(hmXY, false);
            g.addPointAsActive(hmXY);
        }
        //if a dead cell was clicked, make it empty
        else if (g.board.get(hmXY) == false){
            drawCell(gc, p, blocksize, eState.EMPTY);
            g.board.remove(hmXY);
            g.addPointAsActive(hmXY);
        }
        //System.out.println("x:" + p.x);
        //System.out.println("y:" + p.y);

    }

    @FXML private void save(){
        File file = fc.showSaveDialog(root);
        if (file != null) {
            fp.saveToFile(file.getAbsolutePath(), g.board.entrySet().iterator(), g.getDiscreteRuleIterator(), g.getQuantifierRuleIterator());
        }
    }

    @FXML private void load(){
        File file = fc.showOpenDialog(root);
        if (file != null) {
            g.resetGame();
            gc.clearRect(0, 0, c.getWidth(), c.getHeight());
            try{
                fp.loadFromFile(file.getAbsolutePath(), g);}
            catch (NullPointerException e){
                ControllerRuleCreator.displayMessage("Bad file format.");
                return;}
            GraphicsContext gc = c.getGraphicsContext2D();
            drawBoard(gc);
            putCurrentRules();
        }
    }

    protected void putCurrentRules(){
        ObservableList<Map.Entry> discreteList = FXCollections.observableArrayList();
        ObservableList<RuleQuantifier> quantifierList = FXCollections.observableArrayList();

        Iterator it = g.getDiscreteRuleIterator();
        int i = 1;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String name = "Discrete Rule #" + Integer.toString(i);
            discreteList.add(pair);
            i++;
        }
        it = g.getQuantifierRuleIterator();
        while (it.hasNext()) {
            RuleQuantifier rq = (RuleQuantifier)it.next();
            String name = "Quantifier Rule #" + Integer.toString(i);
            quantifierList.add(rq);
            i++;
        }
        lvDiscreteRules.setItems(discreteList);
        lvDiscreteRules.setCellFactory(new Callback<ListView<Map.Entry>, ListCell<Map.Entry>>() {
            @Override
            public ListCell<Map.Entry> call(ListView<Map.Entry> param) {
                return new DiscreteRuleListCell(discreteList, g);
            }
        });
        lvQuantifierRules.setItems(quantifierList);
        lvQuantifierRules.setCellFactory(new Callback<ListView<RuleQuantifier>, ListCell<RuleQuantifier>>() {
            @Override
            public ListCell<RuleQuantifier> call(ListView<RuleQuantifier> param) {
                return new QuantifierRuleListCell(quantifierList, g, lvQuantifierRules);
            }
        });
    }

    private void loadPreset(String file){
        resetPositioning();
        g.resetGame();
        gc.clearRect(0, 0, c.getWidth(), c.getHeight());
        fp.loadFromFile(file, g);
        GraphicsContext gc = c.getGraphicsContext2D();
        drawBoard(gc);
        putCurrentRules();
    }

    private void resetPositioning(){
        xOffset = 0;
        yOffset = 0;
        sZoom.getValueFactory().setValue(10);
    }

    private void drawBoardGrid(Canvas c) {
        double w = c.getWidth();
        double h = c.getHeight();
        GraphicsContext gc2 = c.getGraphicsContext2D();
        gc2.clearRect(0,0,w,h);
        gc2.setFill(Color.BLACK);
        gc2.setLineWidth(1);
        for (int x = 0; x < w; x=x+ blocksize)
            gc2.strokeLine(x,0,x,h);
        for (int y = 0; y < h; y=y+ blocksize)
            gc2.strokeLine(0,y,w,y);
    }

    private void drawBoard(GraphicsContext gc){
        Iterator<Map.Entry<Point, Boolean>> it = g.board.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Point, Boolean> pair = it.next();
            //if cell is alive or dead draw it
            if (pair.getValue())
                drawCell(gc, translateToCanvasPos(pair.getKey(), blocksize, xOffset, yOffset), blocksize, eState.ALIVE);
            else
                drawCell(gc, translateToCanvasPos(pair.getKey(), blocksize, xOffset, yOffset), blocksize, eState.DEAD);
        }
    }

    private void drawCell(GraphicsContext gc, Point xy, int blocksize, eState s){
        if (s == eState.ALIVE)
            gc.setFill(Color.BLACK);
        else if (s == eState.DEAD)
            gc.setFill(Color.LIGHTCORAL);
        else if (s == eState.EMPTY)
            gc.setFill(Color.rgb(244,244,244));
        gc.fillRect(xy.x+1, xy.y+1, blocksize-1, blocksize-1);
    }

    private Point translateToCanvasPos(Point xy, int blocksize, int xOffset, int yOffset) {
        //TODO:assuming canvas is 900x800
        Point out = new Point();
        out.x = 450 + (xy.x * blocksize) + (blocksize*xOffset);
        out.y = 400 + (xy.y * blocksize) + (blocksize*yOffset);
        return out;
    }

    private Point translateToHashMapPos(Point xy, int blocksize, int xOffset, int yOffset){
        //TODO:assuming canvas is 900x800
        Point out = new Point();
        out.x = (xy.x - 450)/ blocksize - (blocksize*xOffset);
        out.y = (xy.y - 400)/ blocksize - (blocksize*yOffset);
        return out;
    }

    @FXML private void displayHelp(ActionEvent event){
        ControllerRuleCreator.displayMessage("                      Developed with ♥ by Kamil Breński :)\n" +
                "A project during my studies in Warsaw Univeristy of Technology.\n\n" +
                "If anyone is wondering composite quantifier rules have order of operations\n" +
                "                   hardcoded as leftmost to rightmost.\n\n" +
                "Also behavior of loading presets, fast forwarding, stepping, while a simulation\n" +
                "           is ongoing is UNDEFINED ! please pause the simulation first!\n");
    }
}
