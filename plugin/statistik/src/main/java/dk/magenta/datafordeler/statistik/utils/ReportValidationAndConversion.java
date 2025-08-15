package dk.magenta.datafordeler.statistik.utils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportValidationAndConversion {

    private static final String reportRegex = ".*?_([A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12})";

    public static boolean validateReportName(String reportName) {
        Pattern splitter = Pattern.compile(reportRegex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = splitter.matcher(reportName);
        return matcher.matches();
    }


    public static synchronized void convertFileToEncryptedZip(File reportName, ArrayList<File> filesToAdd, String password) throws ZipException {
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(CompressionMethod.DEFLATE); // set compression method to deflate compression
        parameters.setCompressionLevel(CompressionLevel.NORMAL);
        parameters.setEncryptFiles(true);
        parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
        parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
        try (ZipFile zipFile = new ZipFile(reportName, password.toCharArray())) {
            zipFile.addFiles(filesToAdd, parameters);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
