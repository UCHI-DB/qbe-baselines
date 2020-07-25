package query;

import java.util.Vector;

public class DisjunctiveSelectionCondition {
    private Vector<SelectionCondition> selectionConditions;

    public DisjunctiveSelectionCondition(SelectionCondition selectionCondition) {
        selectionConditions = new Vector<>();
        add(selectionCondition);
    }

    public DisjunctiveSelectionCondition() {
        selectionConditions = new Vector<>();
    }

    public void add(SelectionCondition selectionCondition) {
        if (selectionCondition.conjunctiveSelectionCondition != null
                || selectionCondition.value != null) {
            selectionConditions.add(selectionCondition);
        }
    }

    public String getDisjunctiveSelectionConditionStr() {
        boolean first = true;
        String result = "";
        for (SelectionCondition cond : selectionConditions) {
            if (!first) {
                result += " OR ";
            }
            first = false;
            result += cond.getSelectionConditionStr();
        }
        if (selectionConditions.size() > 1)
            result = "(" + result + ")";
        return result;
    }

    int size() {
        return selectionConditions.size();
    }

    public Vector<SelectionCondition> getSelectionConditions() {
        return selectionConditions;
    }

}
