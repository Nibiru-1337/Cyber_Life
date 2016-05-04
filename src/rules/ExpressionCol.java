package rules;

import java.util.List;

/**
 * Created by Nibiru on 2016-04-27.
 */
public class ExpressionCol extends AbstractExpression {
    private int col;
    private eState s;
    private eComparison comp;
    private int howMany;

    public ExpressionCol(int col, eState s, eComparison comp, int howMany) {
        //validate input
        if ((0 <= col && col <= 4) && (1 <= howMany && howMany <= 5 ) ) {
            this.col = col;
            this.s = s;
            this.comp = comp;
            this.howMany = howMany;
        }
        else
            throw new IllegalArgumentException("bad row or howMany args");
    }

    @Override
    public boolean solve(List<List<eState>> neighborhood) {
        int count = 0;
        for(int y = 0; y < neighborhood.size(); y++)
            if (neighborhood.get(col).get(y) == s)
                count++;
        return this.compare(count, howMany, comp);
    }
}
