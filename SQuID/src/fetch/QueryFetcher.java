package fetch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import concept.ConceptAttribute;
import concept.DerivedConceptAttribute;
import dbms.Attribute;
import dbms.AttributeWithTable;
import dbms.Table;
import dbms.TableColPK;
import dbms.TableWithAlias;
import query.DisjunctiveSelectionCondition;
import query.Query;
import query.SelectQuery;
import query.SelectionCondition;
import util.DBUtil;
import util.Util;

public class QueryFetcher {
    private ExampleTable et;
    private Vector<Vector<TableColPK>> tableMappings;
    private Vector<CandidateQuery> candidateQueries;
    private Vector<MappedTable> joinTables;
    private Vector<Vector<TableColPK>> tempTableMappings;
    Map<String, Vector<TableColPK>> tableColPkMap;
    private Vector<Vector<String>> precomputedJoinTableConceptAttributesResult;

    public QueryFetcher(ExampleTable et) {
        this.et = et;
        findTableMappings();
        exploreDifferentTableMappings(new Vector<TableColPK>(), 0);
    }

    /**
     * find table:column mapping for each columns of the example table. Find only those that
     * "supports" all the rows for each column.
     */
    private void findTableMappings() {
        tableMappings = new Vector<>();
        for (int j = 0; j < et.getColSize(); j++) {
            Vector<TableColPK> tableMappingsForThisColumn = new Vector<>();
            Vector<String> keys = new Vector<>();
            for (int i = 0; i < et.getRowSize(); i++) {
                keys.add(et.getElement(i, j));
            }
            computeValidTabCol(keys);
            for (int i = 0; i < et.getRowSize(); i++) {
                Vector<TableColPK> curTableMappingsForThisColumn = tempTableMappings.elementAt(i);
                if (i == 0) {
                    tableMappingsForThisColumn = curTableMappingsForThisColumn;
                } else {
                    // find intersection
                    Vector<TableColPK> tempTableMappingsForThisColumn = new Vector<>();
                    for (TableColPK tcpEarlier : tableMappingsForThisColumn) {
                        for (TableColPK tcpNow : curTableMappingsForThisColumn) {

                            TableColPK merged = tcpEarlier.merge(tcpNow);
                            if (merged != null) {
                                tempTableMappingsForThisColumn.add(merged);
                            }
                        }
                    }
                    tableMappingsForThisColumn = tempTableMappingsForThisColumn;
                }
            }
            tableMappings.add(tableMappingsForThisColumn);
        }
        candidateQueries = new Vector<>();
    }

    /**
     * @param tableColPk
     * @param idx
     *            Try all combinations of table:column mapping
     */
    private void exploreDifferentTableMappings(Vector<TableColPK> tableColPk, int idx) {
        if (idx == tableMappings.size()) {
            computeCandidateQueries(tableColPk);
            return;
        }
        for (TableColPK tcp : tableMappings.get(idx)) {
            int curIdx = tableColPk.size();
            tableColPk.add(tcp);
            exploreDifferentTableMappings(tableColPk, idx + 1);
            tableColPk.remove(curIdx);
        }
    }

