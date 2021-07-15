/*
 * @created  2020-09-14 : 13:23
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.scanner.factory;

import io.mixeway.mixewaytesting.scanner.integrations.DependencyTrackScanner;
import io.mixeway.mixewaytesting.scanner.integrations.GitLeaks;
import io.mixeway.mixewaytesting.scanner.integrations.Kics;
import io.mixeway.mixewaytesting.scanner.integrations.TfsecScanner;
import io.mixeway.mixewaytesting.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityScannerFactory {
    final Logger log = LoggerFactory.getLogger(SecurityScannerFactory.class);


    private final DependencyTrackScanner dependencyTrackScanner;
    private final GitLeaks gitLeaks;
    private final TfsecScanner tfsecScanner;
    private final Kics kics;

    public SecurityScannerFactory(GitLeaks gitLeaks, DependencyTrackScanner dependencyTrackScanner, TfsecScanner tfsecScanner, Kics kics){
        this.dependencyTrackScanner =dependencyTrackScanner;
        this.gitLeaks = gitLeaks;
        this.tfsecScanner = tfsecScanner;
        this.kics = kics;
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
        if (!sourceCodeType.equals(SourceCodeType.UNKNOWN))
            dependencyTrackScanner.runScan(sourceCodeType, prepareCIOperation, gitInformations);
        gitLeaks.runScan(sourceCodeType, prepareCIOperation, gitInformations);
        tfsecScanner.runScan(sourceCodeType,prepareCIOperation,gitInformations);
        kics.runScan(sourceCodeType, prepareCIOperation, gitInformations);
    }

    public void runIaCStandalone(SourceCodeType sourceCodeType) throws IOException, InterruptedException {
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        if (sourceCodeType.equals(SourceCodeType.TF)){
            #vulnerabilities.addAll(kics.runScan(sourceCodeType));
            vulnerabilities.addAll(tfsecScanner.runScan(sourceCodeType));
        }
        log.info("Terraform Scan ended, results:");
        for (Vulnerability vulnerability : vulnerabilities){
            log.info("VulnName: {}, Severity: {}, Location: {}, Description {}", vulnerability.getName(), vulnerability.getSeverity(), vulnerability.getFilename(),vulnerability.getDescription());
        }
        if (vulnerabilities.stream().filter(v-> v.getSeverity().equals(Constants.SEVERITY_HIGH)).count()  > 5){
            log.error("Security Policy not met.");
            System.exit(1);
        } else {
            log.info("Security Policy met");
            System.exit(0);
        }
    }

}
