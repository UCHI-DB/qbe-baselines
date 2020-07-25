package dbms;

import java.io.Serializable;
import java.util.Vector;

import util.DBUtil;
import util.QbeeException;

public class Table implements Serializable {
    String name;
    private int tableType;
    Vector<Attribute> attributes;
    private int nAttr = 0;
    private Attribute primaryKey;
    private Vector<Attribute> primaryAttributes; // denotes the meaningful attribute for an object
                                                 // or concept. Example: Name of a person

    Table(Table table) {
        this.name = table.name;
        this.tableType = table.tableType;
        this.attributes = table.attributes;
        this.nAttr = table.nAttr;
        this.primaryKey = table.primaryKey;
        this.primaryAttributes = table.primaryAttributes;
    }

    Table(String name) {
        this.name = name;
        attributes = new Vector<>();
        nAttr = 0;
        primaryAttributes = new Vector<>();
        tableType = 0;
    }

    void addAttr(String attribute, String dtype) {
        Attribute curAttr = new Attribute(this, nAttr++, attribute, dtype);
        this.attributes.addElement(curAttr);
    }

    public Vector<Attribute> getAttributes() {
        return attributes;
    }

    public String getName() {
        return name;
    }

    public Attribute getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Attribute primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setPrimaryAttribute(Attribute primaryAttribute) {
        this.primaryAttributes.add(primaryAttribute);
    }

    public Vector<Attribute> getPrimaryAttributes() {
        if (primaryAttributes.size() > 0) {
            return primaryAttributes;
        }
        /*
         * If this table has no specific primary attribute, all attributes except the primary key
         * will act as primary attribute together
         */
        for (Attribute attr : attributes) {
            if (primaryKey != null && attr.getName().equalsIgnoreCase(primaryKey.getName())) {
                continue;
            }
            primaryAttributes.add(attr);
        }
        return primaryAttributes;
    }

    public boolean hasTableType(int checkTableType) {
        if ((tableType & checkTableType) > 0) {
            return true;
        }
        return false;
    }

    public void setTableType(int tableType) {
        this.tableType |= tableType;
    }

    public String getRealName() {
        return getName();
    }

    public Attribute getConceptConditionAttribute() {
        if (hasTableType(DBUtil.FACT_OBJECT_DEEP_FREQ_CONCEPT_TABLE)) {
            for (Attribute attr : getAttributes()) {
                if (attr.isFrequencyAttribute()) {
                    continue;
                }
                if (attr.getFkRefTo() == null || attr.getFkRefTo().getTable()
                        .hasTableType(DBUtil.DIMENSION_CONCEPT_TABLE)) {
                    return attr;
                }
            }
        }
        return null;
    }

    public Attribute getFrequencyAttribute(String mode) {
        if (hasTableType(DBUtil.FACT_OBJECT_DEEP_FREQ_CONCEPT_TABLE)) {
            for (Attribute attr : getAttributes()) {
                if (attr.getName().equals(mode)) {
                    return attr;
                }
            }
        }
        return null;
    }

    public Attribute getObjectAttribute() {
        if (hasTableType(DBUtil.FACT_OBJECT_DEEP_FREQ_CONCEPT_TABLE)) {
            for (Attribute attr : getAttributes()) {
                if (attr.getFkRefTo() != null && attr.getFkRefTo().getTable()
                        .hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)) {
                    return attr;
                }
            }
        } else if (hasTableType(DBUtil.FACT_OBJECT_FREQ_CONCEPT_TABLE)) {
            for (Attribute attr : getAttributes()) {
                if (attr.isObjectAttributeOrFKey()) {
                    return attr;
                }
            }
        } else if (hasTableType(DBUtil.AGGR_OBJECT_CONCEPT_TABLE)
                || hasTableType(DBUtil.AGGR_OBJECT_OBJECT_TABLE)) {
            for (Attribute attr : getAttributes()) {
                if (attr.isObjectAttributeOrFKey() && !attr.isAggregateAttribute()) {
                    return attr;
                }
            }
        } else if (hasTableType(DBUtil.FACT_OBJECT_CONCEPT_TABLE)) {
            for (Attribute attr : getAttributes()) {
                if (attr.isObjectAttributeOrFKey() && !attr.isAggregateAttribute()) {
                    return attr;
                }
            }
        }
        return null;
    }

    /*
     * For a FOC table, find which attribute is FK to some concept attribute of some DC table
     */
    public Attribute getConceptAttribute() {
        Attribute conceptAttribute = null;
        for (Attribute attr : getAttributes()) {
            if (attr.getFkRefTo() != null
                    && attr.getFkRefTo().getTable().hasTableType(DBUtil.DIMENSION_CONCEPT_TABLE)) {
                if (conceptAttribute == null) {
                    conceptAttribute = attr;
                } else {
                    conceptAttribute = null; // multiple FK to multiple DC
                    break;
                }
            }
        }

        if (conceptAttribute == null) {
            // FOC is supposed to refer to exactly one DC via foreign key
            throw (new QbeeException(
                    "Multiple/No concept attribute foreign key found from table " + getName()));
        }
        return conceptAttribute;
    }
}
