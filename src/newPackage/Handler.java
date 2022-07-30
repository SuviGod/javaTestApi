package newPackage;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Handler implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) {
		//String response = "Hi there!";
		//exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
		//OutputStream os = exchange.getResponseBody();
		//os.write(response.getBytes());
		//System.out.println(exchange.getHttpContext());
		  
		//os.close();
	}
	
}
