package io.ctdev.bedson;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class TestWrite extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println(
                "Please, use post request with next headers: server, port, user, pass, filedir (with name of file and extension).");
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        FTPClient ftpClient = new FTPClient();
        try {
            String server = request.getHeader("server");
            int port = Integer.parseInt(request.getHeader("port"));
            String user = request.getHeader("user");
            String pass = request.getHeader("pass");
            String fileName = request.getHeader("filename");
            String body = request.getHeader("body");

            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalActiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            response.setContentType("application/octet-stream");
            ServletOutputStream stream = response.getOutputStream();
            String initialString = body;
            InputStream inStream = new ByteArrayInputStream(initialString.getBytes());
            ftpClient.storeFile(fileName, inStream);
            inStream.close();
            stream.close();
        } catch (Exception e) {
            PrintWriter out = response.getWriter();
            out.println("Error is " + e.getMessage());
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }
}
