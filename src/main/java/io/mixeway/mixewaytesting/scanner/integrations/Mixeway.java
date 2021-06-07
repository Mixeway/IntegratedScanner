/*
 * @created  2020-09-14 : 13:52
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.scanner.integrations;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mixeway.mixewaytesting.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Component
public class Mixeway {
    final Logger log = LoggerFactory.getLogger(Mixeway.class);
    @Value("${secret.mixeway.key}")
    String mixewayKey;
    @Value("${mixeway.url}")
    String mixewayUrl;

    int tries = 0;

    /**
     * Geting information about OpenSource scan infos
     */
    public PrepareCIOperation getCIInfo(GetInfoRequest getInfoRequest) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, InterruptedException {
        RestTemplate restTemplate = MRestTemplate.createRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(Constants.MIXEWAY_API_KEY, mixewayKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GetInfoRequest> entity = new HttpEntity<>(getInfoRequest,headers);
        try {
        ResponseEntity<PrepareCIOperation> response = restTemplate.exchange(mixewayUrl +
                        Constants.MIXEWAY_GET_SCANNER_INFO_URL,
                HttpMethod.POST, entity, PrepareCIOperation.class);
                   return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e){
            tries++;
            log.error("[Mixeway] Cannot get info for Mixeway configuration... try {} ... reason - {}",tries, e.getLocalizedMessage());
            if (tries < 4){
                Thread.sleep(3000);
                return getCIInfo(getInfoRequest);
            }
        }
        return null;
    }

    /**
     * Send GitLeaks vuln to Mixeway
     */
    public void sendVulns(GitInformations gitInformations, PrepareCIOperation prepareCIOperation, List<Vulnerability> vulns) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, JsonProcessingException {
        RestTemplate restTemplate = MRestTemplate.createRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(Constants.MIXEWAY_API_KEY, mixewayKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<Vulnerability>> entity = new HttpEntity<>(vulns,headers);
        try {
            ResponseEntity<Status> response = restTemplate.exchange(mixewayUrl +
                            Constants.MIXEWAY_PUSH_VULN_URL
                            + "/" + prepareCIOperation.getProjectId()
                            + "/" + gitInformations.getProjectName() + "/" + gitInformations.getBranchName() + "/" + gitInformations.getCommitId(),
                    HttpMethod.POST, entity, Status.class);
        } catch (HttpServerErrorException | HttpClientErrorException e){
            log.error("[Mixeway] Cannot send results to Mixeway. Reaseon - {}", e.getLocalizedMessage());
        }
    }

    /**
     * Starting scan for repository
     */
    public void startSastScanForRepo(PrepareCIOperation prepareCIOperation) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestTemplate restTemplate = MRestTemplate.createRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(Constants.MIXEWAY_API_KEY, mixewayKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        try {
            ResponseEntity<Status> response = restTemplate.exchange(mixewayUrl +
                            Constants.MIXEWAY_URL_SAST_SCAN + "/" +prepareCIOperation.getCodeProjectId(),
                    HttpMethod.GET, entity, Status.class);
        } catch (HttpServerErrorException | HttpClientErrorException e){
            log.error("[Mixeway] Cannot Start SAST Scan - {}", e.getLocalizedMessage());
        }
    }

    /**
     * Loading vulnerabilities for project
     * @param rootOperation
     */
    public MixewaySecurityGatewayResponse loadVulnerabilities(PrepareCIOperation rootOperation) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestTemplate restTemplate = MRestTemplate.createRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(Constants.MIXEWAY_API_KEY, mixewayKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);
        try {
            ResponseEntity<MixewaySecurityGatewayResponse> response = restTemplate.exchange(mixewayUrl +
                            Constants.MIXEWAY_URL_LOAD_VULNS + "/" +rootOperation.getCodeProjectId(),
                    HttpMethod.GET, entity, MixewaySecurityGatewayResponse.class);
            return response.getBody();
        } catch (HttpServerErrorException | HttpClientErrorException e){
            log.error("[Mixeway] Cannot Start SAST Scan - {}", e.getLocalizedMessage());
        }
        return null;
    }
}
