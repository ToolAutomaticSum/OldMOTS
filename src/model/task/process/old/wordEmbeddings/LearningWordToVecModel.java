package model.task.process.old.wordEmbeddings;

import java.io.File;

import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;

import model.task.process.AbstractProcess;
import model.task.process.indexBuilder.wordEmbeddings.ld4j.MultiCorpusSentenceIterator;
import model.task.process.indexBuilder.wordEmbeddings.ld4j.SentenceIterator;
import model.task.process.indexBuilder.wordEmbeddings.ld4j.TokenizerFactory;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.MultiCorpus;

public class LearningWordToVecModel extends AbstractProcess {
		
	public LearningWordToVecModel(int id) throws SupportADNException {
		super(id);
	}
	
	@Override
	public AbstractProcess makeCopy() throws Exception {
		throw new Exception("No copy allowed !");
	}

	@Override
	public void init() throws Exception {
		super.init();
	}
	
	@Override
	public void process() throws Exception {
		SentenceIterator sentenceIterator = new SentenceIterator(corpusToSummarize);
        TokenizerFactory tokenizerFactory = new TokenizerFactory();
 
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(2)
                .layerSize(300)
                .windowSize(5)
                .seed(42)
                .epochs(3)
                .elementsLearningAlgorithm(new SkipGram<VocabWord>())
                .iterate(sentenceIterator)
                .tokenizerFactory(tokenizerFactory)
                .build();
        vec.fit();
 
        WordVectorSerializer.writeWord2VecModel(vec, getModel().getOutputPath() + File.separator + "word2vec.bin");
	}
	
	@Override
	public void finish() throws Exception {
		index.clear();
		corpusToSummarize.clear();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        vec.fit();
	}
}
