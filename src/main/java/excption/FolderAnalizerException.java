package excption;



public class FolderAnalizerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8933479875684288383L;
	
	public FolderAnalizerException() {
		super();
	}
	
	public FolderAnalizerException(FolderAnalizerExcepType type, String msg) {
		super(type.toString() + ": "+ msg);
		printStackTrace();
		
	}
}
