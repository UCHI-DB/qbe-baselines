package dbms;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import plume.Pair;

public class SchemaReader implements StatementVisitor {

    private Map<String, Vector<Pair<String, String>>> tableToCols = new HashMap<>();
    private Vector<ForeignKeyConstraint> fkConstraints;
    private Vector<PrimaryKeyConstraint> pkConstraints;

    public SchemaReader(Statements stmt, Vector<PrimaryKeyConstraint> pkConstraints,
            Vector<ForeignKeyConstraint> fkConstraints) {
        stmt.accept(this);
        this.pkConstraints = pkConstraints;
        this.fkConstraints = fkConstraints;
    }

    public DB getDB() {
        DB db = new DB();
        for (Entry<String, Vector<Pair<String, String>>> tabAndCols : tableToCols.entrySet()) {
            String tableName = tabAndCols.getKey();
            Table table = new Table(tableName);
            for (Pair<String, String> attr : tabAndCols.getValue()) {
                table.addAttr(attr.a, attr.b);
            }
            db.addTable(table);
        }

        for (PrimaryKeyConstraint pk : pkConstraints) {
            db.addPrimaryKeyConstraint(pk.tableName, pk.attrName);

        }
        for (ForeignKeyConstraint fk : fkConstraints) {
            db.addForeignKeyConstraint(fk.fromTableName, fk.fromAttrName, fk.toTableName,
                    fk.toAttrName);

        }
        return db;
    }

    @Override
    public void visit(CreateTable arg0) {
        String table = formatTabOrCol(arg0.getTable().getName());
        Vector<Pair<String, String>> cols = new Vector<>();
        for (ColumnDefinition colDefn : arg0.getColumnDefinitions()) {
            Pair<String, String> currentCol = new Pair<String, String>(
                    formatTabOrCol(colDefn.getColumnName()), colDefn.getColDataType().toString());
            cols.add(currentCol);
        }
        tableToCols.put(table, cols);
    }

    private static String formatTabOrCol(String tabOrCol) {
        if (tabOrCol.contains("\"")) {
            return tabOrCol.replace("\"", "").toLowerCase();
        }
        return tabOrCol.toLowerCase();
    }

    @Override
    public void visit(CreateView arg0) {
    }

    @Override
    public void visit(Statements arg0) {
        for (Statement stmt : arg0.getStatements()) {
            stmt.accept(this);
        }
    }

    /**
     * Read in tables and columns in the schema from a SQL script with CREATE TABLE statements
     */

    @Override
    public void visit(Select arg0) {
    }

    @Override
    public void visit(Delete arg0) {
    }

    @Override
    public void visit(Update arg0) {
    }

    @Override
    public void visit(Insert arg0) {
    }

    @Override
    public void visit(Replace arg0) {
    }

    @Override
    public void visit(Drop arg0) {
    }

    @Override
    public void visit(Truncate arg0) {
    }

    @Override
    public void visit(CreateIndex arg0) {
    }

    @Override
    public void visit(Alter arg0) {
    }

    @Override
    public void visit(Execute arg0) {
    }

    @Override
    public void visit(SetStatement arg0) {
    }

    @Override
    public void visit(AlterView arg0) {

    }

    @Override
    public void visit(Merge arg0) {

    }
}
