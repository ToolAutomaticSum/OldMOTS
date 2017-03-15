package model.task.process.wordEmbeddings;

import java.util.HashMap;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.plot.BarnesHutTsne;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import model.task.process.AbstractProcess;
import optimize.SupportADNException;

public class WordToVec extends AbstractProcess {
	
	static {
		supportADN = new HashMap<String, Class<?>>();
	}
	
	public WordToVec(int id) throws SupportADNException {
		super(id);
	}
	
	@Override
	public void init() throws Exception {
		super.init();
	}
	
	@Override
	public void process() throws Exception {
		TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        SentenceIterator iter = new CollectionSentenceIterator(getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()).getAllStringSentence());
    	
        Word2Vec vec = new Word2Vec.Builder()
        		.minWordFrequency(5)
        		.iterations(1)
        		.layerSize(100)
        		.seed(42)
        		.windowSize(5)
        		.iterate(iter)
        		.tokenizerFactory(t)
        		.build();

        vec.fit();

        // Write word vectors
        WordVectorSerializer.writeWordVectors(vec, "/home/valnyz/pathToWriteto.txt");

		
		super.process();
	}
	
	@Override
	public void finish() throws Exception {
		super.finish();
	}

}
