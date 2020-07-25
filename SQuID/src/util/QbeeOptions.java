package util;

import java.util.logging.Level;

import plume.Option;
import plume.Options;

public class QbeeOptions {
    private Options options;
    public Level logLevel;

    public QbeeOptions(String[] args) {
        options = new Options(this);
        options.parse_or_usage(args);

        logLvl = logLvl.toUpperCase();
        // Set the level according to logLvl
        if (logLvl.equals("SEVERE")) {
            logLevel = Level.SEVERE;
        } else if (logLvl.equals("WARNING")) {
            logLevel = Level.WARNING;
        } else if (logLvl.equals("FINE")) {
            logLevel = Level.FINE;
        } else if (logLvl.equals("FINER")) {
            logLevel = Level.FINER;
        } else if (logLvl.equals("FINEST")) {
            logLevel = Level.FINEST;
        } else {
            logLevel = Level.INFO;
        }

        DBUtil.FILTER_PRIOR = rho;
        DBUtil.MIN_ASSOCIATION_THRESHOLD = tau_a;
        DBUtil.MIN_SKEWNESS = tau_s;
        DBUtil.DIVERSITY_EXPONENT = gamma;
        DBUtil.DIVERSITY_RANGE = eta;

        DBUtil.USE_SKEWNESS = useSkewness;
        DBUtil.FILTER_RELAX_ACTIVE = filterRelaxActive;
        DBUtil.DISAMBIGUATE_ACTIVE = disambiguateActive;
        DBUtil.FREQUENCY_MODE = "freq";
        if (useContextualSemanticSimilarity) {
            DBUtil.USE_CONTEXTUAL_SEMANTIC_SIMILARITY = true;
        } else {
            DBUtil.USE_CONTEXTUAL_SEMANTIC_SIMILARITY = false;
        }
        setDbName(this.dbName);
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
        if (normalizeFrequency) {
            DBUtil.FREQUENCY_MODE = "normalized_freq";
            DBUtil.CONCEPT_COUNT_FILE_NAME = "data/" + dbName + "_normalizedConceptCount.bin";
        } else {
            DBUtil.FREQUENCY_MODE = "freq";
            DBUtil.CONCEPT_COUNT_FILE_NAME = "data/" + dbName + "_conceptCount.bin";
        }
    }

    @Option("The database name")
    public String dbName = null;

    @Option("The database user name")
    public String dbUser = null;

    @Option("The database user password")
    public String dbPassword = null;

    @Option("pg_dump Path")
    public String pgDumpPath = null;

    @Option("Set the log level to any of: SEVERE, WARNING, INFO, FINE, FINER, or FINEST")
    public String logLvl = null;

    @Option("Value of assication threshold tau_a")
    public int tau_a = 10;

    @Option("Value of skewness threshold tau_s")
    public double tau_s = 2.0;

    @Option("Value of filter prior rho")
    public double rho = 0.2;

    @Option("Value of diversity range")
    public double eta = 100.0;

    @Option("Value of diversity exponent") // 0 means don't care about diversity. >= 1
    public double gamma = 1;

    @Option("Use skewness or not")
    public boolean useSkewness = false;

    @Option("Relax filter or not")
    public boolean filterRelaxActive = true;

    @Option("Disambiguate or not")
    public boolean disambiguateActive = false;

    @Option("Normalize frequency or not")
    public boolean normalizeFrequency = false;

    @Option("Use contextual semantic similarity or not")
    public boolean useContextualSemanticSimilarity = false;
}
