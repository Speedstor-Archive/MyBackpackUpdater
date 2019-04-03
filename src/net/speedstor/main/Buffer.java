package net.speedstor.main;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Buffer extends Thread{
	
	private List<String> cookies;
	private HttpsURLConnection conn;

	private ArrayList<ArrayList<List<String>>> UserGrades = new ArrayList<ArrayList<List<String>>>();
	

	private int refreshTimes = 0;	

	private FileReader file = new FileReader();	
	private Data data = new Data(file);
	

	private CookieManager msCookieManager = new CookieManager();
	
	private String username = "";
	private String password = "";

	private String[] subjects = new String[35];
	private String[] overall = new String[35];
	private String[] teachers = new String[35];

	private String clientRequest = "";
	private String ExtendedRequest = "";
	private String response[];
	
	private boolean buffered = false;
	
	//private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3741.0 Safari/537.36";
	private final String USER_AGENT = "GradeBoosterBot v2.0";
	
	long time = 0;
	Socket client;
	
	String user =  "";
	
	
	public Buffer(Socket client) {
		this.client = client;
		
		System.out.println(client);
		
	}
	
	public String recieve(Socket client) throws Exception {
		String returnRequest = "";

	    BufferedReader webIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream());
        
        time = System.currentTimeMillis();
        
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
        DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());
        clientRequest = inFromClient.readLine();
        //System.out.println("Received: " + clientRequest);
        

        String returnClientRequest = "";
        //do things with sever request
    
    	response = clientRequest.split(" ");
        System.out.println(response[0]);
        
        buffered = true;
    		        	
    	
    	switch(response[0]) {
    		case "try":
    			if(response.length >= 3) {
	        		System.out.println("Recieved(request): fetching database for "+response[1]+" --");
        			if(this.getDataForUser(response[1], response[2]) == true) {
        				returnClientRequest = "1";
        		        buffered = true;
        				returnRequest = "send "+response[1]+" "+response[2];
        				System.out.println("Success (request): User exsists and cached data has been updated");
        			}else {
        				System.out.println("Error (request): Username or password wrong --");
        				returnClientRequest = "-";
        			}

        			username = response[1];
        			password = response[2];
	        		        	
    			}else {
    				System.out.println("Error(request): Please provide a username and password after get: get |username| |password|");
    				returnClientRequest = "-2";
    			}
    			break;
    		case "get":
    			if(response.length >= 3) {
    				System.out.println("Success(request): Recieved command from server");
    				returnRequest = "get "+response[1]+' '+response[2];
    				returnClientRequest = "1";			        		        	
    			}else {
    				System.out.println("Error(request): Please provide a username and password after get: get |username| |password|");
    				returnClientRequest = "-2";
    			}
    			break;
    		case "test":
    			System.out.println("Success (request): Php Connection Success --");	        			
    			break;
    	}
    	
    	//System.out.println("This is the end of the request stream");
		outToClient.writeBytes(returnClientRequest);
		client.close();
		
		time = System.currentTimeMillis() - time;
		
		
		return returnRequest;
	}
	
	public void run() {		
		System.out.println("A New Thread had start to execute code for a user -");
		
		try {
			ExtendedRequest = recieve(client);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
    	String[] response = ExtendedRequest.split("\\s+");
		
	    	if(buffered) {
			switch(response[0]) {
			case "get":
				if(response.length >= 3) {
	        		System.out.println("Recieved(request): fetching database for "+response[1]+" --");
	        		try {
		    			if(this.getDataForUser(response[1], response[2]) == true) {
		    				send(response[1], response[2]);
		    			}else {
		    				System.out.println("Error (request): Username or password wrong --");
		    			}
		
		    			username = response[1];
		    			password = response[2];
	    			
	        		} catch (Exception e) {
	        			e.printStackTrace();
	        		}
	        		        	
				}else {
					System.out.println("Error(request): Please provide a username and password after get: get |username| |password|");
				}
				break;
			case "send":
				if(username != "" && password != "") {
					this.send(username, password);
				}
				break;        			
			case "test":
				System.out.println("Success (request): Php Connection Success --");	        			
				break;
	    	}
    	}
	}	
	
	private void send(String username, String password){
		
		try{  
			//Init mysql
			Class.forName("com.mysql.cj.jdbc.Driver");  
			java.sql.Connection con= DriverManager.getConnection("jdbc:mysql://localhost:3306/MyBackpackUpdater","root","");  
			//Statement stmt = con.createStatement();  
			
			//retrieve data
			//ResultSet rs = stmt.executeQuery("select * from users");  
			//while(rs.next()) System.out.println(rs.getInt(1)+"  "+rs.getString(2)+"  "+rs.getString(3));
			
			username = username.replace(".", "");
			//adding data to mysql
			//Statement stmt = con.createStatement();
			String query = "INSERT INTO "+username+"_assignments (subject, assignment, type, score, possible, assigned, due, comment)"
			        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

			Statement stmt = con.createStatement(); 
			//stmt.execute("TRUNCATE TABLE "+username+"_assignments");  
			ResultSet result = stmt.executeQuery("SHOW TABLES like '%"+username+"_assignments%'");
			
			//System.out.println(result.toString());
			if(result.next()) {
				stmt.execute("TRUNCATE TABLE "+username+"_assignments");  
				System.out.println("Cleared table for recording new data - assignments");
			}
			else {
				stmt.execute("CREATE TABLE `"+username+"_assignments` (\r\n" + 
					" `id` int(11) NOT NULL AUTO_INCREMENT,\r\n" + 
					" `subject` tinytext NOT NULL,\r\n" + 
					" `assignment` tinytext NOT NULL,\r\n" + 
					" `type` tinytext NOT NULL,\r\n" + 
					" `score` tinytext,\r\n" + 
					" `possible` tinytext,\r\n" + 
					" `assigned` tinytext,\r\n" + 
					" `due` tinytext,\r\n" + 
					" `comment` tinytext,\r\n" + 
					" PRIMARY KEY (`id`)\r\n" + 
					")");
				System.out.println("New User: "+username+"|| Created new table for user - overall");
			}
			
			//System.out.println(UserGrades.size());
			//	System.out.println(UserGrades.get(6).get(0).get(1));
				
			// create the mysql insert preparedstatement
			for(int i = 0; i < UserGrades.size() - 1; i++) {
				//System.out.println(subjects[i]);
				for(int a = 0; a < UserGrades.get(i).size()-1 && UserGrades.get(i).get(a).get(0) != null; a++) {
			      PreparedStatement preparedStmt = con.prepareStatement(query);
			      //System.out.println(UserGrades.get(i).size());
			      preparedStmt.setString(1, subjects[i]);
			      preparedStmt.setString(2, UserGrades.get(i).get(a).get(0));
			      preparedStmt.setString(3, UserGrades.get(i).get(a).get(1));
			      preparedStmt.setString(4, UserGrades.get(i).get(a).get(2));
			      preparedStmt.setString(5, UserGrades.get(i).get(a).get(3));
			      preparedStmt.setString(6, UserGrades.get(i).get(a).get(4));
			      preparedStmt.setString(7, UserGrades.get(i).get(a).get(5));
			      preparedStmt.setString(8, UserGrades.get(i).get(a).get(6));
			      //if(i == 6) System.out.println(UserGrades.get(i).get(a).get(0));
			      // execute the preparedstatement
			      preparedStmt.executeUpdate(); 
				}
			}
			
			//overall grades
			//Statement stmt = con.createStatement(); 
			//stmt.execute("TRUNCATE TABLE "+username+"_assignments");  
			result = stmt.executeQuery("SHOW TABLES like '%"+username+"_overall%'");
			
			query = "INSERT INTO "+username+"_overall (subject, teacher, overall)"
			        + " VALUES (?, ?, ?)";
			
			//System.out.println(result.toString());
			boolean base = result.next();
			System.out.println(base);
			if(base) {
				stmt.execute("TRUNCATE TABLE "+username+"_overall");  
				System.out.println("Cleared table for recording new data - overall");
			}
			else {
				stmt.execute("CREATE TABLE "+username+"_overall (\r\n" + 
						"    \r\n" + 
						"	id int(11) AUTO_INCREMENT PRIMARY KEY NOT NULL,\r\n" + 
						"    subject TINYTEXT NOT NULL,\r\n" + 
						"    teacher TINYTEXT NOT NULL,\r\n" + 
						"    overall LONGTEXT NOT NULL\r\n" + 
						"\r\n" + 
						")");
				System.out.println("New User: "+username+"|| Created new table for user - overall");
			}
			
			
			
		      // create the mysql insert preparedstatement
			
			for(int i = 0; i < UserGrades.size(); i++) {
				  PreparedStatement preparedStmt = con.prepareStatement(query);
			      //System.out.println(UserGrades.get(i).size());
			      preparedStmt.setString(1, subjects[i]);
			      preparedStmt.setString(2, teachers[i]);
			      preparedStmt.setString(3, overall[i]);
		
			      // execute the preparedstatement
			      preparedStmt.executeUpdate(); 
			}
		     
			System.out.println("Finished: Uploaded UserGrades to mysql db\n");
			con.close();  
		}catch(Exception e){ 
			System.out.println(e);
		}  
			
		
		//not my code, lol
		try {
            // open a connection to the site
            URL url = new URL("http://127.0.0.1/java/recieve.php?Username=PlzWork");
            URLConnection con = url.openConnection();
            // activate the output
            con.setDoOutput(true);
            
            // send your parameters to your site
            PrintStream ps = new PrintStream(con.getOutputStream());

            // we have to get the input stream in order to actually send the request
            con.getInputStream();

            // close the print stream
            ps.close();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
		
	}
		
	
	private boolean getDataForUser(String username, String password) throws Exception {
		
		this.GetFiles(this, username, password);
		
		//System.out.println(password);
		UserGrades = data.getData(username); //the array<array<array variable
		
		if(UserGrades.size() <= 0) {
			System.out.println("Thread: The password or username entered is wrong --\n");
			return false;
		}
		
		subjects = data.getSubjects(username);
		overall = data.getOverallGrades(username);
		teachers = data.getTeachers(username);
		
		return true;
		
		/*int temp = 0;
		while(subjects[temp] != null) {
			System.out.println("Subject:  "+subjects[temp]);
			System.out.println("Teacher:  "+teachers[temp]);
			System.out.println("Overall:  "+overall[temp]+"\n");
			temp++;
		}*/
		
		
	}
	
	
	private void GetFiles(Buffer http, String userId, String pwd) throws Exception{
		String url="https://fairmontschools.seniormbp.com/SeniorApps/facelets/registration/loginCenter.xhtml";
		String destination = "https://fairmontschools.seniormbp.com/SeniorApps/studentParent/academic/dailyAssignments/gradeBookGrades.faces";
		
		
		String page1 = http.GetPageCookie(url);
				
		
		//set Post request based on get request
		http.sendPost(0, url, "AJAXREQUEST=_viewRoot&form=form&javax.faces.ViewState=j_id1&form%3AuserId="+userId+"&form%3AuserPassword="+pwd+"&form%3AsignIn=form%3AsignIn&AJAX%3AEVENTS_COUNT=1&");
		
	
	
		// 3. success then go to gmail.
		String result = http.GetPageContentRedirect(destination);
		//System.out.println(result);
		//System.out.println("Writing and Recording Grades: "+file.writeFile(userId, userId+"_grades.html", result));
		
		
		String result1 = http.sendPost(1, destination, "f=f&javax.faces.ViewState=j_id2&f%3Ainside%3AGradedTab%3AAssignMPSel=2&f%3Ainside%3AGradedTab%3Aj_id_jsp_394614891_10pc8=01-14-2019&f%3Ainside%3AGradedTab%3Aj_id_jsp_394614891_12pc8=06-07-2019&f%3A_idcl=f%3Ainside%3Aj_id_jsp_1774471256_10pc5");
		//System.out.println(result1);
		System.out.println("Writing and Recording Assignments: "+file.writeFile(userId, userId+"_assignments.html", result1));
		
		
	}
	
	private String GetPageCookie(String url) throws Exception{
	//	System.out.println("\n");
		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();
		
		
		//set function as get
		conn.setRequestMethod("GET");
		conn.setUseCaches(false);
		
		//do what a browser would do - act like browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		
		
		/*if(cookies != null) {
			for (String cookie : this.cookies) {
				System.out.println("Adding Cookies: " + cookie.split(";", 1)[0]);
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}*/
		
		conn.getResponseCode();
	//	System.out.println("Sending 'GET' request to URL : " + url);
	//	System.out.println("Response Code: " + responseCode);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		
		
	//	System.out.println("Recording Cookies: " + conn.getHeaderFields().get("Set-Cookie"));
		//if(refreshTimes == 0) setCookiesInit(conn.getHeaderFields().get("Set-Cookie"));
		//else if(refreshTimes > 0) setCookies(conn.getHeaderFields().get("Set-Cookie"));
		
		setCookiesInit(conn.getHeaderFields().get("Set-Cookie"));
		
		
		refreshTimes++;
		
		return response.toString();
	}	
	
	
	private String GetPageContentRedirect(String url) throws Exception{
	//	System.out.println("\n");
		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();
		
		
		//set function as get
		conn.setRequestMethod("GET");
		conn.setUseCaches(false);
		
		//do what a browser would do - act like browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		
		
		/*if(cookies != null) {
			for (String cookie : this.cookies) {
				System.out.println("Adding Cookies: " + cookie.split(";", 1)[0]);
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}*/
	
	//	System.out.println("Rewrite Cookies: " + "cookies=true; "+ cookies.get(0));
		conn.addRequestProperty("Cookie", "cookies=true; "+cookies.get(0));
		
		conn.getResponseCode();
	//	System.out.println("Sending 'GET' request to URL : " + url);
	//	System.out.println("Response Code: " + responseCode);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		
		
		return response.toString();
	}	
	
	
	private String GetPageContent(String url) throws Exception{
	//	System.out.println("\n");
		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();
		
		
		//set function as get
		conn.setRequestMethod("GET");
		conn.setUseCaches(false);
		
		//do what a browser would do - act like browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		
		
		/*if(cookies != null) {
			for (String cookie : this.cookies) {
				System.out.println("Adding Cookies: " + cookie.split(";", 1)[0]);
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}*/
	
	//	System.out.println("Rewrite Cookies: " + "senior.session.expired=true; cookies=true; "+ cookies.get(0));
		conn.addRequestProperty("Cookie", "senior.session.expired=true; cookies=true; "+ cookies.get(0));
		
		conn.getResponseCode();
	//	System.out.println("Sending 'GET' request to URL : " + url);
	//	System.out.println("Response Code: " + responseCode);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		
		
		return response.toString();
	}	
	
	
	public void setCookiesInit(List<String> cookies) {
		this.cookies = cookies;
	//	System.out.println("Final:  " + cookies);
	}
	
	
	
	//form parameters
	public String getFormParams(String html, String username, String password) throws UnsupportedEncodingException{
	//	System.out.println("Extracing form's data...");
		Document doc = Jsoup.parse(html);
		
		//adapting to My backpack form ids
		Element loginform = doc.getElementById("form");
		Elements inputElements = loginform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		for(Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");
			//System.out.println("writing params");
			
			if(key.equals("form:userId")){
				value = username;
				//System.out.println("userId being written");
			}else if(key.equals("form:userPassword")) {
				value = password;
				//System.out.println("password being written");
			}
			
			paramList.add(key + "=" +URLEncoder.encode(value, "UTF-8"));
		}
		
		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		
		return result.toString();		
	}

	
	
	private String sendPost(int number, String url, String postParams) {
	//	System.out.println("\n");
		
		URL obj;
		StringBuffer response = new StringBuffer();;
		
		//postParams = "AJAXREQUEST=_viewRoot&form=form&javax.faces.ViewState=j_id1&form%3AuserId=ycheung256&form%3AuserPassword=aldrin021230&form%3AsignIn=form%3AsignIn&AJAX%3AEVENTS_COUNT=1&";
		
		
		try {
			obj = new URL(url);
			conn = (HttpsURLConnection) obj.openConnection();
			
			
			//Act as browser;
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
			conn.setRequestProperty("Host", "fairmontschools.seniormbp.com");
			
			
			if(number == 0) {
	//			System.out.println("Rewrite Cookies: " + "cookies=true; "+ cookies.get(0));
				conn.addRequestProperty("Cookie", "cookies=true; "+cookies.get(0));
			}else if(number == 1) {
	//			System.out.println("Rewrite Cookies: " + "senior.session.expired=true; cookies=true; "+ cookies.get(0));
				conn.addRequestProperty("Cookie", "senior.session.expired=true; cookies=true; "+ cookies.get(0));
			}
			
			
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Referer", "https://fairmontschools.seniormbp.com/SeniorApps/facelets/home/home.xhtml?convid=451313");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
			
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			//send post request
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			
			
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();
			
			conn.getResponseCode();
	//		System.out.println("Sending 'POST' request to URL : " + url);
	//		System.out.println("Post parameters : " + postParams);
	//		System.out.println("Response Code : " + responseCode);
			
			
			
		
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			response = new StringBuffer();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			
	//		System.out.println("After Post Cookies: "+conn.getHeaderFields().get("Set-Cookie"));
			
	
			//System.out.println(response.toString());
			
			
	
			
		}catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return response.toString();
		
	}	
	
	
}
