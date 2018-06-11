/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

    private LogLine firstLine = null;
    private LogLine lastLine = firstLine;

    @FXML
    private CheckBox checkBoxHeaders;

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
        Main.notifyAction(Action.CLEAR_LOGS, "Log has been cleared");
    }

    @FXML
    public void checkBoxHeadersAction() {
        Main.notifyOption(Option.FILTER_HEADERS, checkBoxHeaders.isSelected(), "");
    }

    @FXML
    public void connectAction() {
        if (buttonConnect.getText().equalsIgnoreCase("start")) {
            try {
                int port = Integer.parseInt(textFieldPortNumber.getText());
                Main.startServerThread(port);
            } catch (NumberFormatException nfe) {
                Main.log(nfe);
                Main.notifyAction(Action.PORT_NUMBER_ERROR, "Invalid port number");
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
        textFieldPortNumber.setEditable(true);
        labelStatus.setText("Ready to start the server");
        textAreaLogging.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        Main.addApplicationController(this);
        if (Main.getConfig().getAutoConnect()) {
            Main.startServerThread(Main.getConfig().getPort());
        }
        resetLog();
        updateLogDisplay(LogCatagory.EMPTY, null);
    }

    @Override
    public boolean notifyAction(Action action, String message) {
        if (action != Action.LOG_BODY) {
            if (message != null) {
                labelStatus.setText(message);
            }
        }
        switch (action) {
            case CLEAR_LOGS:
                resetLog();
                updateLogDisplay(LogCatagory.EMPTY, null);
                labelStatus.setText("Log display cleared");
                break;
            case LOG_BODY:
                updateLogDisplay(LogCatagory.BODY, message);
                break;
            case LOG_HEADER:
                updateLogDisplay(LogCatagory.HEADER, message);
                break;
            case LOG_REFRESH:
                updateLogDisplay(null, null);
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

    private void updateLogDisplay(LogCatagory cat, String message) {
        if (firstLine == null) {
            if ((message == null) || (message.trim().length() == 0)) {
                textAreaLogging.setText("Log is empty 1");
                labelStatus.setText("Log message is empty!");
            } else {
                firstLine = new LogLine(message, cat);
                lastLine = firstLine;
                textAreaLogging.setText(filter());
                labelStatus.setText("Log 1st line:" + message);
            }
        } else {
            lastLine.setNext(new LogLine(message, cat));
            lastLine = lastLine.getNext();
            textAreaLogging.setText(filter());
        }
    }

    private void resetLog() {
        firstLine = null;
    }

    private String filter() {
        StringBuilder sb = new StringBuilder();
        LogLine line = firstLine;
        while (line != null) {
            switch (line.catagory) {
                case EMPTY:
                    if (Main.getConfig().isIncludeEmpty()) {
                        sb.append(line.getText()).append(NL);
                    }
                    break;
                case BODY:
                    if (Main.getConfig().isIncludeBody()) {
                        sb.append(line.getText()).append(NL);
                    }
                    break;
                case HEADER:
                    if (Main.getConfig().isIncludeHeaders()) {
                        sb.append(line.getText()).append(NL);
                    }
                    break;
            }
            line = line.getNext();
        }
        return sb.toString().trim();
    }
}
