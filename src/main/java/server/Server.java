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

/**
 *
 * @author stuart
 */
public class Server {

    private static Map<Integer, ServerThread> serverThreads = new ConcurrentHashMap<>();
    

    public static ServerThread startServer(ServerConfig config, Notifier serverNotifier) {
        ServerThread serverThread = new ServerThread(config, serverNotifier);
        serverThreads.put(config.getPort(), serverThread);
        serverThread.start();
        return serverThread;
    }
    
    public static ServerThread stopServer(int port) {
        ServerThread serverThread = serverThreads.get(port);
        if (serverThread!=null) {
            serverThread.stopServer();
            serverThreads.remove(port);
        }
        return serverThread;
    }
    
    public static void stopAllServers() {
        for (ServerThread serverThread:serverThreads.values()) {
            serverThread.stopServer();
        }
        serverThreads.clear();
    }
    
    public static int countServersRunning() {
        int count = 0;
        for (ServerThread serverThread:serverThreads.values()) {
            if (serverThread.isRunning()) {
                count++;
            }
        }
        return count;
    }


    public static void main(String[] args) {

    }

}
