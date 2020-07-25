package fetch;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import concept.ConceptAttribute;
import concept.DerivedConceptAttribute;
import concept.FrequencyConcept;
import concept.FrequencyConceptAttribute;
import concept.InequalityConceptAttribute;
import dbms.Attribute;
import dbms.AttributeWithTable;
import dbms.Table;
import filter.Filter;
import filter.SimpleConjunctiveFilter;
import filter.SimpleDisjunctiveFilter;
import filter.SimpleFilter;
import filter.SimpleInequalityFilter;
import query.DisjunctiveSelectionCondition;
import query.JoinCondition;
import query.SelectQuery;
import query.SelectionCondition;
import util.DBUtil;
import util.Util;

public class CandidateQuery {
    private Table joinTable;
    private Vector<Table> baseTables;
    private Vector<ConceptAttribute> joinTableConceptAttributes; // assuming one join table
    private Vector<DerivedConceptAttribute> derivedConceptAttributes; // one for each column
    private Vector<Attribute> projectColumns;
    private Set<Attribute> valueBoundObjectPrimarKeyAttributes;
    private SelectQuery selectQuery;
    private Vector<SimpleFilter> filters;
    private int etSize;
    private Set<ConceptAttribute> conceptAttributesToPopulate;

    CandidateQuery(Table joinTable, Vector<Table> baseTables, Vector<Attribute> projectionColumns,
            int etSize) {
        this.joinTable = joinTable;
        this.baseTables = baseTables;
        this.projectColumns = projectionColumns;
        this.filters = new Vector<>();
        this.etSize = etSize;

        joinTableConceptAttributes = new Vector<>();
        derivedConceptAttributes = new Vector<>();
        conceptAttributesToPopulate = new HashSet<>();
    }

    public Set<ConceptAttribute> getConceptAttributesToPopulate() {
        return conceptAttributesToPopulate;
    }

    public void addToConceptAttributesToPopulate(ConceptAttribute ca) {
        conceptAttributesToPopulate.add(ca);
    }

    public void removeFromConceptAttributesToPopulate(ConceptAttribute ca) {
        conceptAttributesToPopulate.remove(ca);
    }

    public void setDerivedConceptAttribute(
            Vector<DerivedConceptAttribute> derivedConceptAttributes) {
        this.derivedConceptAttributes = derivedConceptAttributes;
    }

    public Vector<DerivedConceptAttribute> getDerivedConceptAttributes() {
        return derivedConceptAttributes;
    }

    public void setBaseConceptAttributes(Vector<ConceptAttribute> relationshipConceptAttributes) {
        joinTableConceptAttributes
                .addAll(Util.getProperConceptAttributes(relationshipConceptAttributes));
    }

    public Vector<ConceptAttribute> getBaseConceptAttributes() {
        return joinTableConceptAttributes;
    }

    public Table getJoinTable() {
        return joinTable;
    }

    public Vector<Table> getBaseTables() {
        return baseTables;
    }

    public void computeQuery() {
        valueBoundObjectPrimarKeyAttributes = new HashSet<>();
        selectQuery = new SelectQuery();
        selectQuery.addTable(joinTable);
        for (Attribute pc : projectColumns) {
            selectQuery.addProjectColumn(pc);
        }
        selectQuery.setDistinct(true);
        for (ConceptAttribute ca : conceptAttributesToPopulate) {
            Vector<SelectionCondition> caSelectionConditions = new Vector<>();
            DisjunctiveSelectionCondition caConceptContainmentCondition;
            Vector<JoinCondition> caJoinConditions;
            Vector<Table> caJoinTables;
            if (ca instanceof FrequencyConceptAttribute) {
                FrequencyConceptAttribute fca = (FrequencyConceptAttribute) ca;
                fca.calculateConditions();
                selectQuery.addConjunctiveSelectionCondition(new DisjunctiveSelectionCondition(
                        fca.getConceptEqualityAndFreqCondition()));
                for (JoinCondition jc : fca.getJoinConditions()) {
                    selectQuery.addJoinCondition(jc);
                }
                for (Table t : fca.getJoinTables()) {
                    selectQuery.addTable(t);
                }
            } else {
                if (ca instanceof InequalityConceptAttribute) {
                    if (((InequalityConceptAttribute) ca).isAmbiguous()) {
                        continue;
                    }
                }
                ca.calculateConditions(DBUtil.CONJUNCTION);
                caSelectionConditions = ca.getConceptContainmentCondition()
                        .getSelectionConditions();
                caConceptContainmentCondition = ca.getConceptContainmentCondition();
                caJoinConditions = ca.getJoinConditions();
                caJoinTables = ca.getJoinTables();

                for (SelectionCondition sc : caSelectionConditions) {
                    valueBoundObjectPrimarKeyAttributes.add(sc.getConditionAttribute());
                }
                selectQuery.addConjunctiveSelectionCondition(caConceptContainmentCondition);

                for (JoinCondition jc : caJoinConditions) {
                    selectQuery.addJoinCondition(jc);
                }
                for (Table t : caJoinTables) {
                    selectQuery.addTable(t);
                }
            }

            for (Table bt : baseTables) {
                selectQuery.addTable(bt);
                Attribute joinTableFK = new AttributeWithTable(
                        bt.getPrimaryKey().getFkAttributeFromTable(joinTable), joinTable);
                Attribute baseTablePK = new AttributeWithTable(bt.getPrimaryKey(), bt);
                selectQuery.addJoinCondition(new JoinCondition(baseTablePK, joinTableFK));
            }
        }
    }

