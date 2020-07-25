package dbms;

import java.util.Vector;

import util.DBUtil;

public class DB {
    private Vector<Table> tables;
    private Table invertedColumnIndexTable;

    public DB() {
        tables = new Vector<>();
        invertedColumnIndexTable = null;
    }

    void addTable(Table table) {
        tables.addElement(table);
    }

    /**
     * @param tableName
     * @param attrName
     * @return reference to the attribute object from @tableName and @attrName
     */

    // TODO: needs optimization using precalculation

    public Attribute getAttribute(String tableName, String attrName) {
        for (Table table : tables) {
            if (table.name.equalsIgnoreCase(tableName)) {
                for (Attribute attr : table.attributes) {
                    if (attr.name.equalsIgnoreCase(attrName)) {
                        return attr;
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param fromTableName
     * @param fromAttrName
     * @param toTableName
     * @param toAttrName
     *            Adds a foreign key constraint
     */

    void addForeignKeyConstraint(String fromTableName, String fromAttrName, String toTableName,
            String toAttrName) {
        Attribute fromAttr = getAttribute(fromTableName, fromAttrName);
        Attribute toAttr = getAttribute(toTableName, toAttrName);
        fromAttr.setFkRefTo(toAttr);
        if (!fromAttr.isAggregateAttribute()) {
            toAttr.fkRefFrom.add(fromAttr);
        }
    }

    public Vector<Table> getTables() {
        return tables;
    }

    /**
     * @param tableName
     * @return the reference to the table object with name @tableName
     */
    // TODO: needs optimization using precalculation

    Table getTable(String tableName) {
        for (Table t : tables) {
            if (t.getName().equalsIgnoreCase(tableName)) {
                return t;
            }
        }
        return null;
    }

    public String getInvertedColumnIndexTableName() {
        if (invertedColumnIndexTable == null) {
            for (Table table : tables) {
                if (table.getName().equalsIgnoreCase("_invertedColumnIndex")) {
                    invertedColumnIndexTable = table;
                    return invertedColumnIndexTable.getName();
                }
            }
            return null;
        }
        return invertedColumnIndexTable.getName();
    }

    /**
     * Creates the inverted column index if does not exist already
     */

    public void addInvertedColumnIndexTable() {
        invertedColumnIndexTable = new Table("_invertedColumnIndex");
        invertedColumnIndexTable.addAttr("word", "text");
        invertedColumnIndexTable.addAttr("tabName", "text");
        invertedColumnIndexTable.addAttr("colName", "text");
        tables.addElement(invertedColumnIndexTable);
    }

    /**
     * @param tableName
     * @param attrName
     *            Adds primary key constraints
     */
    void addPrimaryKeyConstraint(String tableName, String attrName) {
        Attribute attr = getAttribute(tableName, attrName);
        Table table = attr.getTable();
        table.setPrimaryKey(attr);
    }

    /**
     * Marks tables as dimension object table, dimension concept table, fact object-object table,
     * fact object-concept table, concept attributes within dimension table and primary attribute
     * for dimension tables
     */
    public void addMetaInfo(String[] dimensionObject, String[] dimensionConcept,
            String[] factObjectObject, String[] factObjectConcept, String[] additionalFreqConcept,
            String[] conceptwithindimension, String[] primaryattributewithindimension) {

        getInvertedColumnIndexTableName();
        for (String s : dimensionObject) {
            getTable(s).setTableType(DBUtil.DIMENSION_OBJECT_TABLE);
        }
        for (String s : dimensionConcept) {
            getTable(s).setTableType(DBUtil.DIMENSION_CONCEPT_TABLE);
        }
        for (String s : factObjectObject) {
            getTable(s).setTableType(DBUtil.FACT_OBJECT_OBJECT_TABLE);
        }
        for (String s : factObjectConcept) {
            getTable(s).setTableType(DBUtil.FACT_OBJECT_CONCEPT_TABLE);
        }
        for (String s : additionalFreqConcept) {
            getTable(s.split(":")[0]).setTableType(DBUtil.FACT_OBJECT_FREQ_CONCEPT_TABLE);
            getAttribute(s.split(":")[0], s.split(":")[2]).setConceptAttribute(true);
            getAttribute(s.split(":")[0], s.split(":")[1]).setObjectAttribute(true);
        }
        for (String s : conceptwithindimension) {
            getAttribute(s.split(":")[0], s.split(":")[1]).setConceptAttribute(true);
        }
        for (String s : primaryattributewithindimension) {
            getAttribute(s.split(":")[0], s.split(":")[1]).setPrimaryAttribute(true);
            getAttribute(s.split(":")[0], s.split(":")[1]).getTable()
                    .setPrimaryAttribute(getAttribute(s.split(":")[0], s.split(":")[1]));
        }
        for (Table t : getTables()) {
            if (t.getName().startsWith("_")) {
                if (!t.equals(invertedColumnIndexTable) && !t.getName().startsWith("_aggr")) {
                    t.setTableType(DBUtil.FACT_OBJECT_DEEP_FREQ_CONCEPT_TABLE);
                } else if (t.getName().startsWith("_aggr")) {
                    if (t.getName().startsWith("_aggr_aoo_")) {
                        t.setTableType(DBUtil.AGGR_OBJECT_OBJECT_TABLE);
                    } else if (t.getName().startsWith("_aggr_aoc_")) {
                        t.setTableType(DBUtil.AGGR_OBJECT_CONCEPT_TABLE);
                    }
                    for (Attribute a : t.getAttributes()) {
                        if (a.getFkRefTo() != null && !a.isAggregateAttribute() && a.getFkRefTo()
                                .getTable().hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)) {
                            a.setObjectAttribute(true);
                        }
                    }
                } else if (!t.equals(invertedColumnIndexTable)) {
                    System.out.println("SEVERE ERROR");
                }
            }
        }
    }

    public void addAggregateFKs() {
        for (Table t : getTables()) {
            if (t.hasTableType(DBUtil.FACT_OBJECT_CONCEPT_TABLE)) {
                Attribute objectAttr = null;
                Attribute conceptAttr = null;
                for (Attribute a : t.getAttributes()) {
                    if (a.getFkRefTo().getTable().hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)) {
                        objectAttr = a;
                    }
                    if (a.getFkRefTo().getTable().hasTableType(DBUtil.DIMENSION_CONCEPT_TABLE)) {
                        conceptAttr = a;
                    }
                }
                if (objectAttr != null && conceptAttr != null) {
                    Table aggrTable = getTable("_aggr_aoc_" + t.getName());
                    if (aggrTable != null) {
                        for (Attribute aggrAttribute : aggrTable.getAttributes()) {
                            if (aggrAttribute.getName().endsWith("aggr")) {
                                Attribute fk = conceptAttr.getFkRefTo();
                                addForeignKeyConstraint(aggrTable.getName(),
                                        aggrAttribute.getName(), fk.getTable().getName(),
                                        fk.getName());
                            }
                        }
                    }
                }
            }
            if (t.hasTableType(DBUtil.FACT_OBJECT_OBJECT_TABLE)) {
                Attribute objectAttr1 = null;
                Attribute objectAttr2 = null;
                for (Attribute a : t.getAttributes()) {
                    if (a.getFkRefTo() != null && a.getFkRefTo().getTable()
                            .hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)) {
                        objectAttr1 = a;
                        for (Attribute b : t.getAttributes()) {
                            if (b.getFkRefTo() != null && b.getFkRefTo().getTable()
                                    .hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)) {
                                objectAttr2 = b;
                                if (objectAttr1.getName().compareTo(objectAttr2.getName()) != 0) {
                                    Table aggrTable = getTable("_aggr_aoo_" + t.getName() + "_"
                                            + objectAttr1.getName() + "to" + objectAttr2.getName());
                                    if (aggrTable != null) {
                                        for (Attribute aggrAttribute : aggrTable.getAttributes()) {
                                            if (aggrAttribute.getName().endsWith("aggr")) {
                                                Attribute fk = objectAttr2.getFkRefTo();
                                                addForeignKeyConstraint(aggrTable.getName(),
                                                        aggrAttribute.getName(),
                                                        fk.getTable().getName(), fk.getName());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
