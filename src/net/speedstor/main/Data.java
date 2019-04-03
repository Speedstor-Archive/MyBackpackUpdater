package net.speedstor.main;

import java.util.ArrayList;
import java.util.List;

public class Data {
	
	FileReader file;
	String gradeFile = "";
	ArrayList<List<String>> subject1 = new ArrayList<List<String>>();
	ArrayList<List<String>> subject2 = new ArrayList<List<String>>();
	ArrayList<List<String>> subject3 = new ArrayList<List<String>>();
	ArrayList<List<String>> subject4 = new ArrayList<List<String>>();
	ArrayList<List<String>> subject5 = new ArrayList<List<String>>();
	ArrayList<List<String>> subject6 = new ArrayList<List<String>>();
	ArrayList<List<String>> subject7 = new ArrayList<List<String>>();
	
	ArrayList<ArrayList<List<String>>> subjectTables = new ArrayList<ArrayList<List<String>>>();
	
	String[] overallGrades = new String[35];
	String[] subjectTeachers = new String[35];
	String[] subjectList = new String[35];
	String[] tempTableList = new String[35];
	
	private String currentUser = "";
	
	public Data(FileReader file) {
		this.file = file;
	
	}
	
	private void resetFiles() {
		subjectTables = new ArrayList<ArrayList<List<String>>>();
		
		overallGrades = new String[35];
		subjectTeachers = new String[35];
		subjectList = new String[35];
		tempTableList = new String[35];
	}
	
	public String[] getSubjects(String username) {
		if(currentUser != username) getData(username);
		return subjectList;
	}
	public String[] getOverallGrades(String username) {
		if(currentUser != username) getData(username);
		return overallGrades;
	}
	public String[] getTeachers(String username) {
		if(currentUser != username) getData(username);
		return subjectTeachers;
	}

	public ArrayList<ArrayList<List<String>>> getData(String username) {
		resetFiles();
		
		this.currentUser = username;
		
		gradeFile = file.readFile("../MyBackpackUpdater_Files/"+username+"_assignments.html");
		if(gradeFile == "-1") return subjectTables;
		
		int firstIndex = gradeFile.indexOf("<hr/>	<table class=\"fullWidth\">"); 
		//System.out.println(firstIndex);
		
		int toIndex = gradeFile.indexOf("</table></span>");
		//System.out.println(toIndex);
		
		//System.out.println(firstIndex);
		if(firstIndex > 0 && toIndex > 0)gradeFile = gradeFile.substring(firstIndex, toIndex);
		else {
			System.out.println("Error: could not find string for assignments ");
			return new ArrayList<ArrayList<List<String>>>();
		}
		
		
		//Parse Subject Names and table strings
		//int temp = 1;
		for(int i = 0, a=0; a < gradeFile.length(); i++) {
			int cursorInit = gradeFile.indexOf("courseNameClick", a);

			a = cursorInit + 1;
			
			if(cursorInit == -1) {
				break;
			}
			
			int cursorFinal = gradeFile.indexOf("<", a);
			

			//a = cursorFinal;
			

			//System.out.println(gradeFile.substring(cursorInit, cursorFinal));
			if(firstIndex > 0 && toIndex>0) subjectList[i] = gradeFile.substring(cursorInit + 34, cursorFinal);
			
			//get overall grades
			cursorInit = gradeFile.indexOf("Grade to Date: </span>", a);
			cursorFinal = gradeFile.indexOf("<", cursorInit+22);
			//System.out.println(gradeFile.substring(cursorInit + 22, cursorFinal));
			if(firstIndex > 0 && toIndex>0) overallGrades[i] = gradeFile.substring(cursorInit + 22, cursorFinal);
			
			//get teachers
			cursorInit = gradeFile.indexOf("Teacher: </span>", a);
			cursorFinal = gradeFile.indexOf("<", cursorInit+16);
			if(firstIndex > 0 && toIndex>0) subjectTeachers[i] = gradeFile.substring(cursorInit + 16, cursorFinal);
			
			int temp4 = cursorInit;
			
			
			cursorInit = gradeFile.indexOf("rich-table da", a);
			cursorInit = gradeFile.indexOf("<tbody", cursorInit);
			
			//cursorInit = cursorFinal;
			
			cursorFinal = gradeFile.indexOf(";</a></td></tr></tbody>", cursorInit);
			
			//if(i == 6)System.out.println(cursorInit + "   "+cursorFinal +gradeFile.substring(cursorInit, cursorFinal));
			if(firstIndex > 0 && toIndex>0) tempTableList[i] = gradeFile.substring(cursorInit, cursorFinal + 10);
			
			if(gradeFile.indexOf("Grade to Date: </span>", temp4) < gradeFile.indexOf("rich-table da", temp4) && gradeFile.indexOf("Grade to Date: </span>", temp4) > 0) {
					
					//System.out.println("empty subject");
					tempTableList[i] = "This is empty";
				
			}else {
			

			//a = cursorInit + 1;
			}
		}
		
		
		//init subjectTables
		int temp2 = 0;
		while(subjectList[temp2] != null) {
			subjectTables.add(new ArrayList<List<String>>());
			temp2++;
		}
		System.out.println("Number of Subjects: "+temp2);
		
		
		
		//add tables to list according to subject name
		//System.out.println("Starting to parse table data::");
		//parse table data
		//System.out.println(tempTableList[6]);
		for(int i = 0; i < temp2; i++) {
			String parse = tempTableList[i];

			//System.out.println(i);
			for(int a = 0, b=0; b < parse.length(); a++) {
				if(parse == "This is empty") {
					subjectTables.get(i).add(new ArrayList<String>());
					for(int l = 0; l < 7; l++) {
						subjectTables.get(i).get(a).add("empty");
						//System.out.println("life");
					}
					

					subjectTables.get(i).add(new ArrayList<String>());
					for(int l = 0; l < 7; l++) {
						subjectTables.get(i).get(a).add("empty");
						//System.out.println("life");
					}
					System.out.println("Empty subject found");
					b = parse.length();
				}else {
				//adding column
				subjectTables.get(i).add(new ArrayList<String>());
				
				//Assignments Column
				int cursorInit = parse.indexOf("assignmentLink", b);
				
				if(cursorInit == -1) {
					break;
				}
				
				cursorInit = parse.indexOf(">", cursorInit);
				
				int cursorFinal = parse.indexOf("<", cursorInit);
				
				//if(i == 6) System.out.println("Assignments: "+parse.substring(cursorInit + 1, cursorFinal));
				if(cursorInit > 0 && cursorFinal>0) subjectTables.get(i).get(a).add(parse.substring(cursorInit+1, cursorFinal));
				
				//set temp to drag on
				b = cursorInit;
				
				
				for(int l = 0; l < 6; l++) {
					cursorInit = parse.indexOf("false;\">", cursorFinal);
					//cursorInit = parse.indexOf(">", cursorInit);
					
					cursorFinal = parse.indexOf("<", cursorInit);
					
					//if(i == 6) System.out.println("Add: "+parse.substring(cursorInit + 8, cursorFinal));
					if(cursorInit > 0 && cursorFinal>0) subjectTables.get(i).get(a).add(parse.substring(cursorInit + 8, cursorFinal));
				}
				
				
				
				//if(i == 6) System.out.println("\n");
				//System.out.println(i);
				}
			}
		}
		
		System.out.println("Finished: User Grades Cached\n");
		return subjectTables;
	}
	
	
	
	
	
}
