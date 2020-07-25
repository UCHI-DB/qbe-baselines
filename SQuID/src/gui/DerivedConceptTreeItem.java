package gui;

import java.util.Vector;

import concept.ConceptAttribute;
import concept.DerivedConceptAttribute;
import concept.FrequencyConcept;
import concept.FrequencyConceptAttribute;
import concept.InequalityConceptAttribute;
import javafx.scene.control.TreeItem;
import plume.Pair;

public class DerivedConceptTreeItem extends SQuIDCheckboxTreeItem {

    private DerivedConceptAttribute derivedConceptAttribute;
    SQuIDCheckboxTreeItem root;

    public DerivedConceptTreeItem(DerivedConceptAttribute derivedConceptAttribute, QbeeGuiMain mc) {
        super(derivedConceptAttribute.getName(), mc);
        this.derivedConceptAttribute = derivedConceptAttribute;
        this.root = this;
        computeBaseTables();
        refreshDeepFreqConceptBaseTables();
    }

    public DerivedConceptTreeItem(DerivedConceptAttribute derivedConceptAttribute,
            CandidateQueryTreeRoot cqRoot, QbeeGuiMain mc) {
        super(derivedConceptAttribute.getName(), mc);
        this.derivedConceptAttribute = derivedConceptAttribute;
        root = cqRoot;
        computeBaseTables();
        for (TreeItem<String> item : getChildren()) {
            cqRoot.add((SQuIDCheckboxTreeItem) item);
        }
        refreshDeepFreqConceptBaseTables();
    }

    private void computeBaseTables() {
        for (ConceptAttribute ca : derivedConceptAttribute.getShallowConceptAttributes()) {
            BaseConceptTreeItem baseTableRoot = new BaseConceptTreeItem(ca.getConceptName(), ca,
                    mainClass);
            if (ca instanceof InequalityConceptAttribute) {
                baseTableRoot.addConcept(new Pair<String, String>(
                        ((InequalityConceptAttribute) ca).getConceptSummary(),
                        ((InequalityConceptAttribute) ca).getConceptSummary()));
                CheckboxItemWithHitBar conceptTreeElement = new CheckboxItemWithHitBar(
                        ((InequalityConceptAttribute) ca).getConceptSummary(), mainClass,
                        ca.getConfidence(), ca.isDroppedDueToLowFScore(), !ca.isDropped());
                baseTableRoot.add(conceptTreeElement, !ca.isDropped());
            } else {
                for (Pair<String, String> curConcept : ca
                        .getConceptsWithValue(ca.getCommonConcepts())) {
                    baseTableRoot.addConcept(curConcept);
                    CheckboxItemWithHitBar conceptTreeElement = new CheckboxItemWithHitBar(
                            curConcept.b, mainClass, ca.getConfidence(),
                            ca.isDroppedDueToLowFScore(), !ca.isDropped());
                    baseTableRoot.add(conceptTreeElement, !ca.isDropped());
                }

                if (ca.getCommonConcepts().size() == 0) {
                    /*
                     * for (Pair<String, String> curConcept : ca
                     * .getConceptsWithValue(ca.getUnionedConcepts())) {
                     * baseTableRoot.addConcept(curConcept); SQuIDCheckboxTreeItem
                     * conceptTreeElement = new SQuIDCheckboxTreeItem( curConcept.b, mainClass);
                     * baseTableRoot.add(conceptTreeElement, false); }
                     */
                    // Ignoring disjunctions
                }
            }
            if (baseTableRoot.getChildren().size() > 0) {
                add(baseTableRoot);
            }
        }

        for (ConceptAttribute ca : derivedConceptAttribute.getDeepConceptAttributes()) {
            BaseConceptTreeItem baseTableRoot = new BaseConceptTreeItem(ca.getConceptName(), ca,
                    mainClass);
            if (ca instanceof InequalityConceptAttribute) {
                baseTableRoot.addConcept(new Pair<String, String>(
                        ((InequalityConceptAttribute) ca).getConceptSummary(),
                        ((InequalityConceptAttribute) ca).getConceptSummary()));
                CheckboxItemWithHitBar conceptTreeElement = new CheckboxItemWithHitBar(
                        ((InequalityConceptAttribute) ca).getConceptSummary(), mainClass,
                        ca.getConfidence(), ca.isDroppedDueToLowFScore(), !ca.isDropped());
                baseTableRoot.add(conceptTreeElement, !ca.isDropped());
            } else {
                for (Pair<String, String> curConcept : ca
                        .getConceptsWithValue(ca.getCommonConcepts())) {
                    baseTableRoot.addConcept(curConcept);
                    CheckboxItemWithHitBar conceptTreeElement = new CheckboxItemWithHitBar(
                            curConcept.b, mainClass, ca.getConfidence(),
                            ca.isDroppedDueToLowFScore(), !ca.isDropped());
                    baseTableRoot.add(conceptTreeElement, true);
                }

                if (ca.getCommonConcepts().size() == 0) {
                    /*
                     * for (Pair<String, String> curConcept : ca
                     * .getConceptsWithValue(ca.getUnionedConcepts())) {
                     * baseTableRoot.addConcept(curConcept); SQuIDCheckboxTreeItem
                     * conceptTreeElement = new SQuIDCheckboxTreeItem( curConcept.b, mainClass);
                     * baseTableRoot.add(conceptTreeElement, false); }
                     */ // ignoring disjunctions
                }
            }
            if (baseTableRoot.getChildren().size() > 0) {
                add(baseTableRoot);
            }
        }
    }

