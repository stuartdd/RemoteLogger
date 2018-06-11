package main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import json.JsonUtils;
import org.joda.time.DateTime;
import server.ServerThread;

public class Main extends Application {

    private static final int PORT_NUMBER = 1088;
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private static final List<ApplicationController> CONTROLLERS = new ArrayList<>();
    private static Stage mainStage;
    private static ServerThread serverThread;
    private static ConfigData config;
    private static String configName;

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        if ((configName != null) && (config != null)) {
            stage.setX(config.getX());
            stage.setY(config.getY());
            stage.setHeight(config.getHeight());
            stage.setWidth(config.getWidth());
        }
        Parent root = FXMLLoader.load(getClass().getResource("/FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        closeApplication(false);
                    }
                });
            }
        });
        stage.setTitle("Log Server:" + (configName == null ? "<Undefined>" : configName));
        stage.show();
    }

    public static void notifyAction(long time, Action action, String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (ApplicationController ap : CONTROLLERS) {
                    ap.notifyAction(time, action, message);
                }
            }
        });
    }

    public static void notifyOption(Option option, boolean selected, String message) {
        switch (option) {
            case FILTER_HEADERS:
                config.setIncludeHeaders(selected);
                notifyAction(System.currentTimeMillis(), Action.LOG_REFRESH, "");
                break;
            case FILTER_BODY:
                config.setIncludeBody(selected);
                notifyAction(System.currentTimeMillis(), Action.LOG_REFRESH, "");
                break;
            case FILTER_EMPTY:
                config.setIncludeEmpty(selected);
                notifyAction(System.currentTimeMillis(), Action.LOG_REFRESH, "");
                break;
            case TIME:
                config.setShowTime(selected);
                notifyAction(System.currentTimeMillis(), Action.LOG_REFRESH, "");
                break;
        }
    }

    public static void addApplicationController(ApplicationController applicationController) {
        CONTROLLERS.add(applicationController);
    }

    public static void startServerThread(int port) {
        config.setPort(port);
        serverThread = new ServerThread(port);
        serverThread.start();
    }

    public static void stopServerThread() {
        if (serverThread != null) {
            serverThread.stopServer(1);
        }
    }

    public static void closeApplication(boolean force) {
        if (configName != null) {
            config.setX(mainStage.getX());
            config.setY(mainStage.getY());
            config.setWidth(mainStage.getWidth());
            config.setHeight(mainStage.getHeight());
            if (serverThread == null) {
                config.setAutoConnect(false);
            } else {
                config.setAutoConnect(serverThread.isRunning());
            }
            try {
                Files.write(
                        FileSystems.getDefault().getPath(configName),
                        JsonUtils.toJsonFormatted(config).getBytes(CHARSET),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException io) {
                if (!force) {
                    Main.notifyAction(System.currentTimeMillis(), Action.CONFIG_SAVE_ERROR, io.toString());
                    return;
                }
            }
        }
        log("Shutting down");
        stopServerThread();
        mainStage.close();
    }

    public static void log(String message) {
        if ((message != null) && (config.isVerbose())) {
            System.out.println(getTimeStamp() + "LOG:" + message);
        }
    }

    public static void log(Throwable throwable) {
        if (throwable != null) {
            System.out.println(getTimeStamp() + "ERROR:" + throwable.getMessage());
        }
    }

    public static ConfigData getConfig() {
        return config;
    }

    public static String getConfigName() {
        return configName;
    }

    public static String getTimeStamp() {
        if ((config.getLogDateFormat() != null) && (config.getLogDateFormat().trim().length() > 0)) {
            return DateTime.now().toString(config.getLogDateFormat());
        }
        return "";
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        config = new ConfigData();
        config.setWidth(600);
        config.setHeight(600);
        config.setX(0);
        config.setY(0);
        configName = null;
        try {
            for (String arg : args) {
                if (arg.toLowerCase().endsWith(".json")) {
                    File f = new File(arg);
                    if (f.exists()) {
                        configName = f.getAbsolutePath();
                        config = ConfigData.loadConfig(f);
                    }
                }
            }
            for (String arg : args) {
                if (arg.startsWith("-p")) {
                    config.setPort(Integer.parseInt(arg.substring(2)));
                }
                if (arg.startsWith("-a")) {
                    config.setAutoConnect(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
        if (config.getPort() == null) {
            config.setPort(PORT_NUMBER);
        }
        if (config.getAutoConnect() == null) {
            config.setAutoConnect(false);
        }
        if (config.getLogDateFormat() == null) {
            config.setLogDateFormat("yyyy-MM-dd':'HH-mm-ss-SSS': '");
        }
        launch(args);
    }
    
    
}
