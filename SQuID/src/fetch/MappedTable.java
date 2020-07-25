package fetch;

import java.util.Vector;

import dbms.Attribute;
import dbms.Table;

class MappedTable {
    Table table;
    Vector<Attribute> attributeMapping;

    MappedTable(Table table, Vector<Attribute> attributeMapping) {
        this.table = table;
        this.attributeMapping = attributeMapping;
    }
}
