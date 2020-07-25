package main;

import java.util.Scanner;
import java.util.Vector;

import fetch.CandidateQuery;
import fetch.ExampleTable;
import fetch.QueryFetcher;
import query.Query;
import util.DBUtil;
import util.QbeeOptions;
import util.Util;

public class ConsoleMain {

    public static QbeeOptions options;
    private static boolean confidenceMode;

    public static void main(String[] args) {
        options = new QbeeOptions(args);
        Util.beginStartUp(options);
        DBUtil.GUI = false;
        Scanner scanner = new Scanner(System.in);
        int nRow = scanner.nextInt();
        int nCol = scanner.nextInt();
        scanner.nextLine();

        ExampleTable et = new ExampleTable(nCol);
        for (int i = 0; i < nRow; i++) {
            Vector<String> currentRow = new Vector<>();
            for (int j = 0; j < nCol; j++) {
                String now = scanner.nextLine();
                currentRow.add(now);
            }
            et.addRow(currentRow);
        }
        scanner.close();
        fetchQuery(et);
        confidenceMode = false;
        if (confidenceMode) {
            Vector<Integer> cardinalities = new Vector<>();
            for (int i = 2; i <= et.getRowSize(); i++) {
                String queryString = fetchQuery(et.subset(i));
                Query query = new Query(queryString);
                query.executeQuery();
                int curCardinality = query.getResult().size();
                cardinalities.add(curCardinality);

                System.out.print("Cardinality:");
                for (Integer c : cardinalities) {
                    System.out.print(c + ":");
                }
                System.out.println("");
                int last = cardinalities.lastElement();
                int count = 0;
                for (int c = cardinalities.size() - 2; c >= 0; c--) {
                    if (cardinalities.elementAt(c) < last) {
                        break;
                    }
                    count++;
                }
                System.out.println("VALUE of T:" + count);
            }
        }
    }

    protected static String fetchQuery(ExampleTable exampleTable) {
        QueryFetcher queryFetcher = new QueryFetcher(exampleTable);
        Vector<CandidateQuery> candidateQueries = queryFetcher.getCandidateQueries();
        for (CandidateQuery cq : candidateQueries) {
            cq.computeFilters();
        }
        if (candidateQueries.size() == 0) {
            System.out.println("OUTPUT: Invalid example table wrt DB instance.");
            return "";
        }

        if (candidateQueries.size() == 0) {
            System.out.println("OUTPUT: No valid query found.");
            return "";
        }
        String queryString = Query.unionAll(candidateQueries);
        if (confidenceMode) {
            System.out.println("OUTPUT: Query: $" + exampleTable.getRowSize() + "$ " + queryString);
        } else {
            System.out.println("OUTPUT: Query: " + queryString);
        }

        return queryString;
    }
}
