package main;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.ServerThread;

public class Main extends Application {

    private static final List<ApplicationController> CONTROLLERS = new ArrayList<>();
    private static Stage mainStage;
    private static ServerThread serverThread;
    private static int portNumber = 1088;

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("/FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    public static void notifyAction(Action action, String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (ApplicationController ap : CONTROLLERS) {
                    ap.notifyAction(action, message);
                }
            }
        });
    }

    public static void addApplicationController(ApplicationController applicationController) {
        CONTROLLERS.add(applicationController);
    }

    public static int getPortNumber() {
        return portNumber;
    }

    public static void startServerThread(int port) {
        portNumber = port;
        serverThread = new ServerThread(port);
        serverThread.start();
    }

    public static void stopServerThread() {
        if (serverThread != null) {
            serverThread.stopServer(1);
        }
    }

    public static void closeApplication() {
        stopServerThread();
        log("Shutting down");
        mainStage.close();
    }

    public static void log(String message) {
        if (message != null) {
            System.out.println("LOG:" + message);
        }
    }

    public static void log(Throwable throwable) {
        if (throwable != null) {
            System.out.println("ERROR:" + throwable.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
