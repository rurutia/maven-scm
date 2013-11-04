package com.cbs.test;

import java.net.*;
import java.io.*;
public class Server {
    static DataOutputStream out;
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8112);
            System.out.println("Server Started");
            while(true) {
            Socket socket = serverSocket.accept();
            System.out.println("A client connected");
            out = new DataOutputStream(socket.getOutputStream());

            //Send a simple-as-can-be handshake encoded with UTF-8
            String handshake = "HTTP/1.1 101 Web Socket Protocol Handshake\r" +
            "Upgrade: WebSocket\r" +
            "Connection: Upgrade\r" +
            "WebSocket-Origin: http://localhost\r" +
            "WebSocket-Location: ws://localhost:8112/\r" +
            "WebSocket-Protocol: sample\r\n\r\n";
            out.write(handshake.getBytes("UTF8"));
            System.out.println("Handshake sent.");

            //Send message 'HI!' encoded with UTF-8
            String message = "HI!";
            out.write(0x00);
            out.write(message.getBytes("UTF8"));
            out.write(0xff);
            System.out.println("Message sent!");

            //Cleanup
            socket.close();
            out.close();
            System.out.println("Everything closed!");
        }
    } catch(Exception e) {
        System.out.println(e.getMessage());
    }
}

}