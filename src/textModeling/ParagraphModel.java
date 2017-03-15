package textModeling;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * � mettre en place extends ArrayList<SentenceModel>
 * @author Val
 *
 */
public class ParagraphModel extends ArrayList<SentenceModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4074259901680254039L;
	protected String paragraph;
	protected Iterator<SentenceModel> it;
	protected int nbSentence;
	
	private TextModel text;
	
	public ParagraphModel() {
		super();
		it = iterator();
	}

	public ParagraphModel(String text) {
		super();
		paragraph = text;
		it = iterator();
	}
	
	public ParagraphModel(String text, TextModel textModel) {
		super();
		paragraph = text;
		this.text = textModel;
		it = iterator();
	}
	
	public TextModel getText() {
		return text;
	}

	public void setText(TextModel text) {
		this.text = text;
	}

	public String getParagraph() {
		return paragraph;
	}

	public void setParagraph(String paragraph) {
		this.paragraph = paragraph;
	}

	/*public ArrayList<SentenceModel> getListSentences() {
		return listSentences;
	}

	public void setListSentences(ArrayList<SentenceModel> listSentences) {
		this.listSentences = listSentences;
	}*/
	
	public int getNbSentence() {
		return nbSentence;
	}

	public void setNbSentence(int nbSentence) {
		this.nbSentence = nbSentence;
	}

	@Override
	public String toString() {
		String str = "";
		for (int i = 0; i<size(); i++) {
			str+="\t\tPhrase " + i + " : \n";
			str+="\t" + get(i).toString() + "\n";
		}
		return str;
	}

	public int getNbWord() {
		int nbWord = 0;
		for (SentenceModel s : this)
			nbWord += s.size();
		return nbWord;
	}
}
