package gethub;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class Tools {

	private static String homeDirectory = System.getProperty("user.home");
	private static String finalDirectory = homeDirectory + "/GEThub";
	
	public static String getFinalDirectory()
	{
		return finalDirectory;
	}
	
	
	public static boolean isInt (String s)
	{
		boolean isInt=false;
		try
		{
			Integer.parseInt(s);
			isInt=true;
		}
		catch (NumberFormatException ex)
	      {
	         // s is not an int
	      }
		return isInt;
	}
	
	public static boolean[] dateValid(Date createdFromDate ,Date createdToDate,Date creationDate )
	{
		
		
		//array[0] denotes if date is valid or not
		//array[1] denotes if we should stop looking for values
		//array initialized to both values false
		boolean []array = new boolean[2];
		
			if(createdFromDate.equals(createdToDate))
			{
				
				if(creationDate.equals(createdFromDate))
				{
					array[0] = true;
					array[1] = false;
				}
			}
			else
			{
				
				if( creationDate.before(createdFromDate) )
				{
					array[0] = false;
					array[1] = false;
				}
				else if( creationDate.after(createdToDate) )
				{
					array[0] = false;
					array[1] = true;
				}
				else
				{
					array[0] = true;
					array[1] = false;
				}
			}
		
			
			
		
		return array;
	}
	
	public static void createDirectory(String subDirectory)
	{
		
		
		try
		{
			
			File file = new File(finalDirectory+"/"+subDirectory);
			if(!file.exists())
			{
				if(file.mkdirs() )
				{
					System.out.println("created "+ finalDirectory + "/"+subDirectory);
				}
				else
				{
					System.out.println("error creating "+finalDirectory+"/"+subDirectory);
				}
			}
			
		}
		catch(Exception e)
		{
			
			e.printStackTrace();
		}
	}
	
	
	public static void saveInDirectory(String dirName,String fileName,ArrayList<JsonObject> response)
	{
		createDirectory(dirName);
		
		try
		{
			
			File file = new File(finalDirectory+"/"+dirName+"/"+fileName);
			if( file.createNewFile())
			{
				System.out.println(fileName+" created");
			}
			else
			{
				System.out.println(fileName+" already exists");
			}
			//decided to go with printwriter since filewriter gives oneliners from hell.
			PrintWriter writer = new PrintWriter( new FileWriter(file) );
			System.out.println("Started writing " + fileName);
			for(JsonObject r : response)
			{
				writer.println(r.toString());
				
			}
			writer.close();
			System.out.println("Done writing " + fileName);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	//overloaded since in commits we can't work with JsonObjects
	public static void saveInDirectory(ArrayList<String> response,String dirName,String fileName)
	{
		createDirectory(dirName);
		
		try
		{
			
			File file = new File(finalDirectory+"/"+dirName+"/"+fileName);
			if( file.createNewFile())
			{
				System.out.println(fileName+" created");
			}
			else
			{
				System.out.println(fileName+" already exists");
			}
			//decided to go with printwriter since filewriter gives oneliners from hell.
			PrintWriter writer = new PrintWriter( new FileWriter(file) );
			System.out.println("Started writing " + fileName);
			for(String r : response)
			{
				writer.println(r);
				
			}
			writer.close();
			System.out.println("Done writing " + fileName);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	
	
	
}
