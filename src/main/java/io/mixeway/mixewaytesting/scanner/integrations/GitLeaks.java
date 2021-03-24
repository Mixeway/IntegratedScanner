/*
 * @created  2020-09-14 : 13:52
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.scanner.integrations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mixeway.mixewaytesting.scanner.factory.SecurityScanner;
import io.mixeway.mixewaytesting.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class GitLeaks implements SecurityScanner {
    private final Mixeway mixeway;
    final Logger log = LoggerFactory.getLogger(SecurityScanner.class);
    
    @Value("${source.path}")
    String sourcePath;
    public GitLeaks(Mixeway mixeway){
        this.mixeway = mixeway;
    }

    @Override
    public void runScan() {

    }

    @Override
    public List<Vulnerability> runScan(SourceCodeType sourceCodeType) {

        return null;
    }

    /**
     * Running gitleaks scan
     */
    @Override
    public List<Vulnerability> runScan(SourceCodeType sourceCodeType, PrepareCIOperation prepareCIOperation, GitInformations gitInformations) throws InterruptedException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        generateGitLeaksReport();
        List<Vulnerability> vulns = createVulnerabilityListFromGitLeaksReport();
        if (vulns!=null)
            mixeway.sendVulns(gitInformations,prepareCIOperation, vulns);
        return null;
    }

    /**
     * given gitleaks report covert it to vulnerability list which can be uploaded to Mixeway
     */
    private List<Vulnerability> createVulnerabilityListFromGitLeaksReport() throws IOException {
        try {
            List<Vulnerability> vulnerabilityList = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            List<GitLeak> leaks = objectMapper.readValue(new File(sourcePath + File.separatorChar + "gitleaks.json"), new TypeReference<List<GitLeak>>() {
            });
            for (GitLeak gitLeak : leaks) {
                vulnerabilityList.add(new Vulnerability(gitLeak));
            }
            log.info("[GitLeaks] Report converted");
            return vulnerabilityList;
        } catch (Exception e){
            log.info("[GitLeaks] Empty report, probably no leaks detected");
        }
        return null;
    }

    /**
     * Running gitleaks binary to produce report of secret leaks inside of given location
     */
    private void generateGitLeaksReport() throws IOException, InterruptedException {
        log.info("[GitLeaks] Starting to generate report...");
        ProcessBuilder generate;
        Process generateProcess;
        generate = new ProcessBuilder("bash", "-c", "gitleaks --repo-path="+sourcePath+" --report-format=json --report=gitleaks.json --depth=150").inheritIO();
        generate.directory(new File(sourcePath));
        generateProcess = generate.start();
        generateProcess.waitFor(3, TimeUnit.MINUTES);
        log.info("[GitLeaks] Generated report");
    }


    @Override
    public void runScan(SourceCodeType sourceCodeType, String mixewayKey, String dTrackKey, Long projectId) {

    }

    @Override
    public boolean canProceedWithScan(SourceCodeType sourceCodeType) {
        return false;
    }
}
