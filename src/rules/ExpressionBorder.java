package rules;

import java.util.List;

/**
 * Created by Nibiru on 2016-04-27.
 */
public class ExpressionBorder extends AbstractExpression implements TreeNode{
    private eState s;
    private eComparison comp;
    private boolean innerBorder;
    private int howMany;
    private eState center;

    public ExpressionBorder(eState s, eComparison comp, boolean innerBorder, int howMany, eState center) {
        if ( (innerBorder && 0 < howMany && howMany <= 8) || (!innerBorder && 0 < howMany && howMany <= 16) ){
            this.s = s;
            this.comp = comp;
            this.innerBorder = innerBorder;
            this.howMany = howMany;
            this.center = center;
        }
        else
            throw new IllegalArgumentException("bad howMany");

    }

    @Override
    public boolean solve(List<List<eState>> neighborhood) {
        if (!checkCenter(neighborhood, center))
            return false;

        int count = 0;
            for (int x = 0; x < neighborhood.size(); x++)
                for(int y = 0; y < neighborhood.size(); y++) {
                    if (innerBorder && !(x == 2 && y == 2) && (x == 1 || x == 2 || x == 3) && (y == 1 || y == 2|| y == 3)
                            && neighborhood.get(x).get(y) == s)
                        count++;
                    if (!innerBorder && (x == 0 || x == 4 || y == 0 || y == 4)  && neighborhood.get(x).get(y) == s)
                        count++;
                }
            return this.compare(count, howMany, comp);
    }
}
