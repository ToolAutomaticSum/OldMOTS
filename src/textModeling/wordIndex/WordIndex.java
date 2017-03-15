package textModeling.wordIndex;

import java.util.ArrayList;

import textModeling.WordModel;

public class WordIndex extends ArrayList<WordModel>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6192277237881669695L;
	private String word;
	private Integer iD;
	protected Index index;

	public WordIndex(String word, Index index) {
		super();
		this.word = word;
		this.index = index;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public Integer getId() {
		return iD;
	}

	public void setId(int iD) {
		this.iD = iD;
	}

	public Index getDictionnary() {
		return index;
	}

	public void setDictionnary(Index dictionnary) {
		this.index = dictionnary;
	}
	
	@Override
	public boolean equals(Object arg0) {
		return super.equals(arg0);
	}
}
