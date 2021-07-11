package excption;

public class WekaException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6853059019806559990L;
	public WekaException() {
		super();
	}
	
	public WekaException(String msg) {
		super("WekaException caused by: "+msg);
		printStackTrace();
		
	}
	
	public WekaException(String msg,StackTraceElement[] elements) {
		super("WekaException caused by: "+msg);
		this.setStackTrace(elements);
		
	}
	
}
