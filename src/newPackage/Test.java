package newPackage;
 
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.json.*;

import com.sun.net.httpserver.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Object;
import java.util.ArrayList;
import java.util.Scanner;




@SuppressWarnings("unused")
public class Test {
	private static String fileName = "emails.txt";
	public static String getRate() throws URISyntaxException, IOException, InterruptedException {
		  
		  HttpClient client = HttpClient.newBuilder()
		      .version(Version.HTTP_2)
		      .followRedirects(Redirect.NORMAL)
		      .build();
		  
		  HttpRequest request = HttpRequest.newBuilder()
		     .uri(new URI("https://api.coinbase.com/v2/prices/spot?currency=UAH"))
		     .GET()
		     .timeout(Duration.ofSeconds(10))
		     .build();
		  
		  HttpResponse<String> response =
		          client.send(request, BodyHandlers.ofString());
		  
		  JSONObject obj = new JSONObject(response.body());
		  //double course = obj.getJSONObject("data").getDouble("amount");
		  //JSONObject newObj = new JSONObject();
		  //newObj.put("response", 200);
		  //newObj.put()
		  return Double.toString(obj.getJSONObject("data").getDouble("amount"));
		  
		  
	}
	
	private static void rate(HttpExchange exchange) throws IOException {
		String response = "";
		try {
			response = getRate();
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			  
			os.close();
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(500, 0);
			OutputStream os = exchange.getResponseBody();
			  
			os.close();
		}
	}
	@SuppressWarnings("resource")
	private static int addEmailToFile(String email) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		Scanner reader = new Scanner(file);
		while(reader.hasNextLine()) {
			String str = reader.nextLine();
			System.out.println("loh" + str);
			if (email.equals(str)) {
				System.out.println("allah" + str);
				reader.close();
				return 409;
			}
		}
		reader.close();
		
		FileWriter fileWriter = new FileWriter(fileName, true);
		fileWriter.write(email + "\n");
		fileWriter.close();
		return 200;
	}
	private static void subscribe(HttpExchange exchange) throws IOException{
			Headers requestHeaders = exchange.getRequestHeaders();
			int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));
			InputStream is = exchange.getRequestBody();
			byte [] requestBodyByteArray = new byte[contentLength];
			is.read(requestBodyByteArray);
			
			String requestBody = new String(requestBodyByteArray);
			String [] parts = requestBody.split("=");
			String email = parts[1];
			System.out.println(email);
			int statusCode = addEmailToFile(email);
			if (statusCode == 409) {
				exchange.sendResponseHeaders(statusCode, 0);
				OutputStream os = exchange.getResponseBody();
				os.close();
				return;
			}else {
				exchange.sendResponseHeaders(statusCode, email.getBytes().length);
			}
			OutputStream os = exchange.getResponseBody();
			os.write(email.getBytes());
			os.close();
	}
	
	private static void handlePostRequest(HttpExchange exchange) throws IOException{
	}
	
	private static void handler(HttpExchange exchange) throws IOException {
		if ("GET".equals(exchange.getRequestMethod())) {
			if("/rate".equals(exchange.getRequestURI().toString())){
				rate(exchange);
			}
		}else if ("POST".equals(exchange.getRequestMethod())) {
			if("/subscribe".equals(exchange.getRequestURI().toString())){
				subscribe(exchange);
			}else if("/sendEmails".equals(exchange.getRequestURI().toString())){
				
			}
		}
	}

	public static void main(String[] args){
		
		HttpServer server;
	    try {
			  server = HttpServer.create(new InetSocketAddress(8000), 0);
			  server.createContext("/", Test::handler);
			  server.start();
	    } catch (IOException e) {
	    
	    e.printStackTrace();
	    }
		
	}

}
