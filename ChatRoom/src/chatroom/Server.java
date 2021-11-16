/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author leonardozanotti
 */
public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private Integer port = 9999;
    
    public Server() {
        connections = new ArrayList<ConnectionHandler>();
        done = false;
    }
    
    @Override
    public void run() {
        try {
            server = new ServerSocket(port);
            pool = Executors.newCachedThreadPool();
            System.out.println("Server running on port " + port);
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }
    
    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }
    
    public void shutdown() {
        try {
            if (!server.isClosed()) {
                done = true;
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader input;
        private PrintWriter output;
        private String nickname;
        
        public ConnectionHandler(Socket client) {
            this.client = client;
        }
        
        @Override
        public void run() {
            try {
                output = new PrintWriter(client.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                output.println("Please enter a nickname: ");
                nickname = input.readLine();
                System.out.println(nickname + " connected!");
                broadcast(nickname + " joined the chat!");
                String message;
                while ((message = input.readLine()) != null) {
                    if (message.startsWith("/nick ")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed himself to " + messageSplit[1]);
                            System.out.println(nickname + " renamed himself to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            output.println("Successfully changed nickname to " + nickname);
                        } else {
                            output.println("No nickname provided!");
                        }
                    } else if (message.startsWith("/quit")) {
                        broadcast(nickname + " left the chat!");
                        shutdown();
                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
        
        public void sendMessage(String message) {
            output.println(message);
        }
        
        public void shutdown() {
            try {
                input.close();
                output.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