    /**
     * @param tableColPk
     *            compute join tables for a given table:col mapping. compute candidate queries and
     *            corresponding concepts residing in the base table. the base table denotes
     *            joinTable for multiple columns and object dimension table for single column
     */
    private void computeCandidateQueries(Vector<TableColPK> tableColPk) {
        Vector<Attribute> primaryKeyAttributes = new Vector<>();
        Vector<Table> baseTables = new Vector<>();
        Vector<Table> aliasedTables = new Vector<>();
        Vector<Attribute> projectColumns = new Vector<>();
        for (TableColPK tcp : tableColPk) {
            primaryKeyAttributes.add(tcp.attr.getTable().getPrimaryKey());
            Table baseTable = new TableWithAlias(tcp.attr.getTable(), DBUtil.getNextAlias());
            baseTables.add(baseTable);
            aliasedTables.add(baseTable);
            projectColumns.add(new AttributeWithTable(tcp.attr, baseTable));
        }

        joinTables = new Vector<>();
        calculateJoinTables(null, primaryKeyAttributes, 0);
        if (tableColPk.size() == 1) {
            // special case, only one attribute and one project column. therefore, no need for
            // intermediate join tables. In this case, join table will be the base table and that's
            // why base table will have no table in it as they are already included in the join
            // table.
            baseTables.removeAllElements();
        }

        for (MappedTable jt : joinTables) {
            for (Table baseTable : aliasedTables) {
                if (jt.table.equals(((TableWithAlias) baseTable).getTable())) {
                    jt.table = baseTable;
                }
            }
            if (!(jt.table instanceof TableWithAlias)) {
                jt.table = new TableWithAlias(jt.table, DBUtil.getNextAlias());
                aliasedTables.addElement(jt.table);
            }
            Vector<ConceptAttribute> baseConceptAttributes = new Vector<>();
            Vector<DerivedConceptAttribute> derivedConceptAttributes = new Vector<>();
            computeJoinTableConceptAttributes(tableColPk, jt);
            for (int etRowIdx = 0; etRowIdx < et.getRowSize(); etRowIdx++) {
                Vector<ConceptAttribute> curBaseConceptAttributes = getJoinTableConceptAttributes(
                        tableColPk, jt, etRowIdx);
                baseConceptAttributes = merge(baseConceptAttributes, curBaseConceptAttributes);
            }
            if (baseConceptAttributes != null) { // only if this join table contains rows for the
                                                 // actual values given in the example table

                // Remove concept attributes that has no concepts
                Vector<ConceptAttribute> finalBaseConceptAttributes = new Vector<>();
                for (ConceptAttribute ca : baseConceptAttributes) {
                    if (ca.getUnionedConcepts().size() > 0) {
                        finalBaseConceptAttributes.add(ca);
                    }
                }
                baseConceptAttributes = finalBaseConceptAttributes;
                CandidateQuery cq = new CandidateQuery(jt.table, baseTables, projectColumns,
                        et.getRowSize());
                cq.setBaseConceptAttributes(baseConceptAttributes);
                for (int idx = 0; idx < baseConceptAttributes.size(); idx++) {
                    Attribute objectIdAttribute = baseConceptAttributes.get(idx)
                            .getConceptConditionAttribute();
                    if (objectIdAttribute != null && objectIdAttribute.isPrimaryAttribute()) {
                        // DO concepts already included once
                        ArrayList<Vector<String>> keys = new ArrayList<>();
                        for (String item : baseConceptAttributes.get(idx).getUnionedConcepts()) {
                            TableColPK tempTcp = getTableColPK(objectIdAttribute, item);
                            Vector<String> tempKeys = new Vector<>();
                            for (Integer p : tempTcp.pks.get(0)) {
                                tempKeys.add(p.toString());
                            }
                            keys.add(tempKeys);
                        }

                        Table objectPrimaryKeyAttributeTable = objectIdAttribute.getTable()
                                .getPrimaryKey().getTable();
                        for (Table baseTable : aliasedTables) {
                            if (objectPrimaryKeyAttributeTable
                                    .equals(((TableWithAlias) baseTable).getTable())) {
                                objectPrimaryKeyAttributeTable = baseTable;
                            }
                        }
                        Attribute objectPrimaryKeyAttribute = new AttributeWithTable(
                                objectIdAttribute.getTable().getPrimaryKey(),
                                objectPrimaryKeyAttributeTable);
                        DerivedConceptAttribute now = new DerivedConceptAttribute(
                                objectPrimaryKeyAttribute, keys);
                        derivedConceptAttributes.add(now);
                    } else if (objectIdAttribute != null && objectIdAttribute.getFkRefTo() != null
                            && objectIdAttribute.getFkRefTo().getTable()
                                    .hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)) {
                        Vector<String> keys = new Vector<>();
                        for (String item : baseConceptAttributes.get(idx).getCommonConcepts()) {
                            keys.add(item);
                        }
                        Table objectPrimaryKeyAttributeTable = objectIdAttribute.getFkRefTo()
                                .getTable();
                        for (Table baseTable : aliasedTables) {
                            if (objectPrimaryKeyAttributeTable
                                    .equals(((TableWithAlias) baseTable).getTable())) {
                                objectPrimaryKeyAttributeTable = baseTable;
                            }
                        }
                        Attribute objectPrimaryKeyAttribute = new AttributeWithTable(
                                objectIdAttribute.getFkRefTo().getTable().getPrimaryKey(),
                                objectPrimaryKeyAttributeTable);
                        DerivedConceptAttribute now = new DerivedConceptAttribute(
                                objectPrimaryKeyAttribute, keys);
                        derivedConceptAttributes.add(now);
                    }
                }
                cq.setDerivedConceptAttribute(derivedConceptAttributes);
                candidateQueries.add(cq);
            }
        }
    }

    /**
     * Computes whatever getJoinTableConceptAttributes does, but in batch mode. This will store the
     * whole batch result and provide only required portion to getJoinTableConceptAttributes
     * 
     * @param tableColPk
     */
    private void computeJoinTableConceptAttributes(Vector<TableColPK> currentTableColPKs,
            MappedTable jt) {
        precomputedJoinTableConceptAttributesResult = new Vector<>();
        SelectQuery selectQuery = new SelectQuery();
        selectQuery.setDistinct(true);
        selectQuery.addProjectColumn(jt.attributeMapping.elementAt(0)); // project PK to distinguish
                                                                        // desired result
        for (Attribute attr : jt.table.getAttributes()) {
            if (!attr.isPrimaryKey()) {
                selectQuery.addProjectColumn(attr);
            }
        }
        selectQuery.addTable(((TableWithAlias) jt.table).getTable());
        int attrCnt = 0;
        for (Attribute attr : jt.attributeMapping) {
            DisjunctiveSelectionCondition disjunctiveCondition = new DisjunctiveSelectionCondition();
            for (int etIndex = 0; etIndex < et.getRowSize(); etIndex++) {
                for (Integer pk : currentTableColPKs.elementAt(attrCnt).pks.get(etIndex)) {
                    SelectionCondition condition = new SelectionCondition(attr, pk.toString());
                    disjunctiveCondition.add(condition);
                }
            }
            attrCnt++;
            selectQuery.addConjunctiveSelectionCondition(disjunctiveCondition);
        }
        precomputedJoinTableConceptAttributesResult = selectQuery.getResult();
    }

    private TableColPK getTableColPK(Attribute objectIdAttribute, String item) {
        for (TableColPK tcp : tableColPkMap.get(convertLowerCase(item))) {
            if (objectIdAttribute.isEqual(tcp.attr)) {
                return tcp;
            }
        }
        return null;
    }

    /**
     * @param possibleJoinTables
     * @param attributes
     * @param idx
     *            find out valid join tables/dimension tables that can be treated as base (join)
     *            table
     */
    private void calculateJoinTables(Vector<Table> possibleJoinTables, Vector<Attribute> attributes,
            int idx) {
        if (attributes.size() == 1) {
            // single column ET, special case. Return some object dimension table for this.
            Vector<Attribute> attributeMapping = new Vector<>();
            attributeMapping.add(attributes.get(0));
            joinTables.add(new MappedTable(attributes.get(0).getTable(), attributeMapping));
            return;
        }
        if (idx == attributes.size()) {
            for (Table tab : possibleJoinTables) {
                Vector<Attribute> attributeMapping = new Vector<>();
                for (Attribute attr : attributes) {
                    attributeMapping.add(attr.getFkAttributeFromTable(tab));
                }
                joinTables.add(new MappedTable(tab, attributeMapping));
            }
        } else if (idx == 0) {
            Vector<Table> newPossibleJoinTables = new Vector<>();
            for (Attribute attr : attributes.get(idx).getFkRefFromAttrs()) {
                newPossibleJoinTables.add(attr.getTable());
            }
            calculateJoinTables(newPossibleJoinTables, attributes, idx + 1);
        } else {
            Vector<Table> newPossibleJoinTables = new Vector<>();
            for (Attribute attr : attributes.get(idx).getFkRefFromAttrs()) {
                for (Table tab : possibleJoinTables) {
                    if (tab.equals(attr.getTable())) {
                        newPossibleJoinTables.add(tab);
                    }
                }
            }
            calculateJoinTables(newPossibleJoinTables, attributes, idx + 1);
        }
    }

    /**
     * @param bca
     * @param curBca
     * @return merge two concept attributes. Basically merges the concepts only.
     */
    private Vector<ConceptAttribute> merge(Vector<ConceptAttribute> bca,
            Vector<ConceptAttribute> curBca) {
        if (bca == null || curBca == null) {
            return null;
        }
        if (bca.size() == 0) {
            return curBca;
        }
        if (curBca.size() == 0) {
            return bca;
        }
        Vector<ConceptAttribute> ret = new Vector<>();
        for (int i = 0; i < bca.size(); i++) {
            for (int j = 0; j < curBca.size(); j++) {
                if ((bca.get(i).getConceptConditionAttribute())
                        .isEqual(curBca.get(j).getConceptConditionAttribute())) {
                    // the refers to the same concept
                    ConceptAttribute now = (Util.getProperConceptAttribute(bca.get(i)));
                    now.merge(Util.getProperConceptAttribute(curBca.get(j)));
                    ret.add(now);
                }
            }
        }
        return ret;
    }

    /**
     * @param currentTableColPKs
     * @param jt
     * @param etIndex
     * @return the concept attributes residing in the mapped table for etIndex row of the example
     *         table only
     */
    private Vector<ConceptAttribute> getJoinTableConceptAttributes(
            Vector<TableColPK> currentTableColPKs, MappedTable jt, int etIndex) {
        Vector<ConceptAttribute> tempBaseConceptAttributes = new Vector<>();
        Vector<ConceptAttribute> currentBaseConceptAttributes = new Vector<>();

        for (Attribute attr : jt.table.getAttributes()) {
            if (!attr.isPrimaryKey()) {
                ConceptAttribute now = new ConceptAttribute(new AttributeWithTable(attr, jt.table));
                tempBaseConceptAttributes.add(now);
            }
        }

        Vector<Vector<String>> result = getPrecomputedJoinTableConceptAttributesselectQueryResult(
                currentTableColPKs, etIndex);
        if (result.size() == 0) {
            // not a valid example tuple
            return null;
        }
        if (tempBaseConceptAttributes.size() > 0) {
            for (int colNo = 0; colNo < result.get(0).size(); colNo++) {
                Vector<String> concepts = new Vector<String>();
                for (Vector<String> row : result) {
                    if (row.get(colNo) == null || row.get(colNo).isEmpty()) {
                        continue;
                    }
                    concepts.add(row.get(colNo));
                }
                Set<String> conceptSet = new HashSet<String>();
                conceptSet.addAll(concepts);
                concepts.clear();
                concepts.addAll(conceptSet);
                tempBaseConceptAttributes.get(colNo).setConcepts(concepts);
            }
        }
        for (ConceptAttribute ca : tempBaseConceptAttributes) {
            if (ca.getCommonConcepts().size() > 0) {
                currentBaseConceptAttributes.add(ca);
            }
        }
        return currentBaseConceptAttributes;
    }

    /**
     * @param currentTableColPKs
     * @param etIndex
     * @return relevant rows from the already precomputed batch query
     */
    private Vector<Vector<String>> getPrecomputedJoinTableConceptAttributesselectQueryResult(
            Vector<TableColPK> currentTableColPKs, int etIndex) {
        Vector<Vector<String>> result = new Vector<>();
        for (Vector<String> row : precomputedJoinTableConceptAttributesResult) {
            for (Integer pk : currentTableColPKs.elementAt(0).pks.get(etIndex)) {
                if (Integer.parseInt(row.get(0)) == pk) {
                    Vector<String> cleanedRow = new Vector<>();
                    cleanedRow.addAll(row.subList(1, row.size()));
                    result.add(cleanedRow);
                }
            }
        }
        return result;
    }

    /**
     * @return candidate queries
     */
    public Vector<CandidateQuery> getCandidateQueries() {
        return candidateQueries;
    }

    /**
     * @param key
     * @return valid table:column mapping given a search key
     */
    private void computeValidTabCol(Vector<String> keys) {
        tempTableMappings = new Vector<>();
        String keyList = "";
        for (String key : keys) {
            if (keyList.length() > 0) {
                keyList += ", ";
            }
            keyList += "'" + convertLowerCase(key.replace("'", "''")) + "'";
        }
        String checkAllSameTableQueryString = "SELECT DISTINCT tabname, colname from _invertedcolumnindex where lower(word) IN ("
                + keyList + ")";
        Query q = new Query(checkAllSameTableQueryString);
        q.executeQuery();

        Vector<Attribute> validAttr = new Vector<>();

        for (Vector<String> v : q.getResult()) {
            Attribute attr = DBUtil.getDB().getAttribute(v.elementAt(0), v.elementAt(1));
            if (attr.isPrimaryAttribute()) {
                String verifyQuery = "SELECT count(*) FROM _invertedcolumnindex WHERE lower(word) IN ("
                        + keyList + ") AND tabname = '" + v.elementAt(0) + "' AND colname = '"
                        + v.elementAt(1) + "'";
                q = new Query(verifyQuery);
                q.executeQuery();
                if (Integer.parseInt(q.getResult().get(0).get(0)) == keys.size()) {
                    validAttr.add(attr);
                }
            }
        }

        tableColPkMap = new HashMap<String, Vector<TableColPK>>();

        for (Attribute attr : validAttr) {
            Attribute pkAttr = attr.getTable().getPrimaryKey();
            String pkAggregateQueryString = "SELECT lower(" + attr.getName() + "), array_agg("
                    + pkAttr.getName() + ") FROM " + attr.getTable().getName() + " WHERE lower("
                    + attr.getName() + ") IN (" + keyList + ") " + "GROUP BY " + attr.getName();
            Query sq = new Query(pkAggregateQueryString);
            sq.executeQuery();

            for (Vector<String> r : sq.getResult()) {
                String key = r.elementAt(0);
                Vector<Integer> resultPk = new Vector<>();
                String pkAggr = r.elementAt(1);
                pkAggr = pkAggr.replace("{", "");
                pkAggr = pkAggr.replace("}", "");
                String[] pks = pkAggr.split(",");
                for (String pkey : pks) {
                    resultPk.addElement(Integer.parseInt(pkey));
                }
                TableColPK newTcp = new TableColPK(attr, key, resultPk);
                Vector<TableColPK> cur = tableColPkMap.get(key);
                if (cur == null) {
                    cur = new Vector<>();
                }
                cur.add(newTcp);
                tableColPkMap.put(key, cur);
            }
        }

        for (String key : keys) {
            Vector<TableColPK> cur = tableColPkMap.get(key);
            if (cur == null) {
                cur = new Vector<>();
            }
            tempTableMappings.add(cur);
        }
    }

    private String convertLowerCase(String word) {
        String ret = "";
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) >= 'A' && word.charAt(i) <= 'Z') {
                ret += Character.toLowerCase(word.charAt(i));
            } else {
                ret += Character.toLowerCase(word.charAt(i));
            }
        }
        return ret;
    }
}
