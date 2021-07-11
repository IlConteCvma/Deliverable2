package logic.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class Ticket {
	private String key;
	private LocalDate resolutionDate;
	private LocalDate created;
	private List<String> affectedVersions;
	private RevCommit commit;
	
	public Ticket() {
		this.affectedVersions = new ArrayList<>();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public LocalDate getResolutionDate() {
		return resolutionDate;
	}

	public void setResolutionDate(LocalDate resolutionDate) {
		this.resolutionDate = resolutionDate;
	}

	public LocalDate getCreated() {
		return created;
	}

	public void setCreated(LocalDate created) {
		this.created = created;
	}

	public List<String> getAffectedVersions() {
		return affectedVersions;
	}

	public void addAffectedVersions(String affectedVersion) {
		this.affectedVersions.add(affectedVersion);
	}

	public RevCommit getCommit() {
		return commit;
	}

	public void setCommit(RevCommit commit) {
		this.commit = commit;
	}

	
	
	
	
}
