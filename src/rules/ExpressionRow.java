package rules;

import java.util.List;

/**
 * Created by Nibiru on 2016-04-27.
 */
public class ExpressionRow extends AbstractExpression implements TreeNode{
    private int row;
    private eState s;
    private eComparison comp;
    private int howMany;
    private eState center;

    public ExpressionRow(int row, eState s, eComparison comp, int howMany, eState center) {
        //validate input
        if ((0 <= row && row <= 4) && (1 <= howMany && howMany <= 5 ) ){
            this.row = row;
            this.s = s;
            this.comp = comp;
            this.howMany = howMany;
            this.center = center;
        }
        else
            throw new IllegalArgumentException("bad row or howMany args");
    }

    @Override
    public boolean solve(List<List<eState>> neighborhood) {
        if (!checkCenter(neighborhood, center))
            return false;
        int count = 0;
        for(int x = 0; x < neighborhood.size(); x++)
            if (neighborhood.get(x).get(row) == s)
                count++;
        return this.compare(count, howMany, comp);
    }
}
