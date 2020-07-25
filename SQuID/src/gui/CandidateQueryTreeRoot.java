package gui;

import fetch.CandidateQuery;

public class CandidateQueryTreeRoot extends SQuIDCheckboxTreeItem {

    private CandidateQuery candidateQuery;
    private String name;

    public CandidateQueryTreeRoot(String name, CandidateQuery candidateQuery, QbeeGuiMain mc) {
        super(name, mc);
        this.name = name;
        this.candidateQuery = candidateQuery;
    }

    public CandidateQuery getCandidateQuery() {
        return candidateQuery;
    }

    public String getName() {
        return name;
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
