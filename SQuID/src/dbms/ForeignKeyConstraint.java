package dbms;

import java.util.regex.Pattern;

/*
 * Works for this syntax only "ALTER TABLE ONLY movie_companies ADD CONSTRAINT movie_id_exists
 * FOREIGN KEY (movie_id) REFERENCES title(id);"
 * 
 * TODO This is ugly parsing. We need to make it more robust.
 */

public class ForeignKeyConstraint {

    String fromTableName, fromAttrName, toTableName, toAttrName;

    public ForeignKeyConstraint(String query) {

        String left = query.split(" ADD CONSTRAINT")[0];
        String right = query.split(" ADD CONSTRAINT")[1];

        fromTableName = left.split("ALTER TABLE ONLY ")[1];
        fromAttrName = right.split(Pattern.quote("("))[1].split(Pattern.quote(")"))[0];
        toTableName = right.split("REFERENCES ")[1].split(Pattern.quote("("))[0];
        toAttrName = right.split("REFERENCES ")[1].split(Pattern.quote("("))[1]
                .split(Pattern.quote(")"))[0];
    }
}
