package concept;

import java.util.Comparator;
import java.util.Vector;

import org.jaxen.saxpath.Operator;

import dbms.Attribute;
import dbms.AttributeWithTable;
import dbms.Table;
import dbms.TableWithAlias;
import query.JoinCondition;
import query.SelectionCondition;
import util.DBUtil;

/**
 * this class is for deep derived frequency based concept attributes.
 */
public class FrequencyConceptAttribute extends ConceptAttribute {
    SampleFreqConcept sampleFreqConcept;
    protected SelectionCondition conceptEqualityAndFreqCondition;
    protected Vector<FrequencyConcept> freqConcepts; // key value frequency triplet for concepts
    protected Vector<FrequencyConcept> selectedFreqConcepts; // Finally selected freq concepts
    private ByFrequency byFrequency;
    private Vector<Table> baseJoinTables;

    public FrequencyConceptAttribute(Attribute objectFkAttribute, Vector<String> keysToObject) {
        byFrequency = new ByFrequency();
        joinTables = new Vector<>();
        baseJoinTables = new Vector<>();
        freqConcepts = new Vector<>();
        fkAttribute = objectFkAttribute;
        if (objectFkAttribute instanceof AttributeWithTable) {
            joinTable = ((AttributeWithTable) objectFkAttribute).getTable();
        } else {
            joinTable = new TableWithAlias(objectFkAttribute.getTable(), DBUtil.getNextAlias());
        }
        baseJoinTables.add(joinTable);
        joinTables.add(joinTable);
        conceptConditionAttribute = new AttributeWithTable(
                objectFkAttribute.getTable().getConceptConditionAttribute(), joinTable);
        if (conceptConditionAttribute.getFkRefTo() == null) {
            conceptName = conceptConditionAttribute.getName();
        } else {
            conceptName = conceptConditionAttribute.getFkRefTo().getTable().getRealName();
        }

        sampleFreqConcept = new SampleFreqConcept(objectFkAttribute, keysToObject);
        sampleFreqConcept.computeFreqConceptSampleValue();

        freqConcepts = sampleFreqConcept.getconceptFkValuesFreqs();
        freqConcepts.sort(byFrequency);

        if (freqConcepts.size() > DBUtil.MAX_FREQ_CONCEPT_SIZE) {
            Vector<FrequencyConcept> tempFreqConcepts = new Vector<>();
            tempFreqConcepts.addAll(freqConcepts.subList(0, DBUtil.MAX_FREQ_CONCEPT_SIZE));
            freqConcepts = tempFreqConcepts;
        }

        selectedFreqConcepts = new Vector<>();
    }

    public FrequencyConceptAttribute(FrequencyConceptAttribute another) {
        this.sampleFreqConcept = another.sampleFreqConcept;
        this.conceptName = another.conceptName;
        this.joinTables = another.joinTables;
        this.baseJoinTables = another.baseJoinTables;
        this.joinConditions = another.joinConditions;
        this.conceptEqualityAndFreqCondition = another.conceptEqualityAndFreqCondition;
        this.conceptConditionAttribute = another.conceptConditionAttribute;
        this.fkAttribute = another.fkAttribute;
        this.freqConcepts = new Vector<>();
        this.freqConcepts.addAll(another.freqConcepts);
        this.joinTable = another.joinTable;
        this.byFrequency = another.byFrequency;
    }

