package servercode.ResImpl;

import java.util.Vector;
import java.io.Serializable;

public class SocketResponse implements Serializable {

	private int statusCode;
	private String responseMessage;
	private String stackTrace;

	public SocketResponse(int statusCode, String message) {
		setStatusCode(statusCode);
		setMessage(message);
	}

	public SocketResponse(int statusCode, String message, String stackTrace) {
		setStatusCode(statusCode);
		setMessage(message);
		setStackTrace(stackTrace);
	}

	private void setStatusCode(int statusCode) {
	
		this.statusCode = statusCode;

		/*
switch(statusCode) {
			case 200:
			case 400:
			case 401:
			case 404:
			case 422:
			case 500:
				this.statusCode = statusCode;
			default:
				this.statusCode = 0;
				
		}
*/
	}

	private void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	private void setMessage(String message) {
		this.responseMessage = message;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public String getResponseMessage() {
		return responseMessage;
	}
	
	public Vector toVector() {
		Vector socketResponse = new Vector();
		socketResponse.add(new Integer(statusCode));
		socketResponse.add(responseMessage);
		return socketResponse;
	}
}