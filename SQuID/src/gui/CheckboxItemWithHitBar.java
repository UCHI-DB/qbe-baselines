package gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

public class CheckboxItemWithHitBar extends SQuIDCheckboxTreeItem {

    public CheckboxItemWithHitBar(String name, QbeeGuiMain mc, double confidence,
            boolean isDroppedDueToLowFScore, boolean isSelected) {
        super("", mc);
        double LOWER_LIMIT = 0.2;
        double logConf;
        if (confidence <= 1) {
            logConf = confidence / (1 / LOWER_LIMIT); // map from 0.1 to LOWER_LIMIT + 0.1
            logConf += 0.1;
        } else {
            logConf = Math.log(confidence);
            logConf /= 20;
            if (!isDroppedDueToLowFScore && !isSelected) {
                logConf /= 3;
            }
            logConf += LOWER_LIMIT + 0.1; // map from LOWER_LIMIT + 0.1 to 1
            logConf = Math.min(logConf, 1);
        }

        Label label = new Label(name);
        label.setPadding(new Insets(0, 5, 0, 0));
        // System.out.println(name + " " + confidence + " " + logConf);
        ProgressBar hitbar = new ProgressBar(logConf);
        HBox hbox = new HBox();
        hbox.getChildren().addAll(label, hitbar);
        setGraphic(hbox);
    }
}
