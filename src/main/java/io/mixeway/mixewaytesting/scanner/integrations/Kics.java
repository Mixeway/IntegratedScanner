/*
 * @created  2021-06-07 : 11:15
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Component
public class Kics implements SecurityScanner {
    @Value("${source.path}")
    String sourcePath;
    final Logger log = LoggerFactory.getLogger(Kics.class);
    private final Mixeway mixeway;

    public Kics(Mixeway mixeway){
        this.mixeway = mixeway;
    }

    @Override
    public void runScan() throws IOException, InterruptedException {
        ProcessBuilder generate;
        Process generateProcess;
        StringBuilder kicsScan = new StringBuilder()
                .append("kics scan -p ")
                .append(sourcePath)
                .append(" -o ")
                .append(sourcePath).append(File.separatorChar).append("results.json");
        log.info("[KICS] About to execute {}", kicsScan.toString());
        generate = new ProcessBuilder("bash", "-c", kicsScan.toString()).inheritIO();
        log.info("[KICS] Running IaC Scan on {}", sourcePath);
        generate.directory(new File(sourcePath));
        generateProcess = generate.start();
        generateProcess.waitFor();
        log.info("[KICS] Generated IaC scan for {}", sourcePath);
    }

    /**
     * No need to support
     */
    @Override
    public List<Vulnerability> runScan(SourceCodeType sourceCodeType) throws IOException, InterruptedException {
        this.runScan();
        return parseKicksReport();
    }

    @Override
    public List<Vulnerability> runScan(SourceCodeType sourceCodeType, PrepareCIOperation prepareCIOperation, GitInformations gitInformations) throws InterruptedException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (sourceCodeType.equals(SourceCodeType.TF)) {
            this.runScan();
            List<Vulnerability> vulnerabilityList = parseKicksReport();
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
    public void runScan(SourceCodeType sourceCodeType, String mixewayKey, String dTrackKey, Long projectId) {

    }

    @Override
    public boolean canProceedWithScan(SourceCodeType sourceCodeType) {
        return sourceCodeType.equals(SourceCodeType.TF);
    }
    /**
     * Getting sourcePath location and file named results.json to parse it into List of Vulnerabilities
     * @return List of Vulnerabilities
     */
    private List<Vulnerability> parseKicksReport() throws IOException {
        FileInputStream fis = new FileInputStream(sourcePath + File.separatorChar + "results.json");
        String kicsReportRaw = IOUtils.toString(fis, StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        KicsReport kicsReport = objectMapper.readValue(kicsReportRaw, KicsReport.class);
        List<Vulnerability> vulnerabilityList = new ArrayList<>();
        for (KicsQuery kicsQuery : kicsReport.getQueries()){
            for(KicsFile kicsFile : kicsQuery.getFiles()){
                vulnerabilityList.add(new Vulnerability(kicsQuery, kicsFile, sourcePath));
            }
        }
        log.info("[KICS] Processed {} vulnerabilities from terraform project", vulnerabilityList.size());
        return vulnerabilityList;
    }

}
