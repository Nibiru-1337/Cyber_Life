package rules;

import java.util.List;

/**
 * Created by Nibiru on 2016-04-27.
 */
public class ExpressionAround extends AbstractExpression {
    private eState s;
    private eComparison comp;
    private int howMany;

    public ExpressionAround(eState s, eComparison comp, int howMany) {
        //validate input (include the center pixel?)
        if (1 <= howMany && howMany <= 25 ) {
            this.s = s;
            this.comp = comp;
            this.howMany = howMany;
        }
        else
            throw new IllegalArgumentException("bad howMany");
    }

    @Override
    public boolean solve(List<List<eState>> neighborhood) {
        int count = 0;
        for (int x = 0; x < neighborhood.size(); x++)
            for(int y = 0; y < neighborhood.size(); y++)
                if (neighborhood.get(x).get(y) == s)
                    count++;
        return this.compare(count, howMany, comp);
    }
}
