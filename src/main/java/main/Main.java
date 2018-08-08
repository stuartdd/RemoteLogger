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
import common.Util;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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
import server.ServerManager;
import server.ServerConfig;

public class Main extends Application {

    private static final int PORT_NUMBER = 1088;
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static boolean headless = false;
    private static ApplicationController applicationController;
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
        setTitle("LOADED");
        stage.show();
    }

    public static void notifyAction(long time, int port, Action action, ActionOn actionOn, String message) {
        if (!headless) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (applicationController != null) {
                        applicationController.notifyAction(time, port, action, actionOn, message);
                    } else {
                        System.out.println("Action:" + action + " on:" + actionOn.getClass());
                    }
                }
            });
        } else {
            Main.logFinal(time, port, action.name() + ":" + message);
        }
    }

    public static void setTitle(String title) {
        mainStage.setTitle("Log Server:" + (configName == null ? "<Undefined>" : configName) + " - " + title);
    }

    public static void notifyConfigChangeOption(Option option, int port, boolean selected, String message) {
        if (!headless) {
            switch (option) {
                case FILTER_HEADERS:
                    config.setIncludeHeaders(selected);
                    break;
                case FILTER_BODY:
                    config.setIncludeBody(selected);
                    break;
                case FILTER_EMPTY:
                    config.setIncludeEmpty(selected);
                    break;
                case TIME:
                    config.setShowTime(selected);
                    break;
                case PORT:
                    config.setShowPort(selected);
                    break;
            }
            notifyAction(System.currentTimeMillis(), port, Action.LOG_REFRESH, null, "");
        }
    }

    public static void setApplicationController(ApplicationController theApplicationController) {
        applicationController = theApplicationController;
    }

    public static void startServer(int port) {
        ServerManager.startServer(port);
    }

    public static void stopServer(int port) {
        ServerManager.stopServer(port);
    }

    public static Server getDefaultServer() {
        return ServerManager.getServer(config.getDefaultPort());
    }

    public static void controlStopEventAction() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Util.sleep(200);
                if (headless) {
                    closeApplication(true);
                } else {
                    ServerManager.stopAllServers();
                }
            }
        }).start();
    }

    public static void closeApplication(boolean force) {
        log(System.currentTimeMillis(), -1, "Shutting down");
        if (!headless) {
            if (ConfigData.writeFileName() != null) {
                config.setX(mainStage.getX());
                config.setY(mainStage.getY());
                config.setWidth(mainStage.getWidth());
                config.setHeight(mainStage.getHeight());
                applicationController.updateConfig(config);
                try {
                    Files.write(
                            FileSystems.getDefault().getPath(ConfigData.writeFileName()),
                            JsonUtils.toJsonFormatted(config).getBytes(CHARSET),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException io) {
                    if (!force) {
                        Main.notifyAction(System.currentTimeMillis(), -1, Action.CONFIG_SAVE_ERROR, null, io.toString());
                        return;
                    }
                }
            }
        }
        ServerManager.stopAllServers();
        if (mainStage != null) {
            mainStage.close();
        } else {
            System.exit(0);
        }
    }

    public static void log(long time, int port, String message) {
        if (message != null) {
            notifyAction(time, port, Action.LOG, null, message);
            logFinal(time, port, message);
        }
    }

    public static void log(long time, int port, Throwable throwable) {
        if (throwable != null) {
            notifyAction(time, port, Action.LOG, null, "ERROR:" + throwable.getMessage());
            logFinal(time, port, "ERROR:" + throwable.getMessage());
        }
    }

    public static void log(long time, int port, String message, Throwable throwable) {
        if (throwable != null) {
            notifyAction(time, port, Action.LOG, null, "ERROR:" + message + ": " + throwable.getMessage());
            logFinal(time, port, "ERROR:" + message + ": " + throwable.getMessage());
        }
    }

    public static void logFinal(long time, int port, String message) {
        System.out.println(getTimeStamp(time) + (port <= 0 ? "" : "[" + port + "] ") + message);
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
        config.setDefaultPort(0);
        configName = null;
        try {
            for (String arg : args) {
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
        if (config.getLogDateFormat() == null) {
            config.setLogDateFormat("yyyy-MM-dd':'HH-mm-ss-SSS': '");
        }
        if ((config.getServers() == null) || (config.getServers().isEmpty())) {
            config.getServers().put("" + PORT_NUMBER, new ServerConfig("", 1, true, true));
            config.setDefaultPort(PORT_NUMBER);
        }
        for (String portStr : config.getServers().keySet()) {
            ServerConfig serverConfig = config.getServers().get(portStr);
            ServerManager.addServer(portStr, serverConfig, new MainNotifier(serverConfig.isVerbose()));
        }
        if (headless) {
            ServerManager.autoStartServers();
            while (ServerManager.countServersRunning() > 0) {
                Util.sleep(1000);
                logFinal(System.currentTimeMillis(), -1, "Servers running :" + ServerManager.countServersRunning());
            }
            System.exit(0);
        } else {
            if (config.getDefaultPort() == 0) {
                if (config.getServers().size() != 1) {
                    System.err.println("Default port is not defined");
                    System.exit(1);
                }
                config.setDefaultPort(ServerManager.ports()[0]);
            }
            if (!ServerManager.hasPort(config.getDefaultPort())) {
                System.err.println("Default port [" + config.getDefaultPort() + "] is not listed in the servers " + ServerManager.ports());
                System.exit(1);
            }
            launch(args);
        }
    }
}
