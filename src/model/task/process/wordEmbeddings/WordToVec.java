package model.task.process.wordEmbeddings;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import model.task.preProcess.GenerateTextModel;
import model.task.process.AbstractProcess;
import model.task.process.VectorCaracteristicBasedOut;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.WordVector;

public class WordToVec extends AbstractProcess implements VectorCaracteristicBasedOut {

	private Map<SentenceModel, double[]> sentenceCaracteristic;
	
	private int dimension;
	private boolean modelLoad = false;
	private Word2Vec vec;
	
	public WordToVec(int id) throws SupportADNException {
		super(id);
	}
	
	@Override
	public AbstractProcess makeCopy() throws Exception {
		WordToVec p = new WordToVec(id);
		initCopy(p);
		p.setDimension(dimension);
		p.setModelLoad(modelLoad);
		return p;
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		
		/**
		 * Demande trop de ram, à tester sur serveur ou avec plus de ram.
		 */
		if (!modelLoad) {
			//File gModel = new File(getModel().getProcessOption(id, "ModelPath"));
			System.out.println(getModel().getProcessOption(id, "ModelPath"));
			vec = WordVectorSerializer.readWord2VecModel(getModel().getProcessOption(id, "ModelPath"), true);
			
		    System.out.println(vec.vocab().numWords());
		   
		    System.out.println("Load Pre-trained Model");
		    //vec = null;
		    modelLoad = true;
		    
			for (Corpus c : getCurrentMultiCorpus()) {
				c = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", c, readStopWords);
				//LearningWordToVecModel.learnWordToVec(vec, temp);
				//System.out.println(c.getCorpusName() + "\t" + vec.vocab().numWords());
				//temp.clear();
			}
			LearningWordToVecModel.learnWordToVecMultiCorpus(vec, getCurrentMultiCorpus());
		    for (Corpus c : getCurrentMultiCorpus()) {
		    	if (c != corpusToSummarize)
		    		c.clear();
		    }
		    	
		    System.out.println(vec.vocab().numWords());
		    modelLoad = true;
		}

	    boolean bDimension = true;

	    int nbMotText = 0;
	    int nbMotWE = 0;
		//Construire index à partir de Word2Vec object
	    for (TextModel text : corpusToSummarize) {
			Iterator<SentenceModel> sentenceIt = text.iterator();
			while (sentenceIt.hasNext()) {
				SentenceModel sentenceModel = sentenceIt.next();
				Iterator<WordModel> wordIt = sentenceModel.iterator();
				while (wordIt.hasNext()) {
					WordModel wm = wordIt.next();
					nbMotText++;
					if (!wm.isStopWord() && !index.containsKey(wm.getmLemma())) {
						if (vec.hasWord(wm.getmLemma())) {
							nbMotWE++;
							if (bDimension) {
								dimension = vec.getWordVector(wm.getmLemma()).length;
								bDimension = false;
							}
							index.put(0, new WordVector(wm.getmLemma(), index, vec.getWordVector(wm.getmLemma())));
						}
						else 
							System.out.println(wm.getmLemma());
					}
				}
			}
	    }
	    System.out.println("Modèle chargé !");
	    System.out.println(nbMotText);
	    System.out.println(nbMotWE);
	}
	
	@Override
	public void process() throws Exception {
		sentenceCaracteristic = new HashMap<SentenceModel, double[]>();
		for (TextModel text : corpusToSummarize) {
			for (SentenceModel sentenceModel : text) {
				double[] sentenceVector = new double[dimension];
				//int nbMot = 0;
				for (WordModel wm : sentenceModel) {
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
	
	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public void setModelLoad(boolean modelLoad) {
		this.modelLoad = modelLoad;
	}
	
	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}

}
