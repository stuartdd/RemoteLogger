/*
 * Copyright (C) 2018 stuartdd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package main;

import common.Action;
import expectations.Expectation;
import expectations.ExpectationManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import json.JsonUtils;
import model.MultiModelManager;
import server.Server;
import server.ServerManager;

/**
 *
 * @author stuart
 */
public class FXMLDocumentController extends BorderPane implements ApplicationController, Initializable {

    private static final String NL = System.getProperty("line.separator");
    private static final Expectation EXAMPLE_EXPECTATION = ExpectationManager.getExampleExpectation();
    private static final PackagedRequest EXAMPLE_PACKAGED_EXCEPTION = PackagedRequestWrapperManager.getExampleRequest();
    private static final String EXAMPLE_EXPECTATION_HEAD = "Example Expectation (annotated):";
    private static final String EXAMPLE_PACKAGED_REQUESTS_HEAD = "Example Packged Exception (annotated):";
    private static final String EXAMPLE_EXPECTATION_HEAD_RO = "Expectations are READ-ONLY:\nThey were not read from the file system.";
    private static final String EXAMPLE_PACKAGED_REQUESTS_HEAD_RO = "Packaged Requests are READ-ONLY:\nThey were not read from the file system.";
    private static final String UL = NL + "--------------------------------------------------------------------" + NL;
    private static final String EXAMPLE_EXPECTATION_NOTES = "Notes:\n"
            + "All fields are optional except name.\n"
            + "[1] Matching or match examples:\n"
            + "  'BBC' Exact match\n"
            + "  '\\\\*BBC' Exact match '*BBC'\n"
            + "  '*BBC*' Contains\n"
            + "  'BBC*AAA' Starts with BBC and ends With AAA\n"
            + "  'BBC*' Starts with\n"
            + "  '*BBC' Ends With\n"
            + "[2] Template is loaded only if body is null\n"
            + "[3] Substitutions from properties (See the logs for values). E.g:\n"
            + "    Method %{METHOD} URL:'%{PATH}' HOST:%{HEAD.Host}\n"
            + "[4] Must be one from the list:";
    private static final String EXAMPLE_PACKAGED_REQUEST_NOTES = "Notes:\n"
            + "All fields are optional except name.\n"
            + "For an empty body define body and template as null:";

    private LogLine firstMainLog = null;
    private LogLine lastMainLog = firstMainLog;

    private LogLine firstLog = null;
    private LogLine lastLog = firstLog;

    private MultiModelManager expectationsModel = MultiModelManager.instance(Expectation.class);
    private MultiModelManager packagedRequModel = MultiModelManager.instance(PackagedRequest.class);

    private ExpectationSelectionChangedListener expectationSelectionChangedListener;
    private PackagedRequestSelectionChangedListener packagedRequestSelectionChangedListener;

    private Server selectedServer;
    private int selectedServerPort = -1;
    private ExpectationWrapperManager expectationWrapperManager;
    private PackagedRequestWrapperList packagedRequestWrapperList;
    private boolean updatedExpectationisValid = true;
    private boolean updatedPackagedRequestisValid = true;

    @FXML
    private TextArea expectationTextAreaErrors;

    @FXML
    private ListView expectationsListView;

    @FXML
    private ListView packagedRequestsListView;

    @FXML
    private SplitPane expectationsSplitPane;

    @FXML
    private SplitPane packagedRequestSplitPane;

    @FXML
    private TextArea expectationTextArea;

    @FXML
    private TextArea packageRequestTextArea;

    @FXML
    private TextArea packageRequestTextAreaErrors;

    @FXML
    private CheckBox checkBoxTime;

    @FXML
    private CheckBox checkBoxLogProperties;

    @FXML
    private CheckBox checkBoxPort;

    @FXML
    private CheckBox checkBoxAutoStart;

    @FXML
    private CheckBox checkBoxShowPort;

    @FXML
    private CheckBox checkBoxHeaders;

    @FXML
    private CheckBox checkBoxBody;

    @FXML
    private CheckBox checkBoxEmpty;

    @FXML
    private TextArea textAreaLogging;

    @FXML
    private TextArea textAreaLog;

    @FXML
    private Label labelStatus;

    @FXML
    private Label labelExpectationList;

    @FXML
    private Button buttonConnect;

