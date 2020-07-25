package concept;

import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import dbms.Attribute;
import dbms.AttributeWithTable;
import plume.Pair;
import query.DisjunctiveSelectionCondition;
import query.SelectQuery;
import query.SelectionCondition;

class SampleComplexConcept extends SampleConcept {

    /*
     * For multiple object attribute, this class finds relationship concepts in FOO tables
     */

    private Attribute objectFkAttribute2;
    private Vector<String> toObjectFks2;

    SampleComplexConcept(Attribute objectFkAttribute1, Attribute objectFkAttribute2,
            Attribute conceptConditionAttribute, Vector<String> fksToObject1,
            Vector<String> fksToObject2) {
        super(objectFkAttribute1, conceptConditionAttribute, fksToObject1);
        this.objectFkAttribute2 = objectFkAttribute2;
        if (this.objectFkAttribute2 instanceof AttributeWithTable) {
            this.objectFkAttribute2 = ((AttributeWithTable) this.objectFkAttribute2).getAttribute();
        }
        this.toObjectFks2 = fksToObject2;
    }

    void computeConceptSampleValue() {
        SelectQuery sq = new SelectQuery();
        sq.setDistinct(true);
        DisjunctiveSelectionCondition dsc = new DisjunctiveSelectionCondition();
        for (String toObjectFk : toObjectFks) {
            dsc.add(new SelectionCondition(objectFkAttribute, toObjectFk));
        }
        sq.addConjunctiveSelectionCondition(dsc);
        dsc = new DisjunctiveSelectionCondition();
        for (String toObjectFk2 : toObjectFks2) {
            dsc.add(new SelectionCondition(objectFkAttribute2, toObjectFk2));
        }
        sq.addConjunctiveSelectionCondition(dsc);

        sq.addTable(conceptConditionAttribute.getTable());
        sq.addProjectColumn(conceptConditionAttribute);
        sq.addIsNotNullColumn(conceptConditionAttribute);

        toConceptFks = new HashSet<String>(); // contains all possible concept fk values
        Vector<Vector<String>> result = sq.getResult();
        for (Vector<String> row : result) {
            String data = row.elementAt(0);
            if (data.startsWith("{")) {
                // aggregated result
                StringTokenizer st = new StringTokenizer(data, "{,}");
                while (st.hasMoreTokens()) {
                    toConceptFks.add(st.nextToken());
                }
            } else {
                toConceptFks.add(data);
            }
        }
    }

    public Vector<String> getToConceptFks() {
        Vector<String> ret = new Vector<>();
        ret.addAll(toConceptFks);
        return ret;
    }

    /**
     * Get corresponding real values using concept FKs
     **/
    public Vector<Pair<String, String>> getConceptValues(Vector<String> conceptFks) {
        return conceptConditionAttribute.getRealValue(conceptFks);
    }
}
