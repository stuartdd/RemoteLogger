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

public class ServerConfig {

    private int port;
    private String expectationsFile;
    private boolean verbose = false;

    public ServerConfig(int port, String expectationsFile, boolean verbose) {
        this.port = port;
        this.expectationsFile = expectationsFile;
        this.verbose = verbose;
    }

    public int getPort() {
        return port;
    }

    public String getExpectationsFile() {
        return expectationsFile;
    }

    public boolean getVerbose() {
        return verbose;
    }

}
