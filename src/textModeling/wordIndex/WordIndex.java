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
	protected Index dictionnary;
	//private List<WordModel> listWord = new ArrayList<WordModel>();

	public WordIndex(String word, Index dictionnary) {
		super();
		this.word = word;
		this.dictionnary = dictionnary;
	}

	/*public WordIndex(String word, Index dictionnary, int iD) {
		super();
		this.word = word;
		this.dictionnary = dictionnary;
		this.iD = iD;
	}
	*/
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
		return dictionnary;
	}

	public void setDictionnary(Index dictionnary) {
		this.dictionnary = dictionnary;
	}
	
	/*public List<WordModel> getListWord() {
		return listWord;
	}

	public void setListWord(List<WordModel> listWord) {
		this.listWord = listWord;
	}

	@Override
	public String toString() {
		return String.valueOf(listWord.size());
	}*/
	
	
}
