package concept;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import dbms.Attribute;
import dbms.AttributeWithTable;
import plume.Pair;
import query.DisjunctiveSelectionCondition;
import query.SelectQuery;
import query.SelectionCondition;

class SampleConcept {

    /*
     * this class will return the concept associated with few objects specified by fksToObject
     */

    // Input:
    protected Attribute objectFkAttribute; // FOC.fk attribute to DO table. Refers to object.
    protected Attribute conceptConditionAttribute; // DC.conceptAttribute
    protected Vector<String> toObjectFks; // fk values for focObjectFkAttribute

    // Output:
    protected Attribute focConceptFkAttribute; // FOC.fk attribute to DC table. Refers to concepts.
    protected Set<String> toConceptFks; // fk value for conceptAttributeFk

    SampleConcept(Attribute objectFkAttribute, Attribute conceptConditionAttribute,
            Vector<String> fksToObject) {
        this.objectFkAttribute = objectFkAttribute;
        this.conceptConditionAttribute = conceptConditionAttribute;
        if (this.objectFkAttribute instanceof AttributeWithTable) {
            this.objectFkAttribute = ((AttributeWithTable) this.objectFkAttribute).getAttribute();
        }
        if (this.conceptConditionAttribute instanceof AttributeWithTable) {
            this.conceptConditionAttribute = ((AttributeWithTable) this.conceptConditionAttribute)
                    .getAttribute();
        }
        this.toObjectFks = fksToObject;
    }

    void computeConceptSampleValue() {
        SelectQuery sq = new SelectQuery();
        sq.setDistinct(true);
        DisjunctiveSelectionCondition dsc = new DisjunctiveSelectionCondition();
        for (String toObjectFk : toObjectFks) {
            // any of these could be the intended object, so we consider disjunction over them,
            // and present ALL concepts associated with ANY of them
            dsc.add(new SelectionCondition(objectFkAttribute, toObjectFk));
        }
        sq.addConjunctiveSelectionCondition(dsc);

        if (conceptConditionAttribute == null) {
            // finding concept from FOC table, concept attribute column is not specified
            sq.addTable(objectFkAttribute.getTable());
            focConceptFkAttribute = objectFkAttribute.getTable().getConceptAttribute();
            sq.addProjectColumn(focConceptFkAttribute);
        } else if (conceptConditionAttribute != null) {
            // finding concept from Aggr_FOO table
            sq.addTable(conceptConditionAttribute.getTable());
            sq.addProjectColumn(conceptConditionAttribute);
            sq.addIsNotNullColumn(conceptConditionAttribute);
        }
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

    public Attribute getFocConceptFkAttribute() {
        return focConceptFkAttribute;
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
