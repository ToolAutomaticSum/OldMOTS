package model.task.process.indexBuilder.wordEmbeddings;

import java.io.File;
import java.util.Iterator;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import model.task.preProcess.GenerateTextModel;
import model.task.process.indexBuilder.AbstractIndexBuilder;
import model.task.process.indexBuilder.IndexBasedIn;
import model.task.process.indexBuilder.IndexBasedOut;
import model.task.process.indexBuilder.wordEmbeddings.ld4j.MultiCorpusSentenceIterator;
import model.task.process.indexBuilder.wordEmbeddings.ld4j.SentenceIterator;
import model.task.process.indexBuilder.wordEmbeddings.ld4j.TokenizerFactory;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.MultiCorpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordVector;

public class WordToVec extends AbstractIndexBuilder<WordVector> implements IndexBasedOut<WordVector> {

	private int dimension;
	private boolean modelLoad = false;
	private Word2Vec vec;
	
	public WordToVec(int id) throws SupportADNException {
		super(id);
	}
	
	@Override
	public WordToVec makeCopy() throws Exception {
		WordToVec p = new WordToVec(id);
		initCopy(p);
		p.setDimension(dimension);
		p.setModelLoad(modelLoad);
		return p;
	}
	
	@Override
	public void initADN() throws Exception {
	}

	@Override
	public void processIndex() throws Exception {/**
		 * Demande trop de ram, à tester sur serveur ou avec plus de ram.
		 */
		if (!modelLoad) {
			//File gModel = new File(getModel().getProcessOption(id, "ModelPath"));
			//System.out.println(getModel().getProcessOption(id, "ModelPath"));
			vec = WordVectorSerializer.readWord2VecModel(getModel().getProcessOption(id, "ModelPath"), true);
			
		    System.out.println("Model size : " + vec.vocab().numWords());
		   
		    System.out.println("Load Pre-trained Model");
		    modelLoad = true;
		    
			for (Corpus c : getCurrentMultiCorpus()) {
				c = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", c, readStopWords);
				//LearningWordToVecModel.learnWordToVec(vec, temp);
				//System.out.println(c.getCorpusName() + "\t" + vec.vocab().numWords());
				//temp.clear();
			}
			WordToVec.learnWordToVecMultiCorpus(vec, getCurrentMultiCorpus());
		    for (Corpus c : getCurrentMultiCorpus()) {
		    	if (c != getCurrentProcess().getCorpusToSummarize())
		    		c.clear();
		    }
		    	
		    System.out.println(vec.vocab().numWords());
		    modelLoad = true;
		}

	    boolean bDimension = true;

	    int nbMotText = 0;
	    int nbMotWE = 0;
		//Construire index à partir de Word2Vec object
	    for (TextModel text : getCurrentProcess().getCorpusToSummarize()) {
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
	
	public static void learnWordToVecMultiCorpus(Word2Vec vec, MultiCorpus multiCorpus) {
		MultiCorpusSentenceIterator sentenceIterator = new MultiCorpusSentenceIterator(multiCorpus);
        TokenizerFactory tokenizerFactory = new TokenizerFactory();

        /**TokenizerFactory first then SentenceIterator, use to build sequenceIterator inside the class.*/
        vec.setTokenizerFactory(tokenizerFactory); 
        vec.setSentenceIter(sentenceIterator);
        vec.buildVocab();
        vec.fit();
	}
	
	public static void learnWordToVec(Word2Vec vec, Corpus corpusToSummarize) {
		SentenceIterator sentenceIterator = new SentenceIterator(corpusToSummarize);
        TokenizerFactory tokenizerFactory = new TokenizerFactory();

        /**TokenizerFactory first then SentenceIterator, use to build sequenceIterator inside the class.*/
        vec.setTokenizerFactory(tokenizerFactory); 
        vec.setSentenceIter(sentenceIterator);
        vec.buildVocab();
        try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        vec.fit();
	}
	
	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public void setModelLoad(boolean modelLoad) {
		this.modelLoad = modelLoad;
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(WordVector.class, Index.class, IndexBasedIn.class));
	}
	
	/**
	 * donne le/les paramètre(s) d'output en input à la class comp méthode
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((IndexBasedIn<WordVector>)compMethod).setIndex(index);
	}
}
