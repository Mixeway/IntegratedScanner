/*
 * @created  2020-09-14 : 12:54
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.scanner.factory;

import io.mixeway.mixewaytesting.utils.GitInformations;
import io.mixeway.mixewaytesting.utils.PrepareCIOperation;
import io.mixeway.mixewaytesting.utils.SourceCodeType;
import io.mixeway.mixewaytesting.utils.Vulnerability;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public interface SecurityScanner {

    void runScan() throws IOException, InterruptedException;
    List<Vulnerability> runScan(SourceCodeType sourceCodeType) throws IOException, InterruptedException;
    List<Vulnerability> runScan(SourceCodeType sourceCodeType, PrepareCIOperation prepareCIOperation, GitInformations gitInformations) throws InterruptedException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException;
    void runScan(SourceCodeType sourceCodeType, String mixewayKey, String dTrackKey, Long projectId) throws IOException, InterruptedException;
    boolean canProceedWithScan(SourceCodeType sourceCodeType);
}
