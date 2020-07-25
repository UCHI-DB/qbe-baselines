package gui;

import java.util.List;
import java.util.Vector;

import org.controlsfx.control.textfield.TextFields;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;

import concept.ConceptAttribute;
import concept.DerivedConceptAttribute;
import concept.FrequencyConcept;
import concept.FrequencyConceptAttribute;
import concept.InequalityConceptAttribute;
import fetch.CandidateQuery;
import fetch.ExampleTable;
import fetch.QueryFetcher;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import plume.Pair;
import query.Query;
import util.DBUtil;
import util.QbeeOptions;
import util.Util;

/**
 * @author afariha This is the GUI based entry point for QBEE
 */
public class QbeeGuiMain extends Application {
    private static final int EXAMPLE_TABLE_POSITION = 3;
    private static final int NEW_TUPLE_POSITION = 4;
    private static final int RESULT_TABLE_POSITION = 2;
    private static final int ELAPSED_TIME_POSITION = 5;
    private static final int SHOW_MORE_BUTTON_POSITION = 4;
    private static final int RESULT_ROW_LABEL_POSITION = 3;
    private static final int CONCEPT_FEEDBACK_POSITION = 2;
    private static final int FEEDBACK_BOX_POSITION = 1;
    private static final int OUTPUT_BOX_POSITION = 2;

    private static final int ROW_HEIGHT = 20;
    private static final double ROW_HEIGHT_MULTIPLIER = 1.25;
    private static final int HEADER_HEIGHT = 45;
    private static final int MAX_RT_ROW = 28;
    private static final int MAX_RT_HEIGHT = MAX_RT_ROW * ROW_HEIGHT;
    private static final int MAX_COL_NO = 1;
    private static final int ADDITIONAL_CONCEPT_BOX_POSITION = 1;
    private static final int RESULT_TABLE_LIMIT = 500;
    private static final int ADDITIONAL_CONCEPT_LIST_VIEW_POSITION = 1;
    private static final boolean ADD_ROW = true;

    int RESULT_TABLE_OFFSET = 0;
    int CONCEPT_OFFSET = 0;

    private double SCREEN_WIDTH;
    private double SCREEN_HEIGHT;
    private double TABLE_VIEW_WIDTH;

    VBox exampleTupleVBox;
    private VBox resultTupleVBox;
    private VBox feedbackConceptVBox;

    ExampleTable exampleTable;
    Vector<TextField> textFields;
    TableView<List<String>> exampleTableView;
    Label elapsedTimeLabel;

    Vector<CandidateQuery> candidateQueries;
    Vector<CandidateQuery> feedbackedCandidateQueries;

    private TableView<List<String>> outputTableView;
    String queryString;
    Button showQueryButton;
    private Button submitFeedbackButton;
    QueryFetcher queryFetcher;

    private HBox feedbackHBox;

    private Button showMoreResultButton;
    private Button discoverIntentButton;
    Button executeQueryButton;
    private HBox mainBox;
    private SQuIDCheckboxTreeItem feedbackRoot;
    TreeView<String> conceptTree;
    private Vector<DerivedConceptTreeItem> allDerivedConceptTreeItem;
    private ComboBox<String> dbComboBox;
    Alert loader;
    public static int selectivityValue;
    public static QbeeOptions options;
    static Autocompletor autoCompletor;

    public void main(String[] args) {
        options = new QbeeOptions(args);
        options.setDbName(DBUtil.getDbName("IMDb"));
        Util.beginStartUp(options);
        autoCompletor = new Autocompletor(DBUtil.getDB().getInvertedColumnIndexTableName());
        DBUtil.GUI = true;
        launch();
    }

