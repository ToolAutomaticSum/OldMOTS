package textModeling;

import java.util.ArrayList;
import java.util.Iterator;

public class TextModel extends ArrayList<ParagraphModel> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2340334806960704550L;

	protected String documentFilePath;
	
	protected int textSize = 0;
	protected String text;
	//protected ArrayList<ParagraphModel> listParagraph = new ArrayList<ParagraphModel>();
	protected int nbSentence;
	
	public TextModel(String filePath) {
		super();
		documentFilePath = filePath;
	}
	
	public String getDocumentFilePath() {
		return documentFilePath;
	}

	public void setDocumentFilePath(String documentFilePath) {
		this.documentFilePath = documentFilePath;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public SentenceModel getSentenceByID(int id) {
		int current = id;
		boolean notFind = true;
		SentenceModel sen = null;
		Iterator<ParagraphModel> parIt = /*listParagraph*/this.iterator();
		while (notFind && parIt.hasNext()) {
			ParagraphModel par = parIt.next();
			if (par.size() <= current)
				current -= par.size();
			else {
				sen = par.get(current);
				notFind = false;
			}
		}
		return sen;
	}

	/*public ArrayList<ParagraphModel> getListParagraph() {
		return listParagraph;
	}

	public void setListParagraph(ArrayList<ParagraphModel> listParagraph) {
		this.listParagraph = listParagraph;
	}*/

	public boolean isEmpty() {
		return (/*listParagraph*/this.size() == 0);
	}

	public int getNbSentence() {
		return nbSentence;
	}

	public void setNbSentence(int nbSentence) {
		this.nbSentence = nbSentence;
	}

	@Override
	public String toString() {
		return text;
	}

	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}
}
