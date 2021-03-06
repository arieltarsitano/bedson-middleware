package io.ctdev.bedson;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class ReadFile extends HttpServlet {
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
            String dir = request.getHeader("dir");

            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            response.setContentType("application/octet-stream");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            FTPUtil.downloadDirectory(ftpClient, dir, "", outputStream);

            System.out.println(new String(outputStream.toByteArray()));
            
            response.getOutputStream().write(outputStream.toByteArray());
            response.getOutputStream().close();
        } catch (Exception e) {
            PrintWriter out = response.getWriter();
            out.println("Error: " + e.getMessage());
            response.setStatus(500);
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }
}
