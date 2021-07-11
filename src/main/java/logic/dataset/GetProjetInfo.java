package logic.dataset;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import logic.model.Release;


public class GetProjetInfo {
	
	private String projectName;
	private ArrayList<Release> releases;
	
	
	public GetProjetInfo(String project){
		
		this.projectName = project;
		releases = new ArrayList<>();
		
	}
	
	public List<Release> getAllReleases() throws JSONException, IOException {
		
		String url = "https://issues.apache.org/jira/rest/api/2/project/" + projectName;
		
		JSONObject json = JSONQuery.readJsonFromUrl(url);
		JSONArray versions = json.getJSONArray("versions");
		Release tmp;
		
		
		int actualId = 1;
        
        for (int i = 0; i < versions.length(); i++ ) {
            String name = "";
            String id = "";
            
            
            if(versions.getJSONObject(i).has("releaseDate")) {
               if (versions.getJSONObject(i).has("name"))
                   name = versions.getJSONObject(i).get("name").toString();
               
               if (versions.getJSONObject(i).has("id"))
            	   id = versions.getJSONObject(i).get("id").toString();
               
               LocalDate date = LocalDate.parse(versions.getJSONObject(i).getString("releaseDate").subSequence(0, 10));
               
               //create Release 
               tmp = new Release(actualId, id, name, date);
               //add to array
               releases.add(tmp);
               actualId++;
            }
         }
        
        
       
        
        return releases;
        		
	}
	
	
	

	
}
