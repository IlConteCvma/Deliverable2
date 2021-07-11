package logic.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONQuery {
	
	private JSONQuery() {
	    throw new IllegalStateException("Utility class");
	  }
	
	//Function for extract a Json from a url
	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	      InputStream is = new URL(url).openStream();	     
	      try( InputStreamReader tmp = new InputStreamReader(is, StandardCharsets.UTF_8);
	    	   BufferedReader rd = new BufferedReader(tmp);
	    	)  
	      
	      {
	         String jsonText = readAll(rd);
	         return new JSONObject(jsonText);
	         
	       } finally {
	         is.close();
	       }
	   }
	
	private static String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	   }
}
