package main.java.liasd.asadera.exception;

public class LacksOfFeatures extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 319024350713774462L;

	public LacksOfFeatures() {
		super("Lack of features.");
	}

	public LacksOfFeatures(String e) {
		super("Lack of feature : " + e);
	}
}
