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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import json.JsonUtils;
import org.joda.time.DateTime;
import server.Server;
import server.ServerConfig;
import server.ServerManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class Main extends Application {

    private static final int PORT_NUMBER = 1088;
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static boolean headless = false;
    private static ApplicationController applicationController;
    private static Stage mainStage;
    private static PackagedRequestWrapperList packagedRequestWrapperList;

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        stage.setX(ConfigData.getInstance().getX());
        stage.setY(ConfigData.getInstance().getY());
        stage.setHeight(ConfigData.getInstance().getHeight());
        stage.setWidth(ConfigData.getInstance().getWidth());
        Parent root = FXMLLoader.load(getClass().getResource("/FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (!closeApplication(false)) {
                    event.consume();
                }
            }
        });
        setTitle("LOADED");
        stage.show();
    }

    public static void notifyAction(long time, int port, Action action, Object actionOn, String message) {
        if (!headless) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (applicationController != null) {
                        applicationController.notifyAction(time, port, action, actionOn, message);
                    } else {
                        System.out.println("Action:" + action + " on:" + (actionOn == null ? "null" : actionOn.getClass()));
                    }
                }
            });
        } else {
            Main.logFinal(time, port, action.name() + ":" + message);
        }
    }

    public static void setTitle(String title) {
        mainStage.setTitle("Log Server:" + (ConfigData.isLoadedFromFile() ? ConfigData.readFileName() : "<Undefined>") + " - " + title);
    }

    public static void notifyConfigChangeOption(Option option, int port, boolean selected, String message) {
        if (!headless) {
            switch (option) {
                case FILTER_HEADERS:
                    ConfigData.getInstance().setIncludeHeaders(selected);
                    break;
                case FILTER_BODY:
                    ConfigData.getInstance().setIncludeBody(selected);
                    break;
                case FILTER_EMPTY:
                    ConfigData.getInstance().setIncludeEmpty(selected);
                    break;
                case TIME:
                    ConfigData.getInstance().setShowTime(selected);
                    break;
                case PORT:
                    ConfigData.getInstance().setShowPort(selected);
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
        return ServerManager.getServer(ConfigData.getInstance().getDefaultPort());
    }

    public static PackagedRequestWrapperList getPackagedRequestWrapperList() {
        return packagedRequestWrapperList;
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

    public static boolean alertOkCancel(String ti, String txt, String ht) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Alert:" + ti);
        alert.setHeaderText(txt);
        alert.setContentText(ht);
        alert.setX(mainStage.getX() + 50);
        alert.setY(mainStage.getY() + 50);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == ButtonType.OK;
    }

    public static void errorDialog(String ti, String txt, String ht) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(ti);
        alert.setHeaderText(txt);
        alert.setContentText(ht);
        alert.setX(mainStage.getX() + 50);
        alert.setY(mainStage.getY() + 50);
        alert.showAndWait();
    }

    public static String textInputDialog(String ti, String ht, String prompt, String txt) {
        TextInputDialog textDialog = new TextInputDialog(txt);
        textDialog.setTitle(ti);
        textDialog.setHeaderText(ht);
        textDialog.setContentText(prompt);
        textDialog.setX(mainStage.getX() + 50);
        textDialog.setY(mainStage.getY() + 50);
        Optional<String> result = textDialog.showAndWait();
        if (result.isPresent()) {
            String res = result.get();
            if (res.equals(txt)) {
                return null;
            }
            return res;
        }
        return null;
    }

    public static boolean closeApplication(boolean force) {
        if (!applicationController.canAppClose() && !force) {
            if (!Main.alertOkCancel("Unsaved Data", "Unsaved Configuration data. Please save before exiting", "Ok to quit anyway and discard the changes?")) {
                return false;
            }
        }

        log(System.currentTimeMillis(), -1, "Shutting down");
        if (!headless) {
            if (ConfigData.canWriteToFile()) {
                ConfigData.getInstance().setX(mainStage.getX());
                ConfigData.getInstance().setY(mainStage.getY());
                ConfigData.getInstance().setWidth(mainStage.getWidth());
                ConfigData.getInstance().setHeight(mainStage.getHeight());
                applicationController.updateConfig(ConfigData.getInstance());
                try {
                    Files.write(
                            FileSystems.getDefault().getPath(ConfigData.writeFileName()),
                            JsonUtils.toJsonFormatted(ConfigData.getInstance()).getBytes(CHARSET),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException io) {
                    if (!force) {
                        Main.notifyAction(System.currentTimeMillis(), -1, Action.CONFIG_SAVE_ERROR, null, io.toString());
                        return true;
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
        return true;
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

    public static String getTimeStamp(long time) {
        if ((ConfigData.getInstance().getLogDateFormat() != null) && (ConfigData.getInstance().getLogDateFormat().trim().length() > 0)) {
            return (new DateTime(time)).toString(ConfigData.getInstance().getLogDateFormat());
        }
        return (new DateTime(time)).toString("HH:mm:ss.SSS: ");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        headless = false;
        try {
            for (String arg : args) {
                if (arg.startsWith("-h")) {
                    headless = true;
                }
            }
            for (String arg : args) {
                if (arg.toLowerCase().endsWith(".json")) {
                    ConfigData.load(arg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            if (headless) {
                return;
            }
            System.exit(1);
        }
        if (ConfigData.getInstance().getLogDateFormat() == null) {
            ConfigData.getInstance().setLogDateFormat("yyyy-MM-dd':'HH-mm-ss-SSS': '");
        }
        if ((ConfigData.getInstance().getServers() == null) || (ConfigData.getInstance().getServers().isEmpty())) {
            ConfigData.getInstance().getServers().put("" + PORT_NUMBER, new ServerConfig("", 1, true, true));
            ConfigData.getInstance().setDefaultPort(PORT_NUMBER);
        }
        for (String portStr : ConfigData.getInstance().getServers().keySet()) {
            ServerConfig serverConfig = ConfigData.getInstance().getServers().get(portStr);
            ServerManager.addServer(portStr, serverConfig, new MainNotifier(serverConfig.isVerbose()));
        }

        if (headless) {
            ServerManager.autoStartServers();
            int count = 0;
            do {
                Util.sleep(1000);
                if ((count % 10) == 0) {
                    logFinal(System.currentTimeMillis(), -1, "Servers running :" + ServerManager.countServersRunning());
                }
                count++;
            } while (ServerManager.countServersRunning() > 0);
            System.exit(0);
        } else {
            if ((ConfigData.getInstance().getPackagedRequestsFile() != null) && (ConfigData.getInstance().getPackagedRequestsFile().trim().length() > 0)) {
                PackagedRequestWrapperManager.load(ConfigData.getInstance().getPackagedRequestsFile());
                packagedRequestWrapperList = PackagedRequestWrapperManager.getPackagedRequestWrapperList(config.getSelectedPackagedRequestName());
                packagedRequestWrapperList.check();
            } else {
                packagedRequestWrapperList = null;
            }
            PackagedRequestWrapperManager.setRequestNotifier(new MainNotifier(PackagedRequestWrapperManager.isVerbose()));

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
