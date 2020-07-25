package gui;

import java.util.Vector;

import concept.FrequencyConcept;
import concept.FrequencyConceptAttribute;

public class BaseFreqConceptTreeItem extends SQuIDCheckboxTreeItem {

    private FrequencyConceptAttribute freqConceptAttribute;
    private Vector<FrequencyConcept> freqConcepts;

    public BaseFreqConceptTreeItem(String name, FrequencyConceptAttribute fca, QbeeGuiMain mc) {
        super("* " + name + ">", mc);
        this.freqConceptAttribute = fca;
        freqConcepts = new Vector<>();
    }

    public FrequencyConceptAttribute getFreqConceptAttribute() {
        return freqConceptAttribute;
    }

    public Vector<FrequencyConcept> getFreqConcepts() {
        return freqConcepts;
    }

    public void add(CheckboxItemWithHitBar item, boolean itemIsSelected) {
        if (!item.isSelected() && !item.isIndeterminate() && !itemIsSelected) {
            if (isSelected()) {
                setIndeterminate(true);
            }
        }
        if (item.isSelected() || item.isIndeterminate() || itemIsSelected) {
            if (getChildren().size() > 0 && !isSelected()) {
                setIndeterminate(true);
            }
            if (getChildren().size() == 0 && !isSelected()) {
                setSelected(true);
            }
        }
        getChildren().add(item);
        item.setSelected(itemIsSelected);
    }

    public boolean addConcept(FrequencyConcept curConcept) {
        freqConcepts.add(curConcept);
        return true;
    }
}
