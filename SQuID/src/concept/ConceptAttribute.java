package concept;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import dbms.Attribute;
import dbms.AttributeWithTable;
import dbms.Table;
import dbms.TableWithAlias;
import plume.Pair;
import query.DisjunctiveSelectionCondition;
import query.JoinCondition;
import query.SelectionCondition;
import util.DBUtil;
import util.QbeeException;
import util.SQLOperator;

public class ConceptAttribute {
    String conceptName;
    Vector<Table> joinTables; // additional join tables required for concepts
    Vector<Table> aggregateJoinTables; // additional join tables required for aggregated concepts
    Vector<JoinCondition> joinConditions; // join conditions for joinTables if required
    DisjunctiveSelectionCondition conceptContainmentCondition;
    Attribute conceptConditionAttribute; // this attribute will be actually used to write an
                                         // equality predicate on a fixed concept
    Attribute conceptConditionAggregateAttribute; // alternatively, this attribute will be used for
                                                  // aggregate concept containment
    Attribute fkAttribute; // fk joining attribute

    Vector<String> commonConcepts; // List of common concepts. The condition will be "WHERE
                                   // conditionAttribute = key"
    Vector<String> unionedConcepts; // store all union concepts

    SampleConcept sampleConcept;
    Table joinTable;
    boolean dropped = false;
    Vector<Pair<Attribute, String>> additionalRelationshipConceptConstraint;
    private Table actualJoinTable;
    private boolean alreadyComputed = false;
    private Attribute ref;
    private double confidence;
    private boolean droppedDueToLowFScore = false;

    ConceptAttribute() {
    }

    ConceptAttribute(Attribute objectFkAttributeOrDOConceptAttribute, Vector<String> keysToObject) {
        /**
         * applicable for finding concepts where objectFkAttributeOrDOConceptAttribute is the
         * foreign key to keysToObject or the table containing it has a primary key
         */
        joinTables = new Vector<>();
        commonConcepts = new Vector<>();
        conceptName = null;

        for (Attribute attr : objectFkAttributeOrDOConceptAttribute.getTable().getAttributes()) {
            if (attr.getFkRefTo() != null
                    && attr.getFkRefTo().getTable().hasTableType(DBUtil.DIMENSION_CONCEPT_TABLE)) {
                if (conceptName == null) {
                    // FOC object FK Attribute
                    conceptName = attr.getFkRefTo().getTable().getRealName();
                    conceptConditionAttribute = new AttributeWithTable(attr,
                            objectFkAttributeOrDOConceptAttribute.getTable());
                } else {
                    // multiple fk attribute to different concepts to different DCs
                    throw (new QbeeException(
                            "Multiple concept attribute foreign keys found from table "
                                    + objectFkAttributeOrDOConceptAttribute.getTable().getName()));
                }
            }
        }

        if (conceptName != null) {
            // FOC object FK Attribute
            fkAttribute = objectFkAttributeOrDOConceptAttribute;
            if (objectFkAttributeOrDOConceptAttribute instanceof AttributeWithTable) {
                joinTable = ((AttributeWithTable) objectFkAttributeOrDOConceptAttribute).getTable();
            } else {
                joinTable = new TableWithAlias(objectFkAttributeOrDOConceptAttribute.getTable(),
                        DBUtil.getNextAlias());
            }
            joinTables.add(joinTable);
            sampleConcept = new SampleConcept(objectFkAttributeOrDOConceptAttribute,
                    objectFkAttributeOrDOConceptAttribute.getTable().getConceptAttribute(),
                    keysToObject);
            sampleConcept.computeConceptSampleValue();
            commonConcepts.addAll(sampleConcept.getToConceptFks());
        } else {
            // DO Concept Attribute
            conceptName = objectFkAttributeOrDOConceptAttribute.getName();
            conceptConditionAttribute = objectFkAttributeOrDOConceptAttribute;
            sampleConcept = new SampleConcept(null, objectFkAttributeOrDOConceptAttribute,
                    keysToObject);
            sampleConcept.computeConceptSampleValue();
            for (int i = 0; i < sampleConcept.getToConceptFks().size(); i++) {
                commonConcepts.add(sampleConcept.getToConceptFks().elementAt(i));
            }
        }
        unionedConcepts = new Vector<>();
        unionedConcepts.addAll(commonConcepts);
    }

