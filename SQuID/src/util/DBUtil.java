package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import org.jaxen.saxpath.Operator;

import dbms.Attribute;
import dbms.AttributeWithTable;
import dbms.DB;
import dbms.ForeignKeyConstraint;
import dbms.PrimaryKeyConstraint;
import dbms.SchemaReader;
import dbms.Table;
import dbms.TableWithAlias;
import filter.Filter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import query.Query;

public class DBUtil {
    public static String PG_DUMP_PATH;
    public static String DB_NAME;
    public static String DB_USER;
    public static String DB_PASSWORD;

    private static final String SCHEMA_DIR = "schema";
    private static final String HOST = "localhost";

    public static final int DIMENSION_OBJECT_TABLE = 1;
    public static final int DIMENSION_CONCEPT_TABLE = 2;
    public static final int FACT_OBJECT_OBJECT_TABLE = 4;
    public static final int FACT_OBJECT_CONCEPT_TABLE = 8;
    public static final int FACT_OBJECT_DEEP_FREQ_CONCEPT_TABLE = 16;
    public static final int AGGR_OBJECT_CONCEPT_TABLE = 32;
    public static final int AGGR_OBJECT_OBJECT_TABLE = 64;
    public static final int FACT_OBJECT_FREQ_CONCEPT_TABLE = 128;

    public static boolean FILTER_RELAX_ACTIVE = true;
    public static boolean DISAMBIGUATE_ACTIVE = true;
    public static boolean USE_SKEWNESS = true;

    public static Double FILTER_PRIOR = 0.2; // rho
    public static Double DIVERSITY_RANGE = 100.0; // eta
    public static Double DIVERSITY_EXPONENT = 1.0; // gamma
    public static Double MIN_SKEWNESS = 2.0; // tau_s
    public static Integer MIN_ASSOCIATION_THRESHOLD = 10; // tau_a
    public static Double EXPLAINABILITY_CONST = 1.0; // beta. Not used in current implementation.

    public static final int MAX_FREQ_CONCEPT_SIZE = 20; // how many p-theta filters to be considered
                                                        // for computing skewness/outlier. Only an
                                                        // implementation parameter, and has nothing
                                                        // to do with theory
    public static final int OUTLIER_DETECTION_STRENGTH = 2; // this is for outlier detection, also,
                                                            // tangential to our theories

    private static String[] dimensionObject = null;
    private static String[] dimensionConcept = null;
    private static String[] factObjectObject = null;
    private static String[] factObjectConcept = null;
    private static String[] conceptWithinDimension = null;
    private static String[] primaryAttributeWithinDimension = null;
    private static String[] additionalFreqConcept = null;

    private static final Map<String, String> DB_NAME_MAP = Collections
            .unmodifiableMap(new HashMap<String, String>() {
                {
                    put("IMDb", "smallimdb");
                    put("DBLP", "dblp");
                }
            });

    public static final int DISJUNCTION = 1;
    public static final int CONJUNCTION = 2;
    public static String FREQUENCY_MODE;

    public static Map<String, Attribute> aggregateFK; // maps table name to attribute denoting the
                                                      // aggregate column of this table refers to
                                                      // the Attribute
    public static Map<String, Filter> dbFilters;
    private static Connection connection = null;
    private static DB db;

    private static int aliasCount = 0;
    static String CONCEPT_COUNT_FILE_NAME;
    public static boolean USE_CONTEXTUAL_SEMANTIC_SIMILARITY = true;
    public static boolean GUI;

    public static Connection getConnection() {
        closeConnection();
        createConnection();
        return connection;
    }

    public static DB getDB() {
        return db;
    }

