package model.task.process.wordEmbeddings.ld4j;

public class TokenPreProcess implements org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess {

	@Override
	public String preProcess(String token) {
		return token;
	}

}
