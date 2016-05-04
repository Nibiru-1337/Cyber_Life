package rules;
import java.util.List;

/**
 * Created by Nibiru on 2016-04-27.
 */
public class RuleQuantifier extends TreeNode implements iRule{
    private AbstractExpression root;
    private TreeNode left;
    private TreeNode right;

    public RuleQuantifier(AbstractExpression root) {
        this.root = root;
    }
    @Override
    public boolean check(List<List<eState>> neighborhood) {
        return root.solve(neighborhood);
    }
}
