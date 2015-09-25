package gethub;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class Tools {

	
	
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
}
