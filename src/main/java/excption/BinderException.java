package excption;

public class BinderException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6886973753250161285L;
	
	public BinderException() {
		super();
	}
	
	public BinderException(String msg) {
		super("BinderException caused by: "+msg);
		printStackTrace();
		
	}

}
