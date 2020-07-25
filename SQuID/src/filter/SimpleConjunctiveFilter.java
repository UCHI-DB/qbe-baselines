package filter;

import java.util.Vector;

import util.DBUtil;

/**
 * Disjunction of Filters within same concept attribute, discrete
 */
public class SimpleConjunctiveFilter extends SimpleFilter {

    int etSize;
    Vector<Filter> filters;
    double fScore;
    boolean isContinuousFilter;

    public SimpleConjunctiveFilter(int etSize) {
        filters = new Vector<>();
        this.etSize = etSize;
        fScore = -1;
    }

    public SimpleConjunctiveFilter(Vector<Filter> filters, int etSize) {
        this.filters = filters;
        this.etSize = etSize;
        fScore = -1;
    }

    public void setContinuousFilter(boolean isContinuousFilter) {
        this.isContinuousFilter = isContinuousFilter;
    }

    public void addFilter(Filter f) {
        filters.add(f);
    }

    public void removeFilter(Filter f) {
        filters.remove(f);
    }

    /*
     * Computes the selectivity considering conjunction among all filters, assumes conditional
     * independence among filters
     */
    public double getInverseSelectivity() {
        double total = 1;
        for (Filter f : filters) {
            total *= f.getInverseSelectivity();
        }
        return Double.max(0, 1 - total);
    }

    public double getLikelihood() {
        return Math.pow(1 - getInverseSelectivity(), etSize);
    }

    public double getUniformity() {
        // Uniformity for conjunctive filter is 1
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
        Filter firstFilter = filters.elementAt(0);
        String filterDescription = firstFilter.table.getRealName() + "."
                + firstFilter.attribute.getName() + " CONTAINS {";
        boolean first = true;
        filters.sort((Filter f1, Filter f2) -> f1.value.compareTo(f2.value));
        for (Filter f : filters) {
            if (!first) {
                filterDescription += ",";
            }
            filterDescription += f.value;
            first = false;
        }
        filterDescription += "}";
        return filterDescription;
    }

    public Vector<Filter> getFilters() {
        return filters;
    }
}
