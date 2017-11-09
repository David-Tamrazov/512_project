// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
//
package servercode.ResImpl;

import servercode.ResInterface.*;

import java.util.*;
import java.net.*;
import java.io.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RMISecurityManager;

public class SocketResourceManagerServer {

    public static void main(String args[]) {    
       
        // Figure out where the server is running
        String objName = "Gr17ResourceManager";
        int port = 1738;
        String host = "localhost";

        RMHashtable m_itemHT = new RMHashtable();


        if(args.length == 1) {
            port = Integer.parseInt(args[0]);
        } 
        
        
        try {
    
            // Setup a socket on port 
            ServerSocket server = new ServerSocket(port);

            
            System.out.println("Socket listening on port: " + port);
            System.err.println("Server ready");

            while(true) {                
                SocketResourceManagerWorker w = new SocketResourceManagerWorker(server.accept(), m_itemHT);
                Thread t = new Thread(w);
                t.run();
    
            }
             
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
    
    }
   

}
