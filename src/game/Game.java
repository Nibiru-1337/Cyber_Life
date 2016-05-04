package game;

import java.awt.*;
import java.util.*;
import java.util.List;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import rules.*;
/**
 * Created by Nibiru on 2016-04-27.
 */
public class Game extends Service{

    public HashMap <Point, Boolean> board;
    private HashMap <Point, Boolean> board2;
    private HashSet <Point> active;
    private HashSet <Point> active2;
    private HashMap <List, eState>discreteRules;
    private int N = 5;
    private int generation;

    public Game() {
        //TODO: give initial size?
        board = new HashMap<>();
        board2 = new HashMap<>();
        active = new HashSet<>();
        active2 = new HashSet<>();
        discreteRules = new HashMap<>();
        generation = 0;
    }

    @Override
    protected Task createTask() {
        Task t = new Task() {
            @Override
            protected Object call() throws Exception {
                Thread.sleep(2000);
                getNextGeneration();
                return null;
            }
        };
        return null;
    }

    public List<List<eState>> getNeighborhood(Point xy){
        //initialize arrayList to store neighborhood
        List<List<eState>> neighborhood = new ArrayList<>();
        for (int x = 0; x < N; x++)
            neighborhood.add(x, new ArrayList<>());

        //generate co-ordinates to check
        ArrayList<Point> toCheck = new ArrayList<>(N*N);
        int offset = N/2;
        for (int i = xy.x - offset; i <= xy.x + offset; i++)
            for (int j = xy.y - offset; j <= xy.y + offset; j++)
                toCheck.add(new Point(i, j));
        //check neighborhood
        int idx = 0;
        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                Point p = toCheck.get(idx);
                Boolean isAlive = board.get(p);
                if (isAlive == null)
                    neighborhood.get(x).add(y,eState.EMPTY);
                else if (isAlive)
                    neighborhood.get(x).add(y,eState.ALIVE);
                else
                    neighborhood.get(x).add(y,eState.DEAD);
                idx++;
            }
        }
        return neighborhood;
    }

    public void getNextGeneration() {
        //prepare active set if none was prepared earlier
        if (generation == 0) {
            Iterator iter = board.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry pair = (Map.Entry) iter.next();
                Point xy = (Point) pair.getKey();
                //generate co-ordinates to add as active
                int offset = N / 2;
                for (int i = xy.x - offset; i <= xy.x + offset; i++)
                    for (int j = xy.y - offset; j <= xy.y + offset; j++)
                        active.add(new Point(i, j));
            }
        }
        //check all the board cells
        Iterator it = board.entrySet().iterator();
        List<List<eState>> neighborhood;
        eState s;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Point xy = (Point) pair.getKey();
            neighborhood = getNeighborhood((Point) pair.getKey());
            s = discreteRules.get(neighborhood);
            if (active.contains(xy) || s != null) {
                //generate co-ordinates to add as active
                int offset = N / 2;
                for (int i = xy.x - offset; i <= xy.x + offset; i++)
                    for (int j = xy.y - offset; j <= xy.y + offset; j++)
                        active2.add(new Point(i, j));
                //copy resulting state to board2
                board2.put((Point) pair.getKey(), (Boolean) pair.getValue());

            } else {
                //rule does not apply or is not active then copy over to board2
                board2.put(xy, (Boolean) pair.getValue());
                active.remove(xy);
            }
        }
        //check all the active cells
        for (Point xy : active) {
            neighborhood = getNeighborhood(xy);
            s = discreteRules.get(neighborhood);
            if (s != null){
                //generate co-ordinates to add as active
                int offset = N / 2;
                for (int i = xy.x - offset; i <= xy.x + offset; i++)
                    for (int j = xy.y - offset; j <= xy.y + offset; j++)
                        active2.add(new Point(i, j));
                //copy resulting state to board2
                if (s == eState.ALIVE)
                    board2.put(xy, true);
                else if (s == eState.DEAD)
                    board2.put(xy, false);
                else
                    board2.remove(xy);
            }
        }
        board = (HashMap<Point, Boolean>) board2.clone();
        board2.clear();
        active = (HashSet<Point>) active2.clone();
        active2.clear();
        generation++;
    }

    public Iterator getDiscreteRuleIterator(){
        return discreteRules.entrySet().iterator();
    }

    public void addDiscreteRule(List<List<eState>>n, eState s){
        this.discreteRules.put(n, s);
    }

    public void resetGame(){
        board.clear();
        discreteRules.clear();
        generation = 0;
    }

    public List<List<eState>> getEmptyNeighborhood(){
        List<List<eState>> n = new ArrayList<>(N);
        for (int x = 0; x < N; x++) {
            n.add(x, new ArrayList<>(N));
            for (int y = 0; y < N; y++)
                n.get(x).add(y, eState.EMPTY);
        }
        return n;
    }

    public int getGeneration(){
        return generation;
    }

    private int save(){
        return 0;
    }
    private int load(){
        return 0;
    }
}
