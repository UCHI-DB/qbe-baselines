package dbms;

import java.util.Vector;

import query.DisjunctiveSelectionCondition;
import query.SelectQuery;
import query.SelectionCondition;

public class TableColPK {

    // Assumption: primary key is always integer
    // This class will contain a table name, a column name in that table and
    // primary keys (specific rows) to identify an object of user interest

    public Attribute attr;
    public Vector<Vector<Integer>> pks;
    private Vector<String> keys;

    public TableColPK(Attribute attr, String key) {
        if (attr instanceof AttributeWithTable) {
            attr = ((AttributeWithTable) attr).getAttribute();
        }
        this.attr = attr;
        pks = new Vector<>();
        keys = new Vector<>();
        keys.add(key);
        pks.add(findPK(key));
    }

    public TableColPK(Attribute attr, String key, Vector<Integer> pk) {
        if (attr instanceof AttributeWithTable) {
            attr = ((AttributeWithTable) attr).getAttribute();
        }
        this.attr = attr;
        pks = new Vector<>();
        keys = new Vector<>();
        keys.add(key);
        pks.add(pk);
    }

    private Vector<Integer> findPK(String value) {
        Vector<Integer> resultPk = new Vector<>();
        Attribute pkAttr = attr.getTable().getPrimaryKey();
        SelectQuery sq = new SelectQuery();
        sq.addTable(attr.getTable());
        sq.addProjectColumn(pkAttr);
        DisjunctiveSelectionCondition dsc = new DisjunctiveSelectionCondition();
        dsc.add(new SelectionCondition(attr, "lower", value.toLowerCase()));
        sq.addConjunctiveSelectionCondition(dsc);
        for (Vector<String> r : sq.getResult()) {
            for (String pkey : r) {
                resultPk.addElement(Integer.decode(pkey));
            }
        }
        return resultPk;
    }

    public TableColPK merge(TableColPK tcp) {
        if (tcp.attr.equals(attr)) {
            pks.addAll(tcp.pks);
            keys.addAll(tcp.keys);
            return this;
        }
        return null;
    }

}
