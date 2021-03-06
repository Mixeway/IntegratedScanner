/*
 * @created  2020-09-14 : 14:00
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.git;

import io.mixeway.mixewaytesting.utils.Constants;
import io.mixeway.mixewaytesting.utils.GitInformations;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;


public class GitHelper {
    /**
     * Get default directory of /opt/sources and try to load active branch, last commitID and repo name from it
     *
     * @return git info object containing above data
     */
    public static GitInformations getGitInformations(String sourcePath, String branch) {
        branch = Arrays.stream(branch.split("/")).reduce((first,last) -> last).get();
        String latestCommitHash;
        final Logger log = LoggerFactory.getLogger(GitHelper.class);
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();

            Repository repository = builder.setGitDir(Paths.get(sourcePath + File.separatorChar + ".git").toFile())
                    .readEnvironment()
                    .findGitDir()
                    .build();
            log.info("[GIT] Loading GIT info for {}", sourcePath + File.separatorChar + ".git");
            log.info("[GIT] Config is: {}", repository.getConfig().getBaseConfig().toText());

            RevCommit latestCommit = new Git(repository).log().setMaxCount(1).call().iterator().next();
            latestCommitHash = latestCommit.getName();

            String branchToSet = branch.equals("git") ? Stream.of(repository.getFullBranch().split("/")).reduce((first, last) -> last).get() : branch;

            GitInformations gitInformations = new GitInformations(Stream.of(repository
                    .getConfig()
                    .getString("remote", "origin", "url")
                    .split("/"))
                    .reduce((first, last) -> last)
                    .get()
                    .split(".git")[0], latestCommitHash, branchToSet
                    , repository.getConfig().getString("remote", "origin", "url") );

            log.info("[GIT] Processing scan for {} with active branch {} and latest commit {}", gitInformations.getProjectName(), gitInformations.getBranchName(), gitInformations.getCommitId());
            return gitInformations;
        } catch (IOException | GitAPIException e){
            e.printStackTrace();
            log.error("[GIT] Unable to load GIT informations reason - {}", e.getLocalizedMessage());
            System.exit(1);
        }
        return null;
    }
}
