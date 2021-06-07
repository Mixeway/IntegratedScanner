/*
 * @created  2021-06-07 : 11:09
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KicsReport {
    private List<KicsQuery> queries;

    public List<KicsQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<KicsQuery> queries) {
        this.queries = queries;
    }
}
