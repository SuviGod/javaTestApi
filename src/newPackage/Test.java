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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Object;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


@SuppressWarnings("unused")
public class Test {
	private static String fileName = "emails.txt";
	
	public static void sendEmails() throws URISyntaxException, IOException, InterruptedException {
        File file = new File(fileName);
        if (!file.exists()) return;
        ArrayList <String> toEmails = new ArrayList<String>();
        Scanner reader;
		
		reader = new Scanner(file);
		
        
		while(reader.hasNextLine()) {
			toEmails.add(reader.nextLine());
		}
		reader.close();
		if (toEmails.isEmpty()) return;
        
        String fromEmail = "sulima.ivan@lll.kpi.ua";

        String host = "smtp.gmail.com";

        Properties properties = System.getProperties();

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(fromEmail, "wsebnjqprptimnxe");

            }

        });

        session.setDebug(true);

        try {
            MimeMessage message = new MimeMessage(session);
            
            for(int i = 0; i < toEmails.size(); i++) {
            	message.setFrom(new InternetAddress(fromEmail));

                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmails.get(i)));

                message.setSubject("Rate is comming!");

                message.setText(getRate());

                System.out.println("sending...");
                Transport.send(message);
                System.out.println("Sent message successfully....");
            }
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }

    }
	
	private static void sendEmailsPrimary(HttpExchange exchange) throws IOException, URISyntaxException, InterruptedException {
		sendEmails();
		exchange.sendResponseHeaders(200, 0);
		OutputStream os = exchange.getResponseBody();
		os.close();
	}
	private static String getRate() throws URISyntaxException, IOException, InterruptedException {
		  
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
	
	private static void handler(HttpExchange exchange) throws IOException, URISyntaxException, InterruptedException {
		if ("GET".equals(exchange.getRequestMethod())) {
			if("/rate".equals(exchange.getRequestURI().toString())){
				rate(exchange);
			}
		}else if ("POST".equals(exchange.getRequestMethod())) {
			if("/subscribe".equals(exchange.getRequestURI().toString())){
				subscribe(exchange);
			}else if("/sendEmails".equals(exchange.getRequestURI().toString())){
				sendEmailsPrimary(exchange);
			}
		}
	}

	public static void main(String[] args){
		
		HttpServer server;
	    try {
			  server = HttpServer.create(new InetSocketAddress(8000), 0);
			  server.createContext("/", exchange -> {
				try {
					handler(exchange);
				} catch (URISyntaxException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			  server.start();
	    } catch (IOException e) {
	    
	    e.printStackTrace();
	    }
		
	}

}
