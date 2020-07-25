package dbms;

public class TableWithAlias extends Table {
    String alias;
    Table table;

    public TableWithAlias(Table table, String alias) {
        super(table);
        this.table = table;
        this.alias = alias;
    }

    public String getName() {
        return alias;
    }

    public Table getTable() {
        return table;
    }

    public String getRealName() {
        return table.getRealName();
    }
}
