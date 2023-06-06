
package javafx.flowExecutionTab;

import dto.DTOFlowExecution;
import dto.DTOFreeInputsFromUser;
import dto.DTOSingleFlowIOData;
import javafx.Controller;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.flowExecutionTab.MasterDetail.MasterDetailController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.MasterDetailPane;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class FlowExecutionTabController {
    private Controller mainController;
    @FXML
    private BorderPane borderPane;
    @FXML
    private GridPane gridPane;
    @FXML
    private HBox inputValuesHBox;
    @FXML
    private Button executeButton;
    private Map<String, Object> freeInputMap;
    private ObservableList<Input> inputList = FXCollections.observableArrayList();
    private MasterDetailController masterDetailController;
    private MasterDetailPane masterDetailPane;
    @FXML
    private Label MandatoryLabel;

    Logic logic;

    private final SimpleStringProperty executedFlowIDProperty;

    public FlowExecutionTabController() {
        executedFlowIDProperty = new SimpleStringProperty();
    }

    public SimpleStringProperty getExecutedFlowID() {
        return this.executedFlowIDProperty;
    }

    public void setExecutedFlowID(UUID id) {
        this.executedFlowIDProperty.set(id.toString());
    }

    @FXML
    public void initialize() throws IOException {
        logic = new Logic();
        freeInputMap = new HashMap<>();
        executeButton.setDisable(true);
        AnchorPane.setTopAnchor(borderPane, 0.0);
        AnchorPane.setBottomAnchor(borderPane, 0.0);
        AnchorPane.setLeftAnchor(borderPane, 0.0);
        AnchorPane.setRightAnchor(borderPane, 0.0);

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("MasterDetail/masterDetails.fxml");
        fxmlLoader.setLocation(url);
        MasterDetailPane MasterDetailComponent = fxmlLoader.load(url.openStream());
        MasterDetailController masterDetailController = fxmlLoader.getController();
        this.setMasterDetailsController(masterDetailController);
        this.masterDetailPane = MasterDetailComponent;
        if (masterDetailController != null) {
            masterDetailController.setFlowExecutionTabController(this);
        }
        VBox masterDetailPaneVbox = new VBox(MasterDetailComponent);
        borderPane.setCenter(masterDetailPaneVbox);

        Text asterisk1 = new Text("*");
        asterisk1.setFill(Color.RED);
        MandatoryLabel.setGraphic(asterisk1);

        //MasterDetailComponent.setDetailNode()// int numThreads = 5; // Set the desired number of threads
        //executorService = Executors.newFixedThreadPool(numThreads);
       // flowExecutionTasks = new ArrayList<>();
    }
    public void initDataInFlowExecutionTab() {
        masterDetailController.initMasterDetailPaneController();
    }
    public void initInputsInFlowExecutionTab() {
        executeButton.setDisable(true);
        inputValuesHBox.getChildren().clear();
    }

    public Controller getMainController() {
        return mainController;
    }
    public void setMasterDetailsController(MasterDetailController masterDetailComponentController) {
        this.masterDetailController = masterDetailComponentController;
        masterDetailComponentController.setFlowExecutionTabController(this);
    }
    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }
    public void initInputsTable(List<DTOSingleFlowIOData> freeInputs) {
        executeButton.setDisable(true);
        inputValuesHBox.getChildren().clear();

        freeInputs.forEach(freeInput -> {// Populate inputList from freeInputs
            Input input = new Input();
            setInputValues(input, freeInput);
            inputList.add(input);


            Label label = new Label((input.getFinalName().equals("TIME_TO_SPEND") ? input.getFinalName() + " (sec)" : input.getFinalName()));
            setLabelSetting(label);

            // Check if input.getMandatory() is "MANDATORY"
            if (input.getMandatory().equals("MANDATORY")) {
                Text asterisk1 = new Text("*");
                asterisk1.setFill(Color.RED);
                label.setGraphic(asterisk1);
            }

            String simpleName = input.getType().getType().getSimpleName();
            VBox vbox = new VBox();
            setVBoxSetting(vbox, label);

            Spinner<Integer> spinner = new Spinner<>();
            TextField textField = new TextField();
            
            if(simpleName.equals("String")) {
                setTextFieldSetting(textField, input);
                if(input.getOriginalName().equals("FILE_NAME")){
                    openFileChooser(textField);
                    textField.setCursor(Cursor.HAND);

                }else if(input.getOriginalName().equals("FOLDER_NAME")){
                    openDirectoryChooser(textField);
                    textField.setCursor(Cursor.HAND);

                }
                else if(input.getOriginalName().equals("SOURCE")){
                    openChooseDialog(textField);
                    textField.setCursor(Cursor.HAND);

                }
                vbox.getChildren().addAll(label, textField);
                vbox.setVgrow(textField, Priority.ALWAYS);
            } else {
                setSpinnerSetting(spinner, input);
                vbox.setVgrow(spinner, Priority.ALWAYS);
                vbox.getChildren().addAll(label, spinner);
            }
            Tooltip tooltip1 = new Tooltip(textField.getText().toString());
            textField.setTooltip(tooltip1);
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                tooltip1.setText(newValue);
            });
            inputValuesHBox.getChildren().add(vbox);
            inputValuesHBox.setSpacing(50);
        });
    }
    public void setInputValues(Input input, DTOSingleFlowIOData freeInput){
        input.setFinalName(freeInput.getFinalName());
        input.setOriginalName(freeInput.getOriginalName());
        input.setStepName(freeInput.getStepName());
        input.setMandatory(freeInput.getNecessity().toString());
        input.setType(freeInput.getType());
    }
    public void openChooseDialog(TextField textField) {
        textField.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                JFileChooser chooser = new JFileChooser(".");
                chooser.setMultiSelectionEnabled(true);
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int ret = chooser.showOpenDialog(null);

                if(ret == JFileChooser.APPROVE_OPTION) {
                    File[] selectedDirectory = chooser.getSelectedFiles();
                    for (File file : selectedDirectory) {
                        textField.setText(file.getAbsolutePath());
                    }
                }
            }
        });

    }
    public void openDirectoryChooser(TextField textField) {
        textField.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Choose Directory");

                Stage stage = (Stage) textField.getScene().getWindow();
                File selectedDirectory = directoryChooser.showDialog(stage);

                if (selectedDirectory != null) {
                    textField.setText(selectedDirectory.getAbsolutePath());
                }
            }
        });
    }
    public void openFileChooser(TextField textField){
        textField.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose File");

                Stage stage = (Stage) textField.getScene().getWindow();
                File selectedFile = fileChooser.showOpenDialog(stage);

                if (selectedFile != null) {
                    textField.setText(selectedFile.getAbsolutePath());
                }
            }
        });
    }
    public void setLabelSetting(Label label){
       label.setWrapText(true);
       label.setAlignment(Pos.CENTER_LEFT);
       label.setTextAlignment(TextAlignment.LEFT);
       label.setTextOverrun(OverrunStyle.CLIP); // Clip the text if it exceeds the label width
   }
    public void setVBoxSetting(VBox vbox,Label label){
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.setSpacing(10);
        vbox.setVgrow(label, Priority.ALWAYS);
    }
    public void setTextFieldSetting(TextField textField, Input input){
        textField.getStyleClass().add("text-field");
        textField.setOnAction(event -> {
            commitEdit(textField.getText(), input);
        });
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                commitEdit(textField.getText(), input);
            }
        });

    }
    public void setSpinnerSetting(Spinner spinner, Input input){
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
        spinner.setEditable(true);
        spinner.getEditor().setAlignment(Pos.CENTER_RIGHT);

        spinner.setOnMouseClicked(event -> {
            if (spinner.isEditable()) {
                spinner.increment(0);
            }
        });
        spinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && spinner.isEditable()) {
                String text = spinner.getEditor().getText();
                if (text.isEmpty()) {
                    spinner.getValueFactory().setValue(0);
                } else {
                    spinner.increment(0); // Increment by 0 to trigger commitEdit
                }
            }
        });
        spinner.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && spinner.isEditable()) {
                String text = spinner.getEditor().getText();
                int newValue = text.isEmpty() ? 0 : Integer.parseInt(text);
                commitEdit(newValue, input);
            }
        });
        spinner.setValueFactory(valueFactory);
    }
    public void commitEdit(Object newValue, Input input) {
        input.setValue(newValue);
        updateFreeInputMap(input, newValue);
        boolean hasAllMandatoryInputs = hasAllMandatoryInputs(freeInputMap);
        executeButton.setDisable(!hasAllMandatoryInputs);
    }
    public void updateFreeInputMap(Input input, Object newValue) {
        freeInputMap.put(input.getStepName() + "." + input.getOriginalName(), newValue);
    }
    public boolean hasAllMandatoryInputs(Map<String, Object> freeInputMap) {
    for (Node node : inputValuesHBox.getChildren()) {
        VBox vbox = (VBox) node;
        Label label = (Label) vbox.getChildren().get(0);
        String finalName;

        String labelText = label.getText();
        boolean startsWithAsterisk = false;
        Node graphic = label.getGraphic();
        if (graphic instanceof Text) {
            Text asterisk = (Text) graphic;
            startsWithAsterisk = asterisk.getText().equals("*");
        }

        finalName = labelText;

        int endIndex = finalName.lastIndexOf(" (");
        if (endIndex != -1) {
            finalName = finalName.substring(0, endIndex);
        }
        
        if (startsWithAsterisk) {
            String finalName1 = finalName;
            Optional<Input> optionalInput = inputList.stream().filter(input1 -> input1.getFinalName().equals(finalName1)).findFirst();
            if (optionalInput.isPresent()) {
                Input input = optionalInput.get();
                String key = input.getStepName() + "." + input.getOriginalName();
                if (!freeInputMap.containsKey(key) || freeInputMap.get(key).equals("")) {
                    return false;
                }
            }
        }
    }
    return true;
}

    @FXML
    void StartExecuteFlowButton(ActionEvent event){
        masterDetailPane = new MasterDetailPane();
        DTOFreeInputsFromUser freeInputs = new DTOFreeInputsFromUser(freeInputMap);
        DTOFlowExecution flowExecution = mainController.getSystemEngineInterface().activateFlowByName(mainController.getFlowName(), freeInputs);
        setExecutedFlowID(flowExecution.getUniqueIdByUUID());

        freeInputMap = new HashMap<>();
        ExecuteFlowTask currentRunningTask = new ExecuteFlowTask(UUID.fromString(executedFlowIDProperty.getValue()),
                masterDetailController,executedFlowIDProperty.getValue(), new SimpleBooleanProperty(false));

        new Thread(currentRunningTask).start();
    }

}
