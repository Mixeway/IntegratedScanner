/*
 * @created  2020-09-14 : 14:48
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.testing;

import io.mixeway.mixewaytesting.git.GitHelper;
import io.mixeway.mixewaytesting.scanner.factory.SecurityScannerFactory;
import io.mixeway.mixewaytesting.scanner.integrations.Mixeway;
import io.mixeway.mixewaytesting.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Service
public class Testing {
    final Logger log = LoggerFactory.getLogger(Testing.class);
    final SecurityScannerFactory securityScannerFactory;
    private final Mixeway mixeway;
    @Value("${source.path}")
    String sourcePath;
    @Value("${branch.name}")
    String branch;
    @Value("${commit.id}")
    String commit;
    @Value("${project.name}")
    String projectName;
    @Value("${repo.url}")
    String repoUrl;
    @Value("${scan.type}")
    String scanType;

    public Testing(Mixeway mixeway, SecurityScannerFactory securityScannerFactory){
        this.mixeway = mixeway;
        this.securityScannerFactory = securityScannerFactory;
    }

    /**
     * Perform tests steps:
     * 1. Get information from .git -> url, reponame, actvie branch, commitid
     * 2. Send it to Mixeway to get dTrack infos, and CodeProjectID (if not present, create one)
     * 3. Run scan
     */
    public void performTest() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, InterruptedException {
        try {
            SourceCodeType sourceCodeType = CodeHelper.getSourceProjectTypeFromDirectory(sourcePath);
            //If scan type is IaC it means that no more integration is needed just run standalone scan of IaC
            if (scanType.equals(Constants.SCAN_TYPE_IAC)){
                log.info("[Mixeway Testing] Performing scan in standalone mode for Infrastructure as Code - no Mixeway integration");
                securityScannerFactory.runIaCStandalone(sourceCodeType);
                System.exit(0);
            }

            PrepareCIOperation rootOperation = null;
            GitInformations gitInformations;
            // If vaules for branch, commit and projectName are set use them for Mixeway integration, otherwise take them from .git
            if (!branch.equals("git") && !commit.equals("123") && !repoUrl.equals("url") && !projectName.equals("name")){
                gitInformations = new GitInformations(projectName,commit,branch,repoUrl);
            } else {
                gitInformations = GitHelper.getGitInformations(sourcePath, branch);
            }
            GitInformations npmAdditionalInformations = null;

            log.info("[Mixeway Tester] Source code Type is: {}", sourceCodeType);
            // If project is type of MVN and contains JavaScript (NPM) code:
            if (sourceCodeType.equals(SourceCodeType.MVN) && CodeHelper.isProjectOfSourceType(SourceCodeType.NPM, sourcePath)){
                npmAdditionalInformations = new GitInformations(gitInformations.getProjectName(), gitInformations.getCommitId(),gitInformations.getBranchName(), gitInformations.getRepoUrl());
                npmAdditionalInformations.setProjectName(npmAdditionalInformations.getProjectName()+ "_"+SourceCodeType.NPM);
                gitInformations.setProjectName(gitInformations.getProjectName()+ "_"+SourceCodeType.MVN);
            }

            if (gitInformations != null) {
                // This one is to create proper entities on Mixeway
                PrepareCIOperation ciOperation = mixeway.getCIInfo(new GetInfoRequest(gitInformations));
                rootOperation = ciOperation;
                if (ciOperation != null) {
                    log.info("[Mixeway Tester] Got request for {} with type {}, proceeding..", gitInformations.getProjectName(), sourceCodeType);
                    securityScannerFactory.runScan(sourceCodeType, ciOperation, gitInformations);
                }
            }
            if (npmAdditionalInformations != null) {
                PrepareCIOperation ciOperation = mixeway.getCIInfo(new GetInfoRequest(npmAdditionalInformations));
                rootOperation = ciOperation;
                if (ciOperation != null) {
                    log.info("[Mixeway Tester] Got request for {} with type {}, proceeding..", npmAdditionalInformations.getProjectName(), sourceCodeType);
                    securityScannerFactory.runScan(SourceCodeType.NPM, ciOperation, npmAdditionalInformations);
                }
            }

            if (rootOperation!=null && !sourceCodeType.equals(SourceCodeType.TF)) {
                log.info("[Mixeway Tester] Requesting for SAST scan...");
                mixeway.startSastScanForRepo(rootOperation);
                log.info("[Mixeway Tester] SAST scan requested.");
            } else {
                log.warn("[Mixeway Tester] Problem requesting for SAST scan, empty request.");
            }

            MixewaySecurityGatewayResponse vulnerabilities = mixeway.loadVulnerabilities(rootOperation);
            log.info("Vulnerabilities detected {} :", vulnerabilities.getVulnList().size());
            for (Vuln v : vulnerabilities.getVulnList()){
                if (v.getType().equals(Constants.PACKAGE_SCAN)){
                    log.info("Vuln Name: {}, Vuln location: {}, Vuln Severity: {}", v.getVulnerabilityName(), v.getPacketName(), v.getSeverity());
                } else if (v.getType().equals(Constants.CODE_SCAN)){
                    log.info("Vuln Name: {}, Vuln location: {}, Vuln Severity: {}", v.getVulnerabilityName(), v.getLocation(), v.getSeverity());
                }
            }
            if ( vulnerabilities.isSecurityPolicyMet()){
                log.info(vulnerabilities.getPolicyResponse());
            } else {
                log.error(vulnerabilities.getPolicyResponse());
                System.exit(1);
            }
        }catch (IllegalStateException | IllegalArgumentException | IOException e){
            e.printStackTrace();
            log.error("[Mixeway Tester] /opt/sources location is not mounted. Make sure to enable mount e.g. -v $PWD:/opt/sources");
        }
        System.exit(0);
    }
}
