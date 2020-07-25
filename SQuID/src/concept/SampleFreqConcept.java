package concept;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import dbms.Attribute;
import dbms.AttributeWithTable;
import plume.Pair;
import query.DisjunctiveSelectionCondition;
import query.SelectQuery;
import query.SelectionCondition;
import util.DBUtil;

class SampleFreqConcept {

    /*
     * this class will return the concept associated with few objects specified by fksToObject
     */

    // Input:
    private Attribute objectFkAttribute; // FOC.fk attribute to DO table.
                                         // Refers to object.
    private Attribute conceptConditionAttribute; // DC.conceptAttribute
    private Vector<String> toObjectFks; // fk values for focObjectFkAttribute

    // Output:
    private Attribute fodcConceptFkAttribute; // FOC.fk attribute to DC table.
                                              // Refers to concepts.
    private Map<String, Pair<Integer, Integer>> conceptFreqMap; // fk, freq map for
                                                                // conceptAttributeFk
    private Vector<Pair<String, String>> conceptValuesMap; // fk, value map
    private Vector<FrequencyConcept> conceptFkValuesFreqs;

    SampleFreqConcept(Attribute objectFkAttribute, Vector<String> fksToObject) {
        this.objectFkAttribute = objectFkAttribute;
        this.conceptConditionAttribute = objectFkAttribute;
        if (this.objectFkAttribute instanceof AttributeWithTable) {
            this.objectFkAttribute = ((AttributeWithTable) this.objectFkAttribute).getAttribute();
        }
        conceptConditionAttribute = objectFkAttribute.getTable().getConceptConditionAttribute();
        this.toObjectFks = fksToObject;
    }

    void computeFreqConceptSampleValue() {
        SelectQuery sq = new SelectQuery();
        sq.addTable(objectFkAttribute.getTable());
        fodcConceptFkAttribute = objectFkAttribute.getTable().getConceptConditionAttribute();
        sq.addProjectColumn(fodcConceptFkAttribute);
        if (DBUtil.FREQUENCY_MODE == "normalized_freq") {
            sq.addProjectColumn(
                    objectFkAttribute.getTable().getFrequencyAttribute("normalized_freq"), "max",
                    false);
        } else {
            sq.addProjectColumn(objectFkAttribute.getTable().getFrequencyAttribute("freq"), "max",
                    false);
        }
        sq.addProjectColumn(objectFkAttribute.getTable().getFrequencyAttribute("freq"), "max",
                false);
        sq.addGroupBy(fodcConceptFkAttribute);

        DisjunctiveSelectionCondition dsc = new DisjunctiveSelectionCondition();

        for (String toObjectFk : toObjectFks) {
            // any of these could be the intended object, so we consider
            // disjunction over them, and present ALL concepts associated with
            // ANY of them so that we can take maximum of the frequencies
            dsc.add(new SelectionCondition(objectFkAttribute, toObjectFk));
        }
        sq.addConjunctiveSelectionCondition(dsc);

        Vector<Vector<String>> result = sq.getResult();

        conceptFreqMap = new HashMap<String, Pair<Integer, Integer>>(); // contains all possible
                                                                        // concept (fk, frequency)
                                                                        // map
        conceptValuesMap = new Vector<>(); // contains all possible concept (fk, values) map
        conceptFkValuesFreqs = new Vector<>(); // contains all possible concept (fk, values,
                                               // frequency) map
        Vector<String> conceptToFks = new Vector<>(); // contains all possible concept fks
        for (Vector<String> row : result) {
            conceptToFks.add(row.elementAt(0));
            conceptFreqMap.put(row.elementAt(0), new Pair<Integer, Integer>(
                    new Integer(row.elementAt(1)), new Integer(row.elementAt(2))));
        }
        conceptValuesMap = fodcConceptFkAttribute.getRealValue(conceptToFks);
        for (Pair<String, String> cv : conceptValuesMap) {
            conceptFkValuesFreqs.add(new FrequencyConcept(cv.a, cv.b, conceptFreqMap.get(cv.a).a,
                    conceptFreqMap.get(cv.a).a, conceptFreqMap.get(cv.a).b,
                    conceptFreqMap.get(cv.a).b));
        }
    }

    public Attribute getFocConceptFkAttribute() {
        return fodcConceptFkAttribute;
    }

    public Vector<FrequencyConcept> getconceptFkValuesFreqs() {
        return conceptFkValuesFreqs;
    }
}
