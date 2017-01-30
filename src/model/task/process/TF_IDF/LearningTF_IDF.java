package model.task.process.TF_IDF;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import model.task.process.AbstractProcess;
import reader_writer.Writer;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.WordIndex;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class LearningTF_IDF extends AbstractProcess {

	protected String pathModel;
	
	public LearningTF_IDF(int id) {
		super(id);
	}

	@Override
	public void init() throws Exception {
		pathModel = getModel().getProcessOption(id, "PathModel");	
	}

	@Override
	public void process() throws Exception {
		generateDictionary(getModel().getDocumentModels(), dictionnary, hashMapWord);
	}

	@Override
	public void finish() throws Exception {
		writeTF_IDFModel();
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
