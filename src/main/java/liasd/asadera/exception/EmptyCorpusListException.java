package main.java.liasd.asadera.exception;

public class EmptyCorpusListException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 319024350713774462L;

	public EmptyCorpusListException() {
		super("Your list of corpus is empty.");
	}

	public EmptyCorpusListException(String multiCorpusIndex) {
		super("Your list of corpus for Multicorpus " + multiCorpusIndex + " is empty.");
	}
}
