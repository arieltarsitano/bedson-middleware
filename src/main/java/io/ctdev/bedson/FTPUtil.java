package io.ctdev.bedson;

import java.io.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONObject;


public class FTPUtil {

    public static JSONObject json = new JSONObject();

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
                    // System.out.println("Archivo: " + currentFileName);
                    ByteArrayOutputStream outTemp = new ByteArrayOutputStream();
                    boolean success = downloadSingleFile(ftpClient, filePath, outTemp);
                    if (success == true) {
                        InputStream archivo = new ByteArrayInputStream(outTemp.toByteArray());
                        decodificar(currentFileName, archivo, outputStream);
                        System.out.println("Archivo descargado: " + filePath);
                    } else {
                        System.out.println("No se pudo descargar el archivo: " + filePath);
                    }
                }
            }
            outputStream.write(json.toString().getBytes());
            json.clear();
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
        } catch (IOException ex) {
            throw ex;
        }
    }


    public static void decodificar(String nombreArchivo, InputStream archivo, ByteArrayOutputStream outputStream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(archivo));
            String linea = "";
            String[] headers = br.readLine().split(";");
            int columnas = headers.length;
            
            JSONObject body = new JSONObject();
            int nroLinea = 1;
            while ((linea = br.readLine()) != null) {
                JSONObject line = new JSONObject();
                
                String[] data = linea.split(";", columnas);
                for(int i = 0; i < columnas; i++){
                    line.put(headers[i], data[i] == null || data[i].isEmpty()? "" : data[i]);
                }
                body.put(nroLinea + "", line);
                nroLinea++;
            }
            json.put(nombreArchivo, body);
        } catch (Exception e) {
            System.out.println(e.getMessage() + " | " + e.getStackTrace().toString());
        }
    }

    /**
     * Realiza el backup de un directorio completo del servidor FTP.
     * 
     * @param ftpClient  Instancia de FTPClient (clase org.apache.commons.net.ftp.FTPClient).
     * @param parentDir  Path del directorio padre.
     * @param currentDir Path del directorio actual.
     * @param backupDir Path de la carpeta backup de destino.
     * @throws IOException En caso de algún error de red o IO.
     */
    public static void backupFile(FTPClient ftpClient, String parentDir, String currentDir, String backupDir) throws IOException {
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
                    backupFile(ftpClient, parentDir, currentDir, backupDir);
                } else {
                    //lo movemos:
                    String pathBackup = backupDir + "/" + currentFileName;
                    boolean success = ftpClient.rename(filePath, pathBackup);
                    if (success == true) {
                        System.out.println("Archivo de backup generado: " + pathBackup);
                    } else {
                        System.out.println("No se pudo realizar backup del archivo: " + pathBackup);
                    }
                }
            }
        }
    }


    /**
     * Realiza el backup de un directorio completo del servidor FTP.
     * 
     * @param ftpClient  Instancia de FTPClient (clase org.apache.commons.net.ftp.FTPClient).
     * @param parentDir  Path del directorio padre.
     * @param currentDir Path del directorio actual.
     * @param backupDir Path de la carpeta backup de destino.
     * @throws IOException En caso de algún error de red o IO.
     */
    public static void errorFile(FTPClient ftpClient, String filePath, String backupDir) throws IOException {
        FTPFile aFile = ftpClient.listFiles(filePath)[0];

        if (aFile != null) {
            String currentFileName = aFile.getName();

            String pathBackup = backupDir + "/" + currentFileName;
            boolean success = ftpClient.rename(filePath, pathBackup);
            if (success == true) {
                System.out.println("Archivo de backup generado: " + pathBackup);
            } else {
                System.out.println("No se pudo realizar backup del archivo: " + pathBackup);
            }
        }
    }
}