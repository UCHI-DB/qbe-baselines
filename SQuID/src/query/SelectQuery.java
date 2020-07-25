package query;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import dbms.Attribute;
import dbms.AttributeWithTable;
import dbms.Table;
import dbms.TableWithAlias;
import util.DBUtil;

public class SelectQuery {
    private Set<Table> tables;
    private Vector<DisjunctiveSelectionCondition> conjunctiveSelectionConditions;
    private Vector<JoinCondition> joinConditions;
    private Vector<AttributeWithTable> projectColumns;
    private Vector<Attribute> orderBy;
    private Vector<Attribute> groupBy;
    private Vector<Attribute> notNullAttributes;
    private boolean isDistinct;
    private int limit;
    private int offset;
    private Vector<String> functionOnProjection;
    private Vector<Boolean> isDistinctProjection;
    private Vector<String> functionOnOrderBy;

    public SelectQuery() {
        tables = new HashSet<>();
        conjunctiveSelectionConditions = new Vector<>();
        joinConditions = new Vector<>();
        projectColumns = new Vector<>();
        orderBy = new Vector<>();
        groupBy = new Vector<>();
        notNullAttributes = new Vector<>();
        functionOnProjection = new Vector<>();
        isDistinctProjection = new Vector<>();
        functionOnOrderBy = new Vector<>();
        isDistinct = false;
        limit = -1;
        offset = -1;
    }

    public void addTable(Table t) {
        tables.add(t);
    }

    public void addConjunctiveSelectionCondition(DisjunctiveSelectionCondition jc) {
        if (jc.size() > 0) {
            // don't add empty disjunctive selection condition
            conjunctiveSelectionConditions.add(jc);
        }
    }

    public void addProjectColumn(Attribute pc) {
        projectColumns.add(new AttributeWithTable(pc, pc.getTable()));
        functionOnProjection.add(null);
        isDistinctProjection.add(false);
    }

    public void addProjectColumn(Attribute pc, String functionString, boolean isDistinctProj) {
        projectColumns.add(new AttributeWithTable(pc, pc.getTable()));
        functionOnProjection.add(functionString);
        isDistinctProjection.add(isDistinctProj);
    }

    public Set<Table> getTables() {
        return tables;
    }

    public Vector<AttributeWithTable> getProjectColumns() {
        return projectColumns;
    }

    public Vector<JoinCondition> getJoinConditions() {
        return joinConditions;
    }

    public void setDistinct(boolean isDistinct) {
        this.isDistinct = isDistinct;
    }

    public String getQueryString() {
        String queryStr = "SELECT ";
        if (isDistinct) {
            queryStr += "DISTINCT ";
        }
        if (projectColumns.size() == 0) {
            queryStr += "* ";
        }
        boolean first = true;
        for (AttributeWithTable attrTable : projectColumns) {
            if (!first) {
                queryStr += ", ";
            }
            first = false;
            String function = functionOnProjection.get(projectColumns.indexOf(attrTable));
            boolean isDistinctProj = isDistinctProjection.get(projectColumns.indexOf(attrTable));
            if (function == null) {
                queryStr += attrTable.getTable().getName() + "." + attrTable.getName();
            } else {
                queryStr += DBUtil.applyFunction(function,
                        attrTable.getTable().getName() + "." + attrTable.getName(), isDistinctProj);
            }
        }
        queryStr += " FROM ";
        first = true;
        for (Table tab : tables) {
            if (!first) {
                queryStr += ", ";
            }
            first = false;

            if (tab instanceof TableWithAlias) {
                // alias
                queryStr += ((TableWithAlias) tab).getTable().getName() + " "
                        + ((TableWithAlias) tab).getName();
            } else {
                queryStr += tab.getName();
            }

        }
        if (conjunctiveSelectionConditions.size() != 0 || joinConditions.size() != 0
                || notNullAttributes.size() != 0) {
            queryStr += " WHERE ";
        }
        first = true;
        if (conjunctiveSelectionConditions.size() > 0) {

            for (DisjunctiveSelectionCondition cond : conjunctiveSelectionConditions) {
                if (!first) {
                    queryStr += " AND ";
                }
                first = false;
                queryStr += "(" + cond.getDisjunctiveSelectionConditionStr() + ")";
            }
        }
        if (joinConditions.size() > 0) {
            for (JoinCondition cond : joinConditions) {
                if (!first) {
                    queryStr += " AND ";
                }
                first = false;
                queryStr += cond.getJoinConditionStr();
            }
        }
        if (notNullAttributes.size() > 0) {
            for (Attribute attr : notNullAttributes) {
                if (!first) {
                    queryStr += " AND ";
                }
                first = false;
                queryStr += attr.getTable().getName() + "." + attr.getName() + " IS NOT NULL ";
            }
        }

        if (groupBy.size() > 0) {
            queryStr += " GROUP BY ";
            first = true;
            for (Attribute attr : groupBy) {
                if (!first) {
                    queryStr += ",";
                }
                first = false;
                queryStr += attr.getName();
            }
        }
        if (orderBy.size() > 0) {
            queryStr += " ORDER BY ";
            first = true;

            for (Attribute attr : orderBy) {
                if (!first) {
                    queryStr += ", ";
                }
                first = false;
                String function = functionOnOrderBy.get(orderBy.indexOf(attr));
                if (function == null) {
                    queryStr += attr.getName();
                } else {
                    queryStr += DBUtil.applyFunction(function, attr.getName(), false);
                }
            }
        }

        if (limit != -1) {
            queryStr += " LIMIT " + limit;
        }
        if (offset != -1) {
            queryStr += " OFFSET " + offset;
        }
        return queryStr;
    }

    public void addJoinCondition(JoinCondition jc) {
        for (JoinCondition j : joinConditions) {
            if (j.isEqual(jc)) {
                return;
            }
        }
        joinConditions.add(jc);
    }

    public void addOrderBy(Attribute attr, String function) {
        orderBy.add(attr);
        functionOnOrderBy.add(function);
    }

    public void addGroupBy(Attribute attr) {
        groupBy.add(attr);
    }

    public Vector<Vector<String>> getResult() {
        Query q = new Query(getQueryString());
        q.executeQuery();
        return q.getResult();
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void addIsNotNullColumn(Attribute attribute) {
        notNullAttributes.add(attribute);
    }

    public void addProjectColumn(Attribute pc, Table table) {
        this.projectColumns.add(new AttributeWithTable(pc, table));
    }
}
