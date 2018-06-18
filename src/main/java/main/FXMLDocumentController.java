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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 *
 * @author stuart
 */
public class FXMLDocumentController extends BorderPane implements ApplicationController, Initializable {

    private static final String NL = System.getProperty("line.separator");

    private LogLine firstMainLog = null;
    private LogLine lastMainLog = firstMainLog;

    private LogLine firstLog = null;
    private LogLine lastLog = firstLog;

    @FXML
    private CheckBox checkBoxTime;

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
    private TextField textFieldPortNumber;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textFieldPortNumber.setText("" + Main.getConfig().getPort());
        buttonConnect.setText("Start");
        checkBoxHeaders.setSelected(Main.getConfig().isIncludeHeaders());
        checkBoxBody.setSelected(Main.getConfig().isIncludeBody());
        checkBoxEmpty.setSelected(Main.getConfig().isIncludeEmpty());
        checkBoxTime.setSelected(Main.getConfig().isShowTime());
        textFieldPortNumber.setEditable(true);
        labelStatus.setText("Ready to start the server");
        textAreaLogging.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        textAreaLog.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        Main.addApplicationController(this);
        if (Main.getConfig().getAutoConnect()) {
            Main.startServerThread(Main.getConfig().getPort());
        }
        resetMainLog();
        updateMainLog(System.currentTimeMillis(), LogCatagory.EMPTY, null);
    }

    @FXML
    public void closeAction() {
        Main.closeApplication(false);
    }

    @FXML
    public void clearMainLogAction() {
        Main.notifyAction(System.currentTimeMillis(), Action.CLEAR_MAIN_LOGS, "Log has been cleared");
    }

    @FXML
    public void clearLogAction() {
        Main.notifyAction(System.currentTimeMillis(), Action.CLEAR_LOGS, "Log has been cleared");
    }

    @FXML
    public void checkBoxTimeAction() {
        Main.notifyOption(Option.TIME, checkBoxTime.isSelected(), "");
    }

    @FXML
    public void checkBoxHeadersAction() {
        Main.notifyOption(Option.FILTER_HEADERS, checkBoxHeaders.isSelected(), "");
    }

    @FXML
    public void checkBoxBodyAction() {
        Main.notifyOption(Option.FILTER_BODY, checkBoxBody.isSelected(), "");
    }

    @FXML
    public void checkBoxEmptyAction() {
        Main.notifyOption(Option.FILTER_EMPTY, checkBoxEmpty.isSelected(), "");
    }

    @FXML
    public void connectAction() {
        if (buttonConnect.getText().equalsIgnoreCase("start")) {
            try {
                int port = Integer.parseInt(textFieldPortNumber.getText());
                Main.startServerThread(port);
            } catch (NumberFormatException nfe) {
                Main.log(System.currentTimeMillis(), nfe);
                Main.notifyAction(System.currentTimeMillis(), Action.PORT_NUMBER_ERROR, "Invalid port number");
            }
        } else {
            Main.stopServerThread();
        }
    }

    @Override
    public boolean notifyAction(long time, Action action, String message) {
        switch (action) {
            case CLEAR_LOGS:
                resetLog();
                updateLog(time, null, null);
                break;
            case CLEAR_MAIN_LOGS:
                resetMainLog();
                updateMainLog(time, null, null);
                break;
            case LOG_BODY:
                updateMainLog(time, LogCatagory.BODY, message);
                break;
            case LOG_HEADER:
                updateMainLog(time, LogCatagory.HEADER, message);
                break;
            case LOG_REFRESH:
                updateMainLog(time, null, null);
                updateLog(time, null, null);
                labelStatus.setText("Log display refreshed");
                break;
            case LOG:
                updateLog(time, LogCatagory.LOG, message);
                labelStatus.setText("Log display refreshed");
                break;
            case PORT_NUMBER_ERROR:
                textFieldPortNumber.setEditable(true);
                textFieldPortNumber.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_START:
                buttonConnect.setText("Stop");
                textFieldPortNumber.setEditable(false);
                textFieldPortNumber.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_STOPPING:
                textFieldPortNumber.setEditable(false);
                textFieldPortNumber.setBackground(new Background(new BackgroundFill(Color.PINK, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_STOP:
                buttonConnect.setText("Start");
                textFieldPortNumber.setEditable(true);
                textFieldPortNumber.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_FAIL:
                buttonConnect.setText("Start");
                textFieldPortNumber.setEditable(true);
                textFieldPortNumber.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
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

    private void updateMainLog(long time, LogCatagory cat, String message) {
        if (firstMainLog == null) {
            if ((message == null) || (message.trim().length() == 0)) {
                textAreaLogging.setText("Log is empty 1");
                labelStatus.setText("Log message is empty!");
            } else {
                firstMainLog = new LogLine(time, message, cat);
                lastMainLog = firstMainLog;
                textAreaLogging.setText(filterMainLog());
                labelStatus.setText("Log 1st line:" + message);
            }
        } else {
            lastMainLog.setNext(new LogLine(time, message, cat));
            lastMainLog = lastMainLog.getNext();
            textAreaLogging.setText(filterMainLog());
        }
    }

    private void updateLog(long time, LogCatagory cat, String message) {
        if (firstLog == null) {
            if ((message == null) || (message.trim().length() == 0)) {
                textAreaLog.setText("Log is empty 1");
                labelStatus.setText("Log message is empty!");
            } else {
                firstLog = new LogLine(time, message, cat);
                lastLog = firstLog;
                textAreaLog.setText(filterLog());
                labelStatus.setText("Log 1st line:" + message);
            }
        } else {
            lastLog.setNext(new LogLine(time, message, cat));
            lastLog = lastLog.getNext();
            textAreaLog.setText(filterLog());
        }
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
            switch (line.getCatagory()) {
                case EMPTY:
                    if (Main.getConfig().isIncludeEmpty()) {
                        sb.append(NL);
                    }
                    break;
                case BODY:
                    if (Main.getConfig().isIncludeBody()) {
                        if (Main.getConfig().isShowTime()) {
                            sb.append(Main.getConfig().timeStamp(line.getTime())).append(":");
                        }
                        sb.append(line.getText()).append(NL);
                    }
                    break;
                case HEADER:
                    if (Main.getConfig().isIncludeHeaders()) {
                        if (Main.getConfig().isShowTime()) {
                            sb.append(Main.getConfig().timeStamp(line.getTime())).append(":");
                        }
                        sb.append(line.getText()).append(NL);
                    }
                    break;
            }
            line = line.getNext();
        }
        return sb.toString().trim();
    }

    private String filterLog() {
        StringBuilder sb = new StringBuilder();
        LogLine line = firstLog;
        while (line != null) {
            if (Main.getConfig().isShowTime()) {
                sb.append(Main.getConfig().timeStamp(line.getTime())).append(":");
            }
            sb.append(line.getText()).append(NL);
            line = line.getNext();
        }
        return sb.toString().trim();
    }

}
