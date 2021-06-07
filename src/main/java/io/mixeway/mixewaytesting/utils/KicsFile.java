/*
 * @created  2021-06-07 : 11:09
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KicsFile {
    private String file_name;
    private String expected_value;
    private String actual_value;

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getExpected_value() {
        return expected_value;
    }

    public void setExpected_value(String expected_value) {
        this.expected_value = expected_value;
    }

    public String getActual_value() {
        return actual_value;
    }

    public void setActual_value(String actual_value) {
        this.actual_value = actual_value;
    }
}
