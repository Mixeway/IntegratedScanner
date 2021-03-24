/*
 * @created  2020-09-14 : 14:06
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.utils;

public class Constants {
    public static final String MIXEWAY_API_KEY = "apikey";
    public static final String MIXEWAY_GET_SCANNER_INFO_URL = "/v2/api/cicd/getscannerinfo";
    public static final String DEPENDENCYTRACK_APIKEY_HEADER = "X-Api-Key";
    public static final String DEPENDENCYTRACK_URL_UPLOAD_BOM = "/api/v1/bom";

    public static final String MIXEWAY_URL = "https://mixer.corpnet.pl";
    public static final String MIXEWAY_URL_SCAN_PERFORMED = "/v2/api/cicd/infoscanperformed";
    public static final String MIXEWAY_PUSH_VULN_URL = "/v2/api/cicd/loadvulnerabilities";
    public static final String MIXEWAY_URL_SAST_SCAN = "/v2/api/cicd/sast/performscan/codeproject";
    public static final String MIXEWAY_URL_LOAD_VULNS = "/v2/api/cicd/vulnerabilities";
    public static final String PACKAGE_SCAN = "packageScan";
    public static final String CODE_SCAN = "codeScanner";
    public static final String TERRAFORM_SCAN = "terraform";
    public static final String SEVERITY_HIGH = "High";
}
