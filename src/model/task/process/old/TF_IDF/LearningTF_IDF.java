package model.task.process.old.TF_IDF;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import exception.LacksOfFeatures;
import model.task.process.old.AbstractProcess;
import optimize.SupportADNException;
import reader_writer.Writer;
import textModeling.Corpus;
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
	}
	
	@Override
	public AbstractProcess makeCopy() throws Exception {
		throw new Exception("No copy allowed !");
	}

	@Override
	public void init() throws Exception {
		listLearningDoc.clear();
		
		pathModel = getModel().getProcessOption(id, "PathModel");
		liveLearning = Boolean.parseBoolean(getModel().getProcessOption(id, "LiveLearning"));
		if (liveLearning) {
			listLearningDoc.addAll(getCurrentMultiCorpus());
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
			generateDictionary(getCorpusToSummarize(), index);
	}

	@Override
	public void finish() throws Exception {
		writeIDFModel(pathModel, index);
	}
	
	/**
	 * Construction du dictionnaire des mots des documents ({@see WordTF_IDF})
	 */
	public static void generateDictionary(Corpus corpus, Index dictionnary) {
		dictionnary.setNbDocument(dictionnary.getNbDocument()+corpus.size());
		//Construction du dictionnaire
		for (TextModel textModel : corpus) {
			for (SentenceModel sentenceModel : textModel) {
				for (WordModel word : sentenceModel) {
					//TODO ajouter filtre à la place de getmLemma
					if (!word.isStopWord()) {
						if(!dictionnary.containsKey(word.getmLemma())) {
							WordTF_IDF w = new WordTF_IDF(word.getmLemma(), dictionnary);
							w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
							dictionnary.put(word.getmLemma(), w);
						}
						else {
							WordTF_IDF w = (WordTF_IDF) dictionnary.get(word.getmLemma());
							w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
						}
						//dictionnary.get(word.getmLemma()).add(word); //Ajout au wordIndex des WordModel correspondant
					}
					else if (!dictionnary.containsKey(word.getmLemma()))
						dictionnary.put(word.getmLemma(), new WordIndex(word.getmLemma(), dictionnary));
				}
			}
		}
		dictionnary.putCorpusNbDoc(corpus.getiD(), corpus.size());
	}
	
	public static void majIDFDictionnary(Corpus corpus, Index dictionnary) {
		dictionnary.setNbDocument(dictionnary.getNbDocument()+corpus.getNbDocument());			
		//Construction du dictionnaire
		for (TextModel text : corpus) {
			for (SentenceModel sentenceModel : text) {
				for (WordModel word : sentenceModel) {
					//TODO ajouter filtre à la place de getmLemma
					if (!word.isStopWord() && dictionnary.containsKey(word.getmLemma())) {
						WordTF_IDF w = (WordTF_IDF) dictionnary.get(word.getmLemma());
						w.addDocumentOccurence(corpus.getiD(), text.getiD());
						//dictionnary.get(word.getmLemma()).add(word); //Ajout au wordIndex des WordModel correspondant
					}
					//else
					//	dictionnary.put(word.getmLemma(), new WordIndex(word.getmLemma(), dictionnary));
				}
			}
		}
		dictionnary.putCorpusNbDoc(corpus.getiD(), corpus.size());
	}
	
	public static void writeIDFModel(String path, Index<WordTF_IDF> index) {
		Writer w = new Writer(path + File.separator + "TF_IDF_Model.txt");
		w.open();
		w.write(String.valueOf(index.getNbDocument()) + "\n");
		for (WordIndex wordIndex : index.values()) {
			WordTF_IDF word = (WordTF_IDF) wordIndex;
			w.write(word.getWord() + "\t" + word.getiD() + "\t" + word.getNbDocumentWithWordSeen() + "\t" + word.getIdf() + "\n");
		}
	}
	
	public static void writeTF_IDFModel(String path, Index<WordTF_IDF> index, int corpusId) {
		Writer w = new Writer(path + File.separator + "TF_IDF_Model.txt");
		w.open();
		w.write(String.valueOf(index.getNbDocument()) + "\n");
		for (WordIndex wordIndex : index.values()) {
			WordTF_IDF word = (WordTF_IDF) wordIndex;
			w.write(word.getWord() + "\t" + word.getiD() + "\t" + word.getTfCorpus(corpusId) + "\t" + word.getIdf() + "\n");
		}
	}
}
