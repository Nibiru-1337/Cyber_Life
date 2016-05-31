package rules;
import java.util.List;

/**
 * Created by Nibiru on 2016-04-27.
 */
public class RuleQuantifier implements iRule{
    private TreeNode root;
    private eState result;
    private String text;

    public RuleQuantifier(TreeNode root, eState res, String text) {
        this.root = root;
        this.result = res;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public eState getResultingState(){
        return result;
    }

    @Override
    public boolean check(List<List<eState>> neighborhood) {
        return root.solve(neighborhood);
    }
}
