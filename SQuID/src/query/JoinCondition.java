package query;

import java.util.Set;

import dbms.Attribute;

public class JoinCondition {
    private Attribute attr1;
    private Attribute attr2;

    public JoinCondition(Attribute attr1, Attribute attr2) {
        this.attr1 = attr1;
        this.attr2 = attr2;
    }

    public String getJoinConditionStr() {
        return "(" + attr1.getTable().getName() + "." + attr1.getName() + "="
                + attr2.getTable().getName() + "." + attr2.getName() + ")";
    }

    public boolean contains(Set<Attribute> attributes) {
        for (Attribute attr : attributes) {
            if (attr1.isEqual(attr) || attr2.isEqual(attr)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEqual(JoinCondition another) {
        if (attr1.equals(another.attr1) && attr2.equals(another.attr2)) {
            return true;
        }
        if (attr2.equals(another.attr1) && attr1.equals(another.attr2)) {
            return true;
        }
        return false;
    }
}
