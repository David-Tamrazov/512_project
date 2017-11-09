package servercode.ResImpl;

import java.util.*;
import java.net.*;
import java.io.*;

public class SocketResourceManagerWorker implements Runnable {

    private Socket client; 
    private RMHashtable dbTable;
    private SocketResourceManager resourceManager;

    public SocketResourceManagerWorker(Socket client, RMHashtable dbTable) {
        setClient(client);
        this.dbTable = dbTable;
        this.resourceManager = new SocketResourceManager(this.dbTable);
    }

    public void run() {

        try {
        
        	BufferedReader streamFromMiddlewareServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
        	ObjectOutputStream streamToMiddlewareServer = new ObjectOutputStream(client.getOutputStream());
        	
        	streamToMiddlewareServer.writeObject(handleCommand(streamFromMiddlewareServer.readLine()));

        } catch(IOException e) {

            System.out.println("Client input or output failed: " + e);

            // NEEDS WORK: how to gracefully handle a failed connection w the MiddlewareSocketWorker without crashing the server
            System.exit(-1);
        }
        
    }

    public SocketResponse handleCommand(String clientCommand) {
		// parse the client command
		Vector arguments = parse(clientCommand.trim());
		
		return executeCommand(arguments);
    }

    private SocketResponse executeCommand(Vector arguments) {

        int Id, Cid;
        int flightNum;
        int flightPrice;
        int flightSeats;
        boolean Room;
        boolean Car;
        int price;
        int numRooms;
        int numCars;
        String location;

        //return "Executing your commands master.";    

        //decide which of the commands this was
        switch ((String) arguments.elementAt(0)) {
            
            /*
case "help": //help section

                if (arguments.size() == 1) //command was "help"
                    return listCommands();
                else if (arguments.size() == 2) //command was "help <commandname>"
                    return listSpecific((String) arguments.elementAt(1));
                else
                    return new SocketResponse(500, "Improper use of help command. Type help or help, <commandname>");
*/

            case "newflight": //new flight

                if (arguments.size() != 5) {
                    return wrongNumber();
                    
                }

                System.out.println("Adding a new Flight using id: " + arguments.elementAt(1));
                System.out.println("Flight number: " + arguments.elementAt(2));
                System.out.println("Add Flight Seats: " + arguments.elementAt(3));
                System.out.println("Set Flight Price: " + arguments.elementAt(4));

                try {

                    Id = getInt(arguments.elementAt(1));
                    flightNum = getInt(arguments.elementAt(2));
                    flightSeats = getInt(arguments.elementAt(3));
                    flightPrice = getInt(arguments.elementAt(4));

                    if (resourceManager.addFlight(Id, flightNum, flightSeats, flightPrice))
                        return new SocketResponse(200, "Flight added.");
                    else
                        return new SocketResponse(500, "Flight could not be added");

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();

                    return new SocketResponse(500, e.getMessage());
                }

                

            case "newcar": //new Car

                if (arguments.size() != 5) {
                    return wrongNumber();
                    
                }

                System.out.println("Adding a new Car using id: " + arguments.elementAt(1));
                System.out.println("Car Location: " + arguments.elementAt(2));
                System.out.println("Add Number of Cars: " + arguments.elementAt(3));
                System.out.println("Set Price: " + arguments.elementAt(4));

                try {

                    Id = getInt(arguments.elementAt(1));
                    location = getString(arguments.elementAt(2));
                    numCars = getInt(arguments.elementAt(3));
                    price = getInt(arguments.elementAt(4));

                    if (resourceManager.addCars(Id, location, numCars, price))
                        return new SocketResponse(200, "Cars added");
                    else
                        return new SocketResponse(500, "Cars could not be added");

                } catch (Exception e) {
                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();

                    return new SocketResponse(500, e.getMessage());
                }

                

            case "newroom": //new Room

                if (arguments.size() != 5) {
                    return wrongNumber();
                    
                }

                System.out.println("Adding a new Room using id: " + arguments.elementAt(1));
                System.out.println("Room Location: " + arguments.elementAt(2));
                System.out.println("Add Number of Rooms: " + arguments.elementAt(3));
                System.out.println("Set Price: " + arguments.elementAt(4));

                try {

                    Id = getInt(arguments.elementAt(1));
                    location = getString(arguments.elementAt(2));
                    numRooms = getInt(arguments.elementAt(3));
                    price = getInt(arguments.elementAt(4));

                    if (resourceManager.addRooms(Id, location, numRooms, price))
                        return new SocketResponse(200, "Rooms added");
                    else
                        return new SocketResponse(500, "Rooms could not be added");

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }

                

            case "newcustomer": //new Customer
            
                if (arguments.size() != 2) {
                    return wrongNumber();
                    
                }

                System.out.println("Adding a new Customer using id:" + arguments.elementAt(1));

                try {

                    Id = getInt(arguments.elementAt(1));
                    int customer = resourceManager.newCustomer(Id);
                    
                    System.out.println("CHECK THIS : " + customer);
                    
                    if(customer != 0) {
                    	return new SocketResponse(200, "Added new customer with id: " + customer);
                    } else {
	                    return new SocketResponse(500, "Could not make new customer.");
                    }

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }

                

            case "deleteflight": //delete Flight

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Deleting a flight using id: " + arguments.elementAt(1));
                System.out.println("Flight Number: " + arguments.elementAt(2));

                try {

                    Id = getInt(arguments.elementAt(1));
                    flightNum = getInt(arguments.elementAt(2));

                    if (resourceManager.deleteFlight(Id, flightNum))
                        return new SocketResponse(200, "Flight Deleted");
                    else
                        return new SocketResponse(500, "Flight could not be deleted");

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }

                

            case "deletecar": //delete Car

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Deleting the cars from a particular location  using id: " + arguments.elementAt(1));
                System.out.println("Car Location: " + arguments.elementAt(2));

                try {

                    Id = getInt(arguments.elementAt(1));
                    location = getString(arguments.elementAt(2));

                    if (resourceManager.deleteCars(Id, location))
                        return new SocketResponse(200, "Cars Deleted");
                    else
                        return new SocketResponse(500, "Cars could not be deleted");

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();

                    return new SocketResponse(500, e.getMessage());
                }

                

            case "deleteroom": //delete Room

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Deleting all rooms from a particular location  using id: " + arguments.elementAt(1));
                System.out.println("Room Location: " + arguments.elementAt(2));

                try {

                    Id = getInt(arguments.elementAt(1));
                    location = getString(arguments.elementAt(2));

                    if (resourceManager.deleteRooms(Id, location))
                        return new SocketResponse(200, "Rooms Deleted");
                    else
                        return new SocketResponse(500, "Rooms could not be deleted");

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }

                

            case "deletecustomer": //delete Customer

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Deleting a customer from the database using id: " + arguments.elementAt(1));
                System.out.println("Customer id: " + arguments.elementAt(2));

                try {

                    Id = getInt(arguments.elementAt(1));
                    int customer = getInt(arguments.elementAt(2));

                    if (resourceManager.deleteCustomer(Id, customer))
                        return new SocketResponse(200, "Customer Deleted");
                    else
                        return new SocketResponse(500, "Customer could not be deleted");

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }

                

            case "queryflight": //querying a flight

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Querying a flight using id: " + arguments.elementAt(1));
                System.out.println("Flight number: " + arguments.elementAt(2));

                try {
                    Id = getInt(arguments.elementAt(1));
                    flightNum = getInt(arguments.elementAt(2));
                    int seats = resourceManager.queryFlight(Id, flightNum);

                    return new SocketResponse(200, "Number of seats available:" + seats);
                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }

                

            case "querycar": //querying a Car Location

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Querying a car location using id: " + arguments.elementAt(1));
                System.out.println("Car location: " + arguments.elementAt(2));

                try {

                    Id = getInt(arguments.elementAt(1));
                    location = getString(arguments.elementAt(2));
                    numCars = resourceManager.queryCars(Id, location);

                    return new SocketResponse(200, "number of Cars at this location:" + numCars);

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();

                    return new SocketResponse(500, e.getMessage());
                }

                

            case "queryroom": //querying a Room location

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Querying a room location using id: " + arguments.elementAt(1));
                System.out.println("Room location: " + arguments.elementAt(2));

                try {

                    Id = getInt(arguments.elementAt(1));
                    location = getString(arguments.elementAt(2));
                    numRooms = resourceManager.queryRooms(Id, location);

                    return new SocketResponse(200, "number of Rooms at this location:" + numRooms);

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();

                    return new SocketResponse(500, e.getMessage());
                }
                

            case "querycustomer": //querying Customer Information

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Querying Customer information using id: " + arguments.elementAt(1));
                System.out.println("Customer id: " + arguments.elementAt(2));

                try {

                    Id = getInt(arguments.elementAt(1));
                    int customer = getInt(arguments.elementAt(2));
                    String bill = resourceManager.queryCustomerInfo(Id, customer);

                    return new SocketResponse(200, "Customer info:" + bill);

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();

                    return new SocketResponse(500, e.getMessage());
                }

                

            case "queryflightprice": //querying a flight Price

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Querying a flight Price using id: " + arguments.elementAt(1));
                System.out.println("Flight number: " + arguments.elementAt(2));

                try {

                    Id = getInt(arguments.elementAt(1));
                    flightNum = getInt(arguments.elementAt(2));
                    price = resourceManager.queryFlightPrice(Id, flightNum);

                    return new SocketResponse(200, "Price of a seat:" + price);

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();

                    return new SocketResponse(500, e.getMessage());
                }

                

            case "querycarprice": //querying a Car Price

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Querying a car price using id: " + arguments.elementAt(1));
                System.out.println("Car location: " + arguments.elementAt(2));

                try {

                    Id = getInt(arguments.elementAt(1));
                    location = getString(arguments.elementAt(2));
                    price = resourceManager.queryCarsPrice(Id, location);

                    return new SocketResponse(200, "Price of a car at this location:" + price);

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }

                

            case "queryroomprice": //querying a Room price

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Querying a room price using id: " + arguments.elementAt(1));
                System.out.println("Room Location: " + arguments.elementAt(2));

                try {

                    Id = getInt(arguments.elementAt(1));
                    location = getString(arguments.elementAt(2));
                    price = resourceManager.queryRoomsPrice(Id, location);

                    return new SocketResponse(200, "Price of Rooms at this location:" + price);

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }

                

            case "reserveflight": //reserve a flight

                if (arguments.size() != 4) {
                    return wrongNumber();
                    
                }

                System.out.println("Reserving a seat on a flight using id: " + arguments.elementAt(1));
                System.out.println("Customer id: " + arguments.elementAt(2));
                System.out.println("Flight number: " + arguments.elementAt(3));

                try {
                    Id = getInt(arguments.elementAt(1));
                    int customer = getInt(arguments.elementAt(2));
                    flightNum = getInt(arguments.elementAt(3));

                    if (resourceManager.reserveFlight(Id, customer, flightNum))
                        return new SocketResponse(200, "Flight Reserved");
                    else
                        return new SocketResponse(500, "Flight could not be reserved.");

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }

                

            case "reservecar": //reserve a car

                if (arguments.size() != 4) {
                    return wrongNumber();
                    
                }

                System.out.println("Reserving a car at a location using id: " + arguments.elementAt(1));
                System.out.println("Customer id: " + arguments.elementAt(2));
                System.out.println("Location: " + arguments.elementAt(3));

                try {

                    Id = getInt(arguments.elementAt(1));
                    int customer = getInt(arguments.elementAt(2));
                    location = getString(arguments.elementAt(3));

                    if (resourceManager.reserveCar(Id, customer, location))
                        return new SocketResponse(200, "Car Reserved");
                    else
                        return new SocketResponse(500, "Car could not be reserved.");

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }

                

            case "reserveroom": //reserve a room

                if (arguments.size() != 4) {
                    return wrongNumber();
                    
                }

                System.out.println("Reserving a room at a location using id: " + arguments.elementAt(1));
                System.out.println("Customer id: " + arguments.elementAt(2));
                System.out.println("Location: " + arguments.elementAt(3));

                try {
                    Id = getInt(arguments.elementAt(1));
                    int customer = getInt(arguments.elementAt(2));
                    location = getString(arguments.elementAt(3));

                    if (resourceManager.reserveRoom(Id, customer, location))
                        return new SocketResponse(200, "Room Reserved");
                    else
                        return new SocketResponse(500, "Room could not be reserved.");

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }


            case "itinerary": //reserve an Itinerary

                if (arguments.size() < 7) {
                    return wrongNumber();
                    
                }

                System.out.println("Reserving an Itinerary using id:" + arguments.elementAt(1));
                System.out.println("Customer id:" + arguments.elementAt(2));

                for (int i = 0; i < arguments.size() - 6; i++) {

                    System.out.println("Flight number" + arguments.elementAt(3 + i));
                    System.out.println("Location for Car/Room booking:" + arguments.elementAt(arguments.size() - 3));
                    System.out.println("Car to book?:" + arguments.elementAt(arguments.size() - 2));
                    System.out.println("Room to book?:" + arguments.elementAt(arguments.size() - 1));

                }

                try {

                    Id = getInt(arguments.elementAt(1));
                    int customer = getInt(arguments.elementAt(2));
                    Vector flightNumbers = new Vector();

                    for (int i = 0; i < arguments.size() - 6; i++) {
                        flightNumbers.addElement(arguments.elementAt(3 + i));
                    }

                    location = getString(arguments.elementAt(arguments.size() - 3));
                    Car = getBoolean(arguments.elementAt(arguments.size() - 2));
                    Room = getBoolean(arguments.elementAt(arguments.size() - 1));

                    if (resourceManager.itinerary(Id, customer, flightNumbers, location, Car, Room))
                        return new SocketResponse(200, "Itinerary Reserved");
                    else
                        return new SocketResponse(500, "Itinerary could not be reserved.");

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();

                    return new SocketResponse(500, e.getMessage());
                }

                

            case "quit": //quit the client

                if (arguments.size() != 1) {
                    return wrongNumber();
                    
                }

                return new SocketResponse(200, "Quitting client.");

            case "newcustomerid": //new Customer given id

                if (arguments.size() != 3) {
                    return wrongNumber();
                    
                }

                System.out.println("Adding a new Customer using id:" + arguments.elementAt(1) + " and cid " + arguments.elementAt(2));

                try {

                    Id = getInt(arguments.elementAt(1));
                    Cid = getInt(arguments.elementAt(2));
                    boolean customer = resourceManager.newCustomer(Id, Cid);
                    
                    if(customer) {
                    	return new SocketResponse(200, "Added new customer with id: " + Cid);
                    } else {
	                    return new SocketResponse(500, "Could not add new customer with id: " + Cid);
                    }

                } catch (Exception e) {

                    System.out.println("EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    
                    return new SocketResponse(500, e.getMessage());
                }

                

            default:
                return new SocketResponse(400, "The interface does not support this command.");
                
            }
    }

    public SocketResponse wrongNumber() {
        return new SocketResponse(400, "The Number of arguments provided in this command are wrong. Type help, <commandname> to check usage of this command.");
    }

    public int getInt(Object temp) throws Exception {
        try {
            return (new Integer((String) temp)).intValue();
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean getBoolean(Object temp) throws Exception {
        try {
            return (new Boolean((String) temp)).booleanValue();
        } catch (Exception e) {
            throw e;
        }
    }

    public String getString(Object temp) throws Exception {
        try {
            return (String) temp;
        } catch (Exception e) {
            throw e;
        }
    }

    private void setClient(Socket client) {
        this.client = client; 
    }
    
    public static Vector parse(String command) {
        Vector arguments = new Vector();
        StringTokenizer tokenizer = new StringTokenizer(command, ",");
        String argument = "";
        while (tokenizer.hasMoreTokens()) {
            argument = tokenizer.nextToken();
            argument = argument.trim();
            arguments.add(argument);
        }
        return arguments;
    }
}