package gethub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	//just do it from the API
	
	
	
	
	
	
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
	public String getRequest(String username,String password,String name,String repositoryName,String resource, Map params, Github g)
	{
		
		
		
		String responseString=null;
		
		try {
			responseString = g
					.entry()
					.uri()
					.path("/repos/"+name+"/"+repositoryName+"/"+resource)
					.queryParams(params)
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
	
	public void getIssues(String username,String password,String name,String repositoryName,int state,String from)
	
	{
		//open a login window
		Githubdownloader o = new Githubdownloader();
		Github g =o.login(username, password);
		
		String stateParameter="all";
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
				stateParameter="all";
			}
			else if( state ==1 )
			{
				stateParameter="open";
			}
			
			else if ( state == 2)
			{
				stateParameter="closed";
			}
			else
			{
				//TODO:print out something more informative
				System.out.println("Invalid selection of issue state parameter. Will reset to default value (all) \n");
				System.out.println("0 -> all , 1 -> open only , 2 -> closed only ");
			}
			
			//actual work
			//hashmap holding the URI request parameters
			Map<String,String> params = new HashMap<String,String>();
			params.put("state", stateParameter);
			
			if(!from.isEmpty())
			{
				params.put("filter", "all");
				//will return issues UPDATED at or before since value
				params.put("since", from);
			}
			
			
			
			
			String responseString = o.getRequest(username, password, name, repositoryName, "issues", params, g);
			
			int lastPage = o.getPages(responseString);
			//System.out.println(lastPage);
		
			
			//Loop through all pages and get response back. 
			//TODO:work on real storage, not just printing it.
		
			for (int i=1 ; i<=lastPage ; i++)
			{
				
				jsonRequestResponse = g
						.entry()
						.uri()
						.path("/repos/"+name+"/"+repositoryName+"/issues")
						.queryParams(params)
						.queryParam("page", String.valueOf(i))
						.back()
						.method(Request.GET)
						.fetch()
						.as(JsonResponse.class)
						.json();
					
				jsonRequestArray= jsonRequestResponse.readArray();
				issueResponses.add(jsonRequestArray.toString());
			}
			params.clear();
			for(String r : issueResponses)
			{
				System.out.println(r);
			}
					
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}

	
	public void getCommits(String username,String password,String name,String repositoryName,String from, String to) throws IOException
	{
		//TODO:add by author support?
		//login window
		Githubdownloader o = new Githubdownloader();
		Github g =o.login(username, password);
		Map<String,String> params = new HashMap<String,String>();
		int i=1;	
		
		params.put("page", String.valueOf(i));
		
		if ( ( !from.isEmpty() ) || (!to.isEmpty()  ) )
		{
			//TODO: leave that as is or give branch option?
			
			params.put("sha", "master");
		}
		
		if ( ( !from.isEmpty() ) )
		{
			params.put("since", from);
		}
		
		if ( ( !to.isEmpty() ) )
		{
			params.put("until", to);
		}
		
		
		
		//arraylist to hold responses
		
		ArrayList<String> commit_responses = new ArrayList<String>();
			
		String responseString = o.getRequest(username, password, name, repositoryName, "commits", params,g);

		
		
		//must match exactly with API, even the spaces.
		
		while(responseString.contains("; rel=\"next\""))
		{
			//trim what we don't need, and keep the json info
			
			commit_responses.add( responseString.substring( responseString.indexOf("[{\"sha\""), responseString.length() ) );
			System.out.println("grabbing commit page "+String.valueOf(i));
			i++;
			//next page
			
			params.put("page", String.valueOf(i));
			responseString = o.getRequest(username, password, name, repositoryName, "commits", params,g);
		}
		//and the last one
		
		System.out.println("grabbing commit page "+String.valueOf(i));
		commit_responses.add( responseString.substring( responseString.indexOf("[{\"sha\""), responseString.length() ) );
		//print for now, will be properly stored later.
		
		params.clear();
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
		
		//TODO:enforce date pattern with simpledateformat
		//decide how user inputs from-to dates.change hardcoded values
		
		
		
		
		int state = 0 ;
		Githubdownloader o = new Githubdownloader();
		o.setUserInfo();
		//o.login(username, password);
		//o.getGithubUserInfo("jcabi",username,password);
		//o.getIssues(username,password,name,repositoryName,state,"2015-01-01");
		o.getCommits(username, password, name, repositoryName,"2015-01-01","2015-08-08");
		//o.cloneRepo();
	
		
		
		

	}

	
}


