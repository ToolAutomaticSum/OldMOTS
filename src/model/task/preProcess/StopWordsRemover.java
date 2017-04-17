package model.task.preProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import exception.LacksOfFeatures;
import grammaticalWords.GrammaticalWordsMap;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;

public class StopWordsRemover extends AbstractPreProcess {

	GrammaticalWordsMap gramWordsMap;
	
	/**
	 * ProcessOption li� StopWordListPath, String.
	 */
	public StopWordsRemover(int id) {
		super(id);
	}
	
	@Override
	public void init() throws LacksOfFeatures {
			gramWordsMap = new GrammaticalWordsMap(getModel().getLanguage(), getModel().getProcessOption(id, "StopWordListPath") + File.separator + getModel().getLanguage() + "StopWords.txt");
	}
	
	@Override
	public void process() {
		Iterator<Corpus> corpusIt = getModel().getCurrentMultiCorpus().iterator();
		while (corpusIt.hasNext()) {
			Iterator<TextModel> textIt = corpusIt.next().iterator();
			while (textIt.hasNext()) {
				TextModel textModel = textIt.next();
				removeGramWords(textModel);
			}
		}
	}
	
	@Override
	public void finish() {
	}
	
	public void removeGramWords(TextModel textModel) {
		Iterator<SentenceModel> sentenceIt = textModel.iterator();
		while (sentenceIt.hasNext()) {
			SentenceModel sentenceModel = sentenceIt.next();
			Iterator<WordModel> wordIt = sentenceModel.iterator();
			while (wordIt.hasNext()) {
				WordModel word = wordIt.next();
				if (gramWordsMap.detectGramWords(word.getmLemma()) != 0) {
					word.setStopWord(true);
					//word.setmLemma("___");
				}/* else if (word.getWord().matches("\\p{Digit}+"))
					word.setStopWord(true);*/
			}
		}
	}
	
	public List<String> removeGramWords(List<String> listOfWord) {
		Iterator<String> wordIt = listOfWord.iterator();
		while (wordIt.hasNext()) {
			String word = wordIt.next();
			if (gramWordsMap.detectGramWords(word) != 0)
				wordIt.remove();
		}
		return listOfWord;
	}
	
	public List<String> returnListOfGramWord(List<String> listOfWord) {
		List<String> listOfGramWord = new ArrayList<String>();
		Iterator<String> wordIt = listOfWord.iterator();
		while (wordIt.hasNext()) {
			String word = wordIt.next();
			if (gramWordsMap.detectGramWords(word) != 0)
				listOfGramWord.add(word);
		}
		return listOfGramWord;
	}
}
