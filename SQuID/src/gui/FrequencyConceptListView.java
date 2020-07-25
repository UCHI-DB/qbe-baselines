package gui;

import java.util.Vector;

import concept.FrequencyConcept;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;

public class FrequencyConceptListView extends ListView<FreqConcept> {
    private Vector<FrequencyConcept> concepts;
    private Vector<FrequencyConcept> selectedConcepts;

    public FrequencyConceptListView(Vector<FrequencyConcept> currentPageConcepts,
            Vector<FrequencyConcept> prevSelectedConcepts) {
        super();
        this.concepts = new Vector<>();
        this.selectedConcepts = new Vector<>();
        for (FrequencyConcept c : currentPageConcepts) {
            this.concepts.add(c);
            boolean isSelected = false;
            if (prevSelectedConcepts != null && prevSelectedConcepts.contains(c)) {
                this.selectedConcepts.add(c);
                isSelected = true;
            }
            this.getItems().add(new FreqConcept(c, isSelected));
        }
        setCellFactory();
    }

    private void setCellFactory() {
        setCellFactory(
                CheckBoxListCell.forListView(new Callback<FreqConcept, ObservableValue<Boolean>>() {
                    @Override
                    public ObservableValue<Boolean> call(FreqConcept item) {
                        BooleanProperty observable = new SimpleBooleanProperty();
                        observable.addListener((obs, wasSelected, isNowSelected) -> {
                            if (!wasSelected && isNowSelected) {
                                // checked
                                selectedConcepts.add(item.value);
                            } else if (wasSelected && !isNowSelected) {
                                // unchecked
                                selectedConcepts.remove(item.value);
                            }
                        });
                        observable.set(selectedConcepts.contains(item.value));
                        return observable;
                    }
                }));
    }

    public void add(Vector<FrequencyConcept> curConcepts) {
        for (FrequencyConcept c : curConcepts) {
            this.concepts.add(c);
            getItems().add(new FreqConcept(c, false));
        }
    }

    public FrequencyConceptListView filter(String filterText) {

        Vector<FrequencyConcept> newContent = new Vector<>();
        Vector<FrequencyConcept> newSelectedContent = new Vector<>();
        for (FrequencyConcept c : concepts) {
            if (c.getValue().toLowerCase().contains(filterText.toLowerCase())) {
                newContent.add(c);
            }
        }
        for (FrequencyConcept c : selectedConcepts) {
            if (c.getValue().toLowerCase().contains(filterText.toLowerCase())) {
                newSelectedContent.add(c);
            }
        }
        FrequencyConceptListView ret = new FrequencyConceptListView(newContent, selectedConcepts);
        ret.setHeight(getHeight());
        return ret;

    }

    public void setWidth(double width) {
        super.setWidth(width);
        setMinWidth(width);
        setMaxWidth(width);
        setPrefWidth(width);
    }

    public void setHeight(double height) {
        super.setHeight(height);
        setMinHeight(height);
        setMaxHeight(height);
        setPrefHeight(height);
    }

    public Vector<FrequencyConcept> getSelectedConcepts() {
        return selectedConcepts;
    }

    public Vector<FrequencyConcept> getConcepts() {
        return concepts;
    }
}

class FreqConcept {
    StringProperty name = new SimpleStringProperty();
    BooleanProperty on = new SimpleBooleanProperty();
    FrequencyConcept value;

    public FreqConcept(FrequencyConcept fc, boolean on) {
        this.value = fc;
        setName(fc.getValue() + " (" + fc.getFrequencyMin() + "-" + fc.getFrequencyMax() + ")");
        setOn(on);
    }

    public final StringProperty nameProperty() {
        return this.name;
    }

    public final String getName() {
        return this.nameProperty().get();
    }

    public final void setName(final String name) {
        this.nameProperty().set(name);
    }

    public final BooleanProperty onProperty() {
        return this.on;
    }

    public final boolean isOn() {
        return this.onProperty().get();
    }

    public final void setOn(final boolean on) {
        this.onProperty().set(on);
    }

    @Override
    public String toString() {
        return getName();
    }
}