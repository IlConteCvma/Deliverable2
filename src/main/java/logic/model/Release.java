package logic.model;

import java.time.LocalDate;

public class Release {
	private Integer index;
	private String versionID;
	private String versionName;
	private LocalDate date;
	private Double pValue;
	private Branch branch;
	
	
	public Release(Integer index, String versionID, String versionName, LocalDate date ) {
		this.setDate(date);
		this.versionName = versionName;
		this.versionID = versionID;
		this.index = index;
		this.setPValue(0.0);
		this.branch = null;
	}
	
	
	
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}	
	
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public String getVersionID() {
		return versionID;
	}
	public void setVersionID(String versionID) {
		this.versionID = versionID;
	}


	public Double getPValue() {
		return pValue;
	}


	public void setPValue(Double pValue) {
		this.pValue = pValue;
	}



	public Branch getBranch() {
		return branch;
	}



	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	
}
