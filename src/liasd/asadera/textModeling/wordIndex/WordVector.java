package liasd.asadera.textModeling.wordIndex;

public class WordVector extends WordIndex {

	private double[] wordVector;
	private int dimension;
	
	public WordVector(String word, Index<WordVector> index, int dimension) {
		super(word, index);
		this.dimension = dimension;
		wordVector = new double[dimension];
	}
	
	public WordVector(String word, Index<WordVector> index, Double[] wordVector) {
		super(word, index);
		dimension = wordVector.length;
		this.wordVector = new double[dimension];
		for (int i = 0; i<dimension; i++)
			this.wordVector[i] = wordVector[i].doubleValue();
	}

	public WordVector(String word, Index<WordVector> index, double[] wordVector) {
		super(word, index);
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
