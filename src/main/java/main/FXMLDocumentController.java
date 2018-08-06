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
import common.ActionOn;
import expectations.Expectation;
import expectations.Expectations;
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
import server.ServerManager;

/**
 *
 * @author stuart
 */
public class FXMLDocumentController extends BorderPane implements ApplicationController, Initializable {

    private static final String EXAMPLE_JSON = "{\n"
            + "  \"name\" : \"Unique Name for Expectation\",\n"
            + "  \"method\" : \"[4] Method Equals: GET, POST, PUT, DELETE etc.\",\n"
            + "  \"path\" : \"[1] Matching Path: E.g. /test/post/xml\",\n"
            + "  \"bodyType\" : \"[4] Body Type Equals: From: XML, JSON, HTML, TXT, EMPTY\",\n"
            + "  \"asserts\" : {\n"
            + "    \"Name of property 1\" : \"[1] Value of property 1 must match\",\n"
            + "    \"Name of property 2\" : \"[1] Value of property 2 must match\"\n"
            + "  },\n"
            + "  \"response\" : {\n"
            + "    \"body\" : \"[3] Response body text\",\n"
            + "    \"status\" : 200,\n"
            + "    \"template\" : \"[2] [3] File or Resource to read response body from\",\n"
            + "    \"headers\" : { "
            + "         \"Accept\": \"%{HEAD.Accept}\",\n"
            + "         \"Name of Header 2\" : \"[3] Value of Header 2\"\n"
            + "    }\n"
            + "  }\n"
            + "}";
    private static final Expectation EXAMPLE = (Expectation) JsonUtils.beanFromJson(Expectation.class, EXAMPLE_JSON);
    private static final String EXAMPLE_HEAD = "Example Expectation (annotated):";
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
    private static final String NL = System.getProperty("line.separator");

    private LogLine firstMainLog = null;
    private LogLine lastMainLog = firstMainLog;

    private LogLine firstLog = null;
    private LogLine lastLog = firstLog;

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
    private Button buttonConnect;
    
    @FXML
    private Button buttonSaveExpectations;
    
    @FXML
    private Label labelSaveExpectations;

