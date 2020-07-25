package gui;

import java.util.Vector;

import concept.ConceptAttribute;
import plume.Pair;

public class BaseConceptTreeItem extends SQuIDCheckboxTreeItem {

    private ConceptAttribute conceptAttribute;
    private Vector<Pair<String, String>> concepts;

    public BaseConceptTreeItem(String name, ConceptAttribute conceptAttribute, QbeeGuiMain mc) {
        super(name, mc);
        if (name.equals("institute")) {
            setValue("affiliation  >");
        } else {
            setValue(name + "  >");
        }
        this.conceptAttribute = conceptAttribute;
        concepts = new Vector<>();
    }

    public ConceptAttribute getConceptAttribute() {
        return conceptAttribute;
    }

    public boolean addConcept(Pair<String, String> curConcept) {
        if (concepts.contains(curConcept)) {
            return false;
        }
        concepts.add(curConcept);
        return true;
    }

    public Vector<Pair<String, String>> getConcepts() {
        return concepts;
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
}
