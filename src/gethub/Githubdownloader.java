package gethub;

import java.io.IOException;
import java.util.ArrayList;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.Request;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.wire.RetryWire;
import java.io.File;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

public class Githubdownloader implements GitHubDownloader {
	
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
	
	public Github login(String username, String password) 
	{
			Github github = new RtGithub(new RtGithub(username,password).entry().through(RetryWire.class));
			//Users u= github.users();
			try {
				github.users().self().login();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			System.out.println("login successful");	
			
			return github;
			
	}
	
	public void getUserInfo(String name,String username,String password) 

	{
		Githubdownloader o = new Githubdownloader();
		Github g =o.login(username, password);
		JsonObject j;
		try 
		{
			j =g.users().get(name).json();	
			System.out.println(j.toString());
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//TODO:use this one?
	/*public String getRequest(String username,String password,String name,String repo_name,String resource)
	{
		
		GithubDownloader o = new GithubDownloader();
		Github g =o.login(username, password);
		
		String x=null;
		try {
			x = g
					.entry()
					.uri()
					.path("/repos/"+name+"/"+repo_name+"/issues")
					.queryParam("state", "all")
					.back()
					.method(Request.GET)
					.fetch()
					.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return x;
	}*/
	
	public void getIssues(String username,String password,String name,String repo_name,int state)
	
	{
		//open a login window
		Githubdownloader o = new Githubdownloader();
		Github g =o.login(username, password);
		
		
		JsonReader j;
		JsonArray jj;
		
		try
		{
			
			/* test code below
			//get the desired repository
			Repos repos = g.repos();
			
			Repo repo = repos.get(new Coordinates.Simple(name, repo_name));
			 * Issues issues = repo.issues();
			Issue issue = issues.get(1113);
			boolean open = new Issue.Smart(issue).isOpen();
			System.out.println(open);
			
			//get issue in json
			//j=issue.json();
			//System.out.println(j.toString()); */
			
			
			
			//If loop to select which issues to download. 
			//0 is for all, 1 is for open only, 2 is for closed only.
			if ( state==0 )
			{
				
				//raw request response String
				String x= g
						.entry()
						.uri()
						.path("/repos/"+name+"/"+repo_name+"/issues")
						.queryParam("state", "all")
						.back()
						.method(Request.GET)
						.fetch()
						.toString();
				
				//See how many pages the API gives us back. Right before the string pattern below is the number we want.
				
				int index = x.indexOf(">; rel=\"last\"");
				int k=1;
				int lastPage=-1;
				//This loop makes sure we grab the full number of the last page, by exploiting the static nature of the API http response.
				//This is done so we know how many more requests we need to get the full number of issues.
				while(isInt( x.substring(index-k, index)  ))
				{
					lastPage = Integer.parseInt(x.substring(index-k, index));
					k++;
				}
				//TODO:Can this be done better? (finding out how many pages in total)
				
				//Loop through all pages and get response back. 
				//TODO:work on real storage, not just printing it.
				for (int i=1 ; i<=lastPage ; i++)
				{
					j = g
							.entry()
							.uri()
							.path("/repos/"+name+"/"+repo_name+"/issues")
							.queryParam("state", "all")
							.queryParam("page", String.valueOf(i))
							.back()
							.method(Request.GET)
							.fetch()
							.as(JsonResponse.class)
							.json();
						
						jj= j.readArray();
						System.out.println(jj.toString());
				}
			}
			else if( state ==1 )
			{
				//do the same for open issues
				String xx= g
						.entry()
						.uri()
						.path("/repos/"+name+"/"+repo_name+"/issues")
						.queryParam("state", "open")
						.back()
						.method(Request.GET)
						.fetch()
						.toString();
				
				
				int index = xx.indexOf(">; rel=\"last\"");
				int k=1;
				int lastPage=-1;
				
				while(isInt( xx.substring(index-k, index)  ))
				{
					lastPage = Integer.parseInt(xx.substring(index-k, index));
					k++;
				}
				
				for (int i=1 ; i<=lastPage ; i++)
				{
					j = g
							.entry()
							.uri()
							.path("/repos/"+name+"/"+repo_name+"/issues")
							.queryParam("state", "open")
							.queryParam("page", String.valueOf(i))
							.back()
							.method(Request.GET)
							.fetch()
							.as(JsonResponse.class)
							.json();
						
						jj= j.readArray();
						System.out.println(jj.toString());
				}
			}
			
			else if ( state == 2)
			{
				//do the same for open issues
				String xx= g
						.entry()
						.uri()
						.path("/repos/"+name+"/"+repo_name+"/issues")
						.queryParam("state", "closed")
						.back()
						.method(Request.GET)
						.fetch()
						.toString();
				
				//See how many pages the API gives us back. Right before the string pattern below is the number we want.
				
				int index = xx.indexOf(">; rel=\"last\"");
				int k=1;
				int lastPage=-1;
				//This loop makes sure we grab the full number of the last page, by exploiting the static nature of the API http response.
				//This is done so we know how many more requests we need to get the full number of issues.
				while(isInt( xx.substring(index-k, index)  ))
				{
					lastPage = Integer.parseInt(xx.substring(index-k, index));
					k++;
				}
				
				for (int i=1 ; i<=lastPage ; i++)
				{
					j = g
							.entry()
							.uri()
							.path("/repos/"+name+"/"+repo_name+"/issues")
							.queryParam("state", "closed")
							.queryParam("page", String.valueOf(i))
							.back()
							.method(Request.GET)
							.fetch()
							.as(JsonResponse.class)
							.json();
						
						jj= j.readArray();
						System.out.println(jj.toString());
				}
			}
			else
			{
				//TODO:print out something more informative
				System.out.println("Invalid selection of issue state parameter. Please try again");
			}
			
			
			
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	//TODO:maybe put the request snippet in a function?	
	}

	public void getCommits(String username,String password,String name,String repo_name)
	{
		//TODO:add by date feature?
		//login window
		Githubdownloader o = new Githubdownloader();
		Github g =o.login(username, password);
		
		JsonReader j;
		JsonArray jj;
		
		//raw request
		try {
			//arraylist to hold responses
			ArrayList<String> commit_responses = new ArrayList<String>();
			int i=1;
			
			/*the API does not explicitly state which page is the last, so we go it 
			one page at a time, as long as we're given a next link in our response
			 */			
			String x= g
					.entry()
					.uri()
					.path("/repos/"+name+"/"+repo_name+"/commits")
					.queryParam("page", String.valueOf(i))
					.back()
					.method(Request.GET)
					.fetch()
					.toString();
			
		
			
			
			
			while(x.contains("; rel=\"next\""))
			{
				commit_responses.add( x.substring( x.indexOf("[{\"sha\""), x.length() ) );
				i++;
				System.out.println("grabbing commit page "+String.valueOf(i));
				x= g
						.entry()
						.uri()
						.path("/repos/"+name+"/"+repo_name+"/commits")
						.queryParam("page", String.valueOf(i))
						.back()
						.method(Request.GET)
						.fetch()
						.toString();
			}
			
			//print for now, will be properly stored later.
			for(String r :commit_responses)
			{
				System.out.println(r);
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
	}
	
/*	public void cloneRepo() throws IOException, InvalidRemoteException, TransportException, GitAPIException
	{
		
		String REMOTE_URL = "https://github.com/github/testrepo.git";
		File localPath = File.createTempFile("TestGitRepository", "");
	    localPath.delete();
	    
	    System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
        Git result = Git.cloneRepository()
                .setURI(REMOTE_URL)
                .setDirectory(localPath)
                .call();
        try {
	        // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
	        System.out.println("Having repository: " + result.getRepository().getDirectory());
        } finally {
        	result.getRepository().close();
        	result.close();
        }
	    
	}*/

	public static void main(String[] args) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		// Login first, using username/password , or your OAuth token (later).
		//username/password are our own
		//name is the name of the github user who owns the repository we're interested in.
		//repo_name is the repository name we're interested in.
		
		String username = "";
		String password = "";
		String name = "jcabi";
		String repo_name = "jcabi-github";
		int state = 0 ;
		Githubdownloader o = new Githubdownloader();
		
		//o.login(username, password);
		//o.getUserInfo("jcabi",username,password);
		//o.getIssues(username,password,name,repo_name,state);
		o.getCommits(username, password, name, repo_name);
	
		
		
		

	}

	
}


