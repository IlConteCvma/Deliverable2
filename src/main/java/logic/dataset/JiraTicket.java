package logic.dataset;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import logic.model.Release;
import logic.model.Ticket;


public class JiraTicket {
	private String projectName;
	
	private static final Integer STEP = 1000;
	private static final Logger LOG = LoggerFactory.getLogger(JiraTicket.class);
	
	public JiraTicket(String project) {
		this.projectName = project;
		
	}
	
	//this function call Jira to get bug tickets
	private List<Ticket> getBugTickets() throws IOException, JSONException{
		//
		Integer start = 0;
		Integer end = 0;
		Integer total = 1;
		
		List<Ticket> tickets = new ArrayList<>();
		
		do {
			end = start + STEP;
			
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
					+ projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
					+"%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22%20ORDER%20BY%20%20resolutiondate%20%20ASC&fields=key,resolutiondate,versions,created&startAt="
					+ start.toString() + "&maxResults=" + end.toString();
			
			//Obtain the JSON
			JSONObject json = JSONQuery.readJsonFromUrl(url);
			
			//starting get info from JSON
			total = json.getInt("total");
			JSONArray issues = json.getJSONArray("issues");
			
			Ticket tmp; 
			//THIS FOR ALSO INCREMENT START
			for (; start < total && start < end; start++) {
				
				JSONObject actualIssue = issues.getJSONObject(start%STEP);
				JSONObject fields = actualIssue.getJSONObject("fields");
				JSONArray versions = fields.getJSONArray("versions");
				tmp = new Ticket();
				
				tmp.setKey(actualIssue.getString("key"));
				LocalDate resolutionDate = LocalDate.parse(fields.getString("resolutiondate").subSequence(0, 10));
				tmp.setResolutionDate(resolutionDate);
				LocalDate created = LocalDate.parse(fields.getString("created").subSequence(0, 10));
				tmp.setCreated(created);
				
				int numberOfVersion = versions.length();
				if(numberOfVersion != 0) {
					for (int i = 0; i < numberOfVersion; i++) {
						
						tmp.addAffectedVersions(versions.getJSONObject(i).getString("name"));
						
					}
				}
				
				
				tickets.add(tmp);
				
			}
			
		} while (start < total);
		
		
		
		return tickets;
	}
	
	
	
	//apply git commit filter
	public List<Ticket> getTickets(GitQuery gitQuery, List<Release> releases) throws IOException, JSONException {
		List<Ticket> tickets = getBugTickets();
		List<Ticket> filteredTickets = new ArrayList<>();
		ProportionClass proportion = new ProportionClass(releases);
		int noCommit = 0;
		boolean ret = true;
		
		try {
			//scan tickets and check commit
			for (Ticket item : tickets) {
				
				//only check if there is relative commit for the ticket
				if(gitQuery.checkCommit(item)) {

					// compute affected version if not present
					if(item.getAffectedVersions().isEmpty()) {
						//proportion
							ret = proportion.setAffectedVersions(item);
					}
					
					if(ret) {
						filteredTickets.add(item);
					}else {
						LOG.error("False");
					}
					
				}else {
					noCommit++;
				}
				
			}	
			
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		LOG.info("Number of Tickets whitout commit: {}",noCommit);
		return filteredTickets;
		
	}

	
	public List<Ticket> getTicketsForRelease(List<Ticket> tickets, Release release){
		List<Ticket> filteredTickets = new ArrayList<>();
		String versiontarget = release .getVersionName();
		for (Ticket ticket : tickets) {
			//control if ticket affected version contains release
			List<String> affectedVersions = ticket.getAffectedVersions();
			
			for (String item : affectedVersions) {
				if (item.equals(versiontarget)) {
					filteredTickets.add(ticket);
					break;
				}
			}
			
		}
		
		return filteredTickets;
	}
	
	
	
}
