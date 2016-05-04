package tests

import game.Game
import rules.ExpressionAround
import rules.ExpressionBorder
import rules.ExpressionRow
import rules.RuleQuantifier
import rules.eComparison
import rules.eState

import java.awt.Point

/**
 * Created by Nibiru on 2016-04-27.
 */
class GameTests extends GroovyTestCase {

    private Game g = new Game();
    int N = 5;

    void testGetNeighbors(){
        g.board.put(new Point(1,1), false);
        g.board.put(new Point(2,2), true);
        Point p = new Point(0,0);

        def n = g.getNeighborhood(p);

        //make proper output of get neighbors
        def r = new eState[N][N];
        //customize rule
        for (int x = 0; x < N; x++)
            for (int y = 0; y < N; y++)
                r[x][y] = eState.EMPTY;
        r[3][3] = eState.DEAD;
        r[4][4] = eState.ALIVE;

        assertEquals r, g.getNeighborhood(new Point(0,0))
    }

    void testDiscreteRuleTrue() {
        g.board.put(new Point(1,1), false);
        g.board.put(new Point(2,2), true);
        Point p = new Point(0,0);

        def n = g.getNeighborhood(p);

        //make new discrete rule
        def r = new eState[N][N];
        //customize rule
        for (int x = 0; x < N; x++)
            for (int y = 0; y < N; y++)
                r[x][y] = eState.EMPTY;
        r[3][3] = eState.DEAD;
        r[4][4] = eState.ALIVE;

        //convert to list of lists, since we need equality by value
        ArrayList<ArrayList<eState>> cR = new ArrayList<>(N);
        for(int x = 0; x < N; x++)
            cR.add(new ArrayList<>(Arrays.asList(r[x])));
        g.discreteRules.put(cR, eState.ALIVE);

        //check rule
        def outcome = g.discreteRules.get(n);
        assertEquals eState.ALIVE, outcome
    }

    void testQuantifierColRuleTrue(){
        g.board.put(new Point(1,1), false);
        g.board.put(new Point(2,2), true);
        Point p = new Point(0,0);

        def n = g.getNeighborhood(p);

        //make new quantifier rule
        ExpressionRow expr = new ExpressionRow(4, eState.ALIVE, eComparison.EQUAL, 1);
        RuleQuantifier q = new RuleQuantifier(expr);
        //check rule
        def neighboorhood = g.getNeighborhood(new Point(0,0));
        assertEquals true, q.check(neighboorhood)
    }

    void testQuantifierRowRuleTrue(){
        g.board.put(new Point(1,1), false);
        g.board.put(new Point(2,2), true);
        Point p = new Point(0,0);

        def n = g.getNeighborhood(p);

        //make new quantifier rule
        ExpressionRow expr = new ExpressionRow(3, eState.DEAD, eComparison.EQUAL, 1);
        RuleQuantifier q = new RuleQuantifier(expr);
        //check rule
        def neighboorhood = g.getNeighborhood(new Point(0,0));
        assertEquals true, q.check(neighboorhood)
    }

    void testDiscreteRuleFalse() {
        g.board.put(new Point(1,1), false);
        g.board.put(new Point(2,2), true);
        Point p = new Point(0,0);

        def n = g.getNeighborhood(p);

        //make new discrete rule
        def r = new eState[N][N];
        //customize rule
        for (int x = 0; x < N; x++)
            for (int y = 0; y < N; y++)
                r[x][y] = eState.EMPTY;
        r[3][3] = eState.DEAD;
        r[4][4] = eState.DEAD;

        //convert to list of lists, since we need equality by value
        ArrayList<ArrayList<eState>> cR = new ArrayList<>(N);
        for(int x = 0; x < N; x++)
            cR.add(new ArrayList<>(Arrays.asList(r[x])));
        g.discreteRules.put(cR, eState.ALIVE);

        //check rule
        ArrayList<ArrayList<eState>> cN = new ArrayList<>(N);
        for(int x = 0; x < N; x++)
            cN.add(new ArrayList<>(Arrays.asList(n[x])));
        def outcome = g.discreteRules.get(cN);
        assertEquals null, outcome
    }

    void testQuantifierColRuleFalse(){
        g.board.put(new Point(1,1), false);
        g.board.put(new Point(2,2), true);
        Point p = new Point(0,0);

        def n = g.getNeighborhood(p);

        //make new quantifier rule
        ExpressionRow expr = new ExpressionRow(4, eState.DEAD, eComparison.EQUAL, 1);
        RuleQuantifier q = new RuleQuantifier(expr);
        //check rule
        def neighboorhood = g.getNeighborhood(new Point(0,0));
        assertEquals false, q.check(neighboorhood)
    }

    void testQuantifierRowRuleFalse(){
        g.board.put(new Point(1,1), false);
        g.board.put(new Point(2,2), true);
        Point p = new Point(0,0);

        def n = g.getNeighborhood(p);

        //make new quantifier rule
        ExpressionRow expr = new ExpressionRow(3, eState.ALIVE, eComparison.EQUAL, 1);
        RuleQuantifier q = new RuleQuantifier(expr);
        //check rule
        def neighboorhood = g.getNeighborhood(new Point(0,0));
        assertEquals false, q.check(neighboorhood)
    }

    void testQuantifierAroundRuleTrue(){
        g.board.put(new Point(1,1), false);
        g.board.put(new Point(2,2), true);
        Point p = new Point(0,0);

        def n = g.getNeighborhood(p);

        //make new quantifier rule
        ExpressionAround expr = new ExpressionAround(eState.EMPTY, eComparison.MORETHEN, 10);
        RuleQuantifier q = new RuleQuantifier(expr);
        //check rule
        def neighboorhood = g.getNeighborhood(new Point(0,0));
        assertEquals true, q.check(neighboorhood)
    }

    void testQuantifierAroundRuleFalse(){
        g.board.put(new Point(1,1), false);
        g.board.put(new Point(2,2), true);
        Point p = new Point(0,0);

        def n = g.getNeighborhood(p);

        //make new quantifier rule
        ExpressionAround expr = new ExpressionAround(eState.EMPTY, eComparison.LESSTHEN, 10);
        RuleQuantifier q = new RuleQuantifier(expr);
        //check rule
        def neighboorhood = g.getNeighborhood(new Point(0,0));
        assertEquals false, q.check(neighboorhood)
    }

    void testQuantifierInnerBorderRuleTrue(){
        g.board.put(new Point(1,1), false);
        g.board.put(new Point(2,2), true);
        Point p = new Point(0,0);

        def n = g.getNeighborhood(p);

        //make new quantifier rule
        ExpressionBorder expr = new ExpressionBorder(eState.EMPTY, eComparison.EQUAL, true, 7);
        RuleQuantifier q = new RuleQuantifier(expr);
        //check rule
        def neighboorhood = g.getNeighborhood(new Point(0,0));
        assertEquals true, q.check(neighboorhood)
    }

    void testQuantifierOuterBorderRuleTrue(){
        g.board.put(new Point(1,1), false);
        g.board.put(new Point(2,2), true);
        Point p = new Point(0,0);

        def n = g.getNeighborhood(p);

        //make new quantifier rule
        ExpressionBorder expr = new ExpressionBorder(eState.EMPTY, eComparison.EQUAL, false, 15);
        RuleQuantifier q = new RuleQuantifier(expr);
        //check rule
        def neighboorhood = g.getNeighborhood(new Point(0,0));
        assertEquals true, q.check(neighboorhood)
    }
}
