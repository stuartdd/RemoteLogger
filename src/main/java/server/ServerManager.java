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
package server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import common.Notifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import main.ConfigDataException;

/**
 *
 * @author stuart
 */
public class ServerManager {

    private static Map<Integer, Server> servers = new ConcurrentHashMap<>();

    public static void addServer(String portStr, ServerConfig config, Notifier serverNotifier) {
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            throw new ConfigDataException("Port number [" + portStr + "] is invalid");
        }
        servers.put(port, new Server(port, config, serverNotifier));
    }

    public static boolean isPortRunning(int port) {
        Server server = servers.get(port);
        if (server != null) {
            return server.isRunning();
        }
        return false;
    }

    public static ServerState state(int port) {
        Server server = servers.get(port);
        if (server != null) {
            return server.state();
        }
        return ServerState.SERVER_STOPPED;
    }

    public static void startServer(int port) {
        Server server = servers.get(port);
        if (server != null) {
            server.start();
        }
    }

    public static void stopServer(int port) {
        Server server = servers.get(port);
        if (server != null) {
            server.stop();
        }
    }

    public static void stopAllServers() {
        for (Server server : servers.values()) {
            server.stop();
        }
    }

    public static void autoStartServers() {
        for (Server server : servers.values()) {
            if (server.isAutoStart()) {
                server.start();
            }
        }
    }
    public static void setShowPort(int port, boolean selected) {
         Server server = servers.get(port);
        if (server != null) {
            server.setShowPort(selected);
        }
    }

    public static boolean isShowPort(int port) {
        Server server = servers.get(port);
        if (server != null) {
            return server.isShowPort();
        }
        return false;
    }
    
    public static void setAutoStart(int port, boolean selected) {
        Server server = servers.get(port);
        if (server != null) {
            server.setAutoStart(selected);
        }
    }

    public static boolean isAutoStart(int port) {
        Server server = servers.get(port);
        if (server != null) {
            return server.isAutoStart();
        }
        return false;
    }

    public static int countServersRunning() {
        int count = 0;
        for (Server server : servers.values()) {
            if (server.isRunning()) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasPort(int port) {
        return servers.containsKey(port);
    }

    public static int[] ports() {
        int[] in = new int[servers.size()];
        int pos = 0;
        for (Integer port : servers.keySet()) {
            in[pos] = port;
            pos++;
        }
        Arrays.sort(in);
        return in;
    }

    public static List<String> portList() {
        List<String> l = new ArrayList<>();
        for (int p : ports()) {
            l.add("" + p);
        }
        return l;
    }


}
