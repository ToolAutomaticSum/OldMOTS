package model.task.process.wordEmbeddings.ld4j;

import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;

import textModeling.Corpus;

public class SentenceIterator implements org.deeplearning4j.text.sentenceiterator.SentenceIterator {

	protected Corpus corpus;
	protected int currentSentence;
	protected int currentText;
	protected SentencePreProcessor sentencePreProcess;
	
	public SentenceIterator(Corpus corpus) {
		this.corpus = corpus;
		reset();
	}
	
	@Override
	public String nextSentence() {
		if (currentText == corpus.size()-1 && currentSentence == corpus.get(currentText).size()-1)
			return null;
		else if (currentSentence == corpus.get(currentText).size()-1) {
			currentText++;
			currentSentence=0;
		}
		else
			currentSentence++;
		return corpus.get(currentText).get(currentSentence).getSentence();
	}

	@Override
	public boolean hasNext() {
		return !(currentText == corpus.size()-1 && currentSentence == corpus.get(currentText).size()-1);
	}

	@Override
	public void reset() {
		currentSentence = -1;
		currentText = 0;
	}

	@Override
	public void finish() {
		reset();	
	}

	@Override
	public SentencePreProcessor getPreProcessor() {
		return null;
	}

	@Override
	public void setPreProcessor(SentencePreProcessor preProcessor) {
	}

}
