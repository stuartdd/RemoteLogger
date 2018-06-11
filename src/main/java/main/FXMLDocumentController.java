/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import static java.lang.Thread.sleep;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
import static main.Main.closeApplication;

/**
 *
 * @author stuart
 */
public class FXMLDocumentController extends BorderPane implements ApplicationController, Initializable {

    private static final String NL = System.getProperty("line.separator");

    private LogLine firstLine = null;
    private LogLine lastLine = firstLine;

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
    private Label labelStatus;

    @FXML
    private Button buttonConnect;

    @FXML
    private TextField textFieldPortNumber;

    @FXML
    public void closeAction() {
        Main.closeApplication(false);
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
                Main.log(nfe);
                Main.notifyAction(System.currentTimeMillis(), Action.PORT_NUMBER_ERROR, "Invalid port number");
            }
        } else {
            Main.stopServerThread();
        }
    }

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
        Main.addApplicationController(this);
        if (Main.getConfig().getAutoConnect()) {
            Main.startServerThread(Main.getConfig().getPort());
        }
        resetLog();
        updateLogDisplay(System.currentTimeMillis(), LogCatagory.EMPTY, null);
    }

    @Override
    public boolean notifyAction(long time, Action action, String message) {
        switch (action) {
            case CLEAR_LOGS:
                resetLog();
                updateLogDisplay(time, null, null);
                labelStatus.setText("Log display cleared");
                break;
            case LOG_BODY:
                updateLogDisplay(time, LogCatagory.BODY, message);
                break;
            case LOG_HEADER:
                updateLogDisplay(time, LogCatagory.HEADER, message);
                break;
            case LOG_REFRESH:
                updateLogDisplay(time, null, null);
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

    private void updateLogDisplay(long time, LogCatagory cat, String message) {
        if (firstLine == null) {
            if ((message == null) || (message.trim().length() == 0)) {
                textAreaLogging.setText("Log is empty 1");
                labelStatus.setText("Log message is empty!");
            } else {
                firstLine = new LogLine(time, message, cat);
                lastLine = firstLine;
                textAreaLogging.setText(filter());
                labelStatus.setText("Log 1st line:" + message);
            }
        } else {
            lastLine.setNext(new LogLine(time, message, cat));
            lastLine = lastLine.getNext();
            textAreaLogging.setText(filter());
        }
        Main.notifyAction(time, Action.SCROLL_TO_END, null);
    }

    private void resetLog() {
        firstLine = null;
    }

    private String filter() {
        StringBuilder sb = new StringBuilder();
        LogLine line = firstLine;
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


}