    @Override
    public void start(Stage stage) {
        try {
            stage.getIcons().add(new Image("file:images/squid.png"));
            SCREEN_WIDTH = Screen.getPrimary().getVisualBounds().getWidth();
            SCREEN_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
            TABLE_VIEW_WIDTH = SCREEN_WIDTH / 3 - 70;
            stage.setWidth(SCREEN_WIDTH);
            stage.setHeight(SCREEN_HEIGHT);

            Scene scene = new Scene(new Group());
            mainBox = new HBox();
            VBox inputBox = getInputBox(SCREEN_WIDTH / 3, SCREEN_HEIGHT - 100);
            VBox feedbackBox = getFeedbackBox(SCREEN_WIDTH / 3, SCREEN_HEIGHT - 100);
            VBox outputBox = getOutputBox(SCREEN_WIDTH / 3, SCREEN_HEIGHT - 100);
            mainBox.getChildren().addAll(inputBox, feedbackBox, outputBox);

            ((Group) scene.getRoot()).getChildren().addAll(mainBox);
            stage.setScene(scene);
            stage.setTitle("SQuID");
            stage.show();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent we) {
                    Util.exit();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    VBox getInputBox(double width, double height) {
        VBox inputBox = new VBox();

        // for some unknown reason, the very first label's text gets
        // darkened as time passes. this dummy label solved the issue.
        Label dummyLabel = new Label(" ");
        dummyLabel.setFont(new Font(0));

        Label exampleTupleLabel = new Label("Example Tuples");
        exampleTupleVBox = new VBox();
        exampleTupleLabel.setFont(new Font("Arial", 20));
        exampleTupleLabel.setPadding(new Insets(5, 0, 10, 5));

        HBox metaHbox = new HBox();

        HBox tableSizeSpinners = new HBox();
        tableSizeSpinners.setSpacing(2);
        tableSizeSpinners.setPadding(new Insets(5, 0, 5, 0));
        Label colNumLabel = new Label("#Columns: ");
        colNumLabel.setPadding(new Insets(5, 0, 0, 5));
        Spinner<Integer> colSpinner = new Spinner<>(1, MAX_COL_NO, 1);
        colSpinner.setPrefWidth(60);
        Button loadETButton = new Button("Reset");
        Label selectDBLabel = new Label("Select Database:");
        selectDBLabel.setPadding(new Insets(5, 0, 0, 5));
        ObservableList<String> dbNamesToShow = FXCollections.observableArrayList("IMDb", "DBLP");

        dbComboBox = new ComboBox<String>(dbNamesToShow);
        dbComboBox.getSelectionModel().selectFirst();
        dbComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String dbName) {
                new Thread() {
                    @Override
                    public void run() {
                        showLoading();
                        options.setDbName(DBUtil.getDbName(dbName));
                        Util.beginStartUp(options);
                        autoCompletor = new Autocompletor(
                                DBUtil.getDB().getInvertedColumnIndexTableName());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                resetFeedbackAndOutputBox();
                                resizeExampleTable(colSpinner.getValue());

                            }
                        });
                        stopLoading();
                    }
                }.start();
            }
        });

        HBox sliderHBox = new HBox();
        Label sliderLabel = new Label("Skewness");

        HBox.setMargin(sliderLabel, new Insets(10, 10, 10, 60));

        loadETButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                resetFeedbackAndOutputBox();
                resizeExampleTable(colSpinner.getValue());
            }
        });

        tableSizeSpinners.setSpacing(15);
        // colNumLabel, colSpinner,
        tableSizeSpinners.getChildren().addAll(selectDBLabel, dbComboBox, loadETButton);
        metaHbox.getChildren().addAll(tableSizeSpinners, sliderHBox);
        elapsedTimeLabel = new Label("");
        discoverIntentButton = new Button("Discover Query Intent");
        VBox.setMargin(discoverIntentButton, new Insets(30, 30, 30, 0));
        discoverIntentButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        elapsedTimeLabel.setText("");
                        new Thread() {
                            @Override
                            public void run() {
                                showLoading();
                                double startTime = System.currentTimeMillis();
                                fetchQuery();
                                double endTime = System.currentTimeMillis();
                                double elapsedTime = (endTime - startTime) / 1000.0;
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        elapsedTimeLabel.setText(
                                                "Prediction took " + elapsedTime + " seconds.");
                                        displayFeedback();
                                        showQueryButton.setDisable(false);
                                        executeQueryButton.setDisable(false);
                                        RESULT_TABLE_OFFSET = 0;
                                        loadMoreResult();
                                    }
                                });
                                stopLoading();
                            }
                        }.start();
                    }
                });

        executeQueryButton = new Button("Execute Query");
        VBox.setMargin(executeQueryButton, new Insets(30, 30, 30, 0));
        executeQueryButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        RESULT_TABLE_OFFSET = 0;
                        loadMoreResult();
                    }
                });

        exampleTupleVBox.setSpacing(5);
        exampleTupleVBox.setPadding(new Insets(10, 10, 20, 10));

        exampleTupleVBox.getChildren().addAll(dummyLabel, exampleTupleLabel, metaHbox,
                new TableView<List<String>>(), new HBox(), elapsedTimeLabel);
        exampleTupleVBox.setStyle("-fx-border-style: solid outside;" + "-fx-border-width: 2;"
                + "-fx-border-insets: 5;" + "-fx-border-radius: 0;" + "-fx-border-color: gray;");
        exampleTupleVBox.setPrefSize(width, height);
        exampleTupleVBox.setMaxSize(width, height);
        exampleTupleVBox.setMinSize(width, height);
        resizeExampleTable(1);

        HBox buttons = new HBox();
        buttons.getChildren().addAll(discoverIntentButton);// , executeQueryButton);
        buttons.setSpacing(20);
        buttons.setPadding(new Insets(20, 0, 0, 0));
        buttons.setAlignment(Pos.BASELINE_CENTER);

        inputBox.getChildren().addAll(exampleTupleVBox, buttons);
        inputBox.setAlignment(Pos.TOP_CENTER);
        return inputBox;
    }

    /**
     * Reverse engineer the query with feedbacks. In absence of feedback, go for the "guessed" query
     */
    protected void fetchQuery() {
        queryFetcher = new QueryFetcher(exampleTable);
        candidateQueries = queryFetcher.getCandidateQueries();
        for (CandidateQuery cq : candidateQueries) {
            cq.computeFilters();
        }
    }

    protected void displayFeedback() {
        if (candidateQueries.size() == 0) {
            resetFeedbackAndOutputBox();
            showAlert("Invalid example table wrt DB instance.");
            return;
        }
        Util.STILL_LOADING = true;
        loadFeedbackBoxContent(true);
        Util.STILL_LOADING = false;
        RESULT_TABLE_OFFSET = 0;
        if (candidateQueries.size() == 0) {
            showAlert("No valid query found.");
            return;
        }
        queryString = Query.unionAll(getCandidateQueries());
        System.out.println("OUTPUT: Query: " + queryString);
    }

    /**
     * generic module for showing any alert message
     * 
     * @param message
     */
    void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Alert");
        alert.setHeaderText("Message");
        alert.getDialogPane().setContent(new Label(message));
        alert.showAndWait();
    }

    VBox getFeedbackBox(double width, double height) {
        VBox feedbackBox = new VBox();

        // for some unknown reason, the very first label's text gets
        // darkened as time passes. this dummy label solved the issue.
        Label dummyLabel = new Label(" ");
        dummyLabel.setFont(new Font(0));
        Label feedbackLabel = new Label("Explanation");
        feedbackLabel.setFont(new Font("Arial", 20));
        feedbackLabel.setPadding(new Insets(5, 0, 10, 5));

        feedbackConceptVBox = new VBox();
        feedbackConceptVBox.setSpacing(5);
        feedbackConceptVBox.setPadding(new Insets(10, 10, 20, 10));
        feedbackConceptVBox.setStyle("-fx-border-style: solid outside;" + "-fx-border-width: 2;"
                + "-fx-border-insets: 5;" + "-fx-border-radius: 0;" + "-fx-border-color: gray;");
        feedbackConceptVBox.setPrefSize(width, height);
        feedbackConceptVBox.setMaxSize(width, height);
        feedbackConceptVBox.setMinSize(width, height);
        feedbackConceptVBox.getChildren().addAll(dummyLabel, feedbackLabel, new HBox());

        submitFeedbackButton = new Button("Submit Feedback");
        submitFeedbackButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        submitFeedback(true);
                    }
                });
        submitFeedbackButton.setDisable(true);
        VBox.setMargin(submitFeedbackButton, new Insets(30, 30, 30, 0));
        feedbackBox.getChildren().addAll(feedbackConceptVBox);// , submitFeedbackButton);
        feedbackBox.setAlignment(Pos.TOP_CENTER);
        return feedbackBox;
    }

    VBox getOutputBox(double width, double height) {
        VBox outputBox = new VBox();

        // for some unknown reason, the very first label's text gets
        // darkened as time passes. this dummy label solved the issue.
        Label dummyLabel = new Label(" ");
        dummyLabel.setFont(new Font(0));

        Label resultTupleLabel = new Label("Result Tuples");
        resultTupleLabel.setFont(new Font("Arial", 20));
        resultTupleLabel.setPadding(new Insets(5, 0, 10, 5));

        showMoreResultButton = new Button("Show More");
        showMoreResultButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        loadMoreResult();
                    }
                });
        showMoreResultButton.setDisable(true);
        showQueryButton = new Button("Show Query");
        showQueryButton.setDisable(true);
        VBox.setMargin(showQueryButton, new Insets(30, 30, 30, 0));
        showQueryButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                showQuery();
            }
        });
        resultTupleVBox = new VBox();
        resultTupleVBox.setSpacing(5);
        resultTupleVBox.setPadding(new Insets(10, 10, 20, 10));

        resultTupleVBox.getChildren().addAll(dummyLabel, resultTupleLabel,
                new TableView<List<String>>(), new Label(), showMoreResultButton);
        resultTupleVBox.setStyle("-fx-border-style: solid outside;" + "-fx-border-width: 2;"
                + "-fx-border-insets: 5;" + "-fx-border-radius: 0;" + "-fx-border-color: gray;");
        resultTupleVBox.setPrefSize(width, height);
        resultTupleVBox.setMaxSize(width, height);
        resultTupleVBox.setMinSize(width, height);
        outputBox.getChildren().addAll(resultTupleVBox, showQueryButton);
        outputBox.setAlignment(Pos.TOP_CENTER);
        return outputBox;
    }

    /**
     * As the output table can be extremely large depending on the query, this module gradually
     * loads tuples
     */
    protected void loadMoreResult() {
        queryString = Query.unionAll(getCandidateQueries());
        Query query = new Query(queryString);
        query.executeQuery(RESULT_TABLE_LIMIT, RESULT_TABLE_OFFSET);
        reloadOutputTable(query.getHeader(), query.getResult());
        RESULT_TABLE_OFFSET += RESULT_TABLE_LIMIT;
    }

    /**
     * Show the reverse engineered query
     */
    protected void showQuery() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Query");
        alert.setHeaderText("Query");

        queryString = Query.unionAll(getCandidateQueries());
        queryString = queryString.replace("(", "");
        queryString = queryString.replace(")", "");
        String formattedSQL = new BasicFormatterImpl().format(queryString);

        TextArea textArea = new TextArea(formattedSQL);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane expContent = new GridPane();
        expContent.add(textArea, 0, 0);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        alert.getDialogPane().setContent(expContent);
        alert.showAndWait();
    }

    /**
     * Once user selects the concepts, re-construct the query and reload the output table
     */
    protected void submitFeedback(boolean manualFeedback) {
        feedbackedCandidateQueries = new Vector<>();
        for (TreeItem<String> cqr : feedbackRoot.getChildren()) {
            CandidateQueryTreeRoot ncqr = (CandidateQueryTreeRoot) cqr;
            if (((SQuIDCheckboxTreeItem) ncqr).isSelected()
                    || ((SQuIDCheckboxTreeItem) ncqr).isIndeterminate()) {
                for (TreeItem<String> bc : ncqr.getChildren()) {
                    ConceptAttribute ca;
                    if (bc instanceof BaseConceptTreeItem) {
                        BaseConceptTreeItem nbc = (BaseConceptTreeItem) bc;
                        ca = nbc.getConceptAttribute();
                        if (ca instanceof InequalityConceptAttribute) {
                            ca.setDropped(true);
                            if (((SQuIDCheckboxTreeItem) nbc.getChildren().get(0)).isSelected()) {
                                ca.setDropped(false);
                            }
                        } else {
                            ca = nbc.getConceptAttribute();
                            ca.setDropped(true);
                            for (int i = 0; i < nbc.getChildren().size(); i++) {
                                if (((SQuIDCheckboxTreeItem) nbc.getChildren().get(i))
                                        .isSelected()) {
                                    ca.setDropped(false);
                                    String toAdd = nbc.getConcepts().get(i).a;
                                    if (!ca.getCommonConcepts().contains(toAdd)) {
                                        ca.getCommonConcepts().addElement(toAdd);
                                    }
                                    if (ca.getUnionedConcepts().contains(toAdd)) {
                                        ca.getUnionedConcepts().removeElement(toAdd);
                                    }
                                } else {
                                    String toRemove = nbc.getConcepts().get(i).a;
                                    if (!ca.getUnionedConcepts().contains(toRemove)) {
                                        ca.getUnionedConcepts().addElement(toRemove);
                                    }
                                    if (ca.getCommonConcepts().contains(toRemove)) {
                                        ca.getCommonConcepts().removeElement(toRemove);
                                    }
                                }
                            }
                        }
                    } else {
                        BaseFreqConceptTreeItem nbc = (BaseFreqConceptTreeItem) bc;
                        ca = nbc.getFreqConceptAttribute();
                        ca.setDropped(true);
                        for (int i = 0; i < nbc.getChildren().size(); i++) {
                            if (((SQuIDCheckboxTreeItem) nbc.getChildren().get(i)).isSelected()) {
                                nbc.getFreqConcepts().get(i).setDropped(false);
                                ca.setDropped(false);
                            } else {
                                nbc.getFreqConcepts().get(i).setDropped(true);
                            }
                        }
                    }
                    if (!ca.isDropped() && !ncqr.getCandidateQuery()
                            .getConceptAttributesToPopulate().contains(ca)) {
                        ncqr.getCandidateQuery().addToConceptAttributesToPopulate(ca);
                    }

                    if (ca.isDropped() && ncqr.getCandidateQuery().getConceptAttributesToPopulate()
                            .contains(ca)) {
                        ncqr.getCandidateQuery().removeFromConceptAttributesToPopulate(ca);
                    }
                }
            }
        }
        RESULT_TABLE_OFFSET = 0;
        loadMoreResult();
    }

    /**
     * After feedback has been provided candidate query conditions might be changed. This function
     * will return the updated candidate queries after user feedback has been taken into account
     * feedbackRoot -> cqTreeRoot (candidate queries) -> baseTableRoot (concept attribute) ->
     * conceptTreeElement (concept)
     * 
     * @return
     */
    Vector<CandidateQuery> getCandidateQueries() {
        return candidateQueries;
    }

    /**
     * Loads all the concepts associated with the example tuples and the additional concepts for
     * concept exploration
     */
    void loadFeedbackBoxContent(boolean firstTimeLoading) {
        feedbackedCandidateQueries = null;
        submitFeedbackButton.setDisable(false);
        feedbackConceptVBox.getChildren().remove(CONCEPT_FEEDBACK_POSITION);
        feedbackHBox = new HBox();
        loadConceptsAssociatedWithET(firstTimeLoading);
        feedbackHBox.getChildren().addAll(conceptTree, new VBox());
        feedbackHBox.setMinHeight(SCREEN_HEIGHT - 200);
        feedbackHBox.setMaxHeight(SCREEN_HEIGHT - 200);
        feedbackHBox.setPrefHeight(SCREEN_HEIGHT - 200);
        feedbackConceptVBox.getChildren().add(feedbackHBox);
    }

    /**
     * Loads concepts associated with the values in the example tuple. This function implements
     * concept fixing and binding.
     * 
     * @return
     */
    private void loadConceptsAssociatedWithET(boolean firstTimeLoading) {
        if (firstTimeLoading) {
            allDerivedConceptTreeItem = new Vector<DerivedConceptTreeItem>();
            feedbackRoot = new SQuIDCheckboxTreeItem("Concepts", this);
            feedbackRoot.setExpanded(true);
            conceptTree = new TreeView<String>(feedbackRoot);
            conceptTree.setEditable(true);

            for (CandidateQuery cq : candidateQueries) {
                CandidateQueryTreeRoot cqTreeRoot = new CandidateQueryTreeRoot(
                        cq.getJoinTable().getRealName(), cq, this);
                cqTreeRoot.setExpanded(true);
                feedbackRoot.getChildren().add(cqTreeRoot);

                for (ConceptAttribute ca : cq.getBaseConceptAttributes()) {
                    BaseConceptTreeItem baseTableRoot = new BaseConceptTreeItem(
                            ca.getConceptConditionAttribute().getRealAttribute().getName(), ca,
                            this);
                    if (ca instanceof InequalityConceptAttribute) {
                        baseTableRoot.addConcept(new Pair<String, String>(
                                ((InequalityConceptAttribute) ca).getConceptSummary(),
                                ((InequalityConceptAttribute) ca).getConceptSummary()));
                        CheckboxItemWithHitBar conceptTreeElement = new CheckboxItemWithHitBar(
                                ((InequalityConceptAttribute) ca).getConceptSummary(), this,
                                ca.getConfidence(), ca.isDroppedDueToLowFScore(), !ca.isDropped());
                        baseTableRoot.add(conceptTreeElement, !ca.isDropped());
                    } else {
                        for (Pair<String, String> curConcept : ca
                                .getConceptsWithValue(ca.getCommonConcepts())) {
                            baseTableRoot.addConcept(curConcept);
                            CheckboxItemWithHitBar conceptTreeElement = new CheckboxItemWithHitBar(
                                    curConcept.b, this, ca.getConfidence(),
                                    ca.isDroppedDueToLowFScore(), !ca.isDropped());
                            baseTableRoot.add(conceptTreeElement, !ca.isDropped());
                        }

                        if (ca.getCommonConcepts().size() == 0) {
                            // for (Pair<String, String> curConcept : ca
                            // .getConceptsWithValue(ca.getUnionedConcepts())) {
                            // baseTableRoot.addConcept(curConcept);
                            // SQuIDCheckboxTreeItem conceptTreeElement = new
                            // SQuIDCheckboxTreeItem(curConcept.b, this);
                            // baseTableRoot.add(conceptTreeElement, false);
                            // no common concept, just show, but do not select
                            // } Ignoring disjunctions
                        }
                    }
                    if (baseTableRoot.getChildren().size() > 0) {
                        cqTreeRoot.add(baseTableRoot);
                    }
                }
                for (DerivedConceptAttribute dca : cq.getDerivedConceptAttributes()) {
                    if (dca.getShallowConceptAttributes().size() > 0
                            || dca.getDeepConceptAttributes().size() > 0
                            || dca.getDeepFreqConceptAttributes().size() > 0) {
                        if (!dca.getName().equals(cqTreeRoot.getName())) {
                            // Create a new tree root for this object and include derived concepts
                            // here
                            DerivedConceptTreeItem derivedConceptTreeItem = new DerivedConceptTreeItem(
                                    dca, this);
                            cqTreeRoot.add(derivedConceptTreeItem);
                            allDerivedConceptTreeItem.add(derivedConceptTreeItem);
                        } else {
                            // Directly add derived concept to root if the base object is same
                            DerivedConceptTreeItem derivedConceptTreeItem = new DerivedConceptTreeItem(
                                    dca, cqTreeRoot, this);
                            allDerivedConceptTreeItem.add(derivedConceptTreeItem);
                        }
                    }
                }
            }
            if (feedbackRoot.getChildren().size() > 0) {
                // some base table concept attributes found
                conceptTree.setCellFactory(CheckBoxTreeCell.<String> forTreeView());
                conceptTree.setRoot(feedbackRoot);
                conceptTree.setShowRoot(true);
            }

            conceptTree.setMinHeight(SCREEN_HEIGHT - 191);
            conceptTree.setMinWidth(SCREEN_WIDTH / 3 - 40);
            conceptTree.setMaxWidth(SCREEN_WIDTH / 3 - 40);
            conceptTree.setPrefWidth(SCREEN_WIDTH / 3 - 40);
        } else

        {
            for (DerivedConceptTreeItem dcti : allDerivedConceptTreeItem) {
                dcti.refreshDeepFreqConceptBaseTables();
            }
        }
    }

    void loadAdditionalConcepts(BaseFreqConceptTreeItem freqConceptAttributeRoot) {
        FrequencyConceptAttribute freqConceptAttribute = freqConceptAttributeRoot
                .getFreqConceptAttribute();
        feedbackHBox.getChildren().remove(ADDITIONAL_CONCEPT_BOX_POSITION);

        VBox additionalConceptVBox = new VBox();
        TextField searchBox = new TextField();

        Vector<FrequencyConcept> currentPageConcepts = freqConceptAttribute.getFrequencyConcepts();
        FrequencyConceptListView listView = new FrequencyConceptListView(currentPageConcepts, null);
        listView.setHeight(SCREEN_HEIGHT - 334);

        Button fixConceptButton = new Button("Fix Selected Concepts");
        VBox.setMargin(fixConceptButton, new Insets(10));

        searchBox.setPromptText("Filter " + freqConceptAttribute.getConceptName());
        VBox.setMargin(searchBox, new Insets(10));
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> {

            Vector<FrequencyConcept> selectedConcepts = ((FrequencyConceptListView) additionalConceptVBox
                    .getChildren().get(ADDITIONAL_CONCEPT_LIST_VIEW_POSITION))
                            .getSelectedConcepts();
            FrequencyConceptListView newListView = new FrequencyConceptListView(
                    listView.getConcepts(), selectedConcepts);
            newListView = newListView.filter(searchBox.getText());
            newListView.setHeight(SCREEN_HEIGHT - 334);
            additionalConceptVBox.getChildren().clear();
            additionalConceptVBox.getChildren().addAll(searchBox, newListView, fixConceptButton);
        });

        fixConceptButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                FrequencyConceptListView newListView = ((FrequencyConceptListView) additionalConceptVBox
                        .getChildren().get(ADDITIONAL_CONCEPT_LIST_VIEW_POSITION));
                for (FrequencyConcept listItem : newListView.getSelectedConcepts()) {
                    if (freqConceptAttributeRoot.addConcept(new FrequencyConcept(listItem.getKey(),
                            listItem.getValue(), listItem.getFrequencyMin(),
                            listItem.getFrequencyMax(), listItem.getFrequencyAbsMin(),
                            listItem.getFrequencyAbsMax()))) {
                        CheckboxItemWithHitBar conceptTreeElement = new CheckboxItemWithHitBar(
                                listItem.getValue() + " (" + listItem.getFrequencyMin() + "-"
                                        + listItem.getFrequencyMax() + ")",
                                QbeeGuiMain.this, listItem.getConfidence(), false, false);
                        freqConceptAttributeRoot.add(conceptTreeElement, true);
                    }
                }
            }
        });

        additionalConceptVBox.setAlignment(Pos.TOP_CENTER);
        additionalConceptVBox.getChildren().addAll(searchBox, listView, fixConceptButton);
        additionalConceptVBox.setStyle("-fx-border-style: solid outside;" + "-fx-border-width: 1;"
                + "-fx-border-insets: 1;" + "-fx-border-radius: 0;"
                + "-fx-border-color: LIGHTGRAY;");

        additionalConceptVBox.setMinWidth(SCREEN_WIDTH / 6 - 20);
        additionalConceptVBox.setMaxWidth(SCREEN_WIDTH / 6 - 20);
        additionalConceptVBox.setPrefWidth(SCREEN_WIDTH / 6 - 20);

        // feedbackHBox.getChildren().add(additionalConceptVBox);
    }

    /**
     * @param header
     * @param result
     *            Reloads the result table showing the result of the reverse engineered query
     */
    void reloadOutputTable(Vector<String> header, Vector<Vector<String>> result) {
        if (RESULT_TABLE_OFFSET == 0) {
            outputTableView = new TableView<List<String>>();
            exampleTableView.setEditable(true);
            double COL_WIDTH = TABLE_VIEW_WIDTH / header.size();
            for (int k = 0; k < header.size(); k++) {

                TableColumn<List<String>, String> curTableCol = new TableColumn<List<String>, String>(
                        header.get(k));

                curTableCol.setPrefWidth(COL_WIDTH);
                curTableCol.setMinWidth(COL_WIDTH);
                curTableCol.setMaxWidth(COL_WIDTH);

                final int colIndex = k;
                curTableCol.setCellValueFactory(data -> {
                    List<String> rowValues = data.getValue();
                    String cellValue;
                    if (colIndex < rowValues.size()) {
                        cellValue = rowValues.get(colIndex);
                    } else {
                        cellValue = "";
                    }
                    return new ReadOnlyStringWrapper(cellValue);
                });
                outputTableView.getColumns().add(curTableCol);
            }

            outputTableView.setPrefWidth(TABLE_VIEW_WIDTH);
            outputTableView.setMinWidth(TABLE_VIEW_WIDTH);
            outputTableView.setMaxWidth(TABLE_VIEW_WIDTH);

            outputTableView.getItems().clear();
        }
        outputTableView.getItems().clear();
        for (Vector<String> row : result) {
            List<String> lrow = FXCollections.observableArrayList();
            for (String value : row) {
                lrow.add(value);
            }
            outputTableView.getItems().add(lrow);
        }
        outputTableView.setMinHeight(HEADER_HEIGHT + ROW_HEIGHT);
        outputTableView.setMaxHeight(HEADER_HEIGHT + MAX_RT_HEIGHT);
        outputTableView.setPrefHeight(HEADER_HEIGHT
                + (outputTableView.getItems().size()) * ROW_HEIGHT * ROW_HEIGHT_MULTIPLIER);
        outputTableView.refresh();

        Label resultRowLabel = new Label("" + outputTableView.getItems().size() + " Row(s)");
        if (result.size() < RESULT_TABLE_LIMIT) {
            showMoreResultButton.setDisable(true);
        } else {
            showMoreResultButton.setDisable(false);
        }
        resultTupleVBox.getChildren().remove(SHOW_MORE_BUTTON_POSITION);
        resultTupleVBox.getChildren().remove(RESULT_ROW_LABEL_POSITION);
        resultTupleVBox.getChildren().remove(RESULT_TABLE_POSITION);
        resultTupleVBox.getChildren().addAll(outputTableView, resultRowLabel, showMoreResultButton);
    }

    /**
     * @param nCol
     *            Resizes the example table to accommodate nCol columns
     */
    void resizeExampleTable(int nCol) {
        discoverIntentButton.setDisable(true);
        executeQueryButton.setDisable(true);
        exampleTable = new ExampleTable(nCol);
        exampleTableView = new TableView<>();
        exampleTableView.setEditable(true);
        double COL_WIDTH = (TABLE_VIEW_WIDTH - 60) / nCol;

        for (int k = 0; k < nCol; k++) {

            TableColumn<List<String>, String> curTableCol = new TableColumn<List<String>, String>(
                    "Values");

            final int colIndex = k;
            curTableCol.setCellValueFactory(data -> {
                List<String> rowValues = data.getValue();
                String cellValue;
                if (colIndex < rowValues.size()) {
                    cellValue = rowValues.get(colIndex);
                } else {
                    cellValue = "";
                }
                return new ReadOnlyStringWrapper(cellValue);
            });

            curTableCol.setPrefWidth(COL_WIDTH);
            curTableCol.setMinWidth(COL_WIDTH);
            curTableCol.setMaxWidth(COL_WIDTH);
            exampleTableView.getColumns().add(curTableCol);
        }

        TableColumn<List<String>, Boolean> delCol = new TableColumn<>("");
        delCol.setSortable(false);
        delCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<List<String>, Boolean>, ObservableValue<Boolean>>() {
                    @Override
                    public ObservableValue<Boolean> call(
                            TableColumn.CellDataFeatures<List<String>, Boolean> features) {
                        return new SimpleBooleanProperty(features.getValue() != null);
                    }
                });
        delCol.setCellFactory(
                new Callback<TableColumn<List<String>, Boolean>, TableCell<List<String>, Boolean>>() {
                    @Override
                    public TableCell<List<String>, Boolean> call(
                            TableColumn<List<String>, Boolean> actionColumn) {
                        return new DeleteCell();
                    }
                });

        TableColumn<List<String>, Boolean> editCol = new TableColumn<>("");
        editCol.setSortable(false);
        editCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<List<String>, Boolean>, ObservableValue<Boolean>>() {
                    @Override
                    public ObservableValue<Boolean> call(
                            TableColumn.CellDataFeatures<List<String>, Boolean> features) {
                        return new SimpleBooleanProperty(features.getValue() != null);
                    }
                });
        editCol.setCellFactory(
                new Callback<TableColumn<List<String>, Boolean>, TableCell<List<String>, Boolean>>() {
                    @Override
                    public TableCell<List<String>, Boolean> call(
                            TableColumn<List<String>, Boolean> actionColumn) {
                        return new EditCell();
                    }
                });

        delCol.setPrefWidth(30);
        delCol.setMinWidth(30);
        delCol.setMaxWidth(30);
        editCol.setPrefWidth(30);
        editCol.setMinWidth(30);
        editCol.setMaxWidth(30);

        exampleTableView.getColumns().add(delCol);
        exampleTableView.getColumns().add(editCol);
        exampleTableView.setPrefWidth(TABLE_VIEW_WIDTH);
        exampleTableView.setMinWidth(TABLE_VIEW_WIDTH);
        exampleTableView.setMaxWidth(TABLE_VIEW_WIDTH);
        exampleTableView.setPrefHeight(
                HEADER_HEIGHT + exampleTable.getRowSize() * (ROW_HEIGHT * ROW_HEIGHT_MULTIPLIER)
                        + ROW_HEIGHT * ROW_HEIGHT_MULTIPLIER);

        exampleTupleVBox.getChildren().remove(ELAPSED_TIME_POSITION);
        exampleTupleVBox.getChildren().remove(NEW_TUPLE_POSITION);
        exampleTupleVBox.getChildren().remove(EXAMPLE_TABLE_POSITION);
        exampleTupleVBox.getChildren().addAll(exampleTableView, getNewTuple(nCol),
                elapsedTimeLabel);
    }

    /**
     * Returns a mechanism to add new tuple to the example table
     */
    private HBox getNewTuple(int nCol) {
        HBox newTuple = new HBox();
        textFields = new Vector<>();
        double COL_WIDTH = TABLE_VIEW_WIDTH / nCol;
        for (int i = 0; i < nCol; i++) {
            TextField cell = new TextField();
            TextFields.bindAutoCompletion(cell, autoCompletor.getKeywords());
            cell.setPromptText("Type Here");
            cell.setPrefWidth(COL_WIDTH);
            cell.setMinWidth(COL_WIDTH);
            cell.setMaxWidth(COL_WIDTH);
            cell.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent keyEvent) {
                    if (keyEvent.getCode() == KeyCode.ENTER) {
                        addRow();
                    }
                }
            });

            textFields.add(cell);
            newTuple.getChildren().add(cell);
        }
        Platform.runLater(() -> newTuple.getChildren().get(0).requestFocus());
        ImageView addIcon = new ImageView();
        addIcon.setImage(new Image("file:images/add.png"));
        addIcon.setFitWidth(ROW_HEIGHT * 0.8);
        addIcon.setFitHeight(ROW_HEIGHT * 0.8);
        Button addRowButton = new Button("", addIcon);
        addRowButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent e) {
                addRow();
            }

        });
        newTuple.getChildren().add(addRowButton);
        HBox.setMargin(addRowButton, new Insets(0, 0, 0, 10));
        return newTuple;
    }

    void addRow() {
        elapsedTimeLabel.setText("");
        insertRowToExampleTable(ADD_ROW);
    }

    // Define the button cell
    private class DeleteCell extends TableCell<List<String>, Boolean> {

        ImageView deleteIcon;
        final Button deleteButton;

        DeleteCell() {
            deleteIcon = new ImageView();
            deleteIcon.setImage(new Image("file:images/delete.png"));
            deleteButton = new Button("", deleteIcon);
            deleteIcon.setFitWidth(ROW_HEIGHT * 0.8);
            deleteIcon.setFitHeight(ROW_HEIGHT * 0.8);
            deleteButton.setPrefSize(ROW_HEIGHT * 0.5, ROW_HEIGHT * 0.5);
            deleteButton.setPadding(new Insets(0, 0, 0, 0));
            deleteButton.setStyle("-fx-background-color: transparent;");
            deleteButton.setOnAction(e -> {
                insertRowToExampleTable(!ADD_ROW);
                int selectedIndex = getTableRow().getIndex();
                exampleTableView.getItems().remove(selectedIndex);
                exampleTable.deleteRow(selectedIndex);
                reloadExampleTable();
            });

        }

        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if (!empty) {
                setGraphic(deleteButton);
            }
        }
    }

    private class EditCell extends TableCell<List<String>, Boolean> {
        ImageView editIcon;
        Button editButton;

        EditCell() {
            editIcon = new ImageView();
            editIcon.setImage(new Image("file:images/edit.png"));
            editButton = new Button("", editIcon);
            editIcon.setFitWidth(ROW_HEIGHT * 0.8);
            editIcon.setFitHeight(ROW_HEIGHT * 0.8);
            editButton.setPrefSize(ROW_HEIGHT * 0.5, ROW_HEIGHT * 0.5);
            editButton.setPadding(new Insets(0, 0, 0, 0));
            editButton.setStyle("-fx-background-color: transparent;");

            editButton.setOnAction(e -> {
                insertRowToExampleTable(!ADD_ROW);
                int selectedIndex = getTableRow().getIndex();
                String curItem = exampleTableView.getItems().get(selectedIndex).get(0);
                exampleTableView.getItems().remove(selectedIndex);
                exampleTable.deleteRow(selectedIndex);
                reloadExampleTable();
                ((TextField) ((HBox) exampleTupleVBox.getChildren().get(NEW_TUPLE_POSITION))
                        .getChildren().get(0)).setText(curItem);
            });
        }

        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if (!empty) {
                setGraphic(editButton);
            }
        }
    }

    /**
     * Reloads the example table after new example tuple is inserted
     */
    void reloadExampleTable() {
        executeQueryButton.setDisable(true);
        exampleTableView.getItems().clear();
        for (Vector<String> row : exampleTable.getData()) {
            List<String> lrow = FXCollections.observableArrayList();
            for (String value : row) {
                lrow.add(value);
            }
            exampleTableView.getItems().add(lrow);
        }
        exampleTableView.setPrefHeight(
                HEADER_HEIGHT + (exampleTable.getRowSize()) * (ROW_HEIGHT * ROW_HEIGHT_MULTIPLIER)
                        + ROW_HEIGHT * ROW_HEIGHT_MULTIPLIER);
        exampleTableView.refresh();
        exampleTupleVBox.getChildren().remove(ELAPSED_TIME_POSITION);
        exampleTupleVBox.getChildren().remove(NEW_TUPLE_POSITION);
        exampleTupleVBox.getChildren().remove(EXAMPLE_TABLE_POSITION);
        exampleTupleVBox.getChildren().addAll(exampleTableView,
                getNewTuple(exampleTable.getColSize()), elapsedTimeLabel);
    }

    /**
     * Inserts a new row to the example table
     */
    void insertRowToExampleTable(boolean source) {
        boolean properRow = true;
        Vector<String> currentRow = new Vector<String>();
        for (TextField tf : textFields) {
            if (tf.getText() == null || tf.getText().isEmpty()) {
                properRow = false;
                break;
            }
            currentRow.add(tf.getText().toLowerCase());
        }
        if (properRow) {
            exampleTable.addRow(currentRow);
            discoverIntentButton.setDisable(false);
            reloadExampleTable();
        } else if (source == ADD_ROW) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid example tuple");
            alert.setContentText("One or more attributes of the tuple is empty.");
            alert.showAndWait();
        }
    }

    /**
     * Resets the feedback and output box as new example tuple input platform is reset
     */
    void resetFeedbackAndOutputBox() {
        elapsedTimeLabel.setText("");
        VBox feedbackBox = getFeedbackBox(SCREEN_WIDTH / 3, SCREEN_HEIGHT - 100);
        VBox outputBox = getOutputBox(SCREEN_WIDTH / 3, SCREEN_HEIGHT - 100);
        mainBox.getChildren().remove(OUTPUT_BOX_POSITION);
        mainBox.getChildren().remove(FEEDBACK_BOX_POSITION);
        mainBox.getChildren().addAll(feedbackBox, outputBox);
    }

    void showLoading() {
        stopLoading();
        Platform.runLater(new Runnable() {
            public void run() {
                loader = new Alert(AlertType.NONE);
                loader.setTitle("Loading");
                loader.setHeaderText("Loading...");
                ImageView loadingImage = new ImageView();
                loadingImage.setImage(new Image("file:images/loading.gif"));
                loadingImage.setFitWidth(200);
                loadingImage.setFitHeight(200);
                loader.getDialogPane().setContent(loadingImage);
                loader.show();
            }
        });
    }

    void stopLoading() {
        Platform.runLater(new Runnable() {
            public void run() {
                if (loader != null && loader.isShowing()) {
                    loader.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
                    loader.close();
                }
            }
        });
    }
}
