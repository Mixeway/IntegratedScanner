/*
 * @created  2020-10-15 : 10:24
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.config;

import io.mixeway.mixewaytesting.scanner.integrations.Mixeway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ConfigCheck {
    final Logger log = LoggerFactory.getLogger(ConfigCheck.class);
    @Value("${secret.mixeway.key}")
    String mixewayApiKey;
    @Value("${dtrack.url}")
    String dTrackUrl;

    @Value("${mixeway.url}")
    String mixewayUrl;
    @Value("${source.path}")
    String sourcePath;
    @Value("${branch.name}")
    String branch;

    @PostConstruct
    private void configureSSL() {
        log.info("Configuration check - Mieway URL: {}", mixewayUrl);
        log.info("Configuration check DTrack URL: {}", dTrackUrl);
        log.info("Configuration check Mixeway API Key: {}", mixewayApiKey!=null && mixewayApiKey.length()>0 ?"OK": "Not Set");
        log.info("Configuration check Source Path: {}", sourcePath);
        log.info("Configuration check Branch: {}", branch.equals("git") ? "Taken fro HEAD" : branch);

    }
}