    @FXML
    private Button buttonSendPackagedRequest;

    @FXML
    private Button buttonSaveExpectations;

    @FXML
    private Button buttonReLoadExpectations;

    @FXML
    private Button buttonRenameExpectation;

    @FXML
    private Button buttonDeleteExpectation;

    @FXML
    private Button buttonNewExpectation;

    @FXML
    private Button buttonSavePackagedRequest;

    @FXML
    private Button buttonReloadPackagedRequest;

    @FXML
    private Button buttonDeletePackagedRequest;

    @FXML
    private Button buttonRenamePackagedRequest;

    @FXML
    private Button buttonNewPackagedRequest;

    @FXML
    private Label labelSaveExpectations;

    @FXML
    private Label labelSavePackagedRequests;

    @FXML
    private ChoiceBox choiceBoxPortNumber;

    @FXML
    public void closeAction() {
        Main.closeApplication(false);
    }

    @FXML
    public void expectationTextAreaKeyTyped() {
        if (expectationWrapperManager.loadedFromFile()) {
            Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.EXPECTATION_TEXT_CHANGED, null, "Validate Expectation JSON");
        }
    }

    @FXML
    public void packagedRequestTextAreaKeyTyped() {
        if (expectationWrapperManager.loadedFromFile()) {
            Main.notifyAction(System.currentTimeMillis(), -1, Action.PACKAGED_REQUEST_TEXT_CHANGED, null, "Validate Packaged Request JSON");
        }
    }

    @FXML
    public void clearMainLogAction() {
        Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.CLEAR_MAIN_LOGS, null, "Log has been cleared");
    }

    @FXML
    public void buttonReloadPackagedRequestAction() {
        Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.RELOAD_PACKAGED_REQUEST, null, "Reload the packaged request");
    }

    @FXML
    public void buttonSendPackagedRequestAction() {
        Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.SEND_PACKAGED_REQUEST, packagedRequestWrapperList.getSelectedPackagedRequest(), "Send the packaged request");
    }

    @FXML
    public void buttonReLoadExpectationsAction() {
        Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.RELOAD_EXPECTATIONS, null, "Expectations reloaded");
    }

    @FXML
    public void buttonNewExpectationAction() {
        String name = Main.textInputDialog("ADD EXPECTATION", "Add new expectation. Note that the name must be unique", "Name", "");
        if (name != null) {
            String cause = expectationWrapperManager.checkNewExpectationName(name);
            if (cause != null) {
                Main.errorDialog("ERROR", cause, "Please try again");
            } else {
                Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.ADD_EXPECTATION, name, "Expectation renamed");
            }
        }
    }

    @FXML
    public void buttonSaveExpectationsAction() {
        Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.SAVE_EXPECTATIONS, null, "Save expectations");
    }

    @FXML
    public void buttonSavePackagedRequestAction() {
        Main.notifyAction(System.currentTimeMillis(), -1, Action.SAVE_PACKAGED_REQUEST, null, "Save Packaged Request");
    }

    @FXML
    public void buttonDeletePackagedRequestAction() {
        Main.notifyAction(System.currentTimeMillis(), -1, Action.DELETE_PACKAGED_REQUEST, null, "Delete Packaged Request");
    }

    @FXML
    public void buttonRenamePackagedRequestAction() {
        String name = Main.textInputDialog("RENAME PACKAGED REQUEST", "Rename the current packaged request. Note that the name must be unique", "Name", packagedRequestWrapperList.getSelectedPackagedRequest().getName());
        if (name != null) {
            String cause = PackagedRequestWrapperManager.checkNewPackagedRequestName(name);
            if (cause != null) {
                Main.errorDialog("ERROR", cause, "Please try again");
            } else {
                Main.notifyAction(System.currentTimeMillis(), -1, Action.RENAME_PACKAGED_REQUEST, name, "Rename Packaged Request");
            }
        }
    }

    @FXML
    public void buttonNewPackagedRequestAction() {
        String name = Main.textInputDialog("ADD PACKAGED REQUEST", "Add new Packaged Request. Note that the name must be unique", "Name", "");
        if (name != null) {
            String cause = PackagedRequestWrapperManager.checkNewPackagedRequestName(name);
            if (cause != null) {
                Main.errorDialog("ERROR", cause, "Please try again");
            } else {
                Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.ADD_PACKAGED_REQUEST, name, "New Packaged Request");
            }
        }
    }

    @FXML
    public void buttonDeleteExpectationAction() {
        Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.DELETE_EXPECTATION, null, "Delete expectation");
    }

    @FXML
    public void buttonRenameExpectationAction() {
        String name = Main.textInputDialog("RENAME EXPECTATION", "Rename the current expectation. Note that the name must be unique", "Name", expectationWrapperManager.getName());
        if (name != null) {
            String cause = expectationWrapperManager.checkNewExpectationName(name);
            if (cause != null) {
                Main.errorDialog("ERROR", cause, "Please try again");
            } else {
                Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.RENAME_EXPECTATION, name, "Expectation renamed");
            }
        }
    }

    @FXML
    public void clearLogAction() {
        Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.CLEAR_LOGS, null, "Log has been cleared");
    }

    @FXML
    public void checkBoxTimeAction() {
        Main.notifyConfigChangeOption(Option.TIME, selectedServerPort, checkBoxTime.isSelected(), "");
    }

    @FXML
    public void checkBoxLogPropertiesAction() {
        selectedServer.setLogProperties(checkBoxLogProperties.isSelected());
        expectationWrapperManager.setLogProperties(checkBoxLogProperties.isSelected());
    }

    @FXML
    public void checkBoxAutoStartAction() {
        selectedServer.setAutoStart(checkBoxAutoStart.isSelected());
    }

    @FXML
    public void checkBoxPortAction() {
        Main.notifyConfigChangeOption(Option.PORT, selectedServerPort, checkBoxPort.isSelected(), "");
    }

    @FXML
    public void checkBoxHeadersAction() {
        Main.notifyConfigChangeOption(Option.FILTER_HEADERS, selectedServerPort, checkBoxHeaders.isSelected(), "");
    }

    @FXML
    public void checkBoxShowPortAction() {
        selectedServer.setShowPort(checkBoxShowPort.isSelected());
        Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.LOG_REFRESH, null, "Log has been Updated");
    }

    @FXML
    public void checkBoxBodyAction() {
        Main.notifyConfigChangeOption(Option.FILTER_BODY, selectedServerPort, checkBoxBody.isSelected(), "");
    }

    @FXML
    public void checkBoxEmptyAction() {
        Main.notifyConfigChangeOption(Option.FILTER_EMPTY, selectedServerPort, checkBoxEmpty.isSelected(), "");
    }

    @FXML
    public void connectAction() {
        if (buttonConnect.getText().equalsIgnoreCase("start")) {
            Main.startServer(getSelectedPort());
        } else {
            Main.stopServer(getSelectedPort());
        }
    }

    private PackagedRequestWrapperList refreshPackagedRequests(PackagedRequestWrapperList packagedRequestWrapperListIn) {
        packagedRequestsListView.setItems(FXCollections.observableArrayList(packagedRequestWrapperListIn.getWrappedPackagedRequests()));
        packagedRequestsListView.getSelectionModel().select(packagedRequestWrapperListIn.getSelectedIndex());
        return packagedRequestWrapperListIn;
    }

    private void changeSelectedServer(Server server) {
        System.out.println("changeSelectedServer:" + server);
        selectedServer = server;
        selectedServerPort = selectedServer.getPort();
        if (!selectedServer.isShowPort()) {
            selectedServer.setShowPort(true);
        }
        checkBoxAutoStart.setSelected(selectedServer.isAutoStart());
        checkBoxShowPort.setSelected(selectedServer.isShowPort());
        checkBoxLogProperties.setSelected(selectedServer.isLogProperties());
        labelExpectationList.setText("Expectations for server on port:" + selectedServerPort);
        updateServerStatus(server);
        changeSelectedExpectationManager(server);
    }

    private void changeSelectedExpectationManager(Server server) {
        System.out.println("changeSelectedExpectationManager:" + server);
        expectationSelectionChangedListener.supressActions(true);
        try {
            expectationWrapperManager.setSelectedPort(server.getPort());
            expectationsListView.setItems(FXCollections.observableArrayList(expectationWrapperManager.getWrappedExpectations()));
            expectationsListView.getSelectionModel().selectFirst();
            expectationWrapperManager.selectFirst();
        } finally {
            expectationSelectionChangedListener.supressActions(false);
        }
        displaySelectedExpectation();
        expectationsListView.refresh();
    }

    private void displaySelectedPackagedRequest() {
        if (packagedRequestWrapperList == null) {
            packageRequestTextArea.setText(
                    "Please Add:\n\n"
                    + "\"packagedRequestsFile\" : \"packagedRequests.json\"\n\n"
                    + "to the main configuration data file:\n"
                    + ConfigData.readFileName() + "\nto enable Packaged Requests:\n\n"
                    + "Then restart the application.");
            buttonSendPackagedRequest.setDisable(true);
            buttonReloadPackagedRequest.setDisable(true);
        } else {
            PackagedRequestWrapper packagedRequestWrapper = packagedRequestWrapperList.getSelectedPackagedRequestWrapper();
            if (packagedRequestWrapper != null) {
                packageRequestTextArea.setText(packagedRequestWrapper.getJson());
                buttonSendPackagedRequest.setText("Send: " + packagedRequestWrapper.getName());
            }
            setPackagedRequestTextColourAndInfo(false, null);
            buttonSendPackagedRequest.setDisable(false);
            buttonReloadPackagedRequest.setDisable(false);
        }
        labelSavePackagedRequests.setVisible(!PackagedRequestWrapperManager.isLoadedFromFile());
        configurePackagedRequestSaveOptions();
    }

    private void displaySelectedExpectation() {
        expectationSelectionChangedListener.supressActions(true);
        try {
            ExpectationWrapper expectationWrapper = expectationWrapperManager.getSelectedExpectationWrapper();
            if (expectationWrapper != null) {
                /*
                 Make sure we have the correct one selected
                 */
                ExpectationWrapper actual = (ExpectationWrapper) expectationsListView.getSelectionModel().getSelectedItem();
                if (actual != expectationWrapper) {
                    expectationsListView.getSelectionModel().select(expectationWrapper);
                }
                expectationTextArea.setText(expectationWrapper.toJson());
                setExpectationTextColourAndInfo(false, null);
            } else {
                expectationTextArea.setText("");
                setExpectationTextColourAndInfo(true, null);
            }
            configureExpectationSaveOptions();
        } finally {
            expectationSelectionChangedListener.supressActions(false);
        }
    }

    private boolean canSaveUpdatedExpectation() {
        return (expectationWrapperManager != null)
                && updatedExpectationisValid
                && expectationWrapperManager.isSelected()
                && expectationWrapperManager.isUpdated()
                && expectationWrapperManager.loadedFromFile();
    }

    private void configureExpectationSaveOptions() {
        if (expectationWrapperManager.loadedFromFile()) {
            buttonSaveExpectations.setDisable(!canSaveUpdatedExpectation());
            buttonReLoadExpectations.setDisable(buttonSaveExpectations.isDisabled());
            buttonRenameExpectation.setDisable(!updatedExpectationisValid);
            buttonNewExpectation.setDisable(false);
            buttonDeleteExpectation.setDisable(expectationWrapperManager.canNotDelete());
            labelSaveExpectations.setVisible(false);
        } else {
            labelSaveExpectations.setVisible(true);
            buttonSaveExpectations.setDisable(true);
            buttonReLoadExpectations.setDisable(true);
            buttonRenameExpectation.setDisable(true);
            buttonNewExpectation.setDisable(true);
            buttonDeleteExpectation.setDisable(true);
        }
    }

    private void configurePackagedRequestSaveOptions() {
        if ((packagedRequestWrapperList == null) || (!PackagedRequestWrapperManager.isLoadedFromFile())) {
            buttonSavePackagedRequest.setDisable(true);
            buttonDeletePackagedRequest.setDisable(true);
            buttonRenamePackagedRequest.setDisable(true);
            buttonNewPackagedRequest.setDisable(true);
        } else {
            buttonSavePackagedRequest.setDisable(!PackagedRequestWrapperManager.isUpdated());
            buttonDeletePackagedRequest.setDisable(PackagedRequestWrapperManager.canNotDelete());
            buttonRenamePackagedRequest.setDisable(false);
            buttonNewPackagedRequest.setDisable(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Main.setApplicationController(this);
        
        expectationSelectionChangedListener = new ExpectationSelectionChangedListener();
        packagedRequestSelectionChangedListener = new PackagedRequestSelectionChangedListener();

        checkBoxHeaders.setSelected(ConfigData.getInstance().isIncludeHeaders());
        checkBoxBody.setSelected(ConfigData.getInstance().isIncludeBody());
        checkBoxEmpty.setSelected(ConfigData.getInstance().isIncludeEmpty());
        checkBoxTime.setSelected(ConfigData.getInstance().isShowTime());
        checkBoxPort.setSelected(ConfigData.getInstance().isShowPort());
        textAreaLogging.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        textAreaLog.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        resetMainLog();
        ServerManager.autoStartServers();
        
        expectationWrapperManager = new ExpectationWrapperManager();
        for (int p : ServerManager.portListSorted()) {
            expectationWrapperManager.add(p, new ExpectationWrapperList(ServerManager.getExpectationManager(p)));
        }

        updateMainLog(System.currentTimeMillis(), selectedServerPort, LogCatagory.EMPTY, null);
        changeSelectedServer(Main.getDefaultServer());
        choiceBoxPortNumber.setItems(FXCollections.observableArrayList(ServerManager.portList()));
        choiceBoxPortNumber.getSelectionModel().select("" + selectedServerPort);
        /*
        Link up th echange notifiers. Must be done AFTER init!
         */
        choiceBoxPortNumber.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue != newValue) {
                    Main.notifyAction(System.currentTimeMillis(), selectedServerPort, Action.SERVER_SELECTED, ServerManager.getServer(ServerManager.portListSorted()[newValue.intValue()]), "Server Selected [" + newValue + "]");
                }
            }
        });
        expectationsListView.getSelectionModel().selectedIndexProperty().addListener(expectationSelectionChangedListener);
        packagedRequestWrapperList = Main.getPackagedRequestWrapperList();
        if (packagedRequestWrapperList != null) {
            packagedRequestsListView.setItems(FXCollections.observableArrayList(packagedRequestWrapperList.getWrappedPackagedRequests()));
            packagedRequestsListView.getSelectionModel().select(packagedRequestWrapperList.getSelectedIndex());
            packagedRequestsListView.getSelectionModel().selectedIndexProperty().addListener(packagedRequestSelectionChangedListener);
            setPackagedRequestTextColourAndInfo(false, null);
        }
        displaySelectedPackagedRequest();
        /*
        Do some stuff later in a separate thread!
         */
        if (ConfigData.getInstance().getExpDividerPos() != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < ConfigData.getInstance().getExpDividerPos().length; i++) {
                        expectationsSplitPane.getDividers().get(i).setPosition(ConfigData.getInstance().getExpDividerPos()[i]);
                    }
                }
            });
        }
        if (ConfigData.getInstance().getPackDividerPos() != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < ConfigData.getInstance().getPackDividerPos().length; i++) {
                        packagedRequestSplitPane.getDividers().get(i).setPosition(ConfigData.getInstance().getPackDividerPos()[i]);
                    }
                }
            });
        }
    }

    @Override
    public boolean notifyAction(long time, int port, Action action, Object actionOn, String message) {
        switch (action) {
            case PACKAGED_REQUEST_TEXT_CHANGED:
                if (PackagedRequestWrapperManager.isLoadedFromFile()) {
                    String json = packageRequestTextArea.getText();
                    try {
                        PackagedRequest validClonedPackagedRequest = (PackagedRequest) JsonUtils.beanFromJson(PackagedRequest.class, json);
                        if (validClonedPackagedRequest.getName().equals(packagedRequestWrapperList.getSelectedPackagedName())) {
                            PackagedRequestWrapperManager.replace(validClonedPackagedRequest);
                            setPackagedRequestTextColourAndInfo(false, null);
                        } else {
                            setPackagedRequestTextColourAndInfo(true, "Cannot rename a Packaged Requet from the Editor.\n\nPlease use the Rename button.\nPress Ctrl+Z to correct and continue");
                        }
                    } catch (Exception ex) {
                        setPackagedRequestTextColourAndInfo(true, ex.getCause().getMessage());
                    }
                } else {
                    setPackagedRequestTextColourAndInfo(false, null);
                }
                configurePackagedRequestSaveOptions();
                break;
            case EXPECTATION_TEXT_CHANGED:
                if (expectationWrapperManager.loadedFromFile()) {
                    String json = expectationTextArea.getText();
                    try {
                        Expectation validClonedExpectation = (Expectation) JsonUtils.beanFromJson(Expectation.class, json);
                        if (validClonedExpectation.getName().equals(expectationWrapperManager.getSelectedExpectation().getName())) {
                            expectationWrapperManager.replaceSelectedExpectation(validClonedExpectation);
                            setExpectationTextColourAndInfo(false, null);
                        } else {
                            setExpectationTextColourAndInfo(true, "Cannot rename an Expectation from the Editor.\n\nPlease use the Rename button.\nPress Ctrl+Z to correct and continue");
                        }
                    } catch (Exception ex) {
                        setExpectationTextColourAndInfo(true, ex.getCause().getMessage());
                    }
                } else {
                    setExpectationTextColourAndInfo(false, null);
                }
                configureExpectationSaveOptions();
                break;
            case SAVE_EXPECTATIONS:
                expectationWrapperManager.save();
                changeSelectedExpectationManager(selectedServer);
                break;
            case DELETE_EXPECTATION:
                expectationWrapperManager.delete();
                changeSelectedExpectationManager(selectedServer);
                break;
            case RELOAD_EXPECTATIONS:
                expectationWrapperManager.reloadExpectations();
                changeSelectedExpectationManager(selectedServer);
                break;
            case ADD_EXPECTATION:
                expectationWrapperManager.addExpectationWithName((String) actionOn);
                changeSelectedExpectationManager(selectedServer);
                break;
            case RENAME_EXPECTATION:
                expectationWrapperManager.rename((String) actionOn);
                changeSelectedExpectationManager(selectedServer);
                break;
            case SEND_PACKAGED_REQUEST:
                PackagedRequestWrapperManager.sendPackagedRequest((PackagedRequest) actionOn);
                break;
            case RENAME_PACKAGED_REQUEST:
                packagedRequestWrapperList = refreshPackagedRequests(PackagedRequestWrapperManager.rename(packagedRequestWrapperList.getSelectedPackagedRequest().getName(), (String) actionOn));
                break;
            case ADD_PACKAGED_REQUEST:
                packagedRequestWrapperList = refreshPackagedRequests(PackagedRequestWrapperManager.add((String) actionOn, packagedRequestWrapperList.getSelectedPackagedRequest().getName()));
                break;
            case SAVE_PACKAGED_REQUEST:
                packagedRequestWrapperList = refreshPackagedRequests(PackagedRequestWrapperManager.save(packagedRequestWrapperList.getSelectedPackagedRequest().getName()));
                break;
            case DELETE_PACKAGED_REQUEST:
                packagedRequestWrapperList = refreshPackagedRequests(PackagedRequestWrapperManager.delete(packagedRequestWrapperList.getSelectedPackagedRequest().getName()));
                break;
            case RELOAD_PACKAGED_REQUEST:
                packagedRequestWrapperList = refreshPackagedRequests(PackagedRequestWrapperManager.reload(packagedRequestWrapperList.getSelectedPackagedRequest().getName()));
                break;
            case PACKAGE_REQUEST_SELECTED:
                packagedRequestWrapperList.setSelectedIndex((Integer) actionOn);
                displaySelectedPackagedRequest();
                break;
            case EXPECTATION_SELECTED:
                expectationWrapperManager.setSelectedExpectationWrapper((Integer) actionOn);
                displaySelectedExpectation();
                break;
            case SERVER_SELECTED:
                changeSelectedServer((Server) actionOn);
                break;
            case SERVER_STATE:
                updateServerStatus((Server) actionOn);
                break;
            case CLEAR_LOGS:
                resetLog();
                updateLog(time, port, LogCatagory.CLEAR, null);
                break;
            case CLEAR_MAIN_LOGS:
                resetMainLog();
                updateMainLog(time, port, LogCatagory.CLEAR, null);
                break;
            case LOG_BODY:
                updateMainLog(time, port, LogCatagory.BODY, message);
                break;
            case LOG_HEADER:
                updateMainLog(time, port, LogCatagory.HEADER, message);
                break;
            case LOG_REFRESH:
                updateMainLog(time, port, LogCatagory.ACTION, null);
                updateLog(time, port, LogCatagory.ACTION, null);
                labelStatus.setText("Log display refreshed");
                break;
            case LOG:
                updateLog(time, port, LogCatagory.LOG, message);
                labelStatus.setText("Log display refreshed");
                break;
            case CONFIG_SAVE_ERROR:
                Alert alert = new Alert(AlertType.CONFIRMATION, message + "\n\nConfiguration data was not updated. \n\nPress OK to exit");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        Main.closeApplication(true);
                    }
                });
                break;
        }
        return true;
    }

    private void setPackagedRequestTextColourAndInfo(boolean isError, String message) {
        Color color;
        updatedPackagedRequestisValid = !isError;
        packageRequestTextAreaErrors.setEditable(false);
        if (!PackagedRequestWrapperManager.isLoadedFromFile()) {
            color = Color.LIGHTCYAN;
            packageRequestTextArea.setEditable(false);
            packageRequestTextAreaErrors.setText(EXAMPLE_PACKAGED_REQUESTS_HEAD_RO + UL + JsonUtils.toJsonFormatted(EXAMPLE_PACKAGED_EXCEPTION) + UL + EXAMPLE_PACKAGED_REQUEST_NOTES);
        } else {
            packageRequestTextArea.setEditable(true);
            if (isError) {
                packageRequestTextAreaErrors.setText(message);
                color = Color.PINK;
            } else {
                packageRequestTextAreaErrors.setText(EXAMPLE_PACKAGED_REQUESTS_HEAD + UL + JsonUtils.toJsonFormatted(EXAMPLE_PACKAGED_EXCEPTION) + UL + EXAMPLE_PACKAGED_REQUEST_NOTES);
                color = Color.LIGHTGREEN;
            }
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Region region1 = (Region) packageRequestTextArea.lookup(".content");
                Region region2 = (Region) packageRequestTextAreaErrors.lookup(".content");
                if (region1 != null) {
                    region1.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
                }
                if (region2 != null) {
                    region2.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }
        });

    }

    private void setExpectationTextColourAndInfo(boolean isError, String message) {
        Color color;
        expectationTextAreaErrors.setEditable(false);
        updatedExpectationisValid = !isError;
        if (!expectationWrapperManager.loadedFromFile()) {
            color = Color.LIGHTCYAN;
            expectationTextArea.setEditable(false);
            expectationTextAreaErrors.setText(EXAMPLE_EXPECTATION_HEAD_RO + UL + JsonUtils.toJsonFormatted(EXAMPLE_EXPECTATION) + UL + EXAMPLE_EXPECTATION_NOTES);
        } else {
            expectationTextArea.setEditable(true);
            if (isError) {
                expectationTextAreaErrors.setText(message);
                color = Color.PINK;
            } else {
                expectationTextAreaErrors.setText(EXAMPLE_EXPECTATION_HEAD + UL + JsonUtils.toJsonFormatted(EXAMPLE_EXPECTATION) + UL + EXAMPLE_EXPECTATION_NOTES);
                color = Color.LIGHTGREEN;
            }
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Region region1 = (Region) expectationTextArea.lookup(".content");
                Region region2 = (Region) expectationTextAreaErrors.lookup(".content");
                if (region1 != null) {
                    region1.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
                }
                if (region2 != null) {
                    region2.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }
        });

    }

    @Override
    public boolean canAppClose() {
        return !((expectationWrapperManager.isUpdated()) || (PackagedRequestWrapperManager.isUpdated()));
    }

    @Override
    public void updateConfig(ConfigData configData) {
        if (expectationsSplitPane != null) {
            int count = expectationsSplitPane.getDividers().size();
            double[] pos = new double[count];
            for (int i = 0; i < count; i++) {
                pos[i] = expectationsSplitPane.getDividers().get(i).getPosition();
            }
            configData.setExpDividerPos(pos);
        }
        if (packagedRequestSplitPane != null) {
            int count = packagedRequestSplitPane.getDividers().size();
            double[] pos = new double[count];
            for (int i = 0; i < count; i++) {
                pos[i] = packagedRequestSplitPane.getDividers().get(i).getPosition();
            }
            configData.setPackDividerPos(pos);
        }
        if (packagedRequestWrapperList == null) {
            configData.setSelectedPackagedRequestName(null);
        } else {
            configData.setSelectedPackagedRequestName(packagedRequestWrapperList.getSelectedPackagedName());
        }

    }

    private void updateMainLog(long time, int port, LogCatagory cat, String message) {
        switch (cat) {
            case ACTION:
                break;
            case CLEAR:
                textAreaLogging.setText("Log is empty 1");
                labelStatus.setText("Log message is empty!");
                firstMainLog = null;
                lastMainLog = firstMainLog;
                break;
            default:
                LogLine ll = new LogLine(time, port, message, cat);
                if (firstMainLog == null) {
                    firstMainLog = ll;
                    lastMainLog = firstMainLog;
                    labelStatus.setText("Log 1st line:" + message);
                } else {
                    lastMainLog.setNext(ll);
                    lastMainLog = ll;
                }
        }
        textAreaLogging.setText(filterMainLog());
    }

    private void updateLog(long time, int port, LogCatagory cat, String message) {
        switch (cat) {
            case ACTION:
                break;
            case CLEAR:
                textAreaLogging.setText("Log is empty 1");
                labelStatus.setText("Log message is empty!");
                firstLog = null;
                lastLog = firstLog;
                break;
            default:
                LogLine ll = new LogLine(time, port, message, cat);
                if (firstLog == null) {
                    firstLog = ll;
                    lastLog = firstLog;
                    labelStatus.setText("Log 1st line:" + message);
                } else {
                    lastLog.setNext(ll);
                    lastLog = ll;
                    textAreaLog.setText(filterLog());
                }
        }
        textAreaLog.setText(filterLog());
    }

    private void resetMainLog() {
        firstMainLog = null;
        lastMainLog = firstMainLog;
    }

    private void resetLog() {
        firstLog = null;
        lastLog = firstLog;
    }

    private String filterMainLog() {
        StringBuilder sb = new StringBuilder();
        LogLine line = firstMainLog;
        while (line != null) {
            if (ServerManager.isShowPort(line.getPort())) {
                switch (line.getCatagory()) {
                    case EMPTY:
                        if (ConfigData.getInstance().isIncludeEmpty()) {
                            sb.append(NL);
                        }
                        break;
                    case BODY:
                        if (ConfigData.getInstance().isIncludeBody()) {
                            line.render(sb, ConfigData.getInstance());
                        }
                        break;
                    case HEADER:
                        if (ConfigData.getInstance().isIncludeHeaders()) {
                            line.render(sb, ConfigData.getInstance());
                        }
                        break;
                }
            }
            line = line.getNext();
        }
        return sb.toString().trim();
    }

    private String filterLog() {
        StringBuilder sb = new StringBuilder();
        LogLine line = firstLog;
        while (line != null) {
            if ((ServerManager.isShowPort(line.getPort()) || (line.getPort() < 0))) {
                line.render(sb, ConfigData.getInstance());
            }
            line = line.getNext();
        }
        return sb.toString().trim();
    }

    private int getSelectedPort() {
        return Integer.parseInt(choiceBoxPortNumber.getSelectionModel().getSelectedItem().toString());
    }

    private void updateServerStatus(Server server) {
        if (server.getPort() == selectedServer.getPort()) {
            Main.setTitle(server.getServerState().getInfo() + " Port[" + server.getPort() + "]");
            switch (server.getServerState()) {
                case SERVER_STARTING:
                case SERVER_STOPPING:
                    buttonConnect.setDisable(true);
                    choiceBoxPortNumber.setBackground(new Background(new BackgroundFill(Color.PINK, CornerRadii.EMPTY, Insets.EMPTY)));
                    break;
                case SERVER_STOPPED:
                    buttonConnect.setDisable(false);
                    buttonConnect.setText("Start");
                    choiceBoxPortNumber.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                    break;
                case SERVER_RUNNING:
                    buttonConnect.setDisable(false);
                    buttonConnect.setText("Stop");
                    choiceBoxPortNumber.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                    break;
                case SERVER_FAIL:
                    buttonConnect.setDisable(false);
                    buttonConnect.setText("Start");
                    choiceBoxPortNumber.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                    break;
                case SERVER_PENDING:
                    buttonConnect.setDisable(false);
                    buttonConnect.setText("Start");
                    choiceBoxPortNumber.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
                    break;
            }
        }
    }

}
