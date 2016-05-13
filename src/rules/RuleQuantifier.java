package rules;
import java.util.List;

/**
 * Created by Nibiru on 2016-04-27.
 */
public class RuleQuantifier extends TreeNode implements iRule{
    private AbstractExpression root;
    private eState result;
    private TreeNode left;
    private TreeNode right;

    public RuleQuantifier(AbstractExpression root, eState res) {
        this.root = root;
        this.result = res;
    }
    public eState getResultingState(){
        return result;
    }
    @Override
    public boolean check(List<List<eState>> neighborhood) {
        //TODO: change to solve when we have tree
        return root.solve(neighborhood);
    }
}
