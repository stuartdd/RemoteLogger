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
import server.Server;
import server.ServerManager;

/**
 *
 * @author stuart
 */
public class FXMLDocumentController extends BorderPane implements ApplicationController, Initializable {

    private static final String NL = System.getProperty("line.separator");
    private static final Expectation EXAMPLE = ExpectationManager.getExampleExpectation();
    private static final String EXAMPLE_HEAD = "Example Expectation (annotated):";
    private static final String EXAMPLE_HEAD_RO = "Expectations are READ-ONLY:\nThey were not read from the file system.";
    private static final String UL = NL + "--------------------------------------------------------------------" + NL;
    private static final String EXAMPLE_NOTES = "Notes:\n"
            + "All fields are optional excep name.\n"
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

    private LogLine firstMainLog = null;
    private LogLine lastMainLog = firstMainLog;

    private LogLine firstLog = null;
    private LogLine lastLog = firstLog;

    private Server selectedServer;
    private ExpectationManager selectedExpectationManager;
    private ExpectationSelectionChangedListener expectationSelectionChangedListener;
    private Expectation validClonedExpectation;
    private Expectation selectedExpectation;
    private String selectedExpectationJson;

    @FXML
    private TextArea expectationTextAreaErrors;

    @FXML
    private ListView expectationsListView;

    @FXML
    private SplitPane expectationsSplitPane;

    @FXML
    private TextArea expectationTextArea;

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
    private Button buttonReLoadExpectations;

    @FXML
    private Button buttonConnect;

    @FXML
    private Button buttonSaveExpectations;

    @FXML
    private Label labelSaveExpectations;

    @FXML
    private ChoiceBox choiceBoxPortNumber;

    @FXML
    public void closeAction() {
        Main.closeApplication(false);
    }

    @FXML
    public void expectationTextAreaKeyTyped() {
        if (selectedExpectationManager.isLoadedFromAFile()) {
            Main.notifyAction(System.currentTimeMillis(), -1, Action.EXPECTATION_TEXT_CHANGED, null, "Validate Expectation JSON");
        }
    }

    @FXML
    public void clearMainLogAction() {
        Main.notifyAction(System.currentTimeMillis(), -1, Action.CLEAR_MAIN_LOGS, null, "Log has been cleared");
    }

    @FXML
    public void buttonReLoadExpectationsAction() {
        Main.notifyAction(System.currentTimeMillis(), -1, Action.RELOAD_EXPECTATIONS, null, "Expectations reloaded");
    }

    @FXML
    public void buttonSaveExpectationsAction() {
        Main.notifyAction(System.currentTimeMillis(), -1, Action.SAVE_EXPECTATIONS, null, "Save updated expectations");
    }

    @FXML
    public void clearLogAction() {
        Main.notifyAction(System.currentTimeMillis(), -1, Action.CLEAR_LOGS, null, "Log has been cleared");
    }

    @FXML
    public void checkBoxTimeAction() {
        Main.notifyConfigChangeOption(Option.TIME, -1, checkBoxTime.isSelected(), "");
    }

    @FXML
    public void checkBoxLogPropertiesAction() {
        selectedServer.setLogProperties(checkBoxLogProperties.isSelected());
    }

    @FXML
    public void checkBoxAutoStartAction() {
        selectedServer.setAutoStart(checkBoxAutoStart.isSelected());
    }

    @FXML
    public void checkBoxPortAction() {
        Main.notifyConfigChangeOption(Option.PORT, -1, checkBoxPort.isSelected(), "");
    }

    @FXML
    public void checkBoxHeadersAction() {
        Main.notifyConfigChangeOption(Option.FILTER_HEADERS, -1, checkBoxHeaders.isSelected(), "");
    }

    @FXML
    public void checkBoxShowPortAction() {
        selectedServer.setShowPort(checkBoxShowPort.isSelected());
        Main.notifyAction(System.currentTimeMillis(), -1, Action.LOG_REFRESH, null, "Log has been Updated");
    }

    @FXML
    public void checkBoxBodyAction() {
        Main.notifyConfigChangeOption(Option.FILTER_BODY, -1, checkBoxBody.isSelected(), "");
    }

    @FXML
    public void checkBoxEmptyAction() {
        Main.notifyConfigChangeOption(Option.FILTER_EMPTY, -1, checkBoxEmpty.isSelected(), "");
    }

    @FXML
    public void connectAction() {
        if (buttonConnect.getText().equalsIgnoreCase("start")) {
            Main.startServer(getSelectedPort());
        } else {
            Main.stopServer(getSelectedPort());
        }
    }

    private void changeSelectedServer(Server server) {
        System.out.println("changeSelectedServer:" + server);
        selectedServer = server;
        if (!selectedServer.isShowPort()) {
            selectedServer.setShowPort(true);
        }
        checkBoxAutoStart.setSelected(selectedServer.isAutoStart());
        checkBoxShowPort.setSelected(selectedServer.isShowPort());
        checkBoxLogProperties.setSelected(selectedServer.isLogProperties());
        updateServerStatus(server);
        changeSelectedExpectationManager(server);
    }

    private void changeSelectedExpectationManager(Server server) {
        System.out.println("changeSelectedExpectationManager:" + server);
        expectationSelectionChangedListener.setSupressActions(true);
        try {
            selectedExpectationManager = server.getExpectationManager();
            expectationsListView.setItems(FXCollections.observableArrayList(ExpectationWrapper.wrap(selectedExpectationManager.getExpectations())));
            expectationsListView.getSelectionModel().selectFirst();
            changeSelectedExpectation(getTheSelectedExpectation());
        } finally {
            expectationSelectionChangedListener.setSupressActions(false);
        }
    }

    private void refreshExpectationListView() {
        System.out.println("refreshExpectationListView");
        expectationsListView.setItems(FXCollections.observableArrayList(ExpectationWrapper.wrap(selectedExpectationManager.getExpectations())));
        changeSelectedExpectation(reSelectThisExpectation(selectedExpectation));
    }

    private void changeSelectedExpectation(Integer expectationIndex) {
        if (expectationIndex >= 0) {
            changeSelectedExpectation(((ExpectationWrapper) expectationsListView.getItems().get(expectationIndex)).getExpectation());
        }
    }

    private void changeSelectedExpectation(Expectation expectation) {
        System.out.println("changeSelectedExpectation:" + expectation);
        selectedExpectation = reSelectThisExpectation(expectation);
        selectedExpectationJson = JsonUtils.toJsonFormatted(selectedExpectation);
        if (expectation != null) {
            expectationTextArea.setText(JsonUtils.toJsonFormatted(expectation));
            setExpectationTextColourAndInfo(false, null);
        }
        validClonedExpectation = null;
        configureExpectationSaveOptions(false);
    }

    private void saveUpdatedExpectation() {
        System.out.println("saveUpdatedExpectation:" + validClonedExpectation);
        if ((selectedExpectationManager != null) && (selectedExpectation != null) && (validClonedExpectation != null) && selectedExpectationManager.isLoadedFromAFile()) {
            selectedExpectationManager.replaceExpectation(selectedExpectation, validClonedExpectation);
        }
        validClonedExpectation = null;
    }

    private Expectation reSelectThisExpectation(Expectation expectation) {
        if (expectation != null) {
            System.out.println("selectThisExpectation:" + expectation.toString());
            for (int i = 0; i < expectationsListView.getItems().size(); i++) {
                ExpectationWrapper wrapper = (ExpectationWrapper) expectationsListView.getItems().get(i);
                if (wrapper.getExpectation().getName().equals(expectation.getName())) {
                    expectationsListView.getSelectionModel().select(i);
                    return wrapper.getExpectation();
                }
            }
        }
        System.out.println("selectThisExpectation:selectFirst()");
        expectationsListView.getSelectionModel().selectFirst();
        return ((ExpectationWrapper) expectationsListView.getSelectionModel().getSelectedItem()).getExpectation();
    }

    private Expectation getTheSelectedExpectation() {
        ExpectationWrapper o = (ExpectationWrapper) expectationsListView.getSelectionModel().getSelectedItem();
        if (o == null) {
            return reSelectThisExpectation(null);
        }
        return o.getExpectation();
    }

