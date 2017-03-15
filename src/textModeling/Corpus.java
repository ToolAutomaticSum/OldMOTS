package textModeling;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.SModel;

public class Corpus extends ArrayList<TextModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3211824677893360515L;

	protected int iD;
	protected String inputPath;
	protected String summaryPath;
	protected SModel model;
	protected List<String> docNames;
	protected List<String> summaryNames;
	protected SentenceModel currentSentence;
	
	public Corpus(int iD) {
		this.iD = iD;
	}
	
	public void loadDocumentModels() {
		Iterator<String> it = docNames.iterator();
		while (it.hasNext()) {
			String docName = it.next();
			this.add(new TextModel(this, inputPath + File.separator + docName));
		}
    }
	
	public SentenceModel getSentenceByID(int id) {
		int current = id;
		boolean notFind = true;
		SentenceModel sen = null;
		Iterator<TextModel> textIt = this.iterator();
		while (notFind && textIt.hasNext()) {
			TextModel text = textIt.next();
			if (text.getNbSentence() <= current)
				current -= text.getNbSentence();
			else {
				sen = text.getSentenceByID(id);
				notFind = false;
			}
		}
		return sen;
	}
	
	public int getNbWord() {
		int nbWord = 0;
		for (TextModel t : this)
			nbWord += t.getNbWord();
		return nbWord;
	}
	
	public int getNbSentence() {
		int nbSentence = 0;
		for (TextModel t : this)
			nbSentence += t.getNbSentence();
		return nbSentence;
	}

	public SModel getModel() {
		return model;
	}

	public void setModel(SModel model) {
		this.model = model;
	}

	public List<String> getDocNames() {
		return docNames;
	}

	public void setDocNames(List<String> docNames) {
		this.docNames = docNames;
	}

	public String getInputPath() {
		return inputPath;
	}

	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}

	public int getiD() {
		return iD;
	}

	public String getSummaryPath() {
		return summaryPath;
	}

	public void setSummaryPath(String summaryPath) {
		this.summaryPath = summaryPath;
	}

	public List<String> getSummaryNames() {
		return summaryNames;
	}

	public void setSummaryNames(List<String> summaryNames) {
		this.summaryNames = summaryNames;
	}
	
	@Override
	public String toString() {
		int current = 0;
		for(TextModel text : this) {
				current += text.getNbSentence();
		}
		String str = "Corpus " + iD + " \n" + current + "\n";
		return str;
	}

	public List<String> getAllStringSentence() {
		List<String> allSentenceList = new ArrayList<String>();
		for (TextModel text : this) {
			allSentenceList.addAll(text.getStringSentence());
		}
		return allSentenceList;
	}
}
