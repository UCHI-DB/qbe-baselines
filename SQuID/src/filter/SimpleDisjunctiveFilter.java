package filter;

import java.util.Vector;

import query.Query;
import util.DBUtil;

/**
 * Disjunction of Filters within same concept attribute, discrete
 */
public class SimpleDisjunctiveFilter extends SimpleFilter {

    int etSize;
    Vector<Filter> filters;
    double fScore;
    boolean isContinuousFilter;
    double min = 1e8, max = -1e8;

    public SimpleDisjunctiveFilter(int etSize) {
        filters = new Vector<>();
        this.etSize = etSize;
        fScore = -1;
    }

    public SimpleDisjunctiveFilter(Vector<Filter> filters, int etSize) {
        this.filters = filters;
        this.etSize = etSize;
        fScore = -1;
    }

    public void setContinuousFilter(boolean isContinuousFilter) {
        this.isContinuousFilter = isContinuousFilter;
    }

    public void addFilter(Filter f) {
        filters.add(f);
        if (isContinuousFilter) {
            min = Math.min(min, Double.parseDouble(f.value));
            max = Math.max(max, Double.parseDouble(f.value));
        }
    }

    public void removeFilter(Filter f) {
        filters.remove(f);
    }

    /*
     * Computes the selectivity considering disjunction among all filters, assumes conditional
     * independence among filters
     */
    public double getInverseSelectivity() {
        double totalSelectivity = 0;
        if (isContinuousFilter) {
            totalSelectivity = computeSelectivity();
        } else {
            for (Filter f : filters) {
                totalSelectivity += f.getInverseSelectivity();
            }
        }
        return Double.max(0, 1 - totalSelectivity);
    }

    /**
     * @return selectivity for numeric attributes, i.e., continuous filters
     */
    private double computeSelectivity() {
        Filter minFilter = null, maxFilter = null;
        for (Filter f : filters) {
            if (isContinuousFilter) {
                Double fvalue = Double.parseDouble(f.value);
                if (fvalue <= min) {
                    min = fvalue;
                    minFilter = f;
                }
                if (fvalue >= max) {
                    max = fvalue;
                    maxFilter = f;
                }
            }
        }
        return maxFilter.getInverseSelectivityLessOrEqual()
                - (minFilter.getInverseSelectivityLessOrEqual()
                        - minFilter.getInverseSelectivity());
    }

    public double getLikelihood() {
        return Math.pow(1 - getInverseSelectivity(), etSize);
    }

    /**
     * @return uniformity. Uniformity is defined as 1/(gamma^diversity). Diversity is a number
     *         between 0 and DBUtil.eta For disjunctive filter: if filter size = 1, diversity = 1.
     *         Otherwise diversity is the percentage of concepts covered.
     */
    public double getUniformity() {
        double diversity = 1;
        if (isContinuousFilter) {
            double globalMin = filters.get(0).getGlobalMin();
            double globalMax = filters.get(0).getGlobalMax();
            diversity = Math.max(1,
                    (max - min + 1) * DBUtil.DIVERSITY_RANGE / (globalMax - globalMin + 1));
        } else {
            if (filters.size() == 1) {
                diversity = 1;
            } else {
                Query totalConceptCountQuery = new Query(
                        "SELECT COUNT(DISTINCT " + filters.elementAt(0).attribute.getName()
                                + ") FROM " + filters.elementAt(0).table.getName());
                totalConceptCountQuery.executeQuery();
                double totalConceptCount = Double
                        .parseDouble(totalConceptCountQuery.getResult().elementAt(0).elementAt(0));
                diversity = Math.max(1,
                        (filters.size() * DBUtil.DIVERSITY_RANGE) / totalConceptCount);
            }
        }
        double uniformity = (1 / Math.pow(diversity, DBUtil.DIVERSITY_EXPONENT));
        return uniformity;
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
                + firstFilter.attribute.getName() + " IN {";
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
        if (isContinuousFilter) {
            filterDescription = firstFilter.table.getRealName() + "."
                    + firstFilter.attribute.getName() + " IN [" + min + ", " + max + "]";
        }
        return filterDescription;
    }

    public Vector<Filter> getFilters() {
        return filters;
    }
}
