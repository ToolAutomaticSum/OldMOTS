package textModeling;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextModel extends ArrayList<SentenceModel> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2340334806960704550L;

	private static int iD = 0;
	private int textID = iD;
	private Corpus parentCorpus;
	protected String documentFilePath;
	protected String textName;
	
	protected int textSize = 0;
	protected String text = "";

	protected int nbSentence;
	
	public TextModel(Corpus parentCorpus, String filePath) {
		super();
		this.parentCorpus = parentCorpus;
		iD++;
		documentFilePath = filePath;
		textName = documentFilePath.split(File.separator)[documentFilePath.split(File.separator).length-1];
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
		boolean notFind = true;
		SentenceModel sen = null;
		Iterator<SentenceModel> senIt = this.iterator();
		while (notFind && senIt.hasNext()) {
			SentenceModel sent = senIt.next();
			if (sent.getiD() == id) {
				sen = sent;
				notFind = false;
			}
		}
		return sen;
	}

	public List<SentenceModel> getSentence() {
		List<SentenceModel> listSentence = new ArrayList<SentenceModel>();
		for (SentenceModel s : this) {
			if (!s.getSentence().equals(""))
				listSentence.add(s);
		}
		return listSentence;
	}
	
	public List<String> getStringSentence() {
		List<String> listSentence = new ArrayList<String>();
		for (SentenceModel s : this) {
			if (!s.getSentence().equals(""))
				listSentence.add(s.getSentence());
		}
		return listSentence;
	}

	public boolean isEmpty() {
		return (this.size() == 0);
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
			str+=get(i).toString() + "\n";
		}
		return str;
	}

	public int getiD() {
		return textID;
	}

	public Corpus getParentCorpus() {
		return parentCorpus;
	}

	public int getNbWord() {
		int nbWord = 0;
		for (SentenceModel p : this)
			nbWord += p.size();
		return nbWord;
	}

	public String getTextName() {
		return textName;
	}
}
