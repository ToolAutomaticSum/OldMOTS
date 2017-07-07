package model.task.preProcess;

import java.util.Iterator;

import model.task.preProcess.snowballStemmer.SnowballStemmer;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;

public class TextStemming extends AbstractPreProcess {
	
	SnowballStemmer stemmer;
	
	public TextStemming(int id) {
		super(id);
	}
	
	@Override
	public void init() {
		String language = getModel().getLanguage();
		Class<?> stemClass;
		try {
			stemClass = Class.forName("model.task.preProcess.snowballStemmer.ext." + language + "Stemmer");
			stemmer = (SnowballStemmer) stemClass.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process() {
		Iterator<Corpus> corpusIt = getCurrentMultiCorpus().iterator();
		while (corpusIt.hasNext()) {
			Iterator<TextModel> textIt = corpusIt.next().iterator();
			while (textIt.hasNext()) {
				TextModel textModel = textIt.next();
				stemmWord(textModel);
			}
		}
	}
	
	@Override
	public void finish() {		
	}
	
	private void stemmWord(TextModel textModel) {
		Iterator<SentenceModel> sentenceIt = textModel.iterator();
		while (sentenceIt.hasNext()) {
			SentenceModel sentenceModel = sentenceIt.next();
			Iterator<WordModel> wordIt = sentenceModel.iterator();
			while (wordIt.hasNext()) {
				stemming(wordIt.next());	
			}
		}
	}
	
	public void stemming(WordModel word) {
		int repeat = 1;
		String currentWord = word.getWord() + " ";
		StringBuffer input = new StringBuffer();
		int i = 0;
		boolean isDigit = true;
		while (i < currentWord.length()) {
		    char ch = (char) currentWord.charAt(i);
		    if (Character.isWhitespace((char) ch)) {
		    	if (isDigit) {
		    		word.setmLemma(input.toString());
		    		word.setStopWord(true);
				    input.delete(0, input.length());
		    	}
		    	else {
					if (input.length() > 0) {
					    stemmer.setCurrent(input.toString());
					    for (int j = repeat; j != 0; j--) {
					    	stemmer.stem();
					    }
					    word.setmLemma(stemmer.getCurrent());
					    input.delete(0, input.length());
					}
		    	}
		    } else
		    	input.append(Character.toLowerCase(ch));
		    if (!Character.isDigit(ch))
		    	isDigit = false;
		    i++;
	    }
	}
	
	public String getLemma(String word) {
		int repeat = 1;
		String currentWord = word + " ";
		StringBuffer input = new StringBuffer();
		int i = 0;
		boolean isDigit = true;
		while (i < currentWord.length()) {
		    char ch = (char) currentWord.charAt(i);
		    if (Character.isWhitespace((char) ch)) {
		    	if (isDigit) {
				    input.delete(0, input.length());
				    return "";
		    	}
		    	else {
					if (input.length() > 0) {
					    stemmer.setCurrent(input.toString());
					    for (int j = repeat; j != 0; j--) {
					    	stemmer.stem();
					    }
					    input.delete(0, input.length());
					    return (stemmer.getCurrent());
					}
		    	}
		    } else
		    	input.append(Character.toLowerCase(ch));
		    if (!Character.isDigit(ch))
		    	isDigit = false;
		    i++;
	    }
		return "";
	}
}

