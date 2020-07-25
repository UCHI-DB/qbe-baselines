package dbms;

public class AttributeWithTable extends Attribute {
    Table table;
    private Attribute attribute;

    public AttributeWithTable(Attribute attribute, Table table) {
        super(attribute);
        this.attribute = attribute;
        this.table = table;
    }

    @Override
    public Table getTable() {
        return table;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    @Override
    public boolean isEqual(Attribute another) {
        Attribute anotherAttribute;
        if (another instanceof AttributeWithTable) {
            anotherAttribute = ((AttributeWithTable) another).getAttribute();
        } else {
            anotherAttribute = another;
        }
        if (getAttribute().isEqual(anotherAttribute)) {
            return true;
        }
        return false;
    }
}