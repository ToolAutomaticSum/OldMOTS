package model.task.process.wordEmbeddings;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import model.task.postProcess.AbstractPostProcess;
import model.task.process.AbstractProcess;
import model.task.process.VectorCaracteristicBasedOut;
import optimize.SupportADNException;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.WordVector;

public class WordToVec extends AbstractProcess implements VectorCaracteristicBasedOut {

	private Map<SentenceModel, double[]> sentenceCaracteristic;
	private int dimension;
	private boolean modelLoad = false;
	
	public WordToVec(int id) throws SupportADNException {
		super(id);
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		
		/**
		 * Demande trop de ram, à tester sur serveur ou avec plus de ram.
		 */
		/**
		 * TODO Ajouter option nom modèle à charger
		 */
		if (!modelLoad) {
			File gModel = new File(getModel().getOutputPath() + File.separator + "word2vec.bin"/*"/home/valnyz/Documents/GoogleNews-vectors-negative300.bin.gz"*/);
			Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel, false);
	
		    boolean bDimension = true;
		    
			//Construire index à partir de Word2Vec object
		    for (TextModel text : corpusToSummarize) {
				Iterator<SentenceModel> sentenceIt = text.iterator();
				while (sentenceIt.hasNext()) {
					SentenceModel sentenceModel = sentenceIt.next();
					Iterator<WordModel> wordIt = sentenceModel.iterator();
					while (wordIt.hasNext()) {
						WordModel wm = wordIt.next();
						if (!wm.isStopWord() && !index.containsKey(wm.getmLemma())) {
							if (vec.hasWord(wm.getmLemma())) {
								if (bDimension) {
									dimension = vec.getWordVector(wm.getmLemma()).length;
									bDimension = false;
								}
								index.put(0, new WordVector(wm.getmLemma(), index, vec.getWordVector(wm.getmLemma())));
							}
						}
					}
				}
		    }
		    vec = null;
		    modelLoad = true;
		}
	}
	
	@Override
	public void process() throws Exception {
		sentenceCaracteristic = new HashMap<SentenceModel, double[]>();
		for (TextModel text : corpusToSummarize) {
			Iterator<SentenceModel> sentenceIt = text.iterator();
			while (sentenceIt.hasNext()) {
				SentenceModel sentenceModel = sentenceIt.next();
				double[] sentenceVector = new double[dimension];
				//int nbMot = 0;
				Iterator<WordModel> wordIt = sentenceModel.iterator();
				while (wordIt.hasNext()) {
					WordModel wm = wordIt.next();
					if (!wm.isStopWord() && index.containsKey(wm.getmLemma())) {
						//System.out.println(index.getKeyId(wm.getmLemma()));
						WordVector word = (WordVector) index.get(wm.getmLemma());
						//nbMot++;
						//System.out.println(word);
						for (int i = 0; i<dimension; i++)
							sentenceVector[i] +=  word.getWordVector()[i];
					}
				}
				sentenceCaracteristic.put(sentenceModel, sentenceVector);
			}
		}
		super.process();
	}
	
	@Override
	public void finish() throws Exception {
		Iterator<AbstractPostProcess> postProIt = postProcess.iterator();
		while (postProIt.hasNext()) {
			AbstractPostProcess p = postProIt.next();
			p.setModel(getModel());
			p.setCurrentProcess(this);
			p.init();
		}
		
		postProIt = postProcess.iterator();
		while (postProIt.hasNext()) {
			AbstractPostProcess p = postProIt.next();
			p.process();
		}
		
		postProIt = postProcess.iterator();
		while (postProIt.hasNext()) {
			AbstractPostProcess p = postProIt.next();
			p.finish();
		}
		
		//index.clear();
		corpusToSummarize.clear();
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}

}
