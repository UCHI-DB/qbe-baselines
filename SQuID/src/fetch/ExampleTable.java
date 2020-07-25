package fetch;

import java.util.Vector;

public class ExampleTable {
    private int nCol;
    private int nRow;
    private Vector<Vector<String>> data = new Vector<>();

    public ExampleTable(int nC) {
        this.nCol = nC;
    }

    public void addRow(Vector<String> currentRow) {
        data.addElement(currentRow);
        nRow = data.size();
    }

    public void deleteRow(int idx) {
        data.remove(idx);
        nRow = data.size();
    }

    public Vector<String> getRow(int idx) {
        return data.elementAt(idx);
    }

    public int getRowSize() {
        return nRow;
    }

    public int getColSize() {
        return nCol;
    }

    String getElement(int i, int j) {
        return getRow(i).get(j);
    }

    public Vector<Vector<String>> getData() {
        return data;
    }

    public ExampleTable subset(int sz) {
        ExampleTable subTable = new ExampleTable(this.nCol);
        for (int i = 0; i < sz; i++) {
            subTable.addRow(getRow(i));
        }
        return subTable;
    }
}