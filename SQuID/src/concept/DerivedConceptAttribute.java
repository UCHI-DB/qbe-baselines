package concept;

import java.util.ArrayList;
import java.util.Vector;

import dbms.Attribute;
import dbms.AttributeWithTable;
import dbms.Table;
import dbms.TableWithAlias;
import util.DBUtil;
import util.Util;

/**
 * For a particular object, compute all its derived concept attributes.
 */
public class DerivedConceptAttribute {
    private Vector<ConceptAttribute> shallowDerivedConceptAttributes;
    private Vector<ConceptAttribute> deepDerivedConceptAttributes;
    private Vector<FrequencyConceptAttribute> deepDerivedFreqConceptAttributes;

    private String name;

    public DerivedConceptAttribute(Attribute objectIdAttribute, Vector<String> objectPKs) {
        // common concepts of all concept attributes should be intersection of allObjectPKs concepts
        this.name = objectIdAttribute.getTable().getRealName();
        ArrayList<Vector<String>> modifiedObjectPKs = new ArrayList<>();
        for (String pk : objectPKs) {
            Vector<String> pks = new Vector<>();
            pks.add(pk);
            modifiedObjectPKs.add(pks);
        }
        computeShallowDerivedConceptAttributes(objectIdAttribute, modifiedObjectPKs, true);
        computeDeepDerivedConceptAttributes(objectIdAttribute, modifiedObjectPKs);
        computeDeepDerivedFreqConceptAttributes(objectIdAttribute, modifiedObjectPKs);
    }

    public DerivedConceptAttribute(Attribute objectIdAttribute,
            ArrayList<Vector<String>> allObjectPKs) {
        // common concepts of all concept attributes should be intersection of union of allObjectPKs
        // concepts
        this.name = objectIdAttribute.getTable().getRealName();
        computeShallowDerivedConceptAttributes(objectIdAttribute, allObjectPKs, false);
        computeDeepDerivedConceptAttributes(objectIdAttribute, allObjectPKs);
        computeDeepDerivedFreqConceptAttributes(objectIdAttribute, allObjectPKs);
    }

    private void computeShallowDerivedConceptAttributes(Attribute objectIdAttribute,
            ArrayList<Vector<String>> allObjectPKs, boolean includeDOConcepts) {
        shallowDerivedConceptAttributes = new Vector<>();
        if (includeDOConcepts) {
            for (Attribute ca : objectIdAttribute.getTable().getAttributes()) {
                if (!ca.isPrimaryKey() && !ca.isPrimaryAttribute()) {
                    ConceptAttribute now = null;
                    for (Vector<String> pks : allObjectPKs) {
                        Attribute caWithTable = new AttributeWithTable(ca,
                                objectIdAttribute.getTable());
                        ConceptAttribute tempNow = Util
                                .getProperConceptAttribute(new ConceptAttribute(caWithTable, pks));
                        if (now == null) {
                            now = tempNow;
                        } else {
                            now.merge(tempNow);
                        }
                    }
                    shallowDerivedConceptAttributes.add(now);
                }
            }
        }
        for (Attribute ca : objectIdAttribute.getFkRefFromAttrs()) {
            if (!ca.getTable().hasTableType(DBUtil.FACT_OBJECT_CONCEPT_TABLE)) {
                // for shallow derived concept attributes, consider only FOC tables
                continue;
            }
            ConceptAttribute now = null;
            ca.setFkRefTo(objectIdAttribute);
            for (Vector<String> pks : allObjectPKs) {
                Attribute caWithTable = new AttributeWithTable(ca,
                        new TableWithAlias(ca.getTable(), DBUtil.getNextAlias()));
                ConceptAttribute tempNow = Util
                        .getProperConceptAttribute(new ConceptAttribute(caWithTable, pks));
                if (now == null) {
                    now = tempNow;
                } else {
                    now.merge(tempNow);
                }
            }
            shallowDerivedConceptAttributes.add(now);
        }
        shallowDerivedConceptAttributes = Util
                .getProperConceptAttributes(shallowDerivedConceptAttributes);
    }

