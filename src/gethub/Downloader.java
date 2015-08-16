package gethub;

import com.jcabi.github.Github;

public interface Downloader {

	
	Github login(String username, String password);
	
}
