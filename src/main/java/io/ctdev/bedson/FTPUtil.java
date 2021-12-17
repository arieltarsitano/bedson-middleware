package io.ctdev.bedson;

import java.io.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.io.BufferedReader;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * This utility class implements a method that downloads a directory completely
 * from a FTP server, using Apache Commons Net API.
 */
public class FTPUtil {

    /**
     * Descarga un directorio completo del servidor FTP.
     * 
     * @param ftpClient  Instancia de FTPClient (clase org.apache.commons.net.ftp.FTPClient).
     * @param parentDir  Path del directorio padre del directorio que está siendo descargado.
     * @param currentDir Path del directorio actual que está siendo descargado.
     * @param outputStream OutputStream donde escribir los archivos.
     * @throws IOException En caso de algún error de red o IO.
     */
    public static void downloadDirectory(FTPClient ftpClient, String parentDir, String currentDir,
            ByteArrayOutputStream outputStream) throws IOException {
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
                    //System.out.println("Es una carpeta: " + currentFileName);
                    downloadDirectory(ftpClient, dirToList, currentFileName, outputStream);
                } else {
                    // si es archivo, lo recuperamos
                    // InputStream archivo = downloadSingleFile(ftpClient, filePath);
                    // System.out.println("Archivo: " + currentFileName);
                    ByteArrayOutputStream outTemp = new ByteArrayOutputStream();
                    boolean success = downloadSingleFile(ftpClient, filePath, outTemp);
                    if (success == true) {
                        InputStream archivo = new ByteArrayInputStream(outTemp.toByteArray());
                        decodificar(archivo, outputStream);
                        // InputStream archivo = new ByteArrayInputStream(outArchivo.toByteArray());
                        System.out.println("Archivo descargado: " + filePath);
                    } else {
                        System.out.println("No se pudo descargar el archivo: " + filePath);
                    }
                }
            }
        }
    }

    /**
     * Descarga un archivo individual del servidor FTP.
     * 
     * @param ftpClient      Instancia del FTPClient (clase org.apache.commons.net.ftp.FTPClient).
     * @param remoteFilePath Path del archivo en el servidor.
     * @param output         OutputStream donde escribir el archivo.
     * @return true si el archivo se descargó correctamente, de lo contrario, false.
     * @throws IOException En caso de algún error de red o IO.
     */
    public static boolean downloadSingleFile(FTPClient ftpClient, String remoteFilePath, ByteArrayOutputStream output)
            throws IOException {
        try {
            return ftpClient.retrieveFile(remoteFilePath, output);
            // return ftpClient.retrieveFileStream(remoteFilePath);
        } catch (IOException ex) {
            throw ex;
        }
    }

    public static void decodificar(InputStream archivo, OutputStream outputStream) {
        String charset = "ISO-8859-1" ; //"UTF-8";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(archivo, Charset.forName(charset)));
            //BufferedReader br = new BufferedReader(new InputStreamReader(archivo));
            String linea, headers;
            char enter = '\n';
            headers = br.readLine();
            outputStream.write(headers.getBytes());
            // leemos las filas
            while ((linea = br.readLine()) != null) {
                outputStream.write(enter);
                outputStream.write(linea.getBytes());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " | " + e.getStackTrace().toString());
        }
    }
}