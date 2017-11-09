package ResImpl;

import ResInterface.*;

import java.util.*;
import java.net.*;
import java.io.*;

public class SocketMiddlewareWorker implements Runnable {

	String clientCommand;
	//////

    private Socket client; 
    private String carManager; 
    private String roomManager; 
    private String flightManager; 
    
    int resourceManagerPort = 1738;
    private static Map<String, String> rmHostnames; 

    public SocketMiddlewareWorker(Socket client) {
        setClient(client);
    }

    public SocketMiddlewareWorker(Socket client, String rmHostOne, String rmHostTwo, String rmHostThree) {
        setClient(client);

        rmHostnames = new HashMap<String, String>();
        rmHostnames.put("Car", rmHostOne);
        rmHostnames.put("Flight", rmHostTwo);
        rmHostnames.put("Room", rmHostThree);
    }


    public void run() {

        BufferedReader clientInput = null;
        ObjectOutputStream streamToClient = null; 
        boolean running = false; 

        // Attempt to set up the socket server-client connection 
        try {

            // Input stream to 
            InputStreamReader inputStream = new InputStreamReader(client.getInputStream());

            // Buffered reader for reading command line input from the client
            clientInput = new BufferedReader(inputStream);

            // Object output stream for writing responses to the client
            streamToClient = new ObjectOutputStream(client.getOutputStream());
            running = true;

            System.out.println("Client connected to socket worker.");
    
        } catch(IOException e) {
            System.out.println("Client input or output failed.");
        }

        
        while (running) {
            
            try {

                String command = clientInput.readLine();
                clientCommand = command;
                //////

                if (command == null) {
                    running = false;
                } 

                System.out.println(command);
                // handle the command- forward to appropriate resource manager 
                SocketResponse response = handleCommand(command);

                // return the response
                // if the client closed the connection, an IOException will be thrown and running will be set to false
                streamToClient.writeObject(response.toVector());

                // if the response code is 201, the command was quit and we should close the socket connection now
                running = response.getStatusCode() != 201;
               

            } catch (IOException e) {
                System.out.println("Connection failed.");
                running = false;
            }

        }

        System.out.println("Closing down the socket connection.");
        
        try {
        	client.close();
        } catch(Exception E) {
        	System.exit(-1);
        }
    }

    private SocketResponse handleCommand(String clientCommand) {

        Vector arguments = parse(clientCommand);

        // switch on whichever resource the client is requesting &
        // re-route to appropriate resource manager 
        switch((String) arguments.elementAt(0)) {
            
            case "help":
                return new SocketResponse(200, new Command(clientCommand).isHelp());
            case "newcar":
            case "deletecar":
            case "querycar":
            case "reservecar":
            case "querycarprice":
                return forwardToResourceManager("Car", clientCommand);
            case "newflight":
            case "deleteflight":
            case "queryflight":
            case "reserveflight":
            case "queryflightprice":
                return forwardToResourceManager("Flight", clientCommand);
            case "newroom":
            case "deleteroom":
            case "queryroom":
            case "reserveroom":
            case "queryroomprice":
                return forwardToResourceManager("Room", clientCommand);
            case "newcustomer":
            case "deletecustomer":
            case "querycustomer":
            case "newcustomerid":
            	return forwardToAllManagers(clientCommand, arguments);
            case "itinerary": 
            	return reserveItinerary(arguments);
            case "quit":
                return new SocketResponse(201, "Quitting the client now.");
            default:
                return forwardToResourceManager("Car", clientCommand);

        }
    }
    
