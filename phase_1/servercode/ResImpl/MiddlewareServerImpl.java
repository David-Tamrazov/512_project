package ResImpl;

import ResInterface.*;

import java.util.*;
import java.io.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RMISecurityManager;

public class MiddlewareServerImpl implements MiddlewareServer {
    
    protected RMHashtable m_itemHT = new RMHashtable();
    Map<String, ResourceManager> resourceManagers;

    public MiddlewareServerImpl(Map<String, ResourceManager> map) {
        resourceManagers = map;
    }

    public void connectToManagers(String [] activeManagers) throws RemoteException {
        String [] resources = new String [] { "car", "flight", "room" };

        for (int i = 0; i < activeManagers.length; i++) {

            try {
                                            
                Registry registry = LocateRegistry.getRegistry(activeManagers[i], 1738);
                System.out.println("Connected to the registry succesfully");

                this.resourceManagers.put(resources[i], (ResourceManager) registry.lookup("Gr17ResourceManager"));

            } catch (Exception e) {

                System.err.println("Server exception: " + e.toString());
                e.printStackTrace();
                System.exit(1);

            }
            
        }
    }

        
    // Create a new flight, or add seats to existing flight
    //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return this.getFlightManager().addFlight(id, flightNum, flightSeats, flightPrice);
    }


    
    public boolean deleteFlight(int id, int flightNum) throws RemoteException {
        return this.getFlightManager().deleteFlight(id, flightNum);
    }



    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int id, String location, int count, int price) throws RemoteException {
        return this.getRoomManager().addRooms(id, location, count, price);
    }

    // Delete rooms from a location
    public boolean deleteRooms(int id, String location) throws RemoteException {
        return this.getRoomManager().deleteRooms(id, location);
        
    }

    // Create a new car location or add cars to an existing location
    //  NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int id, String location, int count, int price) throws RemoteException {
        return this.getCarManager().addCars(id, location, count, price);
    }


    // Delete cars from a location
    public boolean deleteCars(int id, String location) throws RemoteException {
        return this.getCarManager().deleteCars(id, location);
    }



    // Returns the number of empty seats on this flight
    public int queryFlight(int id, int flightNum) throws RemoteException {
        return this.getFlightManager().queryFlight(id, flightNum);
    }

    // Returns the number of reservations for this flight. 
