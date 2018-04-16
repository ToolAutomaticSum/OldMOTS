package main.java.liasd.asadera.exception;

public class StateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -313317765067372494L;

	public StateException() {
		super("State is incorrect due to last value.");
	}

	public StateException(String string) {
		super(string);
	}
}
