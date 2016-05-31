package rules;

import sun.reflect.generics.tree.Tree;

import java.util.List;

/**
 * Created by Nibiru on 2016-04-27.
 */
public class ExpressionOR extends AbstractExpression implements TreeNode {
    private TreeNode left;
    private TreeNode right;

    public ExpressionOR(TreeNode left, TreeNode right){
        this.left = left;
        this.right = right;
    }
    @Override
    public boolean solve(List<List<eState>> neighborhood) {
        return left.solve(neighborhood) || right.solve(neighborhood);
    }
}
