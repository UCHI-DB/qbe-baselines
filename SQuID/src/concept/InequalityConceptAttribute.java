package concept;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Vector;

import org.jaxen.saxpath.Operator;

import query.DisjunctiveSelectionCondition;
import query.SelectionCondition;
import util.DBUtil;

public class InequalityConceptAttribute extends ConceptAttribute {
    double low, high;
    boolean isAmbiguous = false;
    Vector<Vector<String>> allConcepts;
    boolean disambiguated = false;
    private boolean calledBefore = false;

    public InequalityConceptAttribute(ConceptAttribute ca) {
        allConcepts = new Vector<>();
        copy(ca);
        initialize();
    }

    void copy(ConceptAttribute ca) {
        this.aggregateJoinTables = ca.aggregateJoinTables;
        this.commonConcepts = ca.commonConcepts;
        this.conceptConditionAggregateAttribute = ca.conceptConditionAggregateAttribute;
        this.conceptConditionAttribute = ca.conceptConditionAttribute;
        this.conceptContainmentCondition = ca.conceptContainmentCondition;
        this.conceptName = ca.conceptName;
        this.fkAttribute = ca.fkAttribute;
        this.joinConditions = ca.joinConditions;
        this.joinTable = ca.joinTable;
        this.joinTables = ca.joinTables;
        this.unionedConcepts = ca.unionedConcepts;
        this.sampleConcept = ca.sampleConcept;
    }

    @Override
    public void merge(ConceptAttribute another) {
        InequalityConceptAttribute other = (InequalityConceptAttribute) another;

        if (!DBUtil.DISAMBIGUATE_ACTIVE) {
            commonConcepts.addAll(other.commonConcepts);
            return;
        }
        if (other.isAmbiguous) {
            // other is ambiguous
            allConcepts.addAll(other.allConcepts);
        }

        else if (this.isAmbiguous && !other.isAmbiguous) {
            // this is ambiguous, so initialize it with other that is not ambiguous
            copy(other);
            this.isAmbiguous = false;
        } else {
            // both are unambiguous
            commonConcepts.addAll(other.commonConcepts);
        }
    }

    private void similarityMerge() {
        if (!DBUtil.DISAMBIGUATE_ACTIVE) {
            for (Vector<String> tempConcepts : allConcepts) {
                for (String p : tempConcepts) {
                    commonConcepts.add(p);
                }
            }
            return;
        }
        // pick one from each concept vector from allConcepts
        for (Vector<String> tempConcepts : allConcepts) {
            String best = "";
            Double minMse = 0.0;
            for (String p : tempConcepts) {
                double mse = 0;
                for (String q : commonConcepts) {
                    mse += Math.pow(Double.parseDouble(p) - Double.parseDouble(q), 2);
                }
                if (best.isEmpty() || minMse > mse) {
                    minMse = mse;
                    best = p;
                }
            }
            commonConcepts.add(best);
        }
    }

    public void initialize() {
        isAmbiguous = false;
        if (commonConcepts.size() != 1) {
            isAmbiguous = true;
            Vector<String> toStore = new Vector<>();
            toStore.addAll(commonConcepts);
            allConcepts.add(toStore);
            commonConcepts.clear();
        }
    }

    void computeRange() {
        Vector<Double> numericConcepts = new Vector<>();
        for (String s : commonConcepts) {
            numericConcepts.add(Double.parseDouble(s));
        }
        this.low = Collections.min(numericConcepts);
        this.high = Collections.max(numericConcepts);

        if (commonConcepts.size() != 1) {
            unionedConcepts.addAll(commonConcepts);
            commonConcepts.clear();
        }
    }

    public String getConceptSummary() {
        tryDisambiguate();
        if (commonConcepts.size() == 0) {
            NumberFormat nf = new DecimalFormat("##.###");
            return nf.format(low) + " - " + nf.format(high);
        }
        return commonConcepts.get(0).toString();
    }

