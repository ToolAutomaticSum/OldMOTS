package main.java.liasd.asadera.exception;

public class EmptyTextListException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 319024350713774462L;

	public EmptyTextListException() {
		super("Your list of document is empty.");
	}

	public EmptyTextListException(String corpusIndex) {
		super("Your list of document for corpus " + corpusIndex + " is empty.");
	}
}
