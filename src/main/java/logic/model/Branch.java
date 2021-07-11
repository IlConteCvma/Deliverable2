package logic.model;

import org.eclipse.jgit.lib.ObjectId;

public class Branch {
	private String name;
	private ObjectId objectId;
	
	public Branch(String name,ObjectId objectId) {
		this.name = name;
		this.setObjectId(objectId);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ObjectId getObjectId() {
		return objectId;
	}

	public void setObjectId(ObjectId objectId) {
		this.objectId = objectId;
	}
	
}
