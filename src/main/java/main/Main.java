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
import common.Util;
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
import server.Server;
import server.ServerConfig;

public class Main extends Application {

    private static final int PORT_NUMBER = 1088;
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static boolean headless = false;
    private static final List<ApplicationController> CONTROLLERS = new ArrayList<>();
    private static Stage mainStage;
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
        if (!headless) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    for (ApplicationController ap : CONTROLLERS) {
                        ap.notifyAction(time, action, message);
                    }
                }
            });
        } else {
            Main.logFinal(time, action.name() + ":" + message);
        }
    }

    public static void notifyOption(Option option, boolean selected, String message) {
        if (!headless) {
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
    }

    public static void addApplicationController(ApplicationController applicationController) {
        CONTROLLERS.add(applicationController);
    }

    public static void startServer(String portStr, ServerConfig config) {
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (Exception ex) {
            throw new ConfigDataException("Port number [" + portStr + "] is invalid");
        }
        Server.startServer(port, config, new MainNotifier(config.isVerbose()));
    }

    public static void stopServerThread(String portStr) {
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (Exception ex) {
            throw new ConfigDataException("Port number [" + portStr + "] is invalid");
        }
        Server.stopServer(port);
    }

    public static void controlStopEventAction() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Util.sleep(200);
                if (headless) {
                    closeApplication(true);
                } else {
                    Server.stopAllServers();
                }
            }
        }).start();
    }

    public static void closeApplication(boolean force) {
        log(System.currentTimeMillis(), "Shutting down");
        if (!headless) {
            if (ConfigData.writeFileName() != null) {
                config.setX(mainStage.getX());
                config.setY(mainStage.getY());
                config.setWidth(mainStage.getWidth());
                config.setHeight(mainStage.getHeight());
                config.setAutoConnect(Server.countServersRunning() > 0);
                try {
                    Files.write(
                            FileSystems.getDefault().getPath(ConfigData.writeFileName()),
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
        }
        Server.stopAllServers();
        if (mainStage != null) {
            mainStage.close();
        } else {
            System.exit(0);
        }
    }

    public static void log(long time, String message) {
        if (message != null) {
            notifyAction(time, Action.LOG, message);
            logFinal(time, message);
        }
    }

    public static void log(long time, Throwable throwable) {
        if (throwable != null) {
            notifyAction(time, Action.LOG, "ERROR:" + throwable.getMessage());
            logFinal(time, "ERROR:" + throwable.getMessage());
        }
    }

    public static void log(long time, String message, Throwable throwable) {
        if (throwable != null) {
            notifyAction(time, Action.LOG, "ERROR:" + message + ": " + throwable.getMessage());
            logFinal(time, "ERROR:" + message + ": " + throwable.getMessage());
        }
    }

    public static void logFinal(long time, String message) {
        System.out.println(getTimeStamp(time) + message);
    }

    public static ConfigData getConfig() {
        return config;
    }

    public static String getConfigName() {
        return configName;
    }

    public static String getTimeStamp(long time) {
        if (config == null) {
            return (new DateTime(time)).toString("HH:mm:ss.SSS: ");
        }
        if ((config.getLogDateFormat() != null) && (config.getLogDateFormat().trim().length() > 0)) {
            return (new DateTime(time)).toString(config.getLogDateFormat());
        }
        return "";
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        headless = false;
        config = new ConfigData();
        config.setWidth(600);
        config.setHeight(600);
        config.setX(0);
        config.setY(0);
        configName = null;
        try {
            for (String arg : args) {
                if (arg.startsWith("-a")) {
                    config.setAutoConnect(true);
                }
                if (arg.startsWith("-h")) {
                    headless = true;
                }
            }
            for (String arg : args) {
                if (arg.toLowerCase().endsWith(".json")) {
                    config = ConfigData.loadConfig(arg);
                    configName = ConfigData.readFileName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            if (headless) {
                return;
            }
            System.exit(1);
        }
        if (config.getAutoConnect() == null) {
            config.setAutoConnect(false);
        }
        if (config.getLogDateFormat() == null) {
            config.setLogDateFormat("yyyy-MM-dd':'HH-mm-ss-SSS': '");
        }
        if ((config.getServers() == null ) || (config.getServers().isEmpty())) {
            config.getServers().put("1080", new ServerConfig("", true));
        }
        if (headless) {
            for (String portStr : config.getServers().keySet()) {
                startServer(portStr, config.getServers().get(portStr));
            }
            while (Server.countServersRunning() > 0) {
                Util.sleep(1000);
                logFinal(System.currentTimeMillis(), "Servers running :" + Server.countServersRunning());
            }
            System.exit(0);
        } else {
            launch(args);
        }
    }
}