    ConceptAttribute(Attribute objectFkAttribute, Attribute conceptConditionAttribute,
            Vector<String> keysToObject) {
        /**
         * applicable for finding concepts where objectFkAttribute is the foreign key to
         * keysToObject and conceptConditionAttribute is the attribute containing concepts or FK to
         * concept and the container table has no PK. So our goal is to find the concepts from the
         * query "Select distinct conceptConditionAttribute from table where objectFkAttribute =
         * keysTOOBject". Finds concepts from FOO table.
         */
        joinTables = new Vector<>();
        commonConcepts = new Vector<>();
        if (conceptConditionAttribute.getFkRefTo() != null) {
            conceptName = conceptConditionAttribute.getTable().getRealName() + ":"
                    + conceptConditionAttribute.getFkRefTo().getTable().getName();
        } else {
            conceptName = conceptConditionAttribute.getName();
        }
        this.conceptConditionAttribute = conceptConditionAttribute;
        this.conceptName = conceptConditionAttribute.getName();
        fkAttribute = objectFkAttribute;
        joinTable = ((AttributeWithTable) objectFkAttribute).getTable();
        joinTables.add(joinTable);
        sampleConcept = new SampleConcept(objectFkAttribute, conceptConditionAttribute,
                keysToObject);
        sampleConcept.computeConceptSampleValue();
        for (int i = 0; i < sampleConcept.getToConceptFks().size(); i++) {
            commonConcepts.add(sampleConcept.getToConceptFks().elementAt(i));
        }

        unionedConcepts = new Vector<>();
        unionedConcepts.addAll(commonConcepts);
    }

    public ConceptAttribute(Attribute conceptConditionAttribute) {
        this.conceptConditionAttribute = conceptConditionAttribute;
        this.conceptName = conceptConditionAttribute.getName();
        this.joinTable = new TableWithAlias(conceptConditionAttribute.getTable(),
                DBUtil.getNextAlias());
        commonConcepts = new Vector<>();
        unionedConcepts = new Vector<>();
        joinTables = new Vector<>();
    }

    public Attribute getConceptConditionAttribute() {
        return conceptConditionAttribute;
    }

    public Attribute getConceptConditionAggregateAttribute() {
        if (conceptConditionAggregateAttribute == null) {
            aggregateJoinTables = new Vector<>();
            if (conceptConditionAttribute != null) {
                joinTable = DBUtil.findAggregateTable(joinTable);
                aggregateJoinTables.add(joinTable);
                conceptConditionAggregateAttribute = new AttributeWithTable(
                        DBUtil.findAggregateConceptCondition(conceptConditionAttribute, joinTable),
                        joinTable);
                Attribute prevFk = fkAttribute.getFkRefTo();
                fkAttribute = new AttributeWithTable(DBUtil.getObjectAttribute(
                        conceptConditionAggregateAttribute, conceptConditionAttribute), joinTable);
                fkAttribute.setFkRefTo(prevFk);
            }
        }
        return conceptConditionAggregateAttribute;
    }

    public Vector<String> getCommonConcepts() {
        return commonConcepts;
    }

    public Vector<Table> getJoinTables() {
        return joinTables;
    }

    public Vector<JoinCondition> getJoinConditions() {
        return joinConditions;
    }

    public DisjunctiveSelectionCondition getConceptContainmentCondition() {
        return conceptContainmentCondition;
    }

