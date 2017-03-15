package textModeling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextModel extends ArrayList<ParagraphModel> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2340334806960704550L;

	private static int iD = 0;
	private int textID = iD;
	private Corpus parentCorpus;
	protected String documentFilePath;
	
	protected int textSize = 0;
	protected String text = "";
	//protected ArrayList<ParagraphModel> listParagraph = new ArrayList<ParagraphModel>();
	protected int nbSentence;
	
	public TextModel(Corpus parentCorpus, String filePath) {
		super();
		this.parentCorpus = parentCorpus;
		iD++;
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

	public List<SentenceModel> getSentence() {
		List<SentenceModel> listSentence = new ArrayList<SentenceModel>();
		for (ParagraphModel p : this) {
			for (SentenceModel s : p) {
				if (!s.getSentence().equals(""))
					listSentence.add(s);
			}
		}
		return listSentence;
	}
	
	public List<String> getStringSentence() {
		List<String> listSentence = new ArrayList<String>();
		for (ParagraphModel p : this) {
			for (SentenceModel s : p) {
				if (!s.getSentence().equals(""))
					listSentence.add(s.getSentence());
			}
		}
		return listSentence;
	}

	public boolean isEmpty() {
		return (/*listParagraph*/this.size() == 0);
	}

	public int getNbSentence() {
		return nbSentence;
	}

	public void setNbSentence(int nbSentence) {
		this.nbSentence = nbSentence;
	}

	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}
	
	@Override
	public String toString() {
		String str = "";
		for (int i = 0; i<size(); i++) {
			str+="Paragraphe " + i + " : \n";
			str+=get(i).toString() + "\n";
		}
		return str;
	}

	public int getTextID() {
		return textID;
	}

	public Corpus getParentCorpus() {
		return parentCorpus;
	}

	public int getNbWord() {
		int nbWord = 0;
		for (ParagraphModel p : this)
			nbWord += p.getNbWord();
		return nbWord;
	}
}
