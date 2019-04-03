package net.speedstor.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class FileReader {
	
	public boolean writeFile(String User,String name, String content) throws IOException {
		
		File f = new File("../myBackpackUpdater_Files");
		if(!f.exists()) System.out.println("File Storage does not exsist --Creating storage folder: "+(new File("../myBackpackUpdater_Files")).mkdirs());
		
		File file = new File("../myBackpackUpdater_Files",name);
	      
	    // creates the file
		file.createNewFile();
		
		FileWriter writer = new FileWriter(file);
		
		writer.write(User+"\n\n\n"+content);
		
		writer.close();
		
		return true;
	}
	
	public String readFile(String location) {
		  File file = new File(location); 

		  if(!file.exists()) return "-1";
		  
		  Scanner sc;
		  String result = ""; 
		  
		  try {
			 sc = new Scanner(file);
			  while (sc.hasNextLine()) {
				  result += sc.nextLine();
			  };
		  } catch (FileNotFoundException e) {
			 e.printStackTrace();
		  }
		  
		return result;
	}
	
	
	public String cutString(String original, int start, int finish) {
		String result = "";
		
		result = original.substring(start, finish);
		
		return result;
	}
	
	
	
}