    public void calculateConditions(int disjunctionOrConjunction) {
        if (additionalRelationshipConceptConstraint != null) {
            calculateConditionsWithSelfJoin();
            return;
        }
        joinConditions = new Vector<>();
        conceptContainmentCondition = new DisjunctiveSelectionCondition();
        if (disjunctionOrConjunction == DBUtil.CONJUNCTION
                && conceptConditionAttribute.getFkRefTo() != null
                && (conceptConditionAttribute.getFkRefTo().getTable()
                        .hasTableType(DBUtil.DIMENSION_CONCEPT_TABLE)
                        || conceptConditionAttribute.getFkRefTo().getTable()
                                .hasTableType(DBUtil.DIMENSION_OBJECT_TABLE))) {
            conceptConditionAggregateAttribute = getConceptConditionAggregateAttribute();
            if (conceptConditionAggregateAttribute == null) {
                // not possible for conjunction
                disjunctionOrConjunction = DBUtil.DISJUNCTION;
            } else {
                String aggregateArray = "";
                for (String sc : commonConcepts) {
                    if (aggregateArray.isEmpty()) {
                        aggregateArray += "ARRAY[";
                    } else {
                        aggregateArray += ",";
                    }
                    aggregateArray += sc;
                }
                aggregateArray += "]";
                conceptContainmentCondition.add(new SelectionCondition(
                        conceptConditionAggregateAttribute, aggregateArray, SQLOperator.CONTAINS));
                if (fkAttribute != null) {
                    joinConditions.add(new JoinCondition(fkAttribute, fkAttribute.getFkRefTo()));
                }
                joinTables = aggregateJoinTables;
                return;
            }
        }
        // otherwise, perform disjunction
        for (String sc : commonConcepts) {
            conceptContainmentCondition.add(new SelectionCondition(conceptConditionAttribute, sc));
        }
        if (fkAttribute != null) {
            joinConditions.add(new JoinCondition(fkAttribute, fkAttribute.getFkRefTo()));
        }
    }

    /**
     * Computes self join for each commonConcept
     */
    private void calculateConditionsWithSelfJoin() {
        joinConditions = new Vector<>();
        joinTables.clear();
        conceptContainmentCondition = new DisjunctiveSelectionCondition();

        if (!alreadyComputed) {
            ref = fkAttribute.getFkRefTo();
            fkAttribute = fkAttribute.getFkRefTo().getFkAttributeFromTable(actualJoinTable);
            conceptConditionAttribute = conceptConditionAttribute.getFkRefTo()
                    .getFkAttributeFromTable(actualJoinTable);
            alreadyComputed = true;
        }

        SelectionCondition selectionCondition = new SelectionCondition();
        for (String sc : commonConcepts) {
            Table currentJoinTable = new TableWithAlias(actualJoinTable, DBUtil.getNextAlias());
            fkAttribute = new AttributeWithTable(fkAttribute, currentJoinTable);
            joinConditions.add(new JoinCondition(fkAttribute, ref));
            joinTables.add(currentJoinTable);
            conceptConditionAttribute = new AttributeWithTable(conceptConditionAttribute,
                    currentJoinTable);
            selectionCondition.add(new SelectionCondition(conceptConditionAttribute, sc));
            if (DBUtil.USE_CONTEXTUAL_SEMANTIC_SIMILARITY) {
                for (Pair<Attribute, String> additionalConstraint : additionalRelationshipConceptConstraint) {
                    Attribute a = new AttributeWithTable(additionalConstraint.a, currentJoinTable);
                    selectionCondition.add(new SelectionCondition(a, additionalConstraint.b));
                }
            }
        }
        conceptContainmentCondition.add(selectionCondition);
    }

    public void merge(ConceptAttribute ca) {
        // shallow concept or DC concept
        HashSet<String> tempSet = new HashSet<>();
        tempSet.addAll(unionedConcepts);
        tempSet.addAll(ca.getUnionedConcepts());
        unionedConcepts.removeAllElements();
        unionedConcepts.addAll(tempSet);
        computeCommonConcepts(ca.getCommonConcepts());
    }

