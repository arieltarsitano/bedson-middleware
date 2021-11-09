package io.ctdev.bedson;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;

import java.util.*;

import java.io.InputStreamReader;

import javax.servlet.ServletOutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * This utility class implements a method that downloads a directory completely
 * from a FTP server, using Apache Commons Net API.
 */
public class FTPUtil {
    
    public static Map<String,Object> json = new HashMap<String,Object>();

    /**
     * Download a whole directory from a FTP server.
     * 
     * @param ftpClient  an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param parentDir  Path of the parent directory of the current directory being
     *                   downloaded.
     * @param currentDir Path of the current directory being downloaded.
     * @param saveDir    path of directory where the whole remote directory will be
     *                   downloaded and saved.
     * @throws IOException if any network or IO error occurred.
     */
    public static void downloadDirectory(FTPClient ftpClient, String parentDir, String currentDir,
            ServletOutputStream outputStream) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }

        FTPFile[] subFiles = ftpClient.listFiles(dirToList);

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                // tomamos el nombre del archivo o carpeta
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }

                // vamos armando las rutas para llegar hasta los archivos
                String filePath = parentDir + "/" + currentDir + "/" + currentFileName;
                if (currentDir.equals("")) {
                    filePath = parentDir + "/" + currentFileName;
                }

                // si es una carpeta, volvemos a entrar
                if (aFile.isDirectory()) {
                    json.putIfAbsent(currentFileName, new Object());
                    downloadDirectory(ftpClient, dirToList, currentFileName, outputStream);
                } else {
                    // si es archivo, lo recuperamos
                    //InputStream archivo = downloadSingleFile(ftpClient, filePath);
                    Boolean success = downloadSingleFile(ftpClient, filePath, outputStream);
                    if (success != null) {
                        //formatearResultado(archivo, filePath, outputStream);
                        System.out.println("DOWNLOADED the file: " + filePath);
                    } else {
                        System.out.println("COULD NOT download the file: " + filePath);
                    }
                }
            }
        }
    }

    /**
     * Download a single file from the FTP server
     * 
     * @param ftpClient      an instance of org.apache.commons.net.ftp.FTPClient
     *                       class.
     * @param remoteFilePath path of the file on the server
     * @param savePath       path of directory where the file will be stored
     * @return true if the file was downloaded successfully, false otherwise
     * @throws IOException if any network or IO error occurred.
     */
    public static Boolean downloadSingleFile(FTPClient ftpClient, String remoteFilePath, ServletOutputStream outputStream) throws IOException {

        // ServletOutputStream outputStream = output;
        // OutputStream outputStream = new ByteArrayOutputStream();
        try {
            // ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient.retrieveFile(remoteFilePath, outputStream);
            //return ftpClient.retrieveFileStream(remoteFilePath);
        } catch (IOException ex) {
            throw ex;
        } /*
           * finally { if (outputStream != null) { outputStream.close(); } }
           */
    }

    /*public static void formatearResultado(InputStream archivo, String parentDir, ServletOutputStream output){
        System.out.println("-- Parent dir: " + parentDir);
        String key = parentDir.substring(parentDir.indexOf("/Salesforce/"));
        System.out.println("-- Key: " + key);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(archivo));
            String linea = br.readLine();
            String[] headers = null;
            if(linea != null){
                //setear los headers
                headers = linea.split(";");
            }

            while((linea = br.readLine()) != null){
            }
        }
        catch(Exception e){

        }
    }*/
}