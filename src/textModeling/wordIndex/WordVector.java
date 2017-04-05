package textModeling.wordIndex;

public class WordVector extends WordIndex {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2288500102579878483L;

	private double[] wordVector;
	private static int dimension;
	
	public WordVector(String word, Index index) {
		super(word, index);
	}

	public WordVector(String word, Index index, double[] wordVector) {
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