    /**
     * Compute intersection of allConcepts. In case no intersection exists, compute minimum hitting
     * set
     */
    private void computeCommonConcepts(Vector<String> newConcepts) {
        Vector<String> tryCommon = new Vector<>();
        tryCommon.addAll(commonConcepts);
        tryCommon.retainAll(newConcepts);
        commonConcepts = tryCommon;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConcepts(Vector<String> concepts) {
        commonConcepts = concepts;
        unionedConcepts.removeAllElements();
        unionedConcepts.addAll(concepts);
    }

    public Vector<String> getUnionedConcepts() {
        return unionedConcepts;
    }

    public Vector<Pair<String, String>> getConceptsWithValue(Vector<String> concepts) {
        Vector<Pair<String, String>> ret = new Vector<>();
        try {
            ret = sampleConcept.getConceptValues(concepts);
            return ret;
        } catch (NullPointerException e) {
            // sampleConcept is null, that means there is no DC to look for concept values
        }
        for (String key : concepts) {
            ret.add(new Pair<String, String>(key, key));
        }
        return ret;
    }

    public boolean isAggregateConcept() {
        if (joinTable.hasTableType(DBUtil.AGGR_OBJECT_CONCEPT_TABLE)
                || joinTable.hasTableType(DBUtil.AGGR_OBJECT_OBJECT_TABLE)) {
            return true;
        }
        return false;
    }

    public void setDropped(boolean b) {
        dropped = b;
    }

    public boolean isDropped() {
        return dropped;
    }

    public void computeDeepRelationshipConcept(ArrayList<Vector<String>> allObjectPKs,
            Vector<String> vector) {
        Vector<Table> possibleJoinTable = DBUtil.getReverseAggregateTables(joinTable);
        actualJoinTable = null;
        for (Table t : possibleJoinTable) {
            if (t.hasTableType(DBUtil.FACT_OBJECT_OBJECT_TABLE)) {
                actualJoinTable = t;
                break;
            }
        }
        if (actualJoinTable == null) {
            return;
        }

        Attribute objectAttribute1 = fkAttribute.getFkRefTo()
                .getFkAttributeFromTable(actualJoinTable);
        Attribute objectAttribute2 = conceptConditionAttribute.getFkRefTo()
                .getFkAttributeFromTable(actualJoinTable);

        for (Attribute ca : actualJoinTable.getAttributes()) {
            if (ca.isConceptAttributeOrFKey()) {
                Vector<String> caCommonConcept = null;
                for (Vector<String> fks1 : allObjectPKs) {
                    for (String fks2String : getCommonConcepts()) {
                        Vector<String> fks2 = new Vector<>();
                        fks2.add(fks2String);
                        SampleComplexConcept relationshipConcept = new SampleComplexConcept(
                                objectAttribute1, objectAttribute2, ca, fks1, fks2);
                        relationshipConcept.computeConceptSampleValue();
                        Vector<String> currentConcepts = new Vector<>();
                        currentConcepts.addAll(relationshipConcept.getToConceptFks());
                        if (caCommonConcept == null) {
                            caCommonConcept = currentConcepts;
                        } else {
                            Vector<String> tryCommon = new Vector<>();
                            tryCommon.addAll(caCommonConcept);
                            tryCommon.retainAll(currentConcepts);
                            caCommonConcept = tryCommon;
                        }
                    }
                }
                if (caCommonConcept != null && caCommonConcept.size() == 1) {
                    // allow only one common concept
                    if (additionalRelationshipConceptConstraint == null) {
                        additionalRelationshipConceptConstraint = new Vector<>();
                    }
                    additionalRelationshipConceptConstraint
                            .add(new Pair<Attribute, String>(ca, caCommonConcept.elementAt(0)));
                }
            }
        }
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setDroppedDueToLowFScore() {
        droppedDueToLowFScore = true;
    }

    public boolean isDroppedDueToLowFScore() {
        return droppedDueToLowFScore;
    }
}
