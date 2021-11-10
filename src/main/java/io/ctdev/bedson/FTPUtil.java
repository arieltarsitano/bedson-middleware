package io.ctdev.bedson;

import java.io.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONObject;

/**
 * This utility class implements a method that downloads a directory completely
 * from a FTP server, using Apache Commons Net API.
 */
public class FTPUtil {
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
    ByteArrayOutputStream outputStream) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }

        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
        //System.out.println("Cantidad de archivos en " + dirToList + ": " + subFiles.length);

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
                    //System.out.println("Es una carpeta: " + currentFileName);
                    downloadDirectory(ftpClient, dirToList, currentFileName, outputStream);
                } else {
                    // si es archivo, lo recuperamos
                    //InputStream archivo = downloadSingleFile(ftpClient, filePath);
                    //System.out.println("Es un archivo: " + currentFileName);
                    ByteArrayOutputStream outArchivo = new ByteArrayOutputStream();
                    boolean success = downloadSingleFile(ftpClient, filePath, outArchivo);
                    if (success == true) {
                        InputStream archivo = new ByteArrayInputStream(outArchivo.toByteArray());
                        formatearResultado(archivo, currentDir, outputStream);
                        System.out.println("DOWNLOADED the file: " + filePath);
                        outArchivo.close();
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
    public static boolean downloadSingleFile(FTPClient ftpClient, String remoteFilePath,
    ByteArrayOutputStream output) throws IOException {

        try {
            return ftpClient.retrieveFile(remoteFilePath, output);
            //return ftpClient.retrieveFileStream(remoteFilePath);

        } catch (IOException ex) {
            throw ex;
        }
    }

    public static boolean formatearResultado(InputStream archivo, String filePath, ByteArrayOutputStream output) {
        System.out.println("-- File path: " + filePath);
        String key = filePath.replace("/Salesforce/", "");
        System.out.println("-- Key: " + key);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(archivo));
            String primera = br.readLine();
            String[] headers = null;
            if (primera != null) {
                // setear los headers
                headers = primera.split(";");
            }

            LinkedList<Map<String, String>> result = new LinkedList<Map<String, String>>();
            String linea = null;
            int columnas = headers != null ? headers.length : 0;
            JSONObject json = new JSONObject();
            

            while ((linea = br.readLine()) != null) {
                Map<String, String> res = new HashMap<>();
                for (int i = 0; i < columnas; i++) {
                    String[] datos = linea.split(";");
                    res.put(headers[i], datos[i]);
                }
                result.add(res);
            }
            json.put(key, result);
            String body = json.toString();

            System.out.println(body);
            output.write(body.getBytes());
            return true;

        } catch (Exception e) {
            System.out.println("-- Error: " + e.getMessage());
            return false;
        }
    }
}