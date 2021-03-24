/*
 * @created  2021-01-25 : 22:23
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.utils;

public class TfsecResult {
    String rule_id;
    String rule_description;
    String rule_provider;
    String link;
    TfsecLocation location;
    String description;
    String severity;

    public String getRule_id() {
        return rule_id;
    }

    public void setRule_id(String rule_id) {
        this.rule_id = rule_id;
    }

    public String getRule_description() {
        return rule_description;
    }

    public void setRule_description(String rule_description) {
        this.rule_description = rule_description;
    }

    public String getRule_provider() {
        return rule_provider;
    }

    public void setRule_provider(String rule_provider) {
        this.rule_provider = rule_provider;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public TfsecLocation getLocation() {
        return location;
    }

    public void setLocation(TfsecLocation location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