    private void configureExpectationSaveOptions(boolean isInErrorState) {
        System.out.println("configureExpectationSaveOptions:" + isInErrorState);
        if (selectedExpectationManager.isLoadedFromAFile()) {
            if (isInErrorState) {
                buttonSaveExpectations.setDisable(true);
            } else {
                buttonSaveExpectations.setDisable(!selectedExpectationManager.isRequiresSaving());
            }
            labelSaveExpectations.setVisible(false);
        } else {
            buttonSaveExpectations.setDisable(true);
            labelSaveExpectations.setVisible(true);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("initialize:");
        expectationSelectionChangedListener = new ExpectationSelectionChangedListener();

        checkBoxHeaders.setSelected(Main.getConfig().isIncludeHeaders());
        checkBoxBody.setSelected(Main.getConfig().isIncludeBody());
        checkBoxEmpty.setSelected(Main.getConfig().isIncludeEmpty());
        checkBoxTime.setSelected(Main.getConfig().isShowTime());
        checkBoxPort.setSelected(Main.getConfig().isShowPort());
        textAreaLogging.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        textAreaLog.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        Main.setApplicationController(this);
        resetMainLog();
        ServerManager.autoStartServers();
        updateMainLog(System.currentTimeMillis(), -1, LogCatagory.EMPTY, null);
        changeSelectedServer(Main.getDefaultServer());
        choiceBoxPortNumber.setItems(FXCollections.observableArrayList(ServerManager.portList()));
        choiceBoxPortNumber.getSelectionModel().select("" + selectedServer.getPort());
        /*
        Link up th echange notifiers. Must be done AFTER init!
         */
        choiceBoxPortNumber.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue != newValue) {
                    Main.notifyAction(System.currentTimeMillis(), -1, Action.SERVER_SELECTED, ServerManager.getServer(ServerManager.portListSorted()[newValue.intValue()]), "Server Selected [" + newValue + "]");
                }
            }
        });
        expectationsListView.getSelectionModel().selectedIndexProperty().addListener(expectationSelectionChangedListener);
        /*
        Do some stuff later in a separate thread!
         */
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < Main.getConfig().getExpDividerPos().length; i++) {
                    expectationsSplitPane.getDividers().get(i).setPosition(Main.getConfig().getExpDividerPos()[i]);
                }
            }
        });
    }

    @Override
    public boolean notifyAction(long time, int port, Action action, Object actionOn, String message) {
        System.out.println("ACTION:" + action.name() + " On[" + actionOn + "]");
        switch (action) {
            case EXPECTATION_TEXT_CHANGED:
                if (selectedExpectationManager.isLoadedFromAFile()) {
                    String json = expectationTextArea.getText();
                    try {
                        validClonedExpectation = (Expectation) JsonUtils.beanFromJson(Expectation.class, json);
                        configureExpectationSaveOptions(false);
                        setExpectationTextColourAndInfo(false, null);
                        selectedExpectationManager.setRequiresSaving(true);
                    } catch (Exception ex) {
                        validClonedExpectation = null;
                        configureExpectationSaveOptions(true);
                        setExpectationTextColourAndInfo(true, ex.getCause().getMessage());
                    }
                } else {
                    setExpectationTextColourAndInfo(false, null);
                }
                break;
            case SAVE_EXPECTATIONS:
                saveUpdatedExpectation();
                selectedExpectationManager.save();
                refreshExpectationListView();
                break;
            case RELOAD_EXPECTATIONS:
                saveUpdatedExpectation();
                selectedExpectationManager.reloadExpectations(selectedServer.getPort(), true);
                refreshExpectationListView();
                break;
            case EXPECTATION_SELECTED:
                saveUpdatedExpectation();
                changeSelectedExpectation((Integer) actionOn);
                break;
            case SERVER_SELECTED:
                saveUpdatedExpectation();
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

    private void setExpectationTextColourAndInfo(boolean isError, String message) {
        Color color;
        if (!selectedExpectationManager.isLoadedFromAFile()) {
            color = Color.LIGHTCYAN;
            expectationTextArea.setEditable(false);
            expectationTextAreaErrors.setText(EXAMPLE_HEAD_RO + UL + JsonUtils.toJsonFormatted(EXAMPLE) + UL + EXAMPLE_NOTES);
        } else {
            expectationTextArea.setEditable(true);
            if (isError) {
                expectationTextAreaErrors.setText(message);
                color = Color.PINK;
            } else {
                expectationTextAreaErrors.setText(EXAMPLE_HEAD + UL + JsonUtils.toJsonFormatted(EXAMPLE) + UL + EXAMPLE_NOTES);
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
    public void updateConfig(ConfigData configData) {
        if (expectationsSplitPane != null) {
            int count = expectationsSplitPane.getDividers().size();
            double[] pos = new double[count];
            for (int i = 0; i < count; i++) {
                pos[i] = expectationsSplitPane.getDividers().get(i).getPosition();
            }
            configData.setExpDividerPos(pos);
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
                        if (Main.getConfig().isIncludeEmpty()) {
                            sb.append(NL);
                        }
                        break;
                    case BODY:
                        if (Main.getConfig().isIncludeBody()) {
                            line.render(sb, Main.getConfig());
                        }
                        break;
                    case HEADER:
                        if (Main.getConfig().isIncludeHeaders()) {
                            line.render(sb, Main.getConfig());
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
            if (ServerManager.isShowPort(line.getPort())) {
                line.render(sb, Main.getConfig());
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
