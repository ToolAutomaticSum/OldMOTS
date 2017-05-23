package model.task.process.old.TF_IDF;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import exception.LacksOfFeatures;
import model.task.preProcess.GenerateTextModel;
import model.task.process.AbstractProcess;
import model.task.process.old.VectorCaracteristicBasedOut;
import optimize.SupportADNException;
import reader_writer.Reader;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class TF_IDF extends AbstractProcess implements VectorCaracteristicBasedOut {
	
	private Map<SentenceModel, double[]> sentenceCaracteristic;
	private boolean loadModel;
	private String pathModel;
	
	public TF_IDF(int id) throws SupportADNException, NumberFormatException, LacksOfFeatures {
		super(id);
	}
	
	@Override
	public AbstractProcess makeCopy() throws Exception {
		TF_IDF p = new TF_IDF(id);
		initCopy(p);
		p.setLoadModel(loadModel);
		p.setPathModel(pathModel);
		return p;
	}

	@Override
	public void init() throws Exception {
		super.init();
		loadModel = Boolean.parseBoolean(getModel().getProcessOption(id, "LoadModel"));	
		if (loadModel) {
			pathModel = getModel().getProcessOption(id, "PathModel");
			loadModel();
			LearningTF_IDF.generateDictionary(corpusToSummarize, index);
		}
		else {
			LearningTF_IDF.generateDictionary(corpusToSummarize, index);
			for (Corpus c : getCurrentMultiCorpus()) {
				if (c!=corpusToSummarize) {
					Corpus temp = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", c, readStopWords);
					LearningTF_IDF.majIDFDictionnary(temp, index);
					if (!getModel().isMultiThreading())
						temp.clear();
				}
			}
			//LearningTF_IDF.writeTF_IDFModel("/home/valnyz", index, corpusToSummarize.getiD());
		}
	}
	
	/**
	 * Génération des vecteurs TF_IDF des phrases, récupérable avec getCaracteristic() de SentenceModel
	 * @throws Exception 
	 */
	@Override
	public void process() throws Exception {
		sentenceCaracteristic = new HashMap<SentenceModel, double[]>();
		for (TextModel text : corpusToSummarize) {
			Iterator<SentenceModel> sentenceIt = text.iterator();
			while (sentenceIt.hasNext()) {
				SentenceModel sentenceModel = sentenceIt.next();
				double[] tfIdfVector = new double[index.size()];
				Iterator<WordModel> wordIt = sentenceModel.iterator();
				while (wordIt.hasNext()) {
					WordModel wm = wordIt.next();
					if (!wm.isStopWord()) {
						WordTF_IDF word = (WordTF_IDF) index.get(wm.getmLemma());
						tfIdfVector[word.getId()]+=word.getTfCorpus(getSummarizeCorpusId())*word.getIdf();
					}
				}
				sentenceCaracteristic.put(sentenceModel, tfIdfVector);
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

	public void setLoadModel(boolean loadModel) {
		this.loadModel = loadModel;
	}

	public void setPathModel(String pathModel) {
		this.pathModel = pathModel;
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}
}