    static void createConnection() {
        String url = "jdbc:postgresql://" + HOST + "/" + DB_NAME + "?user=" + DB_USER + "&password="
                + DB_PASSWORD;
        try {
            connection = DriverManager.getConnection(url);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getDBSchema() {
        /*
         * Use command line based pg_dump to dump the schema to specified directory
         */
        /*
         * try { String line;
         * 
         * String dbDumpCommand = PG_DUMP_PATH + " --schema-only " + DB_NAME + ">" + SCHEMA_DIR +
         * "/" + DB_NAME + ".sql"; Process p = Runtime.getRuntime().exec(new String[] { "sh", "-c",
         * dbDumpCommand });
         * 
         * BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
         * String errorLine = ""; while ((line = error.readLine()) != null) { errorLine += line; }
         * error.close(); if (errorLine.length() > 0) throw new QbeeException(errorLine); try {
         * String cleanSchema = "/bin/sed -i 's/public\\.//g' " + SCHEMA_DIR + "/" + DB_NAME +
         * ".sql"; p = Runtime.getRuntime().exec(new String[] { "sh", "-c", cleanSchema });
         * 
         * error = new BufferedReader(new InputStreamReader(p.getErrorStream())); errorLine = "";
         * while ((line = error.readLine()) != null) { errorLine += line; } error.close(); if
         * (errorLine.length() > 0) throw new QbeeException(errorLine); } catch (Exception e) { //
         * TODO: handle exception }
         * 
         * } catch (Exception err) { throw new QbeeException(err); }
         */
        return SCHEMA_DIR + "/" + DB_NAME + ".sql";
    }

    private static SchemaReader readSchema(String schemaFName) {
        Scanner schemaSc;
        try {
            schemaSc = new Scanner(new File(schemaFName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new QbeeException(String.format("Schema file '%s' does not exist", schemaFName));
        }

        StringBuilder sb = new StringBuilder();
        Vector<ForeignKeyConstraint> fkConstraints = new Vector<>();
        Vector<PrimaryKeyConstraint> pkConstraints = new Vector<>();
        while (schemaSc.hasNextLine()) {
            String schemaQ = schemaSc.nextLine();
            if (schemaQ.startsWith("CREATE TABLE")) {
                String currentSchema = schemaQ;
                while (schemaSc.hasNextLine()) {
                    schemaQ = schemaSc.nextLine();
                    currentSchema = currentSchema + " " + schemaQ.trim();
                    if (schemaQ.endsWith(";")) {
                        break;
                    }
                }
                sb.append(currentSchema);
            } else if (schemaQ.startsWith("ALTER TABLE")) {
                if (schemaQ.contains("OWNER")) {
                    continue;
                }
                String currentSchema = schemaQ;
                while (schemaSc.hasNextLine()) {
                    schemaQ = schemaSc.nextLine();
                    currentSchema = currentSchema + " " + schemaQ.trim();
                    if (schemaQ.endsWith(";")) {
                        break;
                    }
                }
                if (currentSchema.contains("REFERENCES")) {
                    fkConstraints.add(new ForeignKeyConstraint(currentSchema));
                }
                if (currentSchema.contains("PRIMARY KEY")) {
                    pkConstraints.add(new PrimaryKeyConstraint(currentSchema));
                }
            }
        }

        schemaSc.close();
        String schemaQStr = sb.toString();

        Statements schemaQueries = null;
        try {
            schemaQueries = CCJSqlParserUtil.parseStatements(schemaQStr);
        } catch (JSQLParserException e) {
            e.printStackTrace();
            throw new QbeeException(String.format("Error parsing schema queries from file %s\n%s",
                    schemaFName, schemaQStr));
        }
        return new SchemaReader(schemaQueries, pkConstraints, fkConstraints);
    }

    /**
     * Creates schema directory to store the schema using pg_dump
     */

    private static void createSchemaDirectory() {
        File schemaDir = new File(SCHEMA_DIR);
        if (!schemaDir.exists()) {
            try {
                schemaDir.mkdir();
            } catch (SecurityException se) {
                throw new QbeeException(se);
            }
        }
    }

    static void populateDB() {
        loadDB();
        if (db.getInvertedColumnIndexTableName() == null) {
            createInvertedColumnIndex();
            createDeepDerivedConceptFOCTables();
            computeFOCAggregates();
            loadDB();
            FREQUENCY_MODE = "normalized_freq";
            CONCEPT_COUNT_FILE_NAME = "data/" + DB_NAME + "_normalizedConceptCount.bin";
            computeFilterSelectivity();
            FREQUENCY_MODE = "freq";
            CONCEPT_COUNT_FILE_NAME = "data/" + DB_NAME + "_conceptCount.bin";
            computeFilterSelectivity();
        }
    }

    /**
     * creates a new table containing aggregation of concepts for each object for each FOC table and
     * aggregation of objects for each FOO table for each other objects
     */
    private static void computeFOCAggregates() {
        for (Table t : getDB().getTables()) {
            if (t.hasTableType(DBUtil.FACT_OBJECT_CONCEPT_TABLE)) {
                Attribute objectAttr = null;
                Attribute conceptAttr = null;
                for (Attribute a : t.getAttributes()) {
                    if (a.getFkRefTo().getTable().hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)) {
                        objectAttr = a;
                    }
                    if (a.getFkRefTo().getTable().hasTableType(DBUtil.DIMENSION_CONCEPT_TABLE)) {
                        conceptAttr = a;
                    }
                }
                if (objectAttr != null && conceptAttr != null) {
                    String createTableQS = "CREATE TABLE _aggr_aoc_" + t.getName() + " AS (SELECT "
                            + objectAttr.getName() + ", ARRAY_AGG(" + conceptAttr.getName()
                            + " ORDER BY " + conceptAttr.getName() + ") AS " + conceptAttr.getName()
                            + "_aggr, COUNT(*) AS count FROM " + t.getName() + " GROUP BY "
                            + objectAttr.getName() + ")";

                    String fkQSs = "ALTER TABLE _aggr_aoc_" + t.getName()
                            + " ADD CONSTRAINT _aggr_aoc" + t.getName() + "_" + objectAttr.getName()
                            + "_fk FOREIGN KEY (" + objectAttr.getName() + ")  REFERENCES "
                            + objectAttr.getFkRefTo().getTable().getName() + "("
                            + objectAttr.getFkRefTo().getName() + ")";
                    (new Query(createTableQS)).execute();
                    (new Query(fkQSs)).execute();

                    String tableName = "_aggr_aoc_" + t.getName();
                    String idxQuery = "CREATE INDEX " + tableName + "_idx" + " on " + tableName
                            + "(" + objectAttr.getName() + ")";
                    (new Query(idxQuery)).execute();
                }
            }
            if (t.hasTableType(DBUtil.FACT_OBJECT_OBJECT_TABLE)) {
                Attribute objectAttr1 = null;
                Attribute objectAttr2 = null;
                for (Attribute a : t.getAttributes()) {
                    if (a.getFkRefTo().getTable().hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)) {
                        objectAttr1 = a;
                        for (Attribute b : t.getAttributes()) {
                            if (b.getFkRefTo().getTable()
                                    .hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)) {
                                objectAttr2 = b;
                                if (objectAttr1.getName().compareTo(objectAttr2.getName()) != 0) {
                                    String createTableQS = "CREATE TABLE _aggr_aoo_" + t.getName()
                                            + "_" + objectAttr1.getName() + "to"
                                            + objectAttr2.getName() + " AS (SELECT "
                                            + objectAttr1.getName() + ", ARRAY_AGG("
                                            + objectAttr2.getName() + " ORDER BY "
                                            + objectAttr2.getName() + ") AS "
                                            + objectAttr2.getName()
                                            + "_aggr, COUNT(*) AS count FROM " + t.getName()
                                            + " GROUP BY " + objectAttr1.getName() + ")";
                                    String fkQSs = "ALTER TABLE _aggr_aoo_" + t.getName() + "_"
                                            + objectAttr1.getName() + "to" + objectAttr2.getName()
                                            + " ADD CONSTRAINT _aggr_aoo_" + t.getName() + "_"
                                            + objectAttr1.getName() + "to" + objectAttr2.getName()
                                            + "_" + objectAttr1.getName() + "_fk FOREIGN KEY ("
                                            + objectAttr1.getName() + ")  REFERENCES "
                                            + objectAttr1.getFkRefTo().getTable().getName() + "("
                                            + objectAttr1.getFkRefTo().getName() + ")";
                                    (new Query(createTableQS)).execute();
                                    (new Query(fkQSs)).execute();

                                    String tableName = "_aggr_aoo_" + t.getName() + "_"
                                            + objectAttr1.getName() + "to" + objectAttr2.getName();
                                    String idxQuery = "CREATE INDEX " + tableName + "_idx" + " on "
                                            + tableName + "(" + objectAttr1.getName() + ")";
                                    (new Query(idxQuery)).execute();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static void loadDB() {
        if (DB_NAME.contains("imdb")) {
            dimensionObject = new String[] { "movie", "person", "company" };
            dimensionConcept = new String[] { "certificate", "country", "genre", "language",
                    "role" };
            factObjectObject = new String[] { "castinfo", "distribution", "production" };
            factObjectConcept = new String[] { "movietocertificate", "movietocountry",
                    "movietogenre", "movietolanguage" };
            conceptWithinDimension = new String[] { "movie:production_year", "castinfo:role_id",
                    "person:gender", "person:birth_country", "person:birth_year",
                    "person:death_year", "person:age_group", "person:death_cause",
                    "person:height_group" };
            primaryAttributeWithinDimension = new String[] { "certificate:name", "company:name",
                    "country:name", "genre:name", "language:name", "movie:title", "person:name",
                    "role:role" };
            additionalFreqConcept = new String[] { "castinfo:person_id:role_id" };
        } else if (DB_NAME.contains("adult")) {
            dimensionObject = new String[] { "adult" };
            dimensionConcept = new String[] {};
            factObjectObject = new String[] {};
            factObjectConcept = new String[] {};
            conceptWithinDimension = new String[] { "adult:age", "adult:workclass", "adult:fnlwgt",
                    "adult:education", "adult:educationnum", "adult:maritalstatus",
                    "adult:occupation", "adult:relationship", "adult:race", "adult:sex",
                    "adult:capitalgain", "adult:capitalloss", "adult:hoursperweek",
                    "adult:nativecountry", "adult:income" };
            primaryAttributeWithinDimension = new String[] { "adult:name" };
            additionalFreqConcept = new String[] {};
        } else if (DB_NAME.contains("dblp")) {
            dimensionObject = new String[] { "author", "publication" };
            dimensionConcept = new String[] { "authorposition", "venue", "institute", "domain",
                    "country" };
            factObjectObject = new String[] { "authortopublication" };
            factObjectConcept = new String[] { "authortoinstitute", "publicationtovenue",
                    "authortocountry", "publicationtodomain", "publicationtocountry",
                    "publicationtoinstitute" };
            conceptWithinDimension = new String[] { "author:paper_count", "author:gender",
                    "publication:year" };
            primaryAttributeWithinDimension = new String[] { "author:name", "publication:name",
                    "authorposition:name", "venue:name", "institute:name", "domain:name",
                    "country:name" };
            additionalFreqConcept = new String[] { "authortopublication:author_id:position_id" };

        }
        createSchemaDirectory();
        String schmeaFile = getDBSchema();
        SchemaReader schemaReader = readSchema(schmeaFile);
        db = schemaReader.getDB();
        db = schemaReader.getDB();
        db.addMetaInfo(dimensionObject, dimensionConcept, factObjectObject, factObjectConcept,
                additionalFreqConcept, conceptWithinDimension, primaryAttributeWithinDimension);
        db.addAggregateFKs();
        loadDBFilters();
    }

    @SuppressWarnings("unchecked")
    static void loadDBFilters() {

        dbFilters = new HashMap<>();

        try {
            FileInputStream fin = new FileInputStream(CONCEPT_COUNT_FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fin);
            dbFilters.putAll((Map<String, Filter>) ois.readObject());
            Util.getLogger().info("Loaded " + dbFilters.size() + " filters");
            ois.close();
            fin.close();
        } catch (Exception e) {
            Util.getLogger().info(
                    "Could not read previous db filters from file " + CONCEPT_COUNT_FILE_NAME);
        }
    }

    /**
     * For all pair of different objects, acquire each others concept attributes as deep derived
     * concept attributes
     */
    private static void createDeepDerivedConceptFOCTables() {
        Util.getLogger().info(
                "Computing deep derived concept tables for the first time\nThis may take few minutes...\n");
        for (Table objectTable1 : getDB().getTables()) {
            if (objectTable1.hasTableType(DIMENSION_OBJECT_TABLE)) {
                for (Table objectTable2 : getDB().getTables()) {
                    if (objectTable2.equals(objectTable1)) {
                        continue;
                    }
                    if (objectTable2.hasTableType(DIMENSION_OBJECT_TABLE)) {
                        Vector<Table> fooTables = new Vector<>();
                        for (Table foo : getDB().getTables()) {
                            for (Attribute attr1 : foo.getAttributes()) {
                                if (attr1.getFkRefTo() != null && attr1.getFkRefTo()
                                        .equals(objectTable1.getPrimaryKey())) {
                                    for (Attribute attr2 : foo.getAttributes()) {
                                        if (attr2.getFkRefTo() != null && attr2.getFkRefTo()
                                                .equals(objectTable2.getPrimaryKey())) {
                                            fooTables.add(foo);
                                        }
                                    }
                                }
                            }
                        }
                        if (fooTables.size() > 0) {
                            computeDeepDerivedConceptFocTable(objectTable1.getPrimaryKey(),
                                    objectTable2.getPrimaryKey(), fooTables);
                        }
                    } else if (objectTable2.hasTableType(FACT_OBJECT_FREQ_CONCEPT_TABLE)) {
                        if (objectTable2.getObjectAttribute().getFkRefTo()
                                .equals(objectTable1.getPrimaryKey())) {
                            computeDeepDerivedRelationshipConceptFocTable(
                                    objectTable2.getObjectAttribute(),
                                    objectTable2.getConceptAttribute());
                        }
                    }
                }

            }
        }
    }

    private static void computeDeepDerivedRelationshipConceptFocTable(Attribute objectAttribute,
            Attribute conceptAttribute) {
        String id1 = objectAttribute.getName();
        String id2 = conceptAttribute.getName();

        Util.getLogger()
                .info("Computing deep derived concept attribute between object '"
                        + objectAttribute.getName() + "' and concept '" + conceptAttribute.getName()
                        + "'...");

        String newTableName = "_" + objectAttribute.getFkRefTo().getTable().getName() + "to"
                + conceptAttribute.getTable().getName() + "_" + conceptAttribute.getName();
        String qsTableCreation = "CREATE TABLE " + newTableName + "(" + id1
                + " INTEGER NOT NULL REFERENCES "
                + objectAttribute.getFkRefTo().getTable().getName() + "("
                + objectAttribute.getFkRefTo().getName() + "), " + id2;
        if (conceptAttribute.getFkRefTo() != null) {
            // foreign key, so type is integer
            qsTableCreation += " INTEGER REFERENCES "
                    + conceptAttribute.getFkRefTo().getTable().getName() + "("
                    + conceptAttribute.getFkRefTo().getName() + ")";
        } else if (conceptAttribute.isTextAttribute()) {
            qsTableCreation += " TEXT";
        } else {
            qsTableCreation += " INTEGER";
        }
        qsTableCreation += ", freq INTEGER";
        qsTableCreation += ", normalized_freq INTEGER)";

        String qsTemp1 = "CREATE MATERIALIZED VIEW tempView1 AS SELECT " + id1 + ", " + id2
                + ", count(*) as freq FROM " + objectAttribute.getTable().getName() + " WHERE "
                + id2 + " IS NOT NULL GROUP BY " + id1 + ", " + id2;
        String qsTemp2 = "CREATE MATERIALIZED VIEW tempView2 AS SELECT " + id1
                + ", SUM(freq) as sum FROM tempView1 group by " + id1;
        String dropTempView1 = "DROP MATERIALIZED VIEW tempView1";
        String dropTempView2 = "DROP MATERIALIZED VIEW tempView2";
        String qsInsert = "INSERT INTO " + newTableName + " SELECT tempView1." + id1 + ", " + id2
                + ", freq, ROUND(freq*100/sum) FROM tempView1, tempView2" + " WHERE tempView1."
                + id1 + " = tempView2." + id1;

        String qs1Indexing = "CREATE INDEX " + newTableName + "_idx" + " on " + newTableName + "("
                + id2 + ",freq)";
        String qs2Indexing = "CREATE INDEX " + newTableName + "_idx_2" + " on " + newTableName + "("
                + id1 + ")";
        String qsClustering = "CLUSTER " + newTableName + " USING " + newTableName + "_idx";

        (new Query(qsTableCreation)).execute();
        (new Query(qsTemp1)).execute();
        (new Query(qsTemp2)).execute();
        (new Query(qsInsert)).execute();
        (new Query(qs1Indexing)).execute();
        (new Query(qs2Indexing)).execute();
        (new Query(qsClustering)).execute();
        (new Query(dropTempView2)).execute();
        (new Query(dropTempView1)).execute();

    }

    /**
     * @param attr1Pk
     * @param attr2Pk
     * @param fooTables
     *            Computes deep derived concept attributes based on relationships on fooTables
     *            between object1 and object2
     */
    private static void computeDeepDerivedConceptFocTable(Attribute attr1Pk, Attribute attr2Pk,
            Vector<Table> fooTables) {

        String id1 = attr1Pk.getTable().getName() + "_" + attr1Pk.getName();
        String id2 = attr2Pk.getTable().getName() + "_" + attr2Pk.getName();
        String fooViewQueryString = "";
        for (Table fooTable : fooTables) {
            if (!fooViewQueryString.isEmpty()) {
                fooViewQueryString += " UNION ";
            }
            fooViewQueryString += "SELECT ";
            fooViewQueryString += attr1Pk.getFkAttributeFromTable(fooTable).getName() + " as " + id1
                    + ", ";
            fooViewQueryString += attr2Pk.getFkAttributeFromTable(fooTable).getName() + " as "
                    + id2;
            fooViewQueryString += " FROM " + fooTable.getName();
        }

        fooViewQueryString = "CREATE MATERIALIZED VIEW tempFooView  AS " + fooViewQueryString;
        String fooViewQueryIndex = "CREATE INDEX ON tempFooVIew(" + id2 + ")";

        boolean fooTempMaterialized = false;

        for (Attribute object2FkAttr : attr2Pk.getFkRefFromAttrs()) {
            if (object2FkAttr.getTable().hasTableType(FACT_OBJECT_CONCEPT_TABLE)) {
                for (Attribute ca : object2FkAttr.getTable().getAttributes()) {
                    if (ca.isConceptAttributeOrFKey()) {
                        Util.getLogger()
                                .info("Computing deep derived concept attribute between object '"
                                        + attr1Pk.getTable().getName() + "' and concept '"
                                        + ca.getFkRefTo().getTable().getName() + "'...");
                        if (!fooTempMaterialized) {
                            fooTempMaterialized = true;
                            (new Query(fooViewQueryString)).execute();
                            (new Query(fooViewQueryIndex)).execute();
                        }
                        String newTableName = "_" + attr1Pk.getTable().getName() + "to"
                                + ca.getFkRefTo().getTable().getName();
                        String qsTableCreation = "CREATE TABLE " + newTableName + "(" + id1
                                + " INTEGER NOT NULL REFERENCES " + attr1Pk.getTable().getName()
                                + "(" + attr1Pk.getName() + "), " + ca.getName();
                        if (ca.getFkRefTo() != null) {
                            // foreign key, so type is integer
                            qsTableCreation += " INTEGER REFERENCES "
                                    + ca.getFkRefTo().getTable().getName() + "("
                                    + ca.getFkRefTo().getName() + ")";
                        } else if (ca.isTextAttribute()) {
                            qsTableCreation += " TEXT";
                        } else {
                            qsTableCreation += " INTEGER";
                        }
                        qsTableCreation += ", freq INTEGER";
                        qsTableCreation += ", normalized_freq INTEGER)";

                        String qsTemp1 = "CREATE MATERIALIZED VIEW tempView1 AS SELECT " + id1
                                + ", " + ca.getName() + ", count(*) as freq FROM tempFooView, "
                                + object2FkAttr.getTable().getName() + " WHERE tempFooView." + id2
                                + " = " + object2FkAttr.getTable().getName() + "."
                                + object2FkAttr.getName() + " AND " + ca.getName()
                                + " IS NOT NULL GROUP BY " + id1 + ", " + ca.getName();
                        String qsTemp2 = "CREATE MATERIALIZED VIEW tempView2 AS SELECT " + id1
                                + ", SUM(freq) as sum FROM tempView1 group by " + id1;
                        String dropTempView1 = "DROP MATERIALIZED VIEW tempView1";
                        String dropTempView2 = "DROP MATERIALIZED VIEW tempView2";
                        String qsInsert = "INSERT INTO " + newTableName + " SELECT tempView1." + id1
                                + ", " + ca.getName()
                                + ", freq, ROUND(freq*100/sum) FROM tempView1, tempView2"
                                + " WHERE tempView1." + id1 + " = tempView2." + id1;

                        String qs1Indexing = "CREATE INDEX " + newTableName + "_idx" + " on "
                                + newTableName + "(" + ca.getName() + ",freq)";
                        String qs2Indexing = "CREATE INDEX " + newTableName + "_idx_2" + " on "
                                + newTableName + "(" + id1 + ")";
                        String qsClustering = "CLUSTER " + newTableName + " USING " + newTableName
                                + "_idx";

                        (new Query(qsTableCreation)).execute();
                        (new Query(qsTemp1)).execute();
                        (new Query(qsTemp2)).execute();
                        (new Query(qsInsert)).execute();
                        (new Query(dropTempView2)).execute();
                        (new Query(dropTempView1)).execute();
                        (new Query(qs1Indexing)).execute();
                        (new Query(qs2Indexing)).execute();
                        (new Query(qsClustering)).execute();
                    }
                }
            }
        }

        for (Attribute ca : attr2Pk.getTable().getAttributes()) {
            if (ca.isConceptAttributeOrFKey()) {
                Util.getLogger().info("Computing deep derived concept attribute between object '"
                        + attr1Pk.getTable().getName() + "' and concept '" + ca.getName() + "'...");
                if (!fooTempMaterialized) {
                    fooTempMaterialized = true;
                    (new Query(fooViewQueryString)).execute();
                    (new Query(fooViewQueryIndex)).execute();
                }

                String newTableName = "_" + attr1Pk.getTable().getName() + "to" + ca.getName();
                String qsTableCreation = "CREATE TABLE " + newTableName + "(" + id1
                        + " INTEGER NOT NULL REFERENCES " + attr1Pk.getTable().getName() + "("
                        + attr1Pk.getName() + "), " + ca.getName();
                if (ca.getFkRefTo() != null) {
                    // foreign key, so type is integer
                    qsTableCreation += " INTEGER REFERENCES " + ca.getFkRefTo().getTable().getName()
                            + "(" + ca.getFkRefTo().getName() + ")";
                } else if (ca.isTextAttribute()) {
                    qsTableCreation += " TEXT";
                } else {
                    qsTableCreation += " INTEGER";
                }

                qsTableCreation += ", freq INTEGER";
                qsTableCreation += ", normalized_freq INTEGER)";

                String qsTemp1 = "CREATE MATERIALIZED VIEW tempView1 AS SELECT " + id1 + ", "
                        + ca.getName() + ", count(*) as freq FROM tempFooView, "
                        + attr2Pk.getTable().getName() + " WHERE tempFooView." + id2 + " = "
                        + attr2Pk.getTable().getName() + "." + attr2Pk.getName() + " AND "
                        + ca.getName() + " IS NOT NULL  GROUP BY " + id1 + ", " + ca.getName();
                String qsTemp2 = "CREATE MATERIALIZED VIEW tempView2 AS SELECT " + id1
                        + ", SUM(freq) as sum FROM tempView1 group by " + id1;
                String dropTempView1 = "DROP MATERIALIZED VIEW tempView1";
                String dropTempView2 = "DROP MATERIALIZED VIEW tempView2";
                String qsInsert = "INSERT INTO " + newTableName + " SELECT tempView1." + id1 + ", "
                        + ca.getName() + ", freq, ROUND(freq*100/sum) FROM tempView1, tempView2"
                        + " WHERE tempView1." + id1 + " = tempView2." + id1;

                String qs1Indexing = "CREATE INDEX " + newTableName + "_idx" + " on " + newTableName
                        + "(" + ca.getName() + ",freq)";
                String qs2Indexing = "CREATE INDEX " + newTableName + "_idx_2" + " on "
                        + newTableName + "(" + id1 + ")";
                String qsClustering = "CLUSTER " + newTableName + " USING " + newTableName + "_idx";

                (new Query(qsTableCreation)).execute();
                (new Query(qsTemp1)).execute();
                (new Query(qsTemp2)).execute();
                (new Query(qsInsert)).execute();
                (new Query(dropTempView2)).execute();
                (new Query(dropTempView1)).execute();
                (new Query(qs1Indexing)).execute();
                (new Query(qs2Indexing)).execute();
                (new Query(qsClustering)).execute();
            }
        }
        if (fooTempMaterialized) {
            String dropView = "DROP MATERIALIZED VIEW tempFooView";
            (new Query(dropView)).execute();
        }
    }

    /**
     * index all text columns in all tables in inverted column index
     */

    private static void createInvertedColumnIndex() {
        (new Query(
                "CREATE TABLE _invertedColumnIndex(word text, tabName text, colName text, UNIQUE(word, tabname, colName));"))
                        .execute();
        db.addInvertedColumnIndexTable();
        // first time. Create the inverted column index now
        Util.getLogger().info(
                "Creating inverted column index for the first time\nThis may take few minutes...\n");
        String invertedColumnIndexTableName = db.getInvertedColumnIndexTableName();
        for (Table table : db.getTables()) {
            if (!table.getName().equalsIgnoreCase(invertedColumnIndexTableName)) {
                for (Attribute attr : table.getAttributes()) {
                    if (attr.isTextAttribute()) {
                        indexTableAttribute(invertedColumnIndexTableName, table.getName(),
                                attr.getName());
                    }
                }
            }
        }
        clusterInvertedColumnIndex();
        Util.getLogger().info("Done");
    }

    /**
     * Creates clustered index on inverted column index
     */

    private static void clusterInvertedColumnIndex() {
        try {
            // create clustered index
            (new Query("create index on _invertedcolumnindex(word);")).execute();
            (new Query("cluster _invertedcolumnindex using _invertedcolumnindex_word_idx;"))
                    .execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param invertedColumnIndexTableName
     * @param tabName
     * @param attrName
     *            Adds data from tabName.attrName to inverted column index by transforming all text
     *            to lower case
     */

    private static void indexTableAttribute(String invertedColumnIndexTableName, String tabName,
            String attrName) {
        try {
            (new Query("INSERT INTO " + invertedColumnIndexTableName + " SELECT " + attrName + ", '"
                    + tabName + "', '" + attrName + "' from " + tabName + " where length("
                    + attrName + ") > 0 and " + attrName + " is not null on conflict do nothing;"))
                            .execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Util.getLogger().info("Done indexing table " + tabName + ", column " + attrName);
    }

    public static String getNextAlias() {
        if (aliasCount < 26) {
            return (char) ('a' + aliasCount++) + "";
        }
        int temp = aliasCount;
        aliasCount = (temp / 26) - 1;
        String aliasPrefix = getNextAlias();
        aliasCount = temp % 26;
        String aliasSuffix = getNextAlias();
        aliasCount = temp;
        aliasCount++;
        return aliasPrefix + aliasSuffix + "1";
    }

    public static String applyFunction(String functionOnAttribute, String attributeString,
            boolean isDistinctProj) {
        if (functionOnAttribute == null) {
            return attributeString;
        }
        return functionOnAttribute + "(" + (isDistinctProj ? " DISTINCT " : "") + attributeString
                + ")";
    }

    public static String getOperatorSymbol(int operator) {
        /**
         * TODO handle all operator types as required later No need to handle other type of
         * operators right now
         */
        if (operator == Operator.GREATER_THAN_EQUALS) {
            return ">=";
        }
        if (operator == Operator.LESS_THAN_EQUALS) {
            return "<=";
        }
        if (operator == SQLOperator.IN) {
            return "IN";
        }
        if (operator == SQLOperator.CONTAINS) {
            return "@>";
        }
        if (operator == SQLOperator.IS_CONTAINED_BY) {
            return "<@";
        }
        return "";
    }

    public static Attribute getAggregateConceptAttribute(Attribute conceptConditionAttribute) {
        for (Table t : getDB().getTables()) {
            if ((conceptConditionAttribute.getTable().hasTableType(DBUtil.FACT_OBJECT_CONCEPT_TABLE)
                    && t.hasTableType(DBUtil.AGGR_OBJECT_CONCEPT_TABLE))
                    || (conceptConditionAttribute.getTable()
                            .hasTableType(DBUtil.FACT_OBJECT_OBJECT_TABLE)
                            && t.hasTableType(DBUtil.AGGR_OBJECT_OBJECT_TABLE))) {
                for (Attribute aggregateAttribute : t.getAttributes()) {
                    if (aggregateAttribute.getFkRefTo() == null) {
                        continue;
                    }
                    if (aggregateAttribute.isAggregateAttribute() && aggregateAttribute.getFkRefTo()
                            .equals(conceptConditionAttribute.getFkRefTo())) {
                        for (Attribute objectAttribute : t.getAttributes()) {
                            if (objectAttribute.equals(aggregateAttribute)
                                    || objectAttribute.getFkRefTo() == null) {
                                continue;
                            }
                            for (Attribute anotherObjectAttribute : aggregateAttribute.getTable()
                                    .getAttributes()) {
                                if (!anotherObjectAttribute.isAggregateAttribute()
                                        && anotherObjectAttribute.getFkRefTo()
                                                .equals(objectAttribute.getFkRefTo())) {
                                    return aggregateAttribute;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Attribute getObjectAttribute(Attribute conceptConditionAggregateAttribute,
            Attribute conceptConditionAttribute) {
        for (Attribute aggrObjectAttribute : conceptConditionAggregateAttribute.getTable()
                .getAttributes()) {
            for (Attribute objectAttribute : conceptConditionAttribute.getTable().getAttributes()) {
                if (!aggrObjectAttribute.isAggregateAttribute()
                        && objectAttribute.getFkRefTo().isEqual(aggrObjectAttribute.getFkRefTo())
                        && objectAttribute.getFkRefTo().getTable()
                                .hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)) {
                    return new AttributeWithTable(aggrObjectAttribute,
                            conceptConditionAggregateAttribute.getTable());
                }
            }
        }
        return null;
    }

    /**
     * Stores all filters with computed selectivity to a persistent memory
     */
    private static void storeDbFilters() {
        loadDBFilters();
        try {
            FileOutputStream fout = new FileOutputStream(CONCEPT_COUNT_FILE_NAME);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(dbFilters);
            oos.close();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new QbeeException(e.getMessage());
        }
    }

    /**
     * For each concept attribute, generate all possible simple uniform filters and compute their
     * selectivity
     */
    private static void computeFilterSelectivity() {
        dbFilters.clear();
        for (Table t : getDB().getTables()) {
            if (t.hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)
                    || t.hasTableType(DBUtil.FACT_OBJECT_CONCEPT_TABLE)
                    || t.hasTableType(DBUtil.FACT_OBJECT_OBJECT_TABLE)) {
                for (Attribute a : t.getAttributes()) {
                    if (a.isConceptAttributeOrFKey()) {
                        String totalCountQuery = "SELECT COUNT(*) FROM " + t.getName() + " WHERE "
                                + t.getName() + "." + a.getName() + " IS NOT NULL";
                        Query q = new Query(totalCountQuery);
                        q.executeQuery();
                        int totalCount = Integer.parseInt(q.getResult().elementAt(0).elementAt(0));
                        double globalMin = 0, globalMax = 0;

                        String selectivityQuery = "SELECT DISTINCT " + t.getName() + "."
                                + a.getName() + ", COUNT(*), 0 as lessEqlCol FROM " + t.getName()
                                + " WHERE " + t.getName() + "." + a.getName()
                                + " IS NOT NULL  GROUP BY " + t.getName() + "." + a.getName();
                        if (!a.isTextAttribute()) {
                            selectivityQuery = "SELECT DISTINCT " + a.getName() + ", COUNT(*), ("
                                    + "SELECT COUNT(*) FROM " + t.getName() + " WHERE "
                                    + a.getName() + " <= outerTable." + a.getName()
                                    + ") as lessEqlCol FROM " + t.getName() + " outerTable WHERE "
                                    + a.getName() + " IS NOT NULL  GROUP BY " + a.getName();
                            String globalMinQuery = "SELECT MIN(" + a.getName() + ") FROM "
                                    + t.getName();
                            q = new Query(globalMinQuery);
                            q.executeQuery();
                            globalMin = Double.parseDouble(q.getResult().get(0).get(0));

                            String globalMaxQuery = "SELECT MAX(" + a.getName() + ") FROM "
                                    + t.getName();
                            q = new Query(globalMaxQuery);
                            q.executeQuery();
                            globalMax = Double.parseDouble(q.getResult().get(0).get(0));
                        }
                        q = new Query(selectivityQuery);
                        q.executeQuery();
                        Vector<Vector<String>> rowCountPerConcept = q.getResult();
                        for (Vector<String> vs : rowCountPerConcept) {
                            String key = vs.elementAt(0);
                            Integer currentConceptCount = Integer.parseInt(vs.elementAt(1));
                            Integer currentLessOrEqualCount = Integer.parseInt(vs.elementAt(2));
                            Filter filter = new Filter(t, a, key);
                            filter.setInverseSelectivity(currentConceptCount / (double) totalCount);
                            filter.setInverseSelectivityLessOrEqual(
                                    currentLessOrEqualCount / (double) totalCount);
                            filter.setGlobalMin(globalMin);
                            filter.setGlobalMax(globalMax);
                            dbFilters.put(filter.toString(), filter);
                        }
                    }
                }
            } else if (t.hasTableType(DBUtil.FACT_OBJECT_DEEP_FREQ_CONCEPT_TABLE)) {
                // Store selectivity (attribute(value) <= freq)

                for (Attribute a : t.getAttributes()) {
                    if (a.getFkRefTo() != null
                            && a.getFkRefTo().getTable().hasTableType(DBUtil.DIMENSION_OBJECT_TABLE)
                            || a.getName().contains("freq")) {
                        continue;
                    }
                    // this is a concept attribute
                    String selectivityQuery = "SELECT DISTINCT " + a.getName() + ", "
                            + FREQUENCY_MODE + ", (SELECT COUNT(*) FROM " + t.getName() + " WHERE "
                            + a.getName() + " = outerTable." + a.getName() + " AND "
                            + FREQUENCY_MODE + " <= outerTable." + FREQUENCY_MODE + ") FROM "
                            + t.getName() + " outerTable GROUP BY " + a.getName() + ", "
                            + FREQUENCY_MODE + " ORDER BY " + a.getName() + "," + FREQUENCY_MODE;
                    Query q = new Query(selectivityQuery);
                    q.executeQuery();
                    Vector<Vector<String>> rowCountPerConceptAndFreq = q.getResult();

                    String prevConcept = null;
                    int prevCount = 0;
                    int totalCount = 0;

                    for (Vector<String> vs : rowCountPerConceptAndFreq) {
                        String key = vs.elementAt(0);
                        Integer frequency = Integer.parseInt(vs.elementAt(1));
                        Integer currentConceptCount = Integer.parseInt(vs.elementAt(2));
                        Filter filter = new Filter(t, a, key, frequency);

                        if (!key.equals(prevConcept)) {
                            prevCount = 0;
                            String keyWithQuote = key;
                            if (a.isTextAttribute()) {
                                key = key.replaceAll("'", "''");
                                keyWithQuote = "'" + key + "'";
                            }
                            String totalCountQuery = "SELECT COUNT(*) FROM " + t.getName()
                                    + " WHERE " + a.getName() + " = " + keyWithQuote;
                            Query tq = new Query(totalCountQuery);
                            tq.executeQuery();
                            totalCount = Integer.parseInt(tq.getResult().elementAt(0).elementAt(0));
                        }

                        filter.setInverseSelectivityLessOrEqual(
                                currentConceptCount / (double) totalCount);
                        filter.setInverseSelectivityEqual(
                                (currentConceptCount - prevCount) / (double) totalCount);
                        prevConcept = key;
                        prevCount = currentConceptCount;
                        dbFilters.put(filter.toString(), filter);
                    }
                }
            }
        }
        storeDbFilters();
        Util.getLogger().info("Selectivity computed for " + dbFilters.size() + " Filters");
    }

    /**
     * Find a pre-stored filter with selectivity computed
     */
    public static Filter getFilter(Filter search) {
        if (dbFilters.containsKey(search.toString())) {
            return dbFilters.get(search.toString());
        }
        Util.getLogger().warning("No filter found for " + search);
        return null;
    }

    public static Table findAggregateTable(Table joinTable) {
        if (joinTable.hasTableType(AGGR_OBJECT_CONCEPT_TABLE)
                || joinTable.hasTableType(AGGR_OBJECT_OBJECT_TABLE)) {
            return joinTable;
        }
        for (Table t : getDB().getTables()) {
            if (t.hasTableType(DBUtil.AGGR_OBJECT_CONCEPT_TABLE)) {
                if (joinTable.getObjectAttribute().getFkRefTo()
                        .isEqual(t.getObjectAttribute().getFkRefTo())
                        && joinTable.getConceptAttribute().getFkRefTo()
                                .isEqual(t.getConceptAttribute().getFkRefTo())) {
                    return new TableWithAlias(t, DBUtil.getNextAlias());
                }
            }
        }
        return null;
    }

    public static Attribute findAggregateConceptCondition(Attribute conceptConditionAttribute,
            Table aggrJoinTable) {
        if (conceptConditionAttribute.getTable().equals(aggrJoinTable)) {
            return conceptConditionAttribute;
        }
        for (Attribute attr : aggrJoinTable.getAttributes()) {
            if (attr.getFkRefTo() != null
                    && attr.getFkRefTo().isEqual(conceptConditionAttribute.getFkRefTo())) {
                return attr;
            }
        }
        return null;
    }

    public static Vector<Table> getReverseAggregateTables(Table table) {
        String sourceTableName = table.getRealName().split("aggr_aoo_")[1].split("_")[0];
        Vector<Table> ret = new Vector<>();
        for (Table t : getDB().getTables()) {
            if (t.getRealName().contains(sourceTableName)) {
                ret.add(t);
            }
        }
        return ret;
    }

    public static String getDbName(String dbName) {
        return DB_NAME_MAP.get(dbName);
    }
}
