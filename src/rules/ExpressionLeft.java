package rules;

import java.util.List;

/**
 * Created by Nibiru on 2016-05-28.
 */
public class ExpressionLeft extends AbstractExpression {
    private eState s;
    private eComparison comp;
    private int howMany;
    private eState center;

    public ExpressionLeft(eState s, eComparison comp, int howMany, eState center) {
        this.s = s;
        this.comp = comp;
        this.howMany = howMany;
        this.center = center;
    }
    @Override
    public boolean solve(List<List<eState>> neighborhood) {
        if (!checkCenter(neighborhood, center))
            return false;
        int count = 0;
        for(int y = 0; y < neighborhood.size(); y++) {
            if (neighborhood.get(0).get(y) == s)
                count++;
            if (neighborhood.get(1).get(y) == s)
                count++;
        }
        return this.compare(count, howMany, comp);
    }
}