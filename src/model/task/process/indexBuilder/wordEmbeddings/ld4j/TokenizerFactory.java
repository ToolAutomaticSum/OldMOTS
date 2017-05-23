package model.task.process.indexBuilder.wordEmbeddings.ld4j;

import java.io.InputStream;

import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;


public class TokenizerFactory implements org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory {

	@Override
	public Tokenizer create(String toTokenize) {
		return new Tokenizer(toTokenize);
	}

	@Override
	public Tokenizer create(InputStream toTokenize) {
		return null;
	}

	@Override
	public void setTokenPreProcessor(TokenPreProcess preProcessor) {
	}

	@Override
	public TokenPreProcess getTokenPreProcessor() {
		return null;
	}

}
