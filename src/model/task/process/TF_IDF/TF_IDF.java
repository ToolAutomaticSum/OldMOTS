package model.task.process.TF_IDF;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import exception.LacksOfFeatures;
import model.task.process.AbstractProcess;
import model.task.process.VectorCaracteristicBasedOut;
import optimize.SupportADNException;
import reader_writer.Reader;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class TF_IDF extends AbstractProcess implements VectorCaracteristicBasedOut {

	static {
		supportADN = new HashMap<String, Class<?>>();
	}
	
	private Map<SentenceModel, double[]> sentenceCaracteristic;
	private boolean loadModel;
	private String pathModel;
	//private String pathCorpus;
	
	public TF_IDF(int id) throws SupportADNException, NumberFormatException, LacksOfFeatures {
		super(id);
	}

	@Override
	public void init() throws Exception {
		super.init();
		loadModel = Boolean.parseBoolean(getModel().getProcessOption(id, "LoadModel"));	
		if (loadModel) {
			pathModel = getModel().getProcessOption(id, "PathModel");
			loadModel();
			LearningTF_IDF.generateDictionary(getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()), index);
		}
		else {
			//pathCorpus = getModel().getProcessOption(id, "PathCorpus");
			for (int i = 0; i<getModel().getCurrentMultiCorpus().size();i++) {
				if (i!=getSummarizeCorpusId()) {
					LearningTF_IDF.generateDictionary(getModel().getCurrentMultiCorpus().get(i), index);
				}
			}
			LearningTF_IDF.generateDictionary(getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()), index);
		}
	}
	
	/**
	 * Génération des vecteurs TF_IDF des phrases, récupérable avec getCaracteristic() de SentenceModel
	 * @throws Exception 
	 */
	@Override
	public void process() throws Exception {
		sentenceCaracteristic = new HashMap<SentenceModel, double[]>();
		
		Iterator<TextModel> textIt = getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()).iterator();
		while (textIt.hasNext()) {
			TextModel textModel = textIt.next();
			Iterator<ParagraphModel> paragraphIt = textModel.iterator();
			while (paragraphIt.hasNext()) {
				ParagraphModel paragraphModel = paragraphIt.next();
				Iterator<SentenceModel> sentenceIt = paragraphModel.iterator();
				while (sentenceIt.hasNext()) {
					SentenceModel sentenceModel = sentenceIt.next();
					double[] tfIdfVector = new double[index.size()];
					Iterator<WordModel> wordIt = sentenceModel.iterator();
					while (wordIt.hasNext()) {
						WordModel wm = wordIt.next();
						if (!wm.isStopWord()) {
							WordTF_IDF word = (WordTF_IDF) index.get(wm.getmLemma());
							tfIdfVector[word.getId()]=word.getTfCorpus(getSummarizeCorpusId())*word.getIdf();
						}
					}
					sentenceCaracteristic.put(sentenceModel, tfIdfVector);
				}
			}
		}
		
		super.process();
	}
	
	private void loadModel() {
		Reader r = new Reader(pathModel, true);
		
		r.open();
		String text = r.read();
		if (text!= null) {
			index.setNbDocument(Integer.parseInt(text));
			text = r.read();
		}
		while (text != null)
        {
			String[] tfidf = text.split("\t");
			if (!tfidf[0].equals("") && !tfidf[0].equals(" ")) {
				index.put(tfidf[0], new WordTF_IDF(tfidf[0], index, Integer.parseInt(tfidf[2])));
			}
			text = r.read();
        }
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}
}
