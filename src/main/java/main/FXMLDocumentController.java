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
import javafx.scene.control.Button;
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

    @FXML
    private Label label;

    @FXML
    private TextArea textAreaLogging;

    @FXML
    private Label labelStatus;

    @FXML
    private Button buttonConnect;

    @FXML
    private TextField textFieldPortNumber;

    StringBuffer logText = new StringBuffer();

    @FXML
    public void closeAction() {
        Main.closeApplication();
    }
    
    @FXML
    public void clearLogAction() {
        Main.notifyAction(Action.CLEAR_LOGS, "Log has been cleared");
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
        textFieldPortNumber.setText("" + Main.getPortNumber());
        buttonConnect.setText("Start");
        textFieldPortNumber.setEditable(true);
        labelStatus.setText("Ready to start the server");
        textAreaLogging.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        textAreaLogging.setText("Log is clear");
        logText.setLength(0);
        Main.addApplicationController(this);
        if (Main.shouldAutoConnect()) {
            Main.startServerThread(Main.getPortNumber());
        }
    }

    @Override
    public boolean notifyAction(Action action, String message) {
        if (action != Action.LOG_TEXT) {
            if (message != null) {
                labelStatus.setText(message);
            }
        }
        switch (action) {
            case CLEAR_LOGS:
                logText.setLength(0);
                textAreaLogging.setText("Log is clear");
                break;
            case PORT_NUMBER_ERROR:
                textFieldPortNumber.setEditable(true);
                textFieldPortNumber.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case LOG_TEXT:
                logText.append(message);
                textAreaLogging.setText(logText.toString());
                labelStatus.setText("Message Logged");
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
        }
        return true;
    }

}
