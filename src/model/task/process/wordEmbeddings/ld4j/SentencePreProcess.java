package model.task.process.wordEmbeddings.ld4j;

import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;

public class SentencePreProcess implements SentencePreProcessor {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7368117979775251436L;

	@Override
	public String preProcess(String sentence) {
		return sentence;
	}

}