    /**
     * Compute affiliated objects
     */
    private void computeDeepDerivedConceptAttributes(Attribute objectIdAttribute,
            ArrayList<Vector<String>> allObjectPKs) {
        deepDerivedConceptAttributes = new Vector<>();
        for (Attribute objectFkAttribute : objectIdAttribute.getFkRefFromAttrs()) {
            if (!objectFkAttribute.getTable().hasTableType(DBUtil.AGGR_OBJECT_OBJECT_TABLE)) {
                // for deep derived concept attributes, consider only Fact Aggregate Object Object
                // tables. This will give the objects affiliated with the target object
                continue;
            }
            Table curConceptTable = new TableWithAlias(objectFkAttribute.getTable(),
                    DBUtil.getNextAlias());
            AttributeWithTable curObjectFkAttribute = new AttributeWithTable(objectFkAttribute,
                    curConceptTable);

            for (Attribute conceptConditionAttribute : curObjectFkAttribute.getTable()
                    .getAttributes()) {
                if (conceptConditionAttribute.getFkRefTo() != null && conceptConditionAttribute
                        .getFkRefTo() == ((AttributeWithTable) objectIdAttribute).getAttribute()) {
                    // Refers back to the original input, not a concept. This will already be
                    // included as a base concept
                    continue;
                }
                if (conceptConditionAttribute.isCountAttribute()) {
                    // Count attribute for aggregate object object table
                    continue;
                }
                ConceptAttribute now = null;
                for (Vector<String> pks : allObjectPKs) {
                    curObjectFkAttribute.setFkRefTo(objectIdAttribute);
                    AttributeWithTable curConceptConditionAttribute = new AttributeWithTable(
                            conceptConditionAttribute, curObjectFkAttribute.getTable());
                    ConceptAttribute tempNow = new ConceptAttribute(curObjectFkAttribute,
                            curConceptConditionAttribute, pks);
                    if (now == null) {
                        now = tempNow;
                    } else {
                        now.merge(tempNow);
                    }
                }
                if (now.getCommonConcepts().size() > 0) {
                    // If all primary objects associated with multiple secondary objects, find the
                    // common edge labels of that association if exists
                    now.computeDeepRelationshipConcept(allObjectPKs, now.getCommonConcepts());
                }
                deepDerivedConceptAttributes.add(now);
            }
        }
    }

    private void computeDeepDerivedFreqConceptAttributes(Attribute objectIdAttribute,
            ArrayList<Vector<String>> modifiedObjectPKs) {
        deepDerivedFreqConceptAttributes = new Vector<>();
        for (Attribute objectFkAttribute : objectIdAttribute.getFkRefFromAttrs()) {
            if (!objectFkAttribute.getTable()
                    .hasTableType(DBUtil.FACT_OBJECT_DEEP_FREQ_CONCEPT_TABLE)) {
                // for deep derived frequency concept attributes, consider only FODC tables.
                continue;
            }
            Table curConceptTable = new TableWithAlias(objectFkAttribute.getTable(),
                    DBUtil.getNextAlias());
            AttributeWithTable curObjectFkAttribute = new AttributeWithTable(objectFkAttribute,
                    curConceptTable);
            curObjectFkAttribute.setFkRefTo(objectIdAttribute);
            FrequencyConceptAttribute now = null;
            for (Vector<String> pks : modifiedObjectPKs) {
                FrequencyConceptAttribute tempNow = new FrequencyConceptAttribute(
                        curObjectFkAttribute, pks);
                if (now == null) {
                    now = tempNow;
                } else {
                    now.merge(tempNow);
                }
            }
            deepDerivedFreqConceptAttributes.add(now);
        }
    }

    public String getName() {
        return name;
    }

    public Vector<ConceptAttribute> getShallowConceptAttributes() {
        return shallowDerivedConceptAttributes;
    }

    public Vector<ConceptAttribute> getDeepConceptAttributes() {
        return deepDerivedConceptAttributes;
    }

    public Vector<FrequencyConceptAttribute> getDeepFreqConceptAttributes() {
        return deepDerivedFreqConceptAttributes;
    }
}