    private SocketResponse forwardToAllManagers(String clientCommand, Vector arguments) {
    
    	SocketResponse carManagerResponse;
	    SocketResponse flightManagerResponse;
	    SocketResponse roomManagerResponse;
	        
    	if(arguments.elementAt(0).equals("newcustomer") && arguments.size() == 2) {
	    	carManagerResponse = forwardToResourceManager("Car", clientCommand);
/* 	    	flightManagerResponse = forwardToResourceManager("Flight", clientCommand); */
/* 		    roomManagerResponse = forwardToResourceManager("Room", clientCommand); */

			String id = carManagerResponse.getResponseMessage().split(" ")[carManagerResponse.getResponseMessage().split(" ").length - 1];
			
			flightManagerResponse = forwardToResourceManager("Flight", "newcustomerid," + arguments.elementAt(1) + "," + id);
 	    	roomManagerResponse = forwardToResourceManager("Room", "newcustomerid," + arguments.elementAt(1) + "," + id);
 	    	
    	} else if(arguments.elementAt(0).equals("querycustomer") && arguments.size() == 3) {
    		
	    	carManagerResponse = forwardToResourceManager("Car", clientCommand);
    		flightManagerResponse = forwardToResourceManager("Flight", clientCommand);
    		roomManagerResponse = forwardToResourceManager("Room", clientCommand);
    	
	    	return new SocketResponse(200, "\nCar " + carManagerResponse.getResponseMessage() + "\nFlight " + flightManagerResponse.getResponseMessage() + "\n Room " + roomManagerResponse.getResponseMessage());
    	} else {
    
	    	carManagerResponse = forwardToResourceManager("Car", clientCommand);
		    flightManagerResponse = forwardToResourceManager("Flight", clientCommand);
		    roomManagerResponse = forwardToResourceManager("Room", clientCommand);
		    	    
		    if (carManagerResponse.getStatusCode() == 200 &&  
	           	flightManagerResponse.getStatusCode() == 200 && 
	            roomManagerResponse.getStatusCode() == 200) {
	            
	            return roomManagerResponse;
		            		
	        }
	        
	        System.out.println("Car response: " + carManagerResponse.getResponseMessage());
	        System.out.println("Flight response: " + flightManagerResponse.getResponseMessage());
	        System.out.println("Room response: " + roomManagerResponse.getResponseMessage());
        }
        
        
        return roomManagerResponse;
			    
    }

    private SocketResponse forwardToResourceManager(String resource, String clientCommand) {  
    
       	SocketResponse response;
    	
    	try {
    		//Connect to appropriate resource manager
	    	Socket socket = new Socket(rmHostnames.get(resource), resourceManagerPort);
	    	
	    	PrintWriter sendToResourceManager = new PrintWriter(socket.getOutputStream(), true);
	    	ObjectInputStream receiveFromResourceManager = new ObjectInputStream(socket.getInputStream()); 
	    	
	    	sendToResourceManager.println(clientCommand);
	    	
	    	response = (SocketResponse) receiveFromResourceManager.readObject();
	 
    	} catch(IOException e) {
            System.out.println("Error connecting to server: " + e);
            System.out.println("Aborting client program.");
            response = new SocketResponse(500, "Failed to connect to " + resource + " Manager: please try again later.");
        } catch(ClassNotFoundException e) {
            System.out.println("Improper response received from resource manager: " + e);
            response = new SocketResponse(500, "Improper response received from resource manager: " + e);
        }
        
        return response;
    	
    }
    
    private SocketResponse reserveItinerary(Vector arguments) {

        if (arguments.size() < 7) {
            return new SocketResponse(400, "You have not passed enough arguments. Please refer to the help command to learn how to use this function.");
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

            Object [] flights = getFlightsFromItinerary(arguments);

            int id = Integer.parseInt((String)arguments.elementAt(1));
            int customer = Integer.parseInt((String)arguments.elementAt(2));
            String location = (String) arguments.elementAt(arguments.size() - 3);

            int numCars  = Integer.parseInt((String)arguments.elementAt(arguments.size() - 2));
            boolean bookACar = numCars != 0;

            int numRooms = Integer.parseInt((String)arguments.elementAt(arguments.size() - 1));
            boolean bookARoom = numRooms != 0;

            boolean success = false; 

            if (bookACar) {

                String command = "reservecar,"+id+","+customer+","+location;

                for (int i = 0; i < numCars; i++) {
                    success = forwardToResourceManager("Car", command).getStatusCode() == 200;
                }

            }

            if (bookARoom) {
                String command = "reserveroom,"+id+","+customer+","+location;

                for (int i = 0; i < numCars; i++) {
                    success = forwardToResourceManager("Room", command).getStatusCode() == 200;
                }

            }
        
            for (Object flightNum: flights) {
    
                String command = "reserveflight,"+id+","+customer+","+Integer.parseInt((String)flightNum);
                success = forwardToResourceManager("Flight", command).getStatusCode() == 200;
    
            }

            if (success) {
                return new SocketResponse(200, "Succesfully reserved itinerary.");
            } else {
                return new SocketResponse(500, "Could not reserve itinerary.");
            }
           
        } catch (Exception e) {

            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();

            return new SocketResponse(500, e.getMessage());
        }
		
	}



    private Object [] getFlightsFromItinerary(Vector arguments) {
        Vector flightNumbers = new Vector();

        for (int i = 0; i < arguments.size() - 6; i++) {
            flightNumbers.addElement(arguments.elementAt(3 + i));
        }

        // int [] flights = new int[flightNumbers.size()];
        // flightNumbers.toArray(flights);

        Object[] flights = flightNumbers.toArray();

        return flights;
    }

    private void setClient(Socket client) {
        this.client = client;
    }

    private Vector parse(String command) {
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
