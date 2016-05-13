package rules;

import java.util.List;

/**
 * Created by Nibiru on 2016-04-27.
 */
abstract public class AbstractExpression {
    abstract public boolean  solve(List<List<eState>> neighborhood);

    final boolean compare(int count, int howMany, eComparison comp){
        switch (comp){
            case EQUAL:
                return count == howMany;
            case LESSTHEN:
                return count < howMany;
            case LESSTHENEQUAL:
                return count <= howMany;
            case MORETHEN:
                return count > howMany;
            case MORETHENEQUAL:
                return count >= howMany;
        }
        throw new IllegalArgumentException("bad comparison");
    }

    final boolean checkCenter(List<List<eState>> neighborhood,eState cen){
        //check if center cell is ok
        if (neighborhood.get(2).get(2) != cen)
            return false;
        else
            return true;

    }
}
