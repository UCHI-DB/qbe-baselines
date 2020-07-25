package gui;

import javafx.scene.control.CheckBoxTreeItem;
import util.Util;

public class SQuIDCheckboxTreeItem extends CheckBoxTreeItem<String> {
    protected QbeeGuiMain mainClass;

    public SQuIDCheckboxTreeItem(String name, QbeeGuiMain mc) {
        super(name);
        this.mainClass = mc;
        this.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (Util.STILL_LOADING == true) {
                return;
            }
            mainClass.submitFeedback(true);
        });
    }
}
