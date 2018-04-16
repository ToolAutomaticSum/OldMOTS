package main.java.liasd.asadera.exception;

public class SizeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -313317765067372494L;

	public SizeException() {
		super("Document is too large.");
	}

	public SizeException(String docName) {
		super("Document " + docName + " is too large.");
	}
}
