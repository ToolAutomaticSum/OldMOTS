package model.task.process.TF_IDF;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import exception.LacksOfFeatures;
import model.task.process.AbstractProcess;
import optimize.SupportADNException;
import reader_writer.Writer;
import textModeling.Corpus;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordIndex;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class LearningTF_IDF extends AbstractProcess {

	protected String pathModel;
	private boolean liveLearning = false;
	protected List<Corpus> listLearningDoc = new ArrayList<Corpus>();
	
	public LearningTF_IDF(int id) throws SupportADNException, NumberFormatException, LacksOfFeatures {
		super(id);
		summary = null;
	}

	@Override
	public void init() throws Exception {
		listLearningDoc.clear();
		
		pathModel = getModel().getProcessOption(id, "PathModel");
		liveLearning = Boolean.parseBoolean(getModel().getProcessOption(id, "LiveLearning"));
		if (liveLearning) {
			listLearningDoc.addAll(getModel().getCurrentMultiCorpus());
			listLearningDoc.remove(getSummarizeCorpusId());
		}
	}

	@Override
	public void process() throws Exception {
		if (liveLearning) {
			for (Corpus c : listLearningDoc)
				generateDictionary(c, index);
		}
		else
			generateDictionary(getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()), index);
	}

	@Override
	public void finish() throws Exception {
		writeTF_IDFModel();
	}
	
	/**
	 * Construction du dictionnaire des mots des documents ({@see WordTF_IDF})
	 */
	public static void generateDictionary(Corpus corpus, Index dictionnary) {

		dictionnary.setNbDocument(dictionnary.getNbDocument()+corpus.size());
		
		//Construction du dictionnaire
		Iterator<TextModel> textIt = corpus.iterator();
		while (textIt.hasNext()) {
			TextModel textModel = textIt.next();
			Iterator<ParagraphModel> paragraphIt = textModel.iterator();
			while (paragraphIt.hasNext()) {
				ParagraphModel paragraphModel = paragraphIt.next();
				Iterator<SentenceModel> sentenceIt = paragraphModel.iterator();
				while (sentenceIt.hasNext()) {
					SentenceModel sentenceModel = sentenceIt.next();
					
					Iterator<WordModel> wordIt = sentenceModel.iterator();
					while (wordIt.hasNext()) {
						WordModel word = wordIt.next();
						//TODO ajouter filtre à la place de getmLemma
						if (!word.isStopWord()) {
							if(!dictionnary.containsKey(word.getmLemma())) {
								WordTF_IDF w = new WordTF_IDF(word.getmLemma(), dictionnary);
								w.addDocumentOccurence(corpus.getiD(), textModel.getTextID());
								dictionnary.put(word.getmLemma(), w);
							}
							else {
								WordTF_IDF w = (WordTF_IDF) dictionnary.get(word.getmLemma());
								w.addDocumentOccurence(corpus.getiD(), textModel.getTextID());
							}
							dictionnary.get(word.getmLemma()).add(word); //Ajout au wordIndex des WordModel correspondant
						}
					}
				}
			}
		}
		dictionnary.putCorpusNbDoc(corpus.getiD(), corpus.size());
	}
	
	private void writeTF_IDFModel() {
		Writer w = new Writer(pathModel + File.separator + "TF_IDF_Model.txt");
		w.open();
		w.write(String.valueOf(index.getNbDocument()) + "\n");
		for (WordIndex wordIndex : index.values()) {
			WordTF_IDF word = (WordTF_IDF) wordIndex;
			w.write(word.getWord() + "\t" + word.getId() + "\t" + word.getNbDocumentWithWordSeen() + "\t" + word.getIdf() + "\n");
		}
	}
}
