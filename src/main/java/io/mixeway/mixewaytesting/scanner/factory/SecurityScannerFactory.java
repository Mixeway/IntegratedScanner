/*
 * @created  2020-09-14 : 13:23
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.scanner.factory;

import io.mixeway.mixewaytesting.scanner.integrations.DependencyTrackScanner;
import io.mixeway.mixewaytesting.scanner.integrations.GitLeaks;
import io.mixeway.mixewaytesting.scanner.integrations.TfsecScanner;
import io.mixeway.mixewaytesting.utils.GitInformations;
import io.mixeway.mixewaytesting.utils.PrepareCIOperation;
import io.mixeway.mixewaytesting.utils.SourceCodeType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Service
public class SecurityScannerFactory {


    private final DependencyTrackScanner dependencyTrackScanner;
    private final GitLeaks gitLeaks;
    private final TfsecScanner tfsecScanner;

    public SecurityScannerFactory(GitLeaks gitLeaks, DependencyTrackScanner dependencyTrackScanner, TfsecScanner tfsecScanner){
        this.dependencyTrackScanner =dependencyTrackScanner;
        this.gitLeaks = gitLeaks;
        this.tfsecScanner = tfsecScanner;
    }


    /**
     * Running Scan for DependencyTrack (generation of bom.xml and upload it to dependency-track)
     * Running gitleaks
     *
     * @param sourceCodeType type of project
     * @param prepareCIOperation operations with dTrack data and ids
     * @param gitInformations info about git repository
     */
    public void runScan(SourceCodeType sourceCodeType, PrepareCIOperation prepareCIOperation, GitInformations gitInformations) throws InterruptedException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
        dependencyTrackScanner.runScan(sourceCodeType, prepareCIOperation, gitInformations);
        gitLeaks.runScan(sourceCodeType, prepareCIOperation, gitInformations);
        tfsecScanner.runScan(sourceCodeType,prepareCIOperation,gitInformations);
    }

}
