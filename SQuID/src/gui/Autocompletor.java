/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package gui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import query.Query;
import util.DBUtil;
import util.Util;

/**
 * @author zoeysun
 */
public class Autocompletor {
    private Set<String> keyWords;
    String FXMLOADER_LOCATION = "FXMLDocument.fxml";

    public Autocompletor(String invertedColumnIndexTable) {
        String KEYWORD_DUMP_LOCATION = "data/" + DBUtil.DB_NAME + "_keywords.bin";
        keyWords = new HashSet<String>();
        try {
            FileInputStream fin = new FileInputStream(KEYWORD_DUMP_LOCATION);
            ObjectInputStream ois = new ObjectInputStream(fin);
            keyWords.addAll((HashSet<String>) ois.readObject());
            Util.getLogger().info("Loaded " + keyWords.size() + " keywords");
            ois.close();
            fin.close();
        } catch (Exception e) {
            Query query = new Query("select word from " + invertedColumnIndexTable);
            query.executeQuery();
            for (Vector<String> word : query.getResult()) {
                keyWords.add(word.elementAt(0));
            }
            try {
                FileOutputStream fout = new FileOutputStream(KEYWORD_DUMP_LOCATION);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(keyWords);
                oos.close();
                fout.close();
                Util.getLogger().info("Stored " + keyWords.size() + " keywords.");
            } catch (Exception e1) {
                Util.getLogger().severe("Could not store keywords.");
                e.printStackTrace();
            }
        }
    }

    public Set<String> getKeywords() {
        return keyWords;
    }
}
