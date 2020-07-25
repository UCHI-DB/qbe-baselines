package filter;

import util.DBUtil;

/**
 * Disjunction of Filters within same concept attribute, discrete
 */
public class SimpleInequalityFilter extends SimpleFilter {

    int etSize;
    Filter low;
    Filter high;
    double fScore;

    public SimpleInequalityFilter(int etSize) {
        this.etSize = etSize;
        low = null;
        high = null;
        fScore = -1;
    }

    public SimpleInequalityFilter(Filter low, Filter high, int etSize) {
        this.low = low;
        this.high = high;
        this.etSize = etSize;
        fScore = -1;
    }

    /*
     * Computes the selectivity where low.frequency <= frequency <= high.frequency
     */
    public double getInverseSelectivity() {
        double inverseSelectivity = high.getInverseSelectivityLessOrEqual()
                - low.getInverseSelectivityLessOrEqual() + low.getInverseSelectivityEqual();
        return Double.max(0, 1 - inverseSelectivity);
    }

    public double getLikelihood() {
        return Math.pow(1 - getInverseSelectivity(), etSize);
    }

    /*
     * Assume uniformity = 1 for simple inequality filters TODO: improve this
     */
    public double getUniformity() {
        return 1;
    }

    /*
     * Computes F-Score of this filter
     */
    public double getFScore() {
        if (fScore < 0) {
            double filter_prior = DBUtil.FILTER_PRIOR * getUniformity();
            fScore = filter_prior / ((1 - filter_prior) * getLikelihood());
        }
        return fScore;
    }

    @Override
    public String toString() {
        String filterDescription = "Freq(" + low.table.getRealName() + "." + low.attribute.getName()
                + " = " + low.value + ") IN [";
        filterDescription += low.frequency + ", " + high.frequency + "]";
        return filterDescription;
    }
}
