package io.mixeway.mixewaytesting;

import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * @author gsiewruk
 */
public class CisRequirement {
    private Long id;
    private String name;
    private String type;
    private String severity;
    public CisRequirement(){}
    public CisRequirement(String name, String type) {
        this.name =name;
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}