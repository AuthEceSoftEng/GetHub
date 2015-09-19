package gethub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class Githubdownloader implements GitHubDownloaderInterface {
	
	//TODO:add from:to feature
	//before storage , do all API transactions via json, and get date keyset, then decide if it's storage worthy
	//eg
	// get jsonarray
	//get object
	// isOk(jsonObject.date())
	// store jsonResponse.toString() or the json response itself. we'll see
	//otherwise continue
	
	
	
	
	
	
	//provide info once
	private static String username;
	private static String password;
	private static String name;
	private static String repositoryName;
	
	
	public void setUserInfo()
	{
		
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Username: ");
		try {
			username = input.readLine();
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		System.out.println("Password: ");
		try {
			password = input.readLine();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		System.out.println("Github user: ");
		try {
			name = input.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Github user repository name: ");
		try {
			repositoryName = input.readLine();
		} catch (IOException e) {
						e.printStackTrace();
		}
	}
	

	
	public Github login(String username, String password) 
	{
			Github github = new RtGithub(new RtGithub(username,password).entry().through(RetryWire.class));
			//Users u= github.users();
			try {
				github.users().self().login();
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			System.out.println("login successful");	
			
			return github;
			
	}
	
	public void getGithubUserInfo(String name,String username,String password) 

	{
		Githubdownloader o = new Githubdownloader();
		Github g =o.login(username, password);
		JsonObject jsonResponse;
		try 
		{
			jsonResponse =g.users().get(name).json();	
			System.out.println(jsonResponse.toString());
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//TODO:use this one?
	public String getRequest(String username,String password,String name,String repositoryName,String resource,String param1,String param2, Github g)
	{
		
		
		
		String responseString=null;
		
		try {
			responseString = g
					.entry()
					.uri()
					.path("/repos/"+name+"/"+repositoryName+"/"+resource)
					.queryParam(param1, param2)
					.back()
					.method(Request.GET)
					.fetch()
					.toString();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return responseString;
	}
	
	public int getPages(String responseString)
	{
		//See how many pages the API gives us back. Right before the string pattern below is the number we want.
		
		int index = responseString.indexOf(">; rel=\"last\"");
		int k=1;
		int lastPage=-1;
		//This loop makes sure we grab the full number of the last page, by exploiting the static nature of the API http response.
		//This is done so we know how many more requests we need to get the full number of issues.
		while(Tools.isInt( responseString.substring(index-k, index)  ))
		{
			lastPage = Integer.parseInt(responseString.substring(index-k, index));
			k++;
		}
		
		return lastPage;
	}
	
	public void getIssues(String username,String password,String name,String repositoryName,int state)
	
	{
		//open a login window
		Githubdownloader o = new Githubdownloader();
		Github g =o.login(username, password);
		
		
		JsonReader jsonRequestResponse;
		JsonArray jsonRequestArray;
		
		try
		{
			
			/* test code below
			//get the desired repository
			Repos repos = g.repos();
			
			Repo repo = repos.get(new Coordinates.Simple(name, repositoryName));
			 * Issues issues = repo.issues();
			Issue issue = issues.get(1113);
			boolean open = new Issue.Smart(issue).isOpen();
			System.out.println(open);
			
			//get issue in json
			//j=issue.json();
			//System.out.println(j.toString()); */
			
			
			
			//If loop to select which issues to download. 
			//0 is for all, 1 is for open only, 2 is for closed only.
			ArrayList<String> issueResponses = new ArrayList<String>();
			if ( state==0 )
			{
				
				//raw request response String
				String responseString = o.getRequest(username, password, name, repositoryName, "issues","state","all",g);
				
				int lastPage = o.getPages(responseString);
			
				
				//Loop through all pages and get response back. 
				//TODO:work on real storage, not just printing it.
				for (int i=1 ; i<=lastPage ; i++)
				{
					
					jsonRequestResponse = g
							.entry()
							.uri()
							.path("/repos/"+name+"/"+repositoryName+"/issues")
							.queryParam("state", "all")
							.queryParam("page", String.valueOf(i))
							.back()
							.method(Request.GET)
							.fetch()
							.as(JsonResponse.class)
							.json();
						
					jsonRequestArray= jsonRequestResponse.readArray();
					issueResponses.add(jsonRequestArray.toString());
				}
				for(String r : issueResponses)
				{
					System.out.println(r);
				}
			}
			else if( state ==1 )
			{
				
				//do the same for open issues
				String responseString = o.getRequest(username, password, username, repositoryName, "issues", "state", "open",g);
				int lastPage = o.getPages(responseString);
				
				for (int i=1 ; i<=lastPage ; i++)
				{
					jsonRequestResponse = g
							.entry()
							.uri()
							.path("/repos/"+name+"/"+repositoryName+"/issues")
							.queryParam("state", "open")
							.queryParam("page", String.valueOf(i))
							.back()
							.method(Request.GET)
							.fetch()
							.as(JsonResponse.class)
							.json();
						
					jsonRequestArray= jsonRequestResponse.readArray();
					issueResponses.add(jsonRequestArray.toString());
				}
				for(String r : issueResponses)
				{
					System.out.println(r);
				}
			}
			
			else if ( state == 2)
			{
				
				//do the same for closed issues
				String responseString = o.getRequest(username, password, username, repositoryName, "issues", "state", "closed",g);
				int lastPage = o.getPages(responseString);
				
				
				for (int i=1 ; i<=lastPage ; i++)
				{
					jsonRequestResponse = g
							.entry()
							.uri()
							.path("/repos/"+name+"/"+repositoryName+"/issues")
							.queryParam("state", "closed")
							.queryParam("page", String.valueOf(i))
							.back()
							.method(Request.GET)
							.fetch()
							.as(JsonResponse.class)
							.json();
						
					jsonRequestArray= jsonRequestResponse.readArray();
					issueResponses.add(jsonRequestArray.toString());
				}
				for(String r : issueResponses)
				{
					System.out.println(r);
				}
			}
			else
			{
				//TODO:print out something more informative
				System.out.println("Invalid selection of issue state parameter. Please try again");
				System.out.println("0 -> all , 1 -> open only , 2 -> closed only ");
			}
			
			
			
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}

	
	public void getCommits(String username,String password,String name,String repositoryName) throws IOException
	{
		//TODO:add by date feature?
		//login window
		Githubdownloader o = new Githubdownloader();
		Github g =o.login(username, password);
		
		
		
		//arraylist to hold responses
		ArrayList<String> commit_responses = new ArrayList<String>();
		int i=1;		
		String responseString = o.getRequest(username, password, name, repositoryName, "commits", "page", String.valueOf(i),g);
		
		
		
		while(responseString.contains("; rel=\"next\""))
		{
			
			commit_responses.add( responseString.substring( responseString.indexOf("[{\"sha\""), responseString.length() ) );
			i++;
			System.out.println("grabbing commit page "+String.valueOf(i));
			/*responseString= g
					.entry()
					.uri()
					.path("/repos/"+name+"/"+repositoryName+"/commits")
					.queryParam("page", String.valueOf(i))
					.back()
					.method(Request.GET)
					.fetch()
					.toString();*/
			responseString = o.getRequest(username, password, name, repositoryName, "commits", "page", String.valueOf(i),g);
		}
		
		//print for now, will be properly stored later.
		for(String r :commit_responses)
		{
			System.out.println(r);
		}
		
		
	}
	
	
	// pulled straight from
	// https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/porcelain/CloneRemoteRepository.java
	
	public void cloneRepo() throws IOException, InvalidRemoteException, TransportException, GitAPIException
	{
		
		String REMOTE_URL = "https://github.com/"+name+"/"+repositoryName+".git";
		File localPath = File.createTempFile("TestGitRepository","");
		
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
        	
        }
	    
	}

	
	public static void main(String[] args) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		//Login first, using username/password , or your OAuth token (later).
		//username/password are our own
		//name is the name of the github user who owns the repository we're interested in.
		//repositoryName is the repository name we're interested in.
		
		
		
		int state = 0 ;
		Githubdownloader o = new Githubdownloader();
		o.setUserInfo();
		//o.login(username, password);
		//o.getGithubUserInfo("jcabi",username,password);
		//o.getIssues(username,password,name,repositoryName,state);
		//o.getCommits(username, password, name, repositoryName);
		o.cloneRepo();
	
		
		
		

	}

	
}


