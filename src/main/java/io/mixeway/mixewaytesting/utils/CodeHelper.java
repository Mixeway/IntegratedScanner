/*
 * @created  2020-09-14 : 14:32
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting.utils;

import io.mixeway.mixewaytesting.git.GitHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CodeHelper {
    /**
     * Determine type of source code in given location - JAVA-MVN, JAVA-Gradle, NPM, PIP or PHP-COMPOSER
     *
     */
    public static SourceCodeType getSourceProjectTypeFromDirectory(String sourcePath){

        Collection tf = FileUtils.listFiles(
                new File(sourcePath),
                new RegexFileFilter(".*\\.tf"),
                DirectoryFileFilter.DIRECTORY
        ).stream().map(File::getName).collect(Collectors.toList());
        if (tf.size() > 2) {
            return SourceCodeType.TF;
        }
        Collection pom = FileUtils.listFiles(
                new File(sourcePath),
                new RegexFileFilter("pom.xml"),
                DirectoryFileFilter.DIRECTORY
        ).stream().map(File::getName).collect(Collectors.toList());
        if(pom.size() > 0){
            return SourceCodeType.MVN;
        }
        File gradle = new File(sourcePath + File.separatorChar + "build.sh.xml");
        File gradle2 = new File(sourcePath + File.separatorChar + "build.sh.gradle");
        if (gradle.exists() || gradle2.exists()) {
            return SourceCodeType.GRADLE;
        }
        Collection npm = FileUtils.listFiles(
                new File(sourcePath),
                new RegexFileFilter("package.json"),
                DirectoryFileFilter.DIRECTORY
        ).stream().map(File::getName).collect(Collectors.toList());
        if(npm.size() > 0){
            return SourceCodeType.NPM;
        }
        File composer = new File(sourcePath + File.separatorChar + "composer.json");
        if(composer.exists() || directoryContainsPhp(sourcePath)){
            return SourceCodeType.PHP;
        }
        Collection pip = FileUtils.listFiles(
                new File(sourcePath),
                new RegexFileFilter(".*\\.py"),
                DirectoryFileFilter.DIRECTORY
        ).stream().map(File::getName).collect(Collectors.toList());
        if (pip.size() > 3) {
            return SourceCodeType.PYTHON;
        }
        return SourceCodeType.UNKNOWN;
    }
    /**
     * Check if project is of given type
     */
    public static boolean isProjectOfSourceType(SourceCodeType sourceCodeType, String sourcePath){
        final Logger log = LoggerFactory.getLogger(CodeHelper.class);
        switch ( sourceCodeType){
            case MVN:
                Collection mvn = FileUtils.listFiles(
                        new File(sourcePath),
                        new RegexFileFilter("pom.xml"),
                        DirectoryFileFilter.DIRECTORY
                ).stream().map(File::getName).collect(Collectors.toList());
                return (mvn.size() > 0);
            case GRADLE:
                log.error("[Code Helper] Gradle not support in given version");
                break;
            case PYTHON:
                Collection pip = FileUtils.listFiles(
                        new File(sourcePath),
                        new RegexFileFilter(".*\\.py"),
                        DirectoryFileFilter.DIRECTORY
                ).stream().map(File::getName).collect(Collectors.toList());
                return pip.size()>3;
            case NPM:
                Collection npm = FileUtils.listFiles(
                        new File(sourcePath),
                        new RegexFileFilter("package.json"),
                        DirectoryFileFilter.DIRECTORY
                ).stream().map(File::getName).collect(Collectors.toList());
                return npm.size()>0;
            case PHP:
                File composer = new File(sourcePath + File.separatorChar + "composer.json");
                return (composer.exists() || directoryContainsPhp(sourcePath));
        }
        return false;
    }

    /**
     * Check if directory contains php files
     * @param projectLocation
     * @return
     */
    private static boolean directoryContainsPhp(String projectLocation) {
        Collection php = FileUtils.listFiles(
                new File(projectLocation),
                new RegexFileFilter(".*\\.php"),
                DirectoryFileFilter.DIRECTORY
        ).stream().map(File::getName).collect(Collectors.toList());
        return php.size() > 5;
    }

    /**
     * Method which preare build.sh.xml file to use CycloneDX plugin to generate SBOM
     *
     * @param gradle path to gradle file
     */
    private static void prepareGradle(File gradle) {
        //TODO edit of build.sh.xml
    }

}