//    public int queryFlightReservations(int id, int flightNum)
//        throws RemoteException
//    {
//        Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") called" );
//        RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
//        if ( numReservations == null ) {
//            numReservations = new RMInteger(0);
//        } // if
//        Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") returns " + numReservations );
//        return numReservations.getValue();
//    }


    // Returns price of this flight
    public int queryFlightPrice(int id, int flightNum ) throws RemoteException {
        return this.getFlightManager().queryFlightPrice(id, flightNum);
    }


    // Returns the number of rooms available at a location
    public int queryRooms(int id, String location) throws RemoteException {
        return this.getRoomManager().queryRooms(id, location);
    }


    
    
    // Returns room price at this location
    public int queryRoomsPrice(int id, String location) throws RemoteException {
        return this.getRoomManager().queryRoomsPrice(id, location);
    }


    // Returns the number of cars available at a location
    public int queryCars(int id, String location) throws RemoteException {
        return this.getCarManager().queryCars(id, location);
    }


    // Returns price of cars at this location
    public int queryCarsPrice(int id, String location) throws RemoteException {
        return this.getCarManager().queryCarsPrice(id, location);
    }

    // Returns data structure containing customer reservation info. Returns null if the
    //  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
    //  reservations.
    

    // return a bill
    public String queryCustomerInfo(int id, int customerID) throws RemoteException {
        return "\n" + this.getCarManager().queryCustomerInfo(id, customerID) + "\n" +
		this.getFlightManager().queryCustomerInfo(id, customerID) + "\n" + 
    	this.getRoomManager().queryCustomerInfo(id, customerID);
    }

    // customer functions
    // new customer just returns a unique customer identifier
    
    public synchronized int newCustomer(int id) throws RemoteException {
    	int cid = this.getCarManager().newCustomer(id);
    	this.getFlightManager().newCustomer(id,cid);
    	this.getRoomManager().newCustomer(id,cid);
    	return cid;
    }

    // I opted to pass in customerID instead. This makes testing easier
    public synchronized boolean newCustomer(int id, int customerID) throws RemoteException {
        this.getCarManager().newCustomer(id, customerID);
    	this.getFlightManager().newCustomer(id, customerID);
    	return this.getRoomManager().newCustomer(id, customerID);
    }


    // Deletes customer from the database. 
    public synchronized boolean deleteCustomer(int id, int customerID) throws RemoteException {
	    this.getCarManager().deleteCustomer(id, customerID);
		this.getFlightManager().deleteCustomer(id, customerID);
		return this.getRoomManager().deleteCustomer(id, customerID);   
    }
    
    // Adds car reservation to this customer. 
    public boolean reserveCar(int id, int customerID, String location) throws RemoteException {
        return this.getCarManager().reserveCar(id, customerID, location);
    }


    // Adds room reservation to this customer. 
    public boolean reserveRoom(int id, int customerID, String location) throws RemoteException {
        return this.getCarManager().reserveRoom(id, customerID, location);
    }
    // Adds flight reservation to this customer.  
    public boolean reserveFlight(int id, int customerID, int flightNum) throws RemoteException {
        return this.getFlightManager().reserveFlight(id, customerID, flightNum);
    }
    
    // Reserve an itinerary 
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location, boolean Car, boolean Room) throws RemoteException {

        System.out.println("Reserving an Itinerary using id:" + id);
        System.out.println("Customer id:" + customer);


        try {

            boolean success = false; 

            for (Object flightNum: (Vector)flightNumbers) {
    
                if (!this.getFlightManager().reserveFlight(id, customer, Integer.parseInt((String)flightNum))) {
                    return false;
                }
    
            }

            if ((Car && !this.getCarManager().reserveCar(id, customer, location)) ||
                (Room && !this.getRoomManager().reserveRoom(id, customer, location))) {
                return false;
            }
    
            return true;
           
        } catch (Exception e) {

            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();

            return false;
        }
    }

    public void ping(String ping) throws RemoteException {
        System.out.println(ping);
    }

    public ResourceManager getCarManager() {
        return this.resourceManagers.get("car");
    }

    public ResourceManager getRoomManager() {
        return this.resourceManagers.get("room");
    }

    public ResourceManager getFlightManager() {
        return this.resourceManagers.get("flight");
    }
    

    public static void sayHey() {
        
        System.out.println("Hey there from the MiddlewareServer");
    }

    public static void main(String args[]) {

        // Figure out where server is running
        String objName = "Gr17MiddlewareServer";
        String server = "localhost";
        int port = 1738;

        String [] activeManagers = new String[3];


        // Production code 

        if(args.length == 3) {
            activeManagers[0] = args[0];
            activeManagers[1] = args[1];
            activeManagers[2] = args[2];
        } else {
            System.err.println ("Wrong usage");
            System.out.println("Usage: java ResImpl.MiddlewareServer [firstResourceManager] [secondResourceManager] [thirdResourceManager] ");
            System.exit(1);
        }

        // // Test with one machine

        try {
            // Instantiate a new middleware server object and bind it to the registry for the client to interface with 
            MiddlewareServerImpl obj = new MiddlewareServerImpl(new HashMap<String, ResourceManager>());
            MiddlewareServer mws = (MiddlewareServer) UnicastRemoteObject.exportObject(obj, 0);
            
            // Locate the registry            
            Registry registry;
            registry = LocateRegistry.getRegistry(server, port);

            // Bind the server to the registry
            registry.rebind(objName, mws);

            // Bind to the active resource managers passed to the server
            mws.connectToManagers(activeManagers);

            mws.getCarManager().ping("car");
            mws.getFlightManager().ping("flight");
            mws.getRoomManager().ping("room");
            System.out.println("Middleware server is ready.");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
             System.setSecurityManager(new RMISecurityManager());
        }


    }


}