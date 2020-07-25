package gui;

import java.util.Vector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;
import plume.Pair;

public class ConceptListView extends ListView<Concept> {
    private Vector<Pair<String, String>> concepts;
    private Vector<Pair<String, String>> selectedConcepts;

    public ConceptListView(Vector<Pair<String, String>> concepts,
            Vector<Pair<String, String>> prevSelectedConcepts) {
        super();
        this.concepts = new Vector<>();
        this.selectedConcepts = new Vector<>();
        for (Pair<String, String> c : concepts) {
            this.concepts.add(c);
            boolean isSelected = false;
            if (prevSelectedConcepts != null && prevSelectedConcepts.contains(c)) {
                this.selectedConcepts.add(c);
                isSelected = true;
            }
            this.getItems().add(new Concept(c, isSelected));
        }
        setCellFactory();
    }

    private void setCellFactory() {
        setCellFactory(
                CheckBoxListCell.forListView(new Callback<Concept, ObservableValue<Boolean>>() {
                    @Override
                    public ObservableValue<Boolean> call(Concept item) {
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

    public void add(Vector<Pair<String, String>> curConcepts) {
        for (Pair<String, String> c : curConcepts) {
            this.concepts.add(c);
            getItems().add(new Concept(c, false));
        }
    }

    public ConceptListView filter(String filterText) {

        Vector<Pair<String, String>> newContent = new Vector<>();
        Vector<Pair<String, String>> newSelectedContent = new Vector<>();
        for (Pair<String, String> c : concepts) {
            if (c.b.toLowerCase().contains(filterText.toLowerCase())) {
                newContent.add(c);
            }
        }
        for (Pair<String, String> c : selectedConcepts) {
            if (c.b.toLowerCase().contains(filterText.toLowerCase())) {
                newSelectedContent.add(c);
            }
        }
        ConceptListView ret = new ConceptListView(newContent, selectedConcepts);
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

    public Vector<Pair<String, String>> getSelectedConcepts() {
        return selectedConcepts;
    }

    public Vector<Pair<String, String>> getConcepts() {
        return concepts;
    }
}

class Concept {
    private StringProperty name = new SimpleStringProperty();
    private BooleanProperty on = new SimpleBooleanProperty();
    Pair<String, String> value;

    public Concept(Pair<String, String> value, boolean on) {
        this.value = value;
        setName(value.b);
        setOn(on);
    }

    private final StringProperty nameProperty() {
        return this.name;
    }

    public final String getName() {
        return this.nameProperty().get();
    }

    public final void setName(final String name) {
        this.nameProperty().set(name);
    }

    private final BooleanProperty onProperty() {
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
