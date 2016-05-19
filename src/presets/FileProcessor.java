package presets;

import game.Game;
import rules.eComparison;
import rules.eState;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by Nibiru on 2016-05-11.
 */
public class FileProcessor {
    int N = 5;

    public void saveToFile(String filename, Iterator<Map.Entry<Point, Boolean>> it,
                           Iterator<Map.Entry<java.util.List<java.util.List<eState>>, eState>> it2){

        try (BufferedWriter bw = new BufferedWriter(new PrintWriter(filename))) {
            //save current board to file
            bw.write("# POINTS\r\npoints\r\n");
            while (it.hasNext()) {
                Map.Entry<Point, Boolean> pair = it.next();
                Point xy = pair.getKey();
                boolean isAlive = pair.getValue();
                bw.write(String.valueOf(xy.x) + "," + String.valueOf(xy.y) + "," + isAlive + "\r\n");
            }
            bw.write("points END\r\n");
            //save current rules to file
            bw.write("# RULES\r\n");
            int i = 0;
            while (it2.hasNext()) {
                bw.write("d" + String.valueOf(i) + "\r\n");
                Map.Entry<java.util.List<java.util.List<eState>>, eState> pair =
                        it2.next();
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
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void loadFromFile(String filename, Game g){
        //lead contents of file
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean readingDiscreteRule = false;
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
                    n.get(Integer.parseInt(lineContents[0])).set(Integer.parseInt(lineContents[1])
                            ,getStateFromString(lineContents[2]));
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
}
