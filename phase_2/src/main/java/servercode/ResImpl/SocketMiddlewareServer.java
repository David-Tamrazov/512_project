package servercode.ResImpl;

import servercode.ResInterface.*;

import java.util.*;
import java.io.*;
import java.net.*;


public class SocketMiddlewareServer {
	
	private static final int port = 1090;
	
    public static void main(String args[]) {

        String [] activeManagers = new String[3];

        // Production code 

        if(args.length == activeManagers.length) {
            activeManagers[0] = args[0];
            activeManagers[1] = args[1];
            activeManagers[2] = args[2];
        } else {
            System.err.println ("Wrong usage of socket server.");
            System.out.println("Usage: java ResImpl.SocketMiddlewareServer [firstResourceManager] [secondResourceManager] [thirdResourceManager]");
            System.exit(1);
        }
       
        try {
        
            ServerSocket server = new ServerSocket(port);
 
            System.out.println("SocketMiddlewareServer connected to port " + port);

            while(true) {

                SocketMiddlewareWorker w;
    
                try {
                    w = new SocketMiddlewareWorker(server.accept(), activeManagers[0], activeManagers[1], activeManagers[2]);
                    Thread t = new Thread(w);
                    t.start();
                } catch (IOException e) {
                    System.out.println("Accept failed: 1738");
                    System.exit(-1);
                }

            }

        } catch(IOException e) {
            System.out.println("Could not listen on port 1090");
            System.out.println(e);
            System.exit(-1);
        }

        

    }


}