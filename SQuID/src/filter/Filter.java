package filter;

import java.io.Serializable;

import dbms.Attribute;
import dbms.Table;
import query.Query;
import util.DBUtil;
import util.QbeeException;

public class Filter implements Serializable {

    public Table table;
    public Attribute attribute;
    public String value;
    public Integer frequency; // For parametric filters, we need the frequency too
    double inverseSelectivity;
    double inverseSelectivityEqual; // For parametric filters, freq = frequency
    double inverseSelectivityLessOrEqual; // For parametric filters, where freq <= frequency
    boolean isInequalityFilter;
    private double globalMin;
    private double globalMax;

    /*
     * For Parametric P-Filters
     */
    public Filter(Table table, Attribute attribute, String value, Integer frequency) {
        this.table = table;
        this.attribute = attribute;
        this.value = value;
        this.frequency = frequency;
        isInequalityFilter = true;
        inverseSelectivity = -1;
        inverseSelectivityEqual = -1;
        inverseSelectivityLessOrEqual = -1;
    }

    /*
     * For P-Filters
     */
    public Filter(Table table, Attribute attribute, String value) {
        this.table = table;
        this.attribute = attribute;
        this.value = value;
        this.frequency = 0;
        isInequalityFilter = false;
        inverseSelectivity = -1;
        inverseSelectivityEqual = -1;
        inverseSelectivityLessOrEqual = -1;
    }

    public double getInverseSelectivity() {
        if (!isInequalityFilter) {
            if (inverseSelectivity < 0) {
                throw new QbeeException("Inverse selectivity not computed yet for " + toString());
            }
            return inverseSelectivity;
        }
        throw new QbeeException(
                "Inverse selectivity not possible for inequality filter" + toString());
    }

    public void setInverseSelectivity(double inverseSelectivity) {
        this.inverseSelectivity = inverseSelectivity;
    }

    /*
     * For parametric filters, selectivity where freq <= this.frequency For numeric filters,
     * selectivity where value <= this.value
     */
    public void setInverseSelectivityLessOrEqual(double inverseSelectivityLessOrEqual) {
        this.inverseSelectivityLessOrEqual = inverseSelectivityLessOrEqual;
    }

    public double getInverseSelectivityLessOrEqual() {
        return inverseSelectivityLessOrEqual;
    }

    /*
     * For parametric filters, selectivity where freq = this.frequency
     */

    public void setInverseSelectivityEqual(double inverseSelectivityEqual) {
        this.inverseSelectivityEqual = inverseSelectivityEqual;
    }

    public double getInverseSelectivityEqual() {
        return inverseSelectivityEqual;
    }

    @Override
    public String toString() {
        return table.getRealName() + "." + attribute.getName() + "." + value + "." + frequency;
    }

    public boolean isEqual(Filter other) {
        if (isInequalityFilter) {
            if (table.getRealName().equals(other.table.getRealName())
                    && attribute.getName().equals(other.attribute.getName())
                    && value.equals(other.value) && frequency == other.frequency) {
                return true;
            }
        } else if (table.getRealName().equals(other.table.getRealName())
                && attribute.getName().equals(other.attribute.getName())
                && value.equals(other.value)) {
            return true;
        }
        return false;
    }

    public void computeInverseSelectivity() {
        setInverseSelectivity(0);
        if (attribute.getTable().hasTableType(DBUtil.AGGR_OBJECT_OBJECT_TABLE)
                || attribute.getTable().hasTableType(DBUtil.AGGR_OBJECT_OBJECT_TABLE)) {
            // find opposite mapping
            Attribute objectAttribute = attribute.getTable().getObjectAttribute().getFkRefTo();
            Attribute targetAttribute = attribute.getFkRefTo();

            for (Table t : DBUtil.getReverseAggregateTables(attribute.getTable())) {
                if (t.hasTableType(DBUtil.AGGR_OBJECT_OBJECT_TABLE)) {
                    for (Attribute a1 : t.getAttributes()) {
                        if (a1.getFkRefTo() != null && a1.isAggregateAttribute()
                                && a1.getFkRefTo().equals(objectAttribute)) {
                            for (Attribute a2 : t.getAttributes()) {
                                if (a2.getFkRefTo() != null && !a2.isAggregateAttribute()
                                        && a2.getFkRefTo().equals(targetAttribute)) {
                                    String specificCountQuery = "SELECT COUNT FROM " + t.getName()
                                            + " WHERE "
                                            + targetAttribute.getFkAttributeFromTable(t).getName()
                                            + " = " + value;
                                    String totalCountQuery = "SELECT COUNT(*) FROM "
                                            + attribute.getTable().getRealName();

                                    Query sq = new Query(specificCountQuery);
                                    sq.executeQuery();
                                    double specificCount = Double
                                            .parseDouble(sq.getResult().elementAt(0).elementAt(0));

                                    sq = new Query(totalCountQuery);
                                    sq.executeQuery();
                                    double totalCount = Double
                                            .parseDouble(sq.getResult().elementAt(0).elementAt(0));

                                    setInverseSelectivity(specificCount / totalCount);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public double getGlobalMin() {
        return globalMin;
    }

    public double getGlobalMax() {
        return globalMax;
    }

    public void setGlobalMin(double globalMin) {
        this.globalMin = globalMin;
    }

    public void setGlobalMax(double globalMax) {
        this.globalMax = globalMax;
    }
}
