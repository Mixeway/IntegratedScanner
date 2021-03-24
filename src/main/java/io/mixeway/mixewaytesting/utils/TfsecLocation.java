/*
 * @created  2021-01-25 : 22:24
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.utils;

public class TfsecLocation {
    String filename;
    String start_line;
    String end_line;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStart_line() {
        return start_line;
    }

    public void setStart_line(String start_line) {
        this.start_line = start_line;
    }

    public String getEnd_line() {
        return end_line;
    }

    public void setEnd_line(String end_line) {
        this.end_line = end_line;
    }
}
