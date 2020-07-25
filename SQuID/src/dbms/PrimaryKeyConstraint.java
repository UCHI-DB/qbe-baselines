package dbms;

import java.util.regex.Pattern;

/*
 * Works for this syntax only "ALTER TABLE ONLY certificate ADD CONSTRAINT certificate_pkey PRIMARY
 * KEY (id);"
 * 
 * TODO This is ugly parsing. We need to make it more robust.
 */

public class PrimaryKeyConstraint {

    String tableName, attrName;

    public PrimaryKeyConstraint(String query) {

        String left = query.split(" ADD CONSTRAINT")[0];
        String right = query.split(" ADD CONSTRAINT")[1];

        tableName = left.split("ALTER TABLE ONLY ")[1];
        attrName = right.split("PRIMARY KEY ")[1].split(Pattern.quote("("))[1]
                .split(Pattern.quote(")"))[0];
    }
}
