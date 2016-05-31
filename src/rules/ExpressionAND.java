package rules;

import java.util.List;

/**
 * Created by Nibiru on 2016-04-27.
 */
public class ExpressionAND extends AbstractExpression implements TreeNode{
    private TreeNode left;
    private TreeNode right;

    public ExpressionAND(TreeNode left, TreeNode right){
        this.left = left;
        this.right = right;
    }
    @Override
    public boolean solve(List<List<eState>> neighborhood) {
        return left.solve(neighborhood) && right.solve(neighborhood);
    }
}
