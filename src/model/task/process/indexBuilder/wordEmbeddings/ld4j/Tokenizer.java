package model.task.process.indexBuilder.wordEmbeddings.ld4j;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;

public class Tokenizer implements org.deeplearning4j.text.tokenization.tokenizer.Tokenizer {

	protected List<String> tokens;
	protected int currentToken;
	
	public Tokenizer(String toTokenize) {
		tokens = new ArrayList<String>();
		for (String token : toTokenize.split(" "))
			tokens.add(token);
		currentToken = 0;
	}
	
	@Override
	public boolean hasMoreTokens() {
		return currentToken != tokens.size()-1;
	}

	@Override
	public int countTokens() {
		return tokens.size();
	}

	@Override
	public String nextToken() {
		currentToken++;
		return tokens.get(currentToken);
	}

	@Override
	public List<String> getTokens() {
		return tokens;
	}

	@Override
	public void setTokenPreProcessor(TokenPreProcess tokenPreProcessor) {
	}

}
