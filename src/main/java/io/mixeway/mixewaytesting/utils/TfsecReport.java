/*
 * @created  2021-01-25 : 22:26
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.utils;

import java.util.List;

public class TfsecReport {
    List<TfsecResult> results;

    public List<TfsecResult> getResults() {
        return results;
    }

    public void setResults(List<TfsecResult> results) {
        this.results = results;
    }
}
