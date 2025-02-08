package org.osgomez;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    public HttpRequest(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            requestProcess();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void requestProcess() throws Exception {
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line;
        while ((line = input.readLine()) != null && !line.isEmpty()) {
            StringTokenizer parseLine = new StringTokenizer(line);
            String method = parseLine.nextToken();
            System.out.println("Request: " + line);
            if (method.equals("GET")) {
                String fileName = parseLine.nextToken();
                String regex = "^\\s*/\\s*$";

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(fileName);

                if (matcher.matches()) {
                    fileName = "/index.html";
                }

                fileName = "." + fileName;
                System.out.println(fileName);
                InputStream inputStream = ClassLoader.getSystemResourceAsStream(fileName);
                File file;

                String lineOfState;
                String lineHeader;
                String lineBodyLength;
                String status;
                long filesize;

                if (inputStream != null) {
                    status = "200 OK";
                    file = new File(ClassLoader.getSystemResource(fileName).toURI());
                } else {
                    status = "404 Not Found";
                    fileName = "./404.html";
                    inputStream = ClassLoader.getSystemResourceAsStream("./404.html");
                    file = new File(ClassLoader.getSystemResource("./404.html").toURI());
                }

                filesize = file.length();
                lineOfState = "HTTP/1.1 " + status + CRLF;
                lineHeader = "Content-Type: " + contentType(fileName) + CRLF;
                lineBodyLength = "Content-Length: " + filesize + CRLF;

                sendString(lineOfState, socket.getOutputStream());
                sendString(lineHeader, socket.getOutputStream());
                sendString(lineBodyLength, socket.getOutputStream());
                sendString(CRLF, socket.getOutputStream());
                sendBytes(inputStream, socket.getOutputStream());

                output.flush();
                break;
            }
        }

        output.close();
        input.close();
        socket.close();
    }

    private void sendString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private void sendBytes(InputStream fis, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }
}