    public void calculateConditions(int disjunctionOrConjunction) {
        super.calculateConditions(disjunctionOrConjunction);
        DisjunctiveSelectionCondition newConceptContainmentCondition = new DisjunctiveSelectionCondition();
        SelectionCondition now = new SelectionCondition();
        now.add(new SelectionCondition(conceptConditionAttribute, low,
                Operator.GREATER_THAN_EQUALS));
        now.add(new SelectionCondition(conceptConditionAttribute, high, Operator.LESS_THAN_EQUALS));
        newConceptContainmentCondition.add(now);
        conceptContainmentCondition = newConceptContainmentCondition;
    }

    public void tryDisambiguate() {
        if (!DBUtil.DISAMBIGUATE_ACTIVE) {
            if (!calledBefore) {
                calledBefore = true;
                similarityMerge();
                computeRange();
            }
            return;
        }
        if (disambiguated) {
            return;
        }
        disambiguated = true;
        if (!isAmbiguous) {
            // Either no ambiguity or partial ambiguity. Use unambiguous properties stored in
            // commonConcepts to resolve ambiguity stored in allConcepts

            similarityMerge();
            computeRange();
            return;
        }

        // try to find exactly one matched property because every entity is ambiguous
        Vector<String> tryCommon = new Vector<>();
        boolean first = true;
        for (Vector<String> tempConcepts : allConcepts) {
            if (first) {
                tryCommon.addAll(tempConcepts);
                first = false;
            } else {
                tryCommon.retainAll(tempConcepts);
            }
        }
        if (tryCommon.size() >= 1) {
            commonConcepts.clear();
            commonConcepts.add(tryCommon.elementAt(0)); // multiple common concepts. pick any.
            computeRange();
            isAmbiguous = false;
            return;
        }
        // All entities are ambiguous and there is no single common property among the ambiguous
        // entities
        Vector<Double> P = new Vector<>();
        for (Vector<String> tempConcepts : allConcepts) {
            for (String s : tempConcepts) {
                P.add(Double.parseDouble(s));
            }
        }
        Collections.sort(P);
        int lo = 0, hi;
        Double minP = P.elementAt(0);
        Double maxP;
        for (hi = 0; hi < P.size(); hi++) {
            maxP = P.elementAt(hi);
            int nCovered = 0;
            for (Vector<String> tempConcepts : allConcepts) {
                for (String s : tempConcepts) {
                    if (minP <= Double.parseDouble(s) && Double.parseDouble(s) <= maxP) {
                        nCovered += 1;
                        break;
                    }
                }
            }
            if (nCovered == allConcepts.size()) {
                break;
            }

        }
        int bestLo = lo, bestHi = hi;
        Double min = P.lastElement() - P.firstElement();
        while (hi < P.size()) {
            if (P.elementAt(hi) - P.elementAt(lo) < min) {
                bestLo = lo;
                bestHi = hi;
                min = P.elementAt(hi) - P.elementAt(lo);
            }
            lo = lo + 1;
            minP = P.elementAt(lo);
            boolean covered = false;
            while (!covered) {
                maxP = P.elementAt(hi);
                for (Vector<String> tempConcepts : allConcepts) {
                    covered = false;
                    for (String s : tempConcepts) {
                        if (minP <= Double.parseDouble(s) && Double.parseDouble(s) <= maxP) {
                            covered = true;
                            break;
                        }
                    }
                    if (!covered) {
                        break;
                    }
                }
                if (!covered) {
                    hi += 1;
                    if (hi == P.size()) {
                        break;
                    }
                }
            }
        }
        for (Vector<String> tempConcepts : allConcepts) {
            for (String s : tempConcepts) {
                if (!commonConcepts.contains(s) && (Double.parseDouble(s) == P.elementAt(bestLo)
                        || Double.parseDouble(s) == P.elementAt(bestHi))) {
                    commonConcepts.add(s);
                }
            }
        }
        isAmbiguous = false;
        unionedConcepts.clear();
        computeRange();
    }

    public boolean isAmbiguous() {
        tryDisambiguate();
        return isAmbiguous;
    }
}
