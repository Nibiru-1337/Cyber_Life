package presets;

import game.Game;
import rules.*;
import sun.reflect.generics.tree.Tree;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nibiru on 2016-05-11.
 */
public class FileProcessor {
    int N = 5;

    public void saveToFile(String filename, Iterator<Map.Entry<Point, Boolean>> boardIt,
                           Iterator<Map.Entry<List<List<eState>>, eState>> discreteIt, Iterator<RuleQuantifier> quantifierIt){

        try (BufferedWriter bw = new BufferedWriter(new PrintWriter(filename))) {
            //save current board to file
            bw.write("# POINTS\r\npoints\r\n");
            while (boardIt.hasNext()) {
                Map.Entry<Point, Boolean> pair = boardIt.next();
                Point xy = pair.getKey();
                boolean isAlive = pair.getValue();
                bw.write(String.valueOf(xy.x) + "," + String.valueOf(xy.y) + "," + isAlive + "\r\n");
            }
            bw.write("points END\r\n");
            //save current rules to file
            bw.write("# RULES\r\n");
            int i = 0;
            //save DISCRETE rules
            while (discreteIt.hasNext()) {
                bw.write("d" + String.valueOf(i) + "\r\n");
                Map.Entry<java.util.List<java.util.List<eState>>, eState> pair =
                        discreteIt.next();
                java.util.List<java.util.List<eState>> n = pair.getKey();
                eState result = pair.getValue();
                for (int x = 0; x < N; x++){
                    for (int y = 0; y < N; y++){
                        eState cell = n.get(x).get(y);
                        if (cell != eState.EMPTY)
                            bw.write(String.valueOf(x) + "," + String.valueOf(y) + "," + cell + "\r\n");
                    }
                }
                if (result == eState.ALIVE)
                    bw.write("r,alive\r\n");
                else if (result == eState.DEAD)
                    bw.write("r,dead\r\n");
                else if (result == eState.EMPTY)
                    bw.write("r,empty\r\n");
                i++;
            }
            //save QUANTIFIER rules
            while (quantifierIt.hasNext()) {
                bw.write("q" + String.valueOf(i) + "\r\n");
                RuleQuantifier rq = quantifierIt.next();
                bw.write(rq.getText() + "\r\n");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void loadFromFile(String filename, Game g){
        //lead contents of file
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean readingDiscreteRule = false;
            boolean readingQuantifierRule = false;
            boolean readingPoints = false;
            List<List<eState>> n = null;
            String[] lineContents;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#"))
                    continue;
                else if(line.startsWith("d")) {
                    readingDiscreteRule = true;
                    n = g.getEmptyNeighborhood();
                }
                else if (line.startsWith("q")){
                    readingDiscreteRule = false;
                    readingQuantifierRule = true;
                }
                else if (line.startsWith("r")) {
                    readingDiscreteRule = false;
                    lineContents = line.split(",");
                    eState result = getStateFromString(lineContents[1]);
                    g.addDiscreteRule(n,result);
                }
                else if (line.startsWith("points"))
                    readingPoints = true;
                else if (line.startsWith("points END"))
                    readingPoints = false;
                else if (readingDiscreteRule){
                    lineContents = line.split(",");
                    n.get((Integer.parseInt(lineContents[0]))).set(Integer.parseInt(lineContents[1])
                            ,getStateFromString(lineContents[2]));
                }
                else if (readingQuantifierRule){
                    RuleQuantifier qr = getQRuleFromString(line);
                    g.addQuantifierRule(qr);
                }
                else if (readingPoints){
                    lineContents = line.split(",");
                    g.board.put(new Point(Integer.parseInt(lineContents[0]), Integer.parseInt(lineContents[1])),
                                    Boolean.parseBoolean(lineContents[2]));
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public int flipX(int x){
        if (x == 0)
            return 4;
        else if (x == 1)
            return 3;
        else if (x == 2)
            return 2;
        else if (x == 3)
            return 1;
        else
            return 0;
    }

    static public RuleQuantifier getQRuleFromString(String s){
        eState stateCenter = null; eComparison comp = null; eState stateCount = null; eState stateResult = null;
        String where = null; int howMany = -1;
        //for searching a string with regular expressions
        Pattern pattern; Matcher matcher;
        //split because rule can be composite
        String[] parts = s.split("AND|OR");
        //make an expression for each part
        ArrayList<TreeNode> exps = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            pattern = Pattern.compile("exactly|less than or equal|more than or equal|less than|more than");
            matcher = pattern.matcher(parts[i]);
            if (matcher.find())
                comp = getCompFromString(matcher.group(0));
            pattern = Pattern.compile("1st row|2nd row|3rd row|4th row|5th row|1st column|2nd column|" +
                    "3rd column|4th column|5th column|neighborhood|inner border|outer border|above|below|" +
                    "left|right");
            matcher = pattern.matcher(parts[i]);
            if (matcher.find())
                where = matcher.group(0);
            pattern = Pattern.compile("\\s([0-9]+)\\s");
            matcher = pattern.matcher(parts[i]);
            if (matcher.find()) {
                howMany = Integer.parseInt(matcher.group(0).trim());

                pattern = Pattern.compile("alive|live|ALIVE|dead|die|DEAD|empty|disappear|EMPTY");
                matcher = pattern.matcher(parts[i]);
                int idx = 0; //idx to keep track of which occurance of state we are in
                while (matcher.find()) {
                    if (i == 0 && idx == 0)
                        //if this is the first part then also find center state
                        stateCenter = getStateFromString(matcher.group(0));
                    else if (i == parts.length - 1 && ((i == 0 && idx == 2) || (i != 0 && idx == 1)) ) {
                        //if this is the last part then also find state result
                        //if rule is composite state will be the second occurance, otherwise it will be third
                        stateResult = getStateFromString(matcher.group(0));
                    }
                    else if (idx == 1)
                        stateCount = getStateFromString(matcher.group(0));
                    idx++;
                }
            }
            exps.add(getExpFromStrings(where, stateCount, comp, howMany, stateCenter));
        }
        RuleQuantifier qr = null;
        //if its not a composite rule
        if ( exps.size() == 1)
             qr = new RuleQuantifier(exps.get(0), stateResult, s);
        //if its composite
        else{
            //TODO determine logic operator
            pattern = Pattern.compile("AND|OR");
            matcher = pattern.matcher(s);
            TreeNode logicOperator = null;
            while (matcher.find()) {
                if (matcher.group(0).equals("AND"))
                    logicOperator = new ExpressionAND(exps.get(0), exps.get(1));
                else
                    logicOperator = new ExpressionOR(exps.get(0), exps.get(1));
            }
            qr = new RuleQuantifier(logicOperator, stateResult, s);
        }
        return qr;
    }

    static public eState getStateFromString(String s){
        switch (s){
            case "alive":
            case "live":
            case "ALIVE":
                return eState.ALIVE;
            case "dead":
            case "die":
            case "DEAD":
                return eState.DEAD;
            case "empty":
            case "disappear":
            case "EMPTY":
                return eState.EMPTY;
            default:
                return null;
        }
    }

    static public eComparison getCompFromString(String s) {
        switch (s) {
            case "exactly":
                return eComparison.EQUAL;
            case "less than":
                return eComparison.LESSTHEN;
            case "less than or equal":
                return eComparison.LESSTHENEQUAL;
            case "more than":
                return eComparison.MORETHEN;
            case "more than or equal":
                return eComparison.MORETHENEQUAL;
            default:
                return null;
        }
    }

    static public TreeNode getExpFromStrings(String where, eState stateCount, eComparison comp, int howMany, eState stateCenter){
        AbstractExpression exp = null;
        String lastWord = where.substring(where.lastIndexOf(" ") + 1);
        if (lastWord.equals("row")){
            int row = Integer.parseInt(where.substring(0, 1));
            exp = new ExpressionRow(row-1, stateCount, comp, howMany, stateCenter);
        }
        else if (lastWord.equals("column")){
            int column = Integer.parseInt(where.substring(0, 1));
            exp = new ExpressionCol(column-1, stateCount, comp, howMany, stateCenter);
        }
        else if (where.equals("neighborhood")){
            exp = new ExpressionAround(stateCount, comp, howMany, stateCenter);
        }
        else if (where.split(" ")[0].equals("inner")){
            exp = new ExpressionBorder(stateCount, comp, true, howMany, stateCenter);
        }
        else if (where.split(" ")[0].equals("outer")){
            exp = new ExpressionBorder(stateCount, comp, false, howMany, stateCenter);
        }
        else if (where.split(" ")[0].equals("above")){
            exp = new ExpressionAbove(stateCount, comp, howMany, stateCenter);
        }
        else if (where.split(" ")[0].equals("below")){
            exp = new ExpressionBelow(stateCount, comp, howMany, stateCenter);
        }
        else if (where.split(" ")[0].equals("left")){
            exp = new ExpressionLeft(stateCount, comp, howMany, stateCenter);
        }
        else if (where.split(" ")[0].equals("right")){
            exp = new ExpressionRight(stateCount, comp, howMany, stateCenter);
        }
        return (TreeNode) exp;
    }
}