    @FXML
    private ChoiceBox textFieldPortNumber;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textFieldPortNumber.setItems(FXCollections.observableArrayList(ServerManager.portList()));
        textFieldPortNumber.getSelectionModel().select("" + Main.getConfig().getDefaultPort());
        textFieldPortNumber.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue != newValue) {
                    portNumberChanged();
                }
            }
        });
        expectationsListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue != newValue) {
                    expectationsListViewChanged();
                }
            }
        });
        if (!ServerManager.isShowPort(Main.getConfig().getDefaultPort())) {
            ServerManager.setShowPort(Main.getConfig().getDefaultPort(), true);
        }
        updatePortStatus();
        checkBoxHeaders.setSelected(Main.getConfig().isIncludeHeaders());
        checkBoxBody.setSelected(Main.getConfig().isIncludeBody());
        checkBoxEmpty.setSelected(Main.getConfig().isIncludeEmpty());
        checkBoxTime.setSelected(Main.getConfig().isShowTime());
        checkBoxPort.setSelected(Main.getConfig().isShowPort());
        checkBoxAutoStart.setSelected(ServerManager.isAutoStart(getSelectedPort()));
        checkBoxShowPort.setSelected(ServerManager.isShowPort(getSelectedPort()));
        checkBoxLogProperties.setSelected(Main.getConfig().isLogProperties());
        labelStatus.setText("Ready to start the server");
        textAreaLogging.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        textAreaLog.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        Main.setApplicationController(this);
        resetMainLog();
        ServerManager.autoStartServers();
        updateMainLog(System.currentTimeMillis(), -1, LogCatagory.EMPTY, null);
        /*
        Do some stuff later in a separate thread!
         */
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < Main.getConfig().getExpDividerPos().length; i++) {
                    expectationsSplitPane.getDividers().get(i).setPosition(Main.getConfig().getExpDividerPos()[i]);
                }
                expectationsListView.getSelectionModel().select(0);
                expectationTextAreaKeyTyped();
            }
        });
    }

    @FXML
    public void closeAction() {
        Main.closeApplication(false);
    }

    @FXML
    public void expectationTextAreaKeyTyped() {
        Main.notifyAction(System.currentTimeMillis(), -1, Action.VALIDATE_EXP, null, "Validate Expectation JSON");
    }

    @FXML
    public void clearMainLogAction() {
        Main.notifyAction(System.currentTimeMillis(), -1, Action.CLEAR_MAIN_LOGS, null, "Log has been cleared");
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
        Main.notifyOption(Option.TIME, -1, checkBoxTime.isSelected(), "");
    }
    
    @FXML
    public void checkBoxLogPropertiesAction() {
        Main.notifyOption(Option.LOG_PROPERTIES, -1, checkBoxLogProperties.isSelected(), "");
    }

    @FXML
    public void checkBoxPortAction() {
        Main.notifyOption(Option.PORT, -1, checkBoxPort.isSelected(), "");
    }

    @FXML
    public void checkBoxHeadersAction() {
        Main.notifyOption(Option.FILTER_HEADERS, -1, checkBoxHeaders.isSelected(), "");
    }

    @FXML
    public void checkBoxAutoStartAction() {
        ServerManager.setAutoStart(getSelectedPort(), checkBoxAutoStart.isSelected());
    }

    @FXML
    public void checkBoxShowPortAction() {
        ServerManager.setShowPort(getSelectedPort(), checkBoxShowPort.isSelected());
        Main.notifyAction(System.currentTimeMillis(), -1, Action.LOG_REFRESH, null, "Log has been Updated");
    }

    @FXML
    public void checkBoxBodyAction() {
        Main.notifyOption(Option.FILTER_BODY, -1, checkBoxBody.isSelected(), "");
    }

    @FXML
    public void checkBoxEmptyAction() {
        Main.notifyOption(Option.FILTER_EMPTY, -1, checkBoxEmpty.isSelected(), "");
    }

    @FXML
    public void connectAction() {
        if (buttonConnect.getText().equalsIgnoreCase("start")) {
            Main.startServer(getSelectedPort());
        } else {
            Main.stopServer(getSelectedPort());
        }
    }

    @Override
    public boolean notifyAction(long time, int port, Action action, ActionOn actionOn, String message) {
        switch (action) {
            case VALIDATE_EXP:
                String json = expectationTextArea.getText();
                try {
                    JsonUtils.beanFromJson(Expectation.class, json);
                    setExpectationTextAreaInfo(true, EXAMPLE_HEAD + NL + JsonUtils.toJsonFormatted(EXAMPLE) + NL + EXAMPLE_NOTES);
                } catch (Exception ex) {
                    setExpectationTextAreaInfo(false, ex.getCause().getMessage());
                }
                break;
            case SAVE_EXPECTATIONS:
                break;
            case LOAD_EXPECTATIONS:
                Expectations expectations = (Expectations) actionOn;
                expectationsListView.setItems(FXCollections.observableArrayList(ExpectationWrapper.wrap(expectations)));
                buttonSaveExpectations.setVisible(expectations.loadedFromAFile());
                labelSaveExpectations.setVisible(!expectations.loadedFromAFile());
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
            case SERVER_STATE:
                updatePortStatus();
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

    private void setExpectationTextAreaInfo(boolean ok, String message) {
        buttonSaveExpectations.setDisable(!ok);
        Color colour = (ok ? Color.LIGHTGREEN : Color.PINK);
        Region region1 = (Region) expectationTextArea.lookup(".content");
        Region region2 = (Region) expectationTextAreaErrors.lookup(".content");
        region1.setBackground(new Background(new BackgroundFill(colour, CornerRadii.EMPTY, Insets.EMPTY)));
        region2.setBackground(new Background(new BackgroundFill(colour, CornerRadii.EMPTY, Insets.EMPTY)));
        expectationTextAreaErrors.setText(message);
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

    private void portNumberChanged() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                updatePortStatus();

            }
        });
    }

    private void expectationsListViewChanged() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                updateExpectationsListSelection();
            }
        });
    }

    private void updateExpectationsListSelection() {
        expectationTextArea.setText(JsonUtils.toJsonFormatted(getSelectedExpectation()));
    }

    private Expectation getSelectedExpectation() {
        return ((ExpectationWrapper) expectationsListView.getSelectionModel().getSelectedItem()).getExpectation();
    }

    private int getSelectedPort() {
        return Integer.parseInt(textFieldPortNumber.getSelectionModel().getSelectedItem().toString());
    }

    private void updatePortStatus() {
        checkBoxAutoStart.setSelected(ServerManager.isAutoStart(getSelectedPort()));
        checkBoxShowPort.setSelected(ServerManager.isShowPort(getSelectedPort()));
        switch (ServerManager.state(getSelectedPort())) {
            case SERVER_STARTING:
            case SERVER_STOPPING:
                buttonConnect.setDisable(true);
                textFieldPortNumber.setBackground(new Background(new BackgroundFill(Color.PINK, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_STOPPED:
                buttonConnect.setDisable(false);
                buttonConnect.setText("Start");
                textFieldPortNumber.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_RUNNING:
                buttonConnect.setDisable(false);
                buttonConnect.setText("Stop");
                textFieldPortNumber.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_FAIL:
                buttonConnect.setDisable(false);
                buttonConnect.setText("Start");
                textFieldPortNumber.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_PENDING:
                buttonConnect.setDisable(false);
                buttonConnect.setText("Start");
                textFieldPortNumber.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
        }
    }
}
