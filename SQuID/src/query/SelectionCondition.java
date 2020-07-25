package query;

import java.util.Vector;

import dbms.Attribute;
import util.DBUtil;

public class SelectionCondition {
    private Attribute attribute;
    private String functionOnAttribute;
    String value;
    Vector<SelectionCondition> conjunctiveSelectionCondition;
    private String tableName;
    private String operator;

    public SelectionCondition(Attribute attribute, String functionOnAttribute, String value) {
        this.attribute = attribute;
        this.functionOnAttribute = functionOnAttribute;
        this.value = value;
        this.conjunctiveSelectionCondition = null;
        this.tableName = attribute.getTable().getName();
        this.operator = "=";
    }

    public SelectionCondition(Attribute attribute, String value) {
        this.attribute = attribute;
        this.tableName = attribute.getTable().getName();
        this.value = value;
        this.functionOnAttribute = null;
        this.conjunctiveSelectionCondition = null;
        this.operator = "=";
    }

    @SuppressWarnings("unchecked")
    public SelectionCondition(Attribute attribute, Object value, int operator) {
        this.attribute = attribute;
        this.tableName = attribute.getTable().getName();
        if (value instanceof Integer) {
            this.value = Integer.toString((Integer) value);
        } else if (value instanceof Double) {
            this.value = Double.toString((Double) value);
        } else if (value instanceof Vector<?>) {
            this.value = "";
            for (String v : (Vector<String>) value) {
                if (this.value.isEmpty()) {
                    this.value = "(" + v;
                } else {
                    this.value += "," + v;
                }
            }
            this.value += ")";

        } else if (value instanceof String) {
            this.value = (String) value;
        }
        this.operator = DBUtil.getOperatorSymbol(operator);
        this.functionOnAttribute = null;
        this.conjunctiveSelectionCondition = null;
    }

    public SelectionCondition() {
        conjunctiveSelectionCondition = new Vector<>();
        this.attribute = null;
        this.value = null;
        this.functionOnAttribute = null;
        this.tableName = null;
    }

    public String getSelectionConditionStr() {
        String ret = "";
        if (conjunctiveSelectionCondition == null) { // single selection condition
            ret = DBUtil.applyFunction(functionOnAttribute, tableName + "." + attribute.getName(),
                    false);
            ret += " " + operator + " ";
            if (attribute.isTextAttribute()) {
                ret += "'" + value.replaceAll("'", "''") + "'";
            } else {
                ret += value;
            }
        } else {
            boolean first = true;
            for (SelectionCondition sc : conjunctiveSelectionCondition) {
                if (!first) {
                    ret += " AND ";
                }
                first = false;
                ret += sc.getSelectionConditionStr();
            }
            if (conjunctiveSelectionCondition.size() > 1) {
                ret = "(" + ret + ")";
            }
        }
        return ret;
    }

    public Attribute getConditionAttribute() {
        // returns the actual attribute that is being bound to a fixed value
        if (attribute == null && conjunctiveSelectionCondition != null) {
            return conjunctiveSelectionCondition.elementAt(0).getConditionAttribute();
        }
        if (attribute.isPrimaryKey()) {
            return attribute;
        }
        if (attribute.getFkRefTo() != null && attribute.getFkRefTo().isPrimaryKey()) {
            return attribute.getFkRefTo();
        }
        return attribute;
    }

    public void add(SelectionCondition sc) {
        conjunctiveSelectionCondition.add(sc);
    }
}
