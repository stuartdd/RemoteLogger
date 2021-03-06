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
package common;

public class FileData {
    private final String fileName;
    private final boolean readFromFile;
    private final String content;

    public FileData(String fileName, boolean readFromFile, String content) {
        this.fileName = fileName;
        this.readFromFile = readFromFile;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isReadFromFile() {
        return readFromFile;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return getContent();
    }
    
    
    
}
