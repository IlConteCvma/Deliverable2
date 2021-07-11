package excption;


public class MetricException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -608942883128036566L;
	
	
	public MetricException() {
		super();
	}
	
	public MetricException(String msg) {
		super("MetricException caused by: "+msg);
		printStackTrace();
		
	}
	
	public MetricException(String msg,StackTraceElement[] elements) {
		super("MetricException caused by: "+msg);
		this.setStackTrace(elements);
		
	}
}