    public void merge(FrequencyConceptAttribute another) {
        Vector<FrequencyConcept> newFreqConcepts = new Vector<>();
        for (FrequencyConcept fc : another.getFrequencyConcepts()) {
            for (FrequencyConcept fcnow : getFrequencyConcepts()) {
                if (fc.getKey().equalsIgnoreCase(fcnow.getKey())) {
                    newFreqConcepts.add(new FrequencyConcept(fc.getKey(), fc.getValue(),
                            Math.min(fc.getFrequencyMin(), fcnow.getFrequencyMin()),
                            Math.max(fc.getFrequencyMax(), fcnow.getFrequencyMax()),
                            Math.min(fc.getFrequencyAbsMin(), fcnow.getFrequencyAbsMin()),
                            Math.max(fc.getFrequencyAbsMax(), fcnow.getFrequencyAbsMax())));
                }
            }
        }
        newFreqConcepts.sort(byFrequency);
        this.freqConcepts = newFreqConcepts;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void calculateConditions() {
        joinConditions = new Vector<>();
        joinTables = new Vector<>();
        for (Table curTable : baseJoinTables) {
            joinTables.add(curTable);
        }
        if (fkAttribute != null) {
            joinConditions.add(new JoinCondition(fkAttribute, fkAttribute.getFkRefTo()));
        }
    }

    public Vector<Table> getJoinTables() {
        return joinTables;
    }

    public Vector<JoinCondition> getJoinConditions() {
        return joinConditions;
    }

    public SelectionCondition getConceptEqualityAndFreqCondition() {
        conceptEqualityAndFreqCondition = new SelectionCondition();
        boolean first = true;
        for (FrequencyConcept sc : getSelectedFrequencyConcepts()) {
            SelectionCondition now = new SelectionCondition();
            Table newTable = conceptConditionAttribute.getTable();
            if (!first) {
                newTable = new TableWithAlias(((TableWithAlias) fkAttribute.getTable()).getTable(),
                        DBUtil.getNextAlias());
                joinTables.add(newTable);
                joinConditions.add(new JoinCondition(new AttributeWithTable(fkAttribute, newTable),
                        fkAttribute.getFkRefTo()));
            }
            now.add(new SelectionCondition(
                    new AttributeWithTable(conceptConditionAttribute, newTable), sc.getKey()));
            now.add(new SelectionCondition(
                    new AttributeWithTable(conceptConditionAttribute.getTable()
                            .getFrequencyAttribute(DBUtil.FREQUENCY_MODE), newTable),
                    sc.getFrequencyMin(), Operator.GREATER_THAN_EQUALS));
            if (!DBUtil.FILTER_RELAX_ACTIVE) {
                now.add(new SelectionCondition(
                        new AttributeWithTable(conceptConditionAttribute.getTable()
                                .getFrequencyAttribute(DBUtil.FREQUENCY_MODE), newTable),
                        sc.getFrequencyMax(), Operator.LESS_THAN_EQUALS));
            }
            conceptEqualityAndFreqCondition.add(now);
            first = false;
        }
        return conceptEqualityAndFreqCondition;
    }

    public Attribute getConceptConditionAttribute() {
        return conceptConditionAttribute;
    }

    public Vector<FrequencyConcept> getFrequencyConcepts() {
        return freqConcepts;
    }

    public Vector<FrequencyConcept> getOutliers() {
        Vector<FrequencyConcept> result = new Vector<>();
        double dataSize = freqConcepts.size();
        if (!DBUtil.USE_SKEWNESS || dataSize < 5) {
            // Too few values, there is no way to compute outliers
            result.addAll(freqConcepts);
        } else {
            Double average = (freqConcepts.stream().mapToDouble(a -> a.minFrequency).average())
                    .getAsDouble();
            int median = (freqConcepts.elementAt(freqConcepts.size() / 2)).minFrequency;
            Double std = Math.sqrt((freqConcepts.stream()
                    .mapToDouble(a -> (a.minFrequency - average) * (a.minFrequency - average))
                    .sum()) / (dataSize - 1));
            Double skewness = (freqConcepts.stream()
                    .mapToDouble(a -> Math.pow((a.minFrequency - average), 3)).sum());
            skewness = skewness * dataSize / ((dataSize - 1) * (dataSize - 2) * std * std * std);
            if (skewness > DBUtil.MIN_SKEWNESS) {
                for (FrequencyConcept fc : freqConcepts) {
                    if (fc.minFrequency - median >= DBUtil.OUTLIER_DETECTION_STRENGTH * std) {
                        result.add(fc);
                    }
                }
            }
        }
        for (FrequencyConcept fc : result) {
            if (fc.absoluteMinFrequency > DBUtil.MIN_ASSOCIATION_THRESHOLD) {
                fc.setDropped(false);
            }
        }
        return getSelectedFrequencyConcepts();
    }

    public Vector<FrequencyConcept> getSelectedFrequencyConcepts() {
        selectedFreqConcepts.clear();
        for (FrequencyConcept fc : freqConcepts) {
            if (!fc.isDropped()) {
                selectedFreqConcepts.add(fc);
            }
        }
        return selectedFreqConcepts;
    }
}

class ByFrequency implements Comparator<FrequencyConcept> {
    @Override
    public int compare(FrequencyConcept a, FrequencyConcept b) {
        if (a.getFrequencyMin() > b.getFrequencyMin()) {
            return -1;
        }

        if (a.getFrequencyMin() == b.getFrequencyMin()) {
            if (a.getFrequencyMax() < b.getFrequencyMax()) {
                return -1;
            }
            if (a.getFrequencyMax() == b.getFrequencyMax()) {
                return 0;
            }
            if (a.getFrequencyMax() > b.getFrequencyMax()) {
                return 1;
            }
        }

        return 1;
    }
}
