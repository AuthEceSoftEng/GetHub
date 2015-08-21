package gethub;

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
}
