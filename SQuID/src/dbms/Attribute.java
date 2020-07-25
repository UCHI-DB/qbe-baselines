package dbms;

import java.io.Serializable;
import java.util.Vector;

import plume.Pair;
import query.DisjunctiveSelectionCondition;
import query.SelectQuery;
import query.SelectionCondition;
import util.DBUtil;
import util.QbeeException;
import util.SQLOperator;

public class Attribute implements Serializable {
    String name;
    private Table table;
    Attribute fkRefTo;
    Vector<Attribute> fkRefFrom;
    private boolean isConceptAttributeOrFKey;
    private boolean isObjectAttributeOrFKey;
    private boolean isTextAttribute;
    private boolean isPrimaryAttribute;

    Attribute(Table table, int attrId, String name, String dtype) {
        isConceptAttributeOrFKey = false;
        isObjectAttributeOrFKey = false;
        isTextAttribute = false;
        isPrimaryAttribute = false;
        this.name = name;
        this.table = table;
        if (dtype.equalsIgnoreCase("text") || dtype.contains("character varying")) {
            isTextAttribute = true;
        }
        fkRefTo = null;
        fkRefFrom = new Vector<>();
    }

    public Attribute(Attribute attribute) {
        this.name = attribute.name;
        this.table = attribute.table;
        this.fkRefTo = attribute.fkRefTo;
        this.fkRefFrom = attribute.fkRefFrom;
        this.isConceptAttributeOrFKey = attribute.isConceptAttributeOrFKey;
        this.isObjectAttributeOrFKey = attribute.isObjectAttributeOrFKey;
        this.isTextAttribute = attribute.isTextAttribute;
        this.isPrimaryAttribute = attribute.isPrimaryAttribute;
    }

    public boolean isTextAttribute() {
        return isTextAttribute;
    }

    public void setConceptAttribute(boolean b) {
        this.isConceptAttributeOrFKey = b;
    }

    public boolean isConceptAttributeOrFKey() {
        if (fkRefTo != null && fkRefTo.getTable().hasTableType(DBUtil.DIMENSION_CONCEPT_TABLE))
            isConceptAttributeOrFKey = true;
        return isConceptAttributeOrFKey;
    }

    public void setObjectAttribute(boolean b) {
        this.isObjectAttributeOrFKey = b;
    }

    public boolean isObjectAttributeOrFKey() {
        return isObjectAttributeOrFKey;
    }

    public void setPrimaryAttribute(boolean b) {
        this.isPrimaryAttribute = b;
    }

    public boolean isPrimaryAttribute() {
        return isPrimaryAttribute;
    }

    public String getName() {
        return name;
    }

    public Table getTable() {
        return table;
    }

    /**
     * @return All attributes that refer to this attribute as foreign key
     */
    public Vector<Attribute> getFkRefFromAttrs() {
        return fkRefFrom;
    }

    /**
     * @param t
     * @return Attribute within t that refers to this attribute
     */

    public Attribute getFkAttributeFromTable(Table t) {
        if (t instanceof TableWithAlias) {
            t = ((TableWithAlias) t).getTable();
        }
        if (getTable().equals(t)) {
            // Same table where this attribute is in
            return this;
        }
        for (Attribute attr : fkRefFrom) {
            if (attr.getTable().equals(t)) {
                return attr;
            }
        }
        return null;
    }

    public Attribute getFkRefTo() {
        return fkRefTo;
    }

    /**
     * @param fkOrValue
     * @return a list of string in the format "attribute:value". if this attribute is actually a
     *         foreign key to something else, return the original
     */
    public Vector<Pair<String, String>> getRealValue(Vector<String> fkOrValue) {
        Vector<Pair<String, String>> valueMap = new Vector<>();
        if (!DBUtil.GUI) {
            for (String now : fkOrValue) {
                valueMap.add(new Pair<String, String>(now, now));
            }
            return valueMap;
        }

        if (fkRefTo == null || fkOrValue.size() == 0) {
            for (String key : fkOrValue) {
                valueMap.addElement(new Pair<String, String>(key, key));
            }
            return valueMap;
        }
        SelectQuery sq = new SelectQuery();
        sq.addProjectColumn(fkRefTo);
        sq.addProjectColumn(new AttributeWithTable(getRealAttribute(), fkRefTo.getTable()));
        sq.addTable(fkRefTo.getTable());
        DisjunctiveSelectionCondition dsc = new DisjunctiveSelectionCondition();
        dsc.add(new SelectionCondition(fkRefTo, fkOrValue, SQLOperator.IN));
        sq.addConjunctiveSelectionCondition(dsc);

        Vector<Vector<String>> result = sq.getResult();
        for (Vector<String> row : result) {
            valueMap.add(new Pair<String, String>(row.elementAt(0), row.elementAt(1)));
        }
        return valueMap;
    }

    /**
     * @param fkOrValue
     * @return a string in the format "attribute:value". if this attribute is actually a foreign key
     *         to something else, return the original
     */
    public String getRealValue(String fkOrValue) {
        if (fkRefTo == null) {
            return fkOrValue;
        }
        SelectQuery sq = new SelectQuery();
        sq.addProjectColumn(getRealAttribute());
        sq.addTable(fkRefTo.getTable());
        DisjunctiveSelectionCondition dsc = new DisjunctiveSelectionCondition();
        dsc.add(new SelectionCondition(fkRefTo, fkOrValue));
        sq.addConjunctiveSelectionCondition(dsc);

        Vector<Vector<String>> result = sq.getResult();
        if (result.size() != 1) {
            throw new QbeeException(String.format("Invalid foreign key reference to table: "
                    + fkRefTo.getTable().getName() + " with key: " + fkOrValue));
        }
        // only one row should be returned
        return result.elementAt(0).elementAt(0);
    }

    public Attribute getRealAttribute() {
        if (fkRefTo == null) {
            return this;
        }
        if (fkRefTo.getTable().getPrimaryAttributes().size() != 1) {
            throw (new QbeeException(
                    "No/Multiple concept attribute in table " + fkRefTo.getTable().getName()));
        }
        return fkRefTo.getTable().getPrimaryAttributes().elementAt(0);
    }

    public boolean isPrimaryKey() {
        if (this.equals(getTable().getPrimaryKey())) {
            return true;
        }
        return false;
    }

    public void setFkRefTo(Attribute attribute) {
        this.fkRefTo = attribute;
        if (attribute.getTable().hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)) {
            setObjectAttribute(true);
        }
    }

    public boolean isEqual(Attribute attribute) {
        return equals(attribute);
    }

    public boolean isAggregateAttribute() {
        return getName().endsWith("aggr");
    }

    public boolean isFrequencyAttribute() {
        return getName().contains(DBUtil.FREQUENCY_MODE);
    }

    public String getNameWithTable() {
        return getTable().getRealName() + "." + getName();
    }

    public boolean isCountAttribute() {
        return getName().startsWith("count");
    }
}
