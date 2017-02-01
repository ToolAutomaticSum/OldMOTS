package model.task.process.TF_IDF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import model.task.process.AbstractProcess;
import reader_writer.Writer;
import textModeling.Corpus;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.WordIndex;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class LearningTF_IDF extends AbstractProcess {

	protected String pathModel;
	private boolean liveLearning = false;
	protected List<Corpus> listLearningDoc = new ArrayList<Corpus>();
	
	public LearningTF_IDF(int id) {
		super(id);
		summary = null;
	}

	@Override
	public void init() throws Exception {
		listLearningDoc.clear();
		
		pathModel = getModel().getProcessOption(id, "PathModel");
		liveLearning = Boolean.parseBoolean(getModel().getProcessOption(id, "LiveLearning"));
		if (liveLearning) {
			String[] listCorpus = getModel().getProcessOption(id, "LearningCorpusID").trim().split("\n");
			String[] learningCorpus = listCorpus[getModel().getDocumentModels().getiD()].trim().split(" ");
			for (int i = 1; i<learningCorpus.length;i++)
				listLearningDoc.add(getModel().getCorpusModels().get(Integer.parseInt(learningCorpus[i])));
		}
	}

	@Override
	public void process() throws Exception {
		if (liveLearning) {
			for (Corpus c : listLearningDoc)
				generateDictionary(c, dictionnary, hashMapWord);
		}
		else
			generateDictionary(getModel().getDocumentModels(), dictionnary, hashMapWord);
	}

	@Override
	public void finish() throws Exception {
		if (liveLearning)
			writeTF_IDFModel();
		else {
			if (getModel().getDocumentModels().equals(getModel().getCorpusModels().get(getModel().getCorpusModels().size()-1)))
				writeTF_IDFModel();
		}
	}
	
	/**
	 * Construction du dictionnaire des mots des documents ({@see WordTF_IDF})
	 */
	public static void generateDictionary(List<TextModel> listText, Dictionnary dictionnary, Map<Integer, String> hashMapWord) {
		
		int nbSentence = 0;
		for (int i = 0; i < listText.size(); i++) {
			nbSentence += listText.get(i).getNbSentence();
		}
		
		dictionnary.setNbSentence(dictionnary.getNbSentence()+nbSentence);
		
		//Construction du dictionnaire
		int idWord = 0;
		Iterator<TextModel> textIt = listText.iterator();
		while (textIt.hasNext()) {
			TextModel textModel = textIt.next();
			Iterator<ParagraphModel> paragraphIt = textModel.iterator();
			while (paragraphIt.hasNext()) {
				ParagraphModel paragraphModel = paragraphIt.next();
				Iterator<SentenceModel> sentenceIt = paragraphModel.iterator();
				while (sentenceIt.hasNext()) {
					SentenceModel sentenceModel = sentenceIt.next();
					Set<String> wordSet = new TreeSet<String>();
					for (int i = 0;i<sentenceModel.size();i++)
						wordSet.add(sentenceModel.get(i).getmLemma());
					
					Iterator<WordModel> wordIt = sentenceModel.iterator();
					while (wordIt.hasNext()) {
						WordModel word = wordIt.next();
						if(!dictionnary.containsKey(word.getmLemma())) {
							dictionnary.put(word.getmLemma(), new WordTF_IDF(word.getmLemma(), dictionnary, idWord));
							hashMapWord.put(idWord, word.getmLemma());
							idWord++;
						}
						if (wordSet.contains(word.getmLemma())) {
							wordSet.remove(word.getmLemma());
							((WordTF_IDF)dictionnary.get(word.getmLemma())).incrementSentenceWithWordSeen();
						}
						dictionnary.get(word.getmLemma()).add(word); //Ajout au wordEmbeddings des WordModel correspondant
					}
				}
			}
		}
	}
	
	private void writeTF_IDFModel() {
		Writer w = new Writer(pathModel + "\\TF_IDF_Model.txt");
		w.open();
		w.write(String.valueOf(dictionnary.getNbSentence()) + "\n");
		for (WordIndex wordIndex : dictionnary.values()) {
			WordTF_IDF word = (WordTF_IDF) wordIndex;
			w.write(word.getWord() + "\t" + word.getId() + "\t" + word.getNbDocumentWithWordSeen() + "\t" + word.getIdf() + "\n");
		}
	}
}
