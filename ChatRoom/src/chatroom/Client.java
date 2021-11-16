/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author leonardozanotti
 */
public class Client implements Runnable {
    private Socket client;
    private BufferedReader input;
    private PrintWriter output;
    private boolean done;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 9999);
            output = new PrintWriter(client.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            
            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();
            
            String inMessage;
            while ((inMessage = input.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            shutdown();
        }
    }
    
    public void shutdown() {
        try {
            done = true;
            input.close();
            output.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message.equals("/quit")) {
                        output.println("/quit");
                        inReader.close();
                        shutdown();
                    } else {
                        output.println(message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }
    
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
