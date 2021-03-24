/*
 * @created  2020-09-14 : 13:52
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.scanner.integrations;

import io.mixeway.mixewaytesting.utils.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import io.mixeway.mixewaytesting.scanner.factory.SecurityScanner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class DependencyTrackScanner implements SecurityScanner {
    final Logger log = LoggerFactory.getLogger(DependencyTrackScanner.class);
    @Value("${secret.mixeway.key}")
    String mixewayApiKey;
    @Value("${dtrack.url}")
    String dTrackUrl;

    @Value("${mixeway.url}")
    String mixewayUrl;
    @Value("${source.path}")
    String sourcePath;
    @Value("${MAVEN_CONFIG:}")
    private String mavenConfig;

    @Override
    public void runScan() {

    }

    @Override
    public List<Vulnerability> runScan(SourceCodeType sourceCodeType) {
        return null;
    }

    /**
     * Run Scan which depends on technology and then send info To Mixeway to download vulnerabilities
     *
     * @param sourceCodeType type of code to scan
     * @param prepareCIOperation info for mixeway to inform
     * @param gitInformations info about git repository
     */
    @Override
    public List<Vulnerability> runScan(SourceCodeType sourceCodeType, PrepareCIOperation prepareCIOperation, GitInformations gitInformations) {
        try {
            log.info("[Dependency Track] Processing for {}", sourceCodeType);
            switch ( sourceCodeType) {
                case NPM:
                    sendBom(processNpmDependencyTrackScan(), prepareCIOperation);
                    Thread.sleep(10000);
                    sendMixewayInfo(prepareCIOperation, gitInformations);
                    break;
                case MVN:
                    sendBom(processMvnDependencyTrack(), prepareCIOperation);
                    Thread.sleep(10000);
                    sendMixewayInfo(prepareCIOperation, gitInformations);
                    break;
                case PHP:
                    sendBom(processPhpDependencyTrack(), prepareCIOperation);
                    Thread.sleep(10000);
                    sendMixewayInfo(prepareCIOperation, gitInformations);
                    break;
                case PYTHON:
                    sendBom(processPythonDependencyTrack(), prepareCIOperation);
                    Thread.sleep(10000);
                    sendMixewayInfo(prepareCIOperation, gitInformations);
                    break;
                default:
                    log.info("[Dependency Track] {} not yet supported", sourceCodeType);
            }
            return null;
        } catch (IllegalArgumentException | IOException | InterruptedException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e){
            log.error("[Dependency Track] Error occured: {}", e.getLocalizedMessage());
        }
        return new ArrayList<>();
    }


    /**
     * Upload BOM file to dependency track
     *
     * @param bomLocation location of bom.xml
     * @param prepareCIOperation with info where to upload
     */
    private void sendBom(String bomLocation, PrepareCIOperation prepareCIOperation) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        RestTemplate restTemplate = MRestTemplate.createRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(Constants.DEPENDENCYTRACK_APIKEY_HEADER, prepareCIOperation.getOpenSourceScannerCredentials());
        HttpEntity<SendBomRequest> entity = new HttpEntity<>(
                new SendBomRequest(prepareCIOperation.getOpenSourceScannerProjectId(),
                        encodeFileToBase64Binary(bomLocation)), headers);
        ResponseEntity<String> response = restTemplate.exchange(dTrackUrl +
                Constants.DEPENDENCYTRACK_URL_UPLOAD_BOM, HttpMethod.PUT, entity, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK)){
            log.info("[Dependency Track] SBOM for {} uploaded successfully", prepareCIOperation.getOpenSourceScannerProjectId());
        }
    }
    /**
     * Encodes file content to base64
     * @param fileName file name to encode
     * @return return base64 string
     */
    private static String encodeFileToBase64Binary(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
        return new String(encoded, StandardCharsets.US_ASCII);
    }


    /**
     * Generate BOM for PIP projects
     *
     */
    private String processPythonDependencyTrack() throws IOException, InterruptedException {
        ProcessBuilder install, generate;
        Process installProcess,  generateProcess;
        ProcessBuilder freeze = new ProcessBuilder("bash", "-c", "pipreqs . --force").inheritIO();
        install = new ProcessBuilder("bash", "-c", "pip3 install cyclonedx-bom").inheritIO();
        generate = new ProcessBuilder("bash", "-c", "cyclonedx-py -i requirements.txt -o bom.xml").inheritIO();
        freeze.directory(new File(sourcePath));
        Process freezeProcess = freeze.start();
        freezeProcess.waitFor();
        log.info("[Dependency Track] Freezing PIP dependencies for {}", sourcePath);
        install.directory(new File(sourcePath));
        installProcess = install.start();
        installProcess.waitFor();
        log.info("[Dependency Track] Installed CycloneDX PIP for {}", sourcePath);
        generate.directory(new File(sourcePath));
        generateProcess = generate.start();
        generateProcess.waitFor();
        log.info("[Dependency Track] Generated SBOM for {}", sourcePath);
        return sourcePath + File.separatorChar + "bom.xml";
    }

    /**
     * Generate BOM for composer project
     */
    private String processPhpDependencyTrack() throws InterruptedException, IOException {
        ProcessBuilder install, generate;
        Process installProcess, generateProcess;
        install = new ProcessBuilder("bash", "-c", "composer require --dev cyclonedx/cyclonedx-php-composer");
        install.directory(new File(sourcePath));
        installProcess = install.start();
        installProcess.waitFor();
        log.info("[Dependency Track] Installed CycloneDX COMPOSER for {}", sourcePath);
        generate = new ProcessBuilder("bash", "-c", "composer make-bom");
        generate.directory(new File(sourcePath));
        generateProcess = generate.start();
        generateProcess.waitFor();
        log.info("[Dependency Track] Generated SBOM for {}", sourcePath);
        return sourcePath + File.separatorChar + "bom.xml";
    }

    /**
     * Generate BOM for MVN project
     *
     */
    private String processMvnDependencyTrack() throws IOException, InterruptedException {
        String rootPomPath = getPathOfRootPom();
        ProcessBuilder generate;
        Process generateProcess;
        generate = new ProcessBuilder("bash", "-c", "mvn "+mavenConfig +" -DskipTests -DSPDXParser.OnlyUseLocalLicenses=true org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom").inheritIO();
        Map<String, String> env = generate.environment();
        // set environment variable u
        env.put("MAVEN_CONFIG", mavenConfig);
        generate.directory(new File(rootPomPath));
        generateProcess = generate.start();
        generateProcess.waitFor();
        log.info("[Dependency Track] Generated SBOM for {}", rootPomPath);
        return rootPomPath + File.separatorChar + "target" + File.separatorChar + "bom.xml";
    }

    /**
     * Send info to Mixeway about uploaded BOM. Mixeway after getting info is downloading vulnerabilities
     *
     */
    private void sendMixewayInfo(PrepareCIOperation prepareCIOperation, GitInformations gitInformations) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        InfoScanPerformed infoScanPerformed = new InfoScanPerformed(gitInformations.getBranchName(),gitInformations.getCommitId(),"opensource",prepareCIOperation.getCodeProjectId() );
        RestTemplate restTemplate = MRestTemplate.createRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(Constants.MIXEWAY_API_KEY, mixewayApiKey);
        HttpEntity<InfoScanPerformed> entity = new HttpEntity<>(infoScanPerformed, headers);
        ResponseEntity<String> response = restTemplate.exchange(mixewayUrl +
                Constants.MIXEWAY_URL_SCAN_PERFORMED, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK)){
            log.info("[Dependency Track] Mixeway informed");
        }
    }

    /**
     * Processing NPM project
     */
    private String processNpmDependencyTrackScan() throws IOException, InterruptedException {
       return buildBomForNPM();
    }

    /**
     * generation of bom.xml for NPM project
     */
    private String buildBomForNPM() throws IOException, InterruptedException {
        List<String> bomForPackagePath = new ArrayList<>();
        List<String> packages = FileUtils.listFiles(
            new File(sourcePath),
                new RegexFileFilter("package.json"),
                DirectoryFileFilter.DIRECTORY
        ).stream().map(File::getAbsolutePath).collect(Collectors.toList());

        List<String> packagesSorted =
            packages.stream()
                .sorted((f1, f2) -> Long.compare(f1.split(Pattern.quote(String.valueOf(File.separatorChar))).length, f2.split(Pattern.quote(String.valueOf(File.separatorChar))).length))
                .collect(Collectors.toList());

        packagesSorted.removeIf(path -> path.contains("node_modules"));
        packagesSorted.forEach(s -> {
            try {
                processFirstLevelNpmBomGeneration(s,bomForPackagePath);
            } catch (IOException | InterruptedException e) {
                log.error("[Dependency Track] Error during genration of SBOM - {}", e.getLocalizedMessage());
            }
        });

        concatenateAllBomsForNPM(bomForPackagePath);
        return sourcePath + File.separatorChar + "bom.xml";
    }

    /**
     * For multimodule NPM files with multiple packages.json.
     * Cyclone produce bom.xml for single package.json file
     * Method is concatenating all bom.xml into one
     * @param paths of bom.xml files
     */
    public void concatenateAllBomsForNPM(List<String> paths) throws IOException, InterruptedException {
            ProcessBuilder append;
            Process appendProcess;
            append = new ProcessBuilder("bash", "-c", "cyclonedx-bom -a " + StringUtils.join(paths,","));
            append.directory(new File(sourcePath));
            appendProcess = append.start();
            appendProcess.waitFor();
        log.info("[Dependency Track] SBOM appended all into one");
    }

    public void processFirstLevelNpmBomGeneration(String path, List<String> bomPaths) throws IOException, InterruptedException {
        path = path.split("package.json")[0];
        path = path.substring(0,path.length()-1);
        ProcessBuilder install, npmInstall, generate;
        Process installProcess, npmInstallProcess, generateProcess;
        install = new ProcessBuilder("bash", "-c", "npm install -g @cyclonedx/bom").inheritIO();
        install.environment().putAll(System.getenv());
        install.directory(new File(path));
        installProcess = install.start();
        installProcess.waitFor();
        log.info("[Dependency Track] Installed CycloneDX NPM for {}", path);
        npmInstall = new ProcessBuilder("bash", "-c", "npm install").inheritIO();
        npmInstall.environment().putAll(System.getenv());
        npmInstall.directory(new File(path));
        npmInstallProcess = npmInstall.start();
        npmInstallProcess.waitFor(5, TimeUnit.MINUTES);
        log.info("[Dependency Track] NPM install for {}", path);
        generate = new ProcessBuilder("bash", "-c", "cyclonedx-bom").inheritIO();
        generate.environment().putAll(System.getenv());
        generate.directory(new File(path));
        generateProcess = generate.start();
        generateProcess.waitFor();
        log.info("[Dependency Track] Generated SBOM for {}", path);
        bomPaths.add(path+File.separatorChar+"bom.xml");


    }

    @Override
    public void runScan(SourceCodeType sourceCodeType, String mixewayKey, String dTrackKey, Long projectId) {

    }

    @Override
    public boolean canProceedWithScan(SourceCodeType sourceCodeType) {
        return (sourceCodeType.equals(SourceCodeType.MVN) || sourceCodeType.equals(SourceCodeType.NPM) ||
                sourceCodeType.equals(SourceCodeType.PYTHON) || sourceCodeType.equals(SourceCodeType.PHP));
    }

    /**
     * Getting root path for pom (top level parent)
     */
    private String getPathOfRootPom() throws IllegalArgumentException{
        String rootPomPath = null;
        List<String> mvn = FileUtils.listFiles(
                new File(sourcePath),
                new RegexFileFilter("pom.xml"),
                DirectoryFileFilter.DIRECTORY
        ).stream().map(File::getAbsolutePath).collect(Collectors.toList());
        List<Integer> pomLengths = mvn.stream().map(pom -> pom.split(Pattern.quote(String.valueOf(File.separatorChar))).length).collect(Collectors.toList());
        int min = pomLengths.stream().min(Integer::compareTo).get();
        List<String> rootPom = mvn.stream()
                .filter(mvnPom ->
                        mvnPom.split(Pattern.quote(String.valueOf(File.separatorChar))).length == min)
                .collect(Collectors.toList());
        if (rootPom.size() == 1){
            rootPomPath = rootPom.get(0).split(Pattern.quote(File.separatorChar + "pom.xml"))[0];
            log.info("[Dependency Track] Got Root POM in directory of {}", rootPomPath);
            return rootPomPath;
        }else {
            throw new IllegalArgumentException("Multiple POMs on same level");
        }
    }
}
