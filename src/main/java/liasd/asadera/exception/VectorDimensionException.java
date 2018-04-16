package main.java.liasd.asadera.exception;

public class VectorDimensionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -871275990622634164L;

	public VectorDimensionException() {
		super("Vector need the same dimension for scalar product.");
	}

	public VectorDimensionException(String string) {
		super(string);
	}
}
