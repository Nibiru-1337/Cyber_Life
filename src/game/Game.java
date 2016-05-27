package game;

import java.awt.*;
import java.util.*;
import java.util.List;

import UI.CanvasRedrawTask;
import rules.*;
/**
 * Created by Nibiru on 2016-04-27.
 */
public class Game implements Runnable{

    public HashMap <Point, Boolean> board;
    private HashMap <Point, Boolean> board2;
    private HashSet <Point> active;
    private HashSet <Point> active2;
    private HashMap<List<List<eState>>, eState> discreteRules;
    private ArrayList<RuleQuantifier> quanitifierRules;
    private int N = 5;
    private int generation;
    private CanvasRedrawTask<HashMap> task;
    public boolean work = false;

    public Game() {
        //TODO: give initial size?
        board = new HashMap<>();
        board2 = new HashMap<>();
        active = new HashSet<>();
        active2 = new HashSet<>();
        discreteRules = new HashMap<List<List<eState>>, eState>();
        quanitifierRules = new ArrayList<>();
        generation = 0;
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
            Iterator<Map.Entry<Point, Boolean>> iter = board.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Point, Boolean> pair = iter.next();
                Point xy = pair.getKey();
                //generate co-ordinates to add as active
                int offset = N / 2;
                for (int i = xy.x - offset; i <= xy.x + offset; i++)
                    for (int j = xy.y - offset; j <= xy.y + offset; j++)
                        active.add(new Point(i, j));
            }
        }
        List<List<eState>> neighborhood;
        eState s;
        //check all the existing board cells
        Iterator<Map.Entry<Point, Boolean>> it = board.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Point, Boolean> pair = it.next();
            Point xy = pair.getKey();
            neighborhood = getNeighborhood(pair.getKey());
            boolean someRuleWasApplied = false;
            //check if some discrete rule
            s = discreteRules.get(neighborhood);
            if (active.contains(xy) || s != null) {
                //copy resulting state to board2
                board2.put(xy, pair.getValue());
                someRuleWasApplied = true;
            }
            //check if some quantifier rule applies
            if (checkQuantifierRulesForPoint(neighborhood, xy))
                someRuleWasApplied = true;
            //check if some rule was applied
            if (someRuleWasApplied){
                addPointAsActive(xy);
            }
            else {
                //rule does not apply or is not active then copy over to board2
                board2.put(xy, pair.getValue());
                active.remove(xy);
            }
        }
        //check all the active cells
        for (Point xy : active) {
            boolean someRuleWasApplied = false;
            neighborhood = getNeighborhood(xy);
            //check if some discrete rule applies
            s = discreteRules.get(neighborhood);
            if (s != null){
                someRuleWasApplied = true;
                //copy resulting state to board2
                if (s == eState.ALIVE)
                    board2.put(xy, true);
                else if (s == eState.DEAD)
                    board2.put(xy, false);
            }
            //check if some quantifier rule applies
            if (checkQuantifierRulesForPoint(neighborhood, xy))
                someRuleWasApplied = true;
            //check if some rule was applied
            if (someRuleWasApplied){
                addPointAsActive(xy);
            }
        }
        board = (HashMap<Point, Boolean>) board2.clone();
        board2.clear();
        active = (HashSet<Point>) active2.clone();
        active2.clear();
        generation++;
    }

    public Iterator<Map.Entry<List<List<eState>>, eState>> getDiscreteRuleIterator(){
        return discreteRules.entrySet().iterator();
    }

    public Iterator<RuleQuantifier> getQuantifierRuleIterator(){
        return quanitifierRules.iterator();
    }

    public void addDiscreteRule(List<List<eState>>n, eState s){
        this.discreteRules.put(n, s);
    }

    public void removeDiscreteRule(List<List<eState>> key){
        discreteRules.remove(key);
    }

    public void addQuantifierRule(RuleQuantifier qr){
        quanitifierRules.add(qr);
    }

    public void removeQuantifierRule(RuleQuantifier qr){
        quanitifierRules.remove(qr);
    }

    public void resetGame(){
        board.clear();
        discreteRules.clear();
        quanitifierRules.clear();
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

    public void setTask(CanvasRedrawTask<HashMap> t){
        this.task = t;
    }

    private boolean checkQuantifierRulesForPoint(List<List<eState>> neighborhood, Point xy){
        //iterate over quantifer rules check if neighborhood applies
        boolean someRuleWasApplied = false;
        for (int i = 0; i < quanitifierRules.size(); i++){
            RuleQuantifier rq = quanitifierRules.get(i);
            if (rq.check(neighborhood)) {
                someRuleWasApplied = true;
                if (rq.getResultingState() == eState.ALIVE)
                    board2.put(xy, true);
                else if (rq.getResultingState() == eState.DEAD)
                    board2.put(xy, false);
                //stop if we found a rule that applies with highest priority
                break;
            }
        }
        return someRuleWasApplied;
    }

    public void addPointAsActive(Point xy){
        //generate co-ordinates to add as active
        int offset = N / 2;
        for (int i = xy.x - offset; i <= xy.x + offset; i++)
            for (int j = xy.y - offset; j <= xy.y + offset; j++)
                active2.add(new Point(i, j));
    }

    @Override
    public void run() {
        while (work) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getNextGeneration();
            task.requestRedraw(board);
        }
    }
}
