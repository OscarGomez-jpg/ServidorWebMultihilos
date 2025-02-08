package org.osgomez;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        int port  = 8080;
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            System.out.println("WebServer waiting for connection");
            Socket socket = serverSocket.accept();
            System.out.println("Connection accepted");

            HttpRequest newClient = new HttpRequest(socket);
            Thread thread = new Thread(newClient);
            thread.start();
        }
    }
}