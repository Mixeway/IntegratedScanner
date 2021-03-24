/*
 * @created  2020-09-14 : 14:03
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.utils;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GitInformations {
    private String projectName;
    private String commitId;
    private String branchName;
    private String repoUrl;

    public GitInformations(){}
    public GitInformations(String projectName, String commitId, String branchName, String repoUrl) {
        this.projectName = projectName;
        this.commitId = commitId;
        this.branchName = branchName;
        this.repoUrl = repoUrl;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
