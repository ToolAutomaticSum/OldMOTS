package main.java.liasd.asadera.textModeling.wordIndex;

public class WordVector extends WordIndex {

	private double[] wordVector;
	private int dimension;

	public WordVector(String word, int dimension) {
		super(word);
		this.dimension = dimension;
		wordVector = new double[dimension];
	}

	public WordVector(String word, Double[] wordVector) {
		super(word);
		dimension = wordVector.length;
		this.wordVector = new double[dimension];
		for (int i = 0; i < dimension; i++)
			this.wordVector[i] = wordVector[i].doubleValue();
	}

	public WordVector(String word, double[] wordVector) {
		super(word);
		this.wordVector = wordVector;
		dimension = wordVector.length;
	}

	public double[] getWordVector() {
		return wordVector;
	}

	public int getDimension() {
		return dimension;
	}
}