    /**
     * Compute filters for this candidate query and rank them according to their F-score
     */
    public void computeFilters() {
        Vector<ConceptAttribute> allConceptAttributes = new Vector<>();
        allConceptAttributes.addAll(getBaseConceptAttributes());

        for (DerivedConceptAttribute dca : getDerivedConceptAttributes()) {
            allConceptAttributes.addAll(dca.getShallowConceptAttributes());
            allConceptAttributes.addAll(dca.getDeepConceptAttributes());
            allConceptAttributes.addAll(dca.getDeepFreqConceptAttributes());
        }

        for (ConceptAttribute ca : allConceptAttributes) {
            Table filterTable = ca.getConceptConditionAttribute().getTable();
            Attribute filterAttribute = ca.getConceptConditionAttribute();
            if (ca instanceof FrequencyConceptAttribute) {
                FrequencyConceptAttribute fca = (FrequencyConceptAttribute) ca;
                fca.setDropped(true);
                for (FrequencyConcept fc : fca.getOutliers()) {
                    Filter lowFilter = DBUtil.getFilter(new Filter(filterTable, filterAttribute,
                            fc.getKey(), fc.getFrequencyMin()));
                    Filter highFilter = DBUtil.getFilter(new Filter(filterTable, filterAttribute,
                            fc.getKey(), fc.getFrequencyMax()));
                    SimpleInequalityFilter inequalityFilter = new SimpleInequalityFilter(lowFilter,
                            highFilter, etSize);
                    if (inequalityFilter.getFScore() >= 1) {
                        conceptAttributesToPopulate.add(fca);
                        fca.setDropped(false);
                        fc.setDropped(false);
                    } else {
                        fc.setDropped(true);
                        fc.setDroppedDueToLowFScore();
                    }
                    fc.setConfidence(inequalityFilter.getFScore());
                    filters.add(inequalityFilter);
                }
                for (FrequencyConcept fc : fca.getFrequencyConcepts()) {
                    Filter lowFilter = DBUtil.getFilter(new Filter(filterTable, filterAttribute,
                            fc.getKey(), fc.getFrequencyMin()));
                    Filter highFilter = DBUtil.getFilter(new Filter(filterTable, filterAttribute,
                            fc.getKey(), fc.getFrequencyMax()));
                    SimpleInequalityFilter inequalityFilter = new SimpleInequalityFilter(lowFilter,
                            highFilter, etSize);
                    fc.setConfidence(inequalityFilter.getFScore());
                }
            } else {
                SimpleDisjunctiveFilter disjunctiveFilter = new SimpleDisjunctiveFilter(etSize);
                SimpleConjunctiveFilter conjunctiveFilter = new SimpleConjunctiveFilter(etSize);
                SimpleFilter collectiveFilter;
                Vector<String> commonConcepts;
                if (ca instanceof InequalityConceptAttribute) {
                    ((InequalityConceptAttribute) ca).tryDisambiguate();
                    commonConcepts = ca.getCommonConcepts(); // First try for strict semantic
                                                             // similarity, if not found, relax
                    if (commonConcepts.size() == 0) {
                        commonConcepts = ca.getUnionedConcepts();
                    }
                } else {
                    commonConcepts = ca.getCommonConcepts();
                }
                if (ca.isAggregateConcept()) {
                    collectiveFilter = conjunctiveFilter;
                } else {
                    collectiveFilter = disjunctiveFilter;
                }
                if (ca instanceof InequalityConceptAttribute) {
                    collectiveFilter.setContinuousFilter(true);
                }
                for (String concept : commonConcepts) {
                    Filter rawFilter = new Filter(filterTable, filterAttribute, concept);
                    Filter newFilter = DBUtil.getFilter(rawFilter);

                    if (newFilter != null) {
                        collectiveFilter.addFilter(newFilter);
                    } else {
                        collectiveFilter.addFilter(rawFilter); // does not have selectivity score
                                                               // computed
                        rawFilter.computeInverseSelectivity();
                    }
                }

                if (collectiveFilter.getFilters().size() > 0) {
                    if (collectiveFilter.getFScore() >= 1) {
                        conceptAttributesToPopulate.add(ca);
                    } else {
                        ca.setDropped(true);
                        ca.setDroppedDueToLowFScore();
                    }
                    ca.setConfidence(collectiveFilter.getFScore());
                    filters.add(collectiveFilter);
                }
            }
        }
        Collections.sort(filters, new Comparator<SimpleFilter>() {
            public int compare(SimpleFilter f1, SimpleFilter f2) {
                return Double.compare(f2.getFScore(), f1.getFScore());
            }
        });
        Vector<Double> fScores = new Vector<>();
        for (SimpleFilter f : filters) {
            String dropMessage = "";

            if (f.getFScore() < 1) {
                dropMessage = " (Dropped due to Low F-Score)";
            } else {
                fScores.add(f.getFScore());
            }
            System.out.println("FILTER: " + f + " " + f.getFScore() + dropMessage);
        }
        /*
         * System.out.print("FILTER SCORES:"); for (double f : fScores) { System.out.print(f + ":");
         * } System.out.println("");
         */

        // System.out.println("MIN FSCORE: " + Collections.min(fScores));
        // System.out.println("NUMBER OF FILTERS: " + fScores.size());
    }

    public SelectQuery getQuery() {
        try {
            computeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return selectQuery;
    }
}
