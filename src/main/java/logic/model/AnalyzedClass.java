package logic.model;

public class AnalyzedClass {
	private String path;
	private boolean buggy;
	private Integer loc;
	private Integer numberOfRevision;
	private Integer locTouched;
	private Integer locAdded;
	private Integer maxLocAdded;
	private Integer avgLocAdded;
	private Integer churn;
	private Integer maxChurn;
	private Integer avgChurn;
	private Integer nFix;
	private Integer chgSetSize;
	private Integer maxChgSet;
	private Integer avgChgSet;
	
	public AnalyzedClass (String path) {
		this.setPath(path);
		this.setBuggy(false);
		this.setLoc(0);
		this.nFix = 0;
	}
	
	
	public void addBug() {
		this.nFix ++;
	}

	public Integer getNFixedBug() {
		return this.nFix;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isBuggy() {
		return buggy;
	}

	public void setBuggy(boolean buggy) {
		this.buggy = buggy;
	}

	public Integer getLoc() {
		return loc;
	}


	public void setLoc(Integer loc) {
		this.loc = loc;
	}





	public Integer getNR() {
		return numberOfRevision;
	}





	public void setNR(Integer numberOfRevision) {
		this.numberOfRevision = numberOfRevision;
	}





	public Integer getLocTouched() {
		return locTouched;
	}





	public void setLocTouched(Integer locTouched) {
		this.locTouched = locTouched;
	}





	public Integer getLocAdded() {
		return locAdded;
	}





	public void setLocAdded(Integer locAdded) {
		this.locAdded = locAdded;
	}





	public Integer getMaxLocAdded() {
		return maxLocAdded;
	}





	public void setMaxLocAdded(Integer maxLocAdded) {
		this.maxLocAdded = maxLocAdded;
	}





	public Integer getAvgLocAdded() {
		return avgLocAdded;
	}





	public void setAvgLocAdded(Integer avgLocAdded) {
		this.avgLocAdded = avgLocAdded;
	}





	public Integer getChurn() {
		return churn;
	}





	public void setChurn(Integer churn) {
		this.churn = churn;
	}





	public Integer getMaxChurn() {
		return maxChurn;
	}





	public void setMaxChurn(Integer maxChurn) {
		this.maxChurn = maxChurn;
	}





	public Integer getAvgChurn() {
		return avgChurn;
	}





	public void setAvgChurn(Integer avgChurn) {
		this.avgChurn = avgChurn;
	}


	public Integer getChgSetSize() {
		return chgSetSize;
	}


	public void setChgSetSize(Integer chgSetSize) {
		this.chgSetSize = chgSetSize;
	}


	public Integer getMaxChgSet() {
		return maxChgSet;
	}


	public void setMaxChgSet(Integer maxChgSet) {
		this.maxChgSet = maxChgSet;
	}


	public Integer getAvgChgSet() {
		return avgChgSet;
	}


	public void setAvgChgSet(Integer avgChgSet) {
		this.avgChgSet = avgChgSet;
	}
	
	
	
}
