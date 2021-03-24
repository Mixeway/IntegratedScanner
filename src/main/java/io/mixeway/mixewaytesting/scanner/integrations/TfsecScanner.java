/*
 * @created  2021-01-25 : 22:13
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.scanner.integrations;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mixeway.mixewaytesting.scanner.factory.SecurityScanner;
import io.mixeway.mixewaytesting.utils.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

@Component
public class TfsecScanner implements SecurityScanner {
    @Value("${source.path}")
    String sourcePath;
    private final Mixeway mixeway;

    public TfsecScanner(Mixeway mixeway){
        this.mixeway = mixeway;
    }
    final Logger log = LoggerFactory.getLogger(TfsecScanner.class);

    /**
     * Running Scan of Tfscan
     */
    @Override
    public void runScan() throws IOException, InterruptedException {
        ProcessBuilder generate;
        Process generateProcess;
        generate = new ProcessBuilder("bash", "-c", "tfsec . --format json > tfsec.json").inheritIO();
        log.info("[Tfsec] Running TFSCAN on {}", sourcePath);
        generate.directory(new File(sourcePath));
        generateProcess = generate.start();
        generateProcess.waitFor();
        log.info("[Tfsec] Generated tfscan for {}", sourcePath);
    }

    /**
     * No need to support
     */
    @Override
    public List<Vulnerability> runScan(SourceCodeType sourceCodeType) throws IOException, InterruptedException {
        return null;
    }

    /**
     * Getting sourcePath location and file named tfsec.json to parse it into List of Vulnerabilities
     * @return List of Vulnerabilities
     */
    private List<Vulnerability> parseTfSecReport() throws IOException {
        FileInputStream fis = new FileInputStream(sourcePath + File.separatorChar + "tfsec.json");
        String tfSecReportRaw = IOUtils.toString(fis, StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        TfsecReport tfsecReport = objectMapper.readValue(tfSecReportRaw, TfsecReport.class);
        List<Vulnerability> vulnerabilityList = new ArrayList<>();
        for (TfsecResult tfsecResult : tfsecReport.getResults()){
            vulnerabilityList.add(new Vulnerability((tfsecResult)));
        }
        log.info("[Tfsec] Processed {} vulnerabilities from terraform project", vulnerabilityList.size());
        return vulnerabilityList;
    }

    /**
     * Runninf TF Scan, parsing the report and sending vulnerabilities to Mixeway
     */
    @Override
    public List<Vulnerability> runScan(SourceCodeType sourceCodeType, PrepareCIOperation prepareCIOperation, GitInformations gitInformations) throws InterruptedException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (sourceCodeType.equals(SourceCodeType.TF)) {
            this.runScan();
            List<Vulnerability> vulnerabilityList = parseTfSecReport();
            if (vulnerabilityList.size() > 0) {
                mixeway.sendVulns(gitInformations, prepareCIOperation, vulnerabilityList);
            }
        }
        return null;
    }

    /**
     * No Need to support
     */
    @Override
    public void runScan(SourceCodeType sourceCodeType, String mixewayKey, String dTrackKey, Long projectId) throws IOException, InterruptedException {

    }

    @Override
    public boolean canProceedWithScan(SourceCodeType sourceCodeType) {
        return sourceCodeType.equals(SourceCodeType.TF);
    }
}
