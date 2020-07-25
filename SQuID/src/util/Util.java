package util;

import java.util.Vector;
import java.util.logging.Logger;

import concept.ConceptAttribute;
import concept.InequalityConceptAttribute;

public class Util {
    private static Logger logger;
    public static boolean STILL_LOADING;

    public static void beginStartUp(QbeeOptions opts) {
        logger = Logger.getLogger("Squid");
        logger.setLevel(opts.logLevel);
        DBUtil.DB_NAME = opts.dbName;
        DBUtil.DB_USER = opts.dbUser;
        DBUtil.DB_PASSWORD = opts.dbPassword;
        DBUtil.PG_DUMP_PATH = opts.pgDumpPath;
        DBUtil.createConnection();
        DBUtil.populateDB();
    }

    public static void exit() {

        DBUtil.closeConnection();
    }

    public static Logger getLogger() {
        return logger;
    }

    public static double getMean(Vector<Double> data) {
        double sum = 0.0;
        for (double a : data)
            sum += a;
        return sum / data.size();
    }

    public static Vector<Double> normalize(Vector<Double> data) {
        Vector<Double> result = new Vector<>();
        double min = data.lastElement();
        double max = data.firstElement();
        for (double d : data) {
            double r = (d - min) / (max - min);
            result.add(r);
        }
        return result;
    }

    public static double getSkewness(Vector<Double> data) {
        int n = data.size();
        if (n < 3) {
            // skewness value not computable
            return 0;
        }

        double avg = getMean(data);
        double stdev = 0;
        for (double d : data) {
            d = d - avg;
            stdev = stdev + d * d;
        }
        stdev = Math.sqrt(stdev / (n - 1));

        double skew = 0.;
        for (double d : data) {
            d = d - avg;
            d = d / stdev;
            skew = skew + d * d * d;
        }
        return skew * n / ((n - 1) * (n - 2));
    }

    /**
     * @param data
     * @return number of outliers
     */
    public static int getOutliers(Vector<Double> data) {
        int n = data.size();
        double avg = getMean(data);
        double var = 0;
        for (double d : data) {
            var += (d - avg) * (d - avg);
        }
        var /= (n - 1);
        double std = Math.sqrt(var);
        Vector<Double> outliers = new Vector<>();
        for (double d : data) {
            if (d - avg > 2 * std) {
                outliers.add(d);
            } else {
                break;
            }
        }
        return outliers.size();
    }

    public static Vector<ConceptAttribute> getProperConceptAttributes(
            Vector<ConceptAttribute> conceptAttributes) {
        Vector<ConceptAttribute> ret = new Vector<>();
        for (ConceptAttribute ca : conceptAttributes) {
            if (ca.getConceptConditionAttribute().getFkRefTo() == null
                    && ca.getCommonConcepts().size() > 0
                    && !ca.getConceptConditionAttribute().isTextAttribute()) { // number
                InequalityConceptAttribute ica;
                if (ca instanceof InequalityConceptAttribute) {
                    ica = (InequalityConceptAttribute) ca;
                } else {
                    ica = new InequalityConceptAttribute(ca);
                }
                ret.add(ica);
            } else {
                ret.add(ca);
            }
        }
        return ret;
    }

    public static ConceptAttribute getProperConceptAttribute(ConceptAttribute ca) {
        if (ca.getConceptConditionAttribute().getFkRefTo() == null
                && !ca.getConceptConditionAttribute().isTextAttribute()) { // number
            if (ca instanceof InequalityConceptAttribute) {
                return ca;
            }
            return new InequalityConceptAttribute(ca);
        }
        return ca;
    }

}
