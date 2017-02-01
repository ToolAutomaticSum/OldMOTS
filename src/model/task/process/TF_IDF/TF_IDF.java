package model.task.process.TF_IDF;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import model.task.VectorCaracteristicBasedOut;
import model.task.process.AbstractProcess;
import reader_writer.Reader;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class TF_IDF extends AbstractProcess implements VectorCaracteristicBasedOut {

	private Map<SentenceModel, double[]> sentenceCaracteristic;
	private boolean loadModel;
	private String pathModel;
	//private String pathCorpus;
	
	public TF_IDF(int id) {
		super(id);
	}

	@Override
	public void init() throws Exception {
		super.init();
		loadModel = Boolean.parseBoolean(getModel().getProcessOption(id, "LoadModel"));	
		if (loadModel) {
			pathModel = getModel().getProcessOption(id, "PathModel");
			loadModel();
			LearningTF_IDF.generateDictionary(getModel().getDocumentModels(), dictionnary, hashMapWord);
		}
		else {
			//pathCorpus = getModel().getProcessOption(id, "PathCorpus");
			LearningTF_IDF.generateDictionary(getModel().getDocumentModels(), dictionnary, hashMapWord);
		}
	}
	
	/**
	 * Génération des vecteurs TF_IDF des phrases, récupérable avec getCaracteristic() de SentenceModel
	 * @throws Exception 
	 */
	@Override
	public void process() throws Exception {
		sentenceCaracteristic = new HashMap<SentenceModel, double[]>();
		
		Iterator<TextModel> textIt = getModel().getDocumentModels().iterator();
		while (textIt.hasNext()) {
			TextModel textModel = textIt.next();
			Iterator<ParagraphModel> paragraphIt = textModel.iterator();
			while (paragraphIt.hasNext()) {
				ParagraphModel paragraphModel = paragraphIt.next();
				Iterator<SentenceModel> sentenceIt = paragraphModel.iterator();
				while (sentenceIt.hasNext()) {
					SentenceModel sentenceModel = sentenceIt.next();
					double[] tfIdfVector = new double[dictionnary.size()];
					Iterator<WordModel> wordIt = sentenceModel.iterator();
					while (wordIt.hasNext()) {
						WordModel wm = wordIt.next();
						WordTF_IDF word = (WordTF_IDF) dictionnary.get(wm.getmLemma());
						//tfIdfVector[word.getId()]++;
						tfIdfVector[word.getId()]=word.getTf();
					}
					wordIt = sentenceModel.iterator();
					while (wordIt.hasNext()) {
						WordModel wm = wordIt.next();
						WordTF_IDF word = (WordTF_IDF) dictionnary.get(wm.getmLemma());
						tfIdfVector[word.getId()]*=word.getIdf();
					}
					//sentenceModel.getCaracteristic().setdTab(tfIdfVector);//.setScore(score);
					sentenceCaracteristic.put(sentenceModel, tfIdfVector);
				}
			}
		}
		
		super.process();
	}

	/**
	 * Construction du dictionnaire des mots des documents ({@see WordTF_IDF})
	 */
	/*private void generateDictionary() {
		int nbSentence = 0;
		for (int i = 0; i < getModel().getDocumentModels().size(); i++) {
			nbSentence += getModel().getDocumentModels().get(i).getNbSentence();
		}
		
		dictionnary.setNbSentence(dictionnary.getNbSentence()+nbSentence);
		
		//Construction du dictionnaire
		int idWord = dictionnary.size();
		Iterator<TextModel> textIt = getModel().getDocumentModels().iterator();
		while (textIt.hasNext()) {
			TextModel textModel = textIt.next();
			Iterator<ParagraphModel> paragraphIt = textModel.getListParagraph().iterator();
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
	}*/
	
	private void loadModel() {
		Reader r = new Reader(pathModel, true);
		
		r.open();
		String text = r.read();
		if (text!= null) {
			dictionnary.setNbSentence(Integer.parseInt(text));
			text = r.read();
		}
		while (text != null)
        {
			String[] tfidf = text.split("\t");
			dictionnary.put(tfidf[0], new WordTF_IDF(tfidf[0], dictionnary, Integer.parseInt(tfidf[1]), Integer.parseInt(tfidf[2])));
			hashMapWord.put(Integer.parseInt(tfidf[1]), tfidf[0]);
			text = r.read();
        }
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}
}