    /**
     * This part is done separately to accommodate slider functionality dynamically
     */
    public void refreshDeepFreqConceptBaseTables() {
        Vector<BaseFreqConceptTreeItem> itemToRemove = new Vector<>();
        for (TreeItem<String> item : root.getChildren()) {
            if (item instanceof BaseFreqConceptTreeItem) {
                itemToRemove.add((BaseFreqConceptTreeItem) item);
            }
        }
        for (BaseFreqConceptTreeItem item : itemToRemove) {
            root.getChildren().remove(item);
        }
        for (FrequencyConceptAttribute fca : derivedConceptAttribute
                .getDeepFreqConceptAttributes()) {
            Vector<FrequencyConcept> fcaConcepts = fca.getFrequencyConcepts();
            BaseFreqConceptTreeItem baseTableRoot = new BaseFreqConceptTreeItem(
                    fca.getConceptName() + " (" + fcaConcepts.size() + ") ", fca, mainClass);
            for (FrequencyConcept curConcept : fcaConcepts) {
                baseTableRoot.addConcept(curConcept);
                CheckboxItemWithHitBar conceptTreeElement = new CheckboxItemWithHitBar(
                        curConcept.getValue() + " (" + curConcept.getFrequencyMin() + "-"
                                + curConcept.getFrequencyMax() + ")",
                        mainClass, curConcept.getConfidence(), curConcept.isDroppedDueToLowFScore(),
                        !curConcept.isDropped());
                baseTableRoot.add(conceptTreeElement, !curConcept.isDropped());
            }
            if (baseTableRoot.getChildren().size() > 0) {
                if (root instanceof DerivedConceptTreeItem) {
                    ((DerivedConceptTreeItem) root).add(baseTableRoot);
                } else if (root instanceof CandidateQueryTreeRoot) {
                    ((CandidateQueryTreeRoot) root).add(baseTableRoot);
                }
            }
        }
    }

    public DerivedConceptAttribute getDerivedConceptAttribute() {
        return derivedConceptAttribute;
    }

    public void add(SQuIDCheckboxTreeItem item) {
        if (!item.isSelected() && !item.isIndeterminate()) {
            if (isSelected()) {
                setIndeterminate(true);
            }
        }
        if (item.isSelected() || item.isIndeterminate()) {
            if (getChildren().size() > 0 && !isSelected()) {
                setIndeterminate(true);
            }
            if (getChildren().size() == 0 && !isSelected()) {
                setSelected(true);
            }
        }
        getChildren().add(item);
    }

}
