package model.task.process.wordEmbeddings.ld4j;

import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;

import textModeling.MultiCorpus;

public class MultiCorpusSentenceIterator implements org.deeplearning4j.text.sentenceiterator.SentenceIterator {

	private MultiCorpus multiCorpus;
	private int currentCorpus;
	private SentenceIterator currentSentenceIterator;
	
	public MultiCorpusSentenceIterator(MultiCorpus multiCorpus) {
		this.multiCorpus = multiCorpus;
		reset();
	}
	
	@Override
	public String nextSentence() {
		if (currentCorpus == multiCorpus.size()-1 && !currentSentenceIterator.hasNext())
			return null;
		else if (!currentSentenceIterator.hasNext()) {
			currentCorpus++;
			currentSentenceIterator = new SentenceIterator(multiCorpus.get(currentCorpus));
		}
		return currentSentenceIterator.nextSentence();
	}

	@Override
	public boolean hasNext() {
		if (currentCorpus == multiCorpus.size()-1 && !currentSentenceIterator.hasNext())
			return false;
		else
			return true;
	}

	@Override
	public void reset() {
		currentCorpus = 0;
		currentSentenceIterator = new SentenceIterator(multiCorpus.get(currentCorpus));
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
