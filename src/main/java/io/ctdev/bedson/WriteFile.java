package io.ctdev.bedson;

import java.io.*;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class WriteFile extends HttpServlet {
    private static final long serialVersionUID = 1L;
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println(
                "Please, use post request with next headers: server, port, user, pass, filedir (with name of file and extension).     :D");
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

            byte[] inicial = Base64.getDecoder().decode(body);

            System.out.println("Body string:");
            String b = new String(inicial);
            System.out.println(b);

            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setControlEncoding("UTF-8");

            response.setContentType("application/octet-stream");
            InputStream inStream = new ByteArrayInputStream(inicial);

            Boolean status = ftpClient.storeFile(fileName, inStream);
            response.setStatus(ftpClient.getReplyCode());
            response.getWriter().write(ftpClient.getReplyString());
            response.getWriter().flush();
            inStream.close();
        } catch (Exception e) {
            PrintWriter out = response.getWriter();
            out.println("Error is " + e.getMessage());
            response.setStatus(400);
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }
}
