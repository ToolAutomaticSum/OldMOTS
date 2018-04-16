package main.java.liasd.asadera.optimize;

public class SupportADNException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9218061768746835539L;

	public SupportADNException() {
		super("Individu need a support for his ADN.");
	}

	public SupportADNException(String e) {
		super("Individu need a support for his ADN : " + e);
	}
}
