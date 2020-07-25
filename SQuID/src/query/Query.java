package query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import fetch.CandidateQuery;
import util.DBUtil;
import util.Util;

public class Query {

    private String queryString;
    private Vector<Vector<String>> result;
    private Vector<String> header;
    private Logger logger;
    private Vector<Integer> maxWidthOfValue;

    public Query(String queryString) {
        this.queryString = queryString;
        result = new Vector<>();
        header = new Vector<>();
        maxWidthOfValue = new Vector<>();
        logger = Util.getLogger();
    }

    public void execute() {
        Util.getLogger().info("Executing query: " + queryString);

        try {
            Connection conn = DBUtil.getConnection();
            Statement st = conn.createStatement();
            st.execute(queryString);
            st.close();
            conn.commit();
        } catch (Exception e) {
            logger.warning("Error executing query " + queryString);
        }
    }

    public void executeQuery(int limit, int offset) {
        queryString = queryString + " LIMIT " + limit;
        queryString = queryString + " OFFSET " + offset;
        executeQuery();
    }

    public void executeQuery() {
        Util.getLogger().info("Executing query: " + queryString);
        Statement st;
        try {
            st = DBUtil.getConnection().createStatement();

            st.setFetchSize(0);
            ResultSet resultSet = st.executeQuery(queryString);

            for (int colNumber = 0; colNumber < resultSet.getMetaData()
                    .getColumnCount(); colNumber++) {
                header.add(resultSet.getMetaData().getColumnLabel(colNumber + 1));
                maxWidthOfValue.add(0);
            }

            while (resultSet.next()) {
                Vector<String> thisRow = new Vector<>();
                for (int colNumber = 0; colNumber < resultSet.getMetaData()
                        .getColumnCount(); colNumber++) {
                    String currentString = resultSet.getString(colNumber + 1);
                    thisRow.add(currentString);

                    if (currentString != null && !currentString.isEmpty()
                            && currentString.length() > maxWidthOfValue.get(colNumber)) {
                        maxWidthOfValue.set(colNumber, currentString.length());
                    }
                }
                result.add(thisRow);
            }
            resultSet.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Vector<String> getHeader() {
        return header;
    }

    public Vector<Vector<String>> getResult() {
        return result;
    }

    public String getQueryString() {
        return queryString;
    }

    public static String unionAll(Vector<CandidateQuery> candidateQueries) {
        String query = "";
        for (CandidateQuery sq : candidateQueries) {
            String kk = sq.getQuery().getQueryString();
            if (kk.length() > query.length()) {
                query = kk;
            }
        }
        return query;
    }

    public String getFormattedResult() {
        StringBuilder sb = new StringBuilder("Result tuples:\n");
        StringBuilder sbBar = new StringBuilder();

        int colNum = 0;
        for (String colHeader : header) {
            if (colNum > 0) {
                sb.append("  |  ");
                sbBar.append("--+--");
            }
            sb.append(String.format("%-" + maxWidthOfValue.get(colNum) + "s", colHeader));
            sbBar.append(String.format(StringUtils.repeat("-", maxWidthOfValue.get(colNum++))));
        }
        sb.append("\n");
        sb.append(sbBar);
        sb.append("\n");
        colNum = 0;
        for (Vector<String> row : result) {
            for (String value : row) {
                if (colNum > 0) {
                    sb.append("  |  ");
                }
                sb.append(String.format("%-" + maxWidthOfValue.get(colNum++) + "s", value));
            }
            sb.append("\n");
        }
        sb.append("(" + result.size() + " row" + (result.size() > 1 ? "s" : "") + ")" + "\n");
        return sb.toString();
    }
}
