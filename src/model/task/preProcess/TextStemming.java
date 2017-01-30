package model.task.preProcess;

import java.util.Iterator;

import model.task.preProcess.snowballStemmer.SnowballStemmer;
import textModeling.ParagraphModel;
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
		Iterator<TextModel> textIt = getModel().getDocumentModels().iterator();
		while (textIt.hasNext()) {
			TextModel textModel = textIt.next();
			stemmWord(textModel);
		}
	}
	
	@Override
	public void finish() {		
	}
	
	private void stemmWord(TextModel textModel) {
		Iterator<ParagraphModel> paragraphIt = textModel.iterator();
		while (paragraphIt.hasNext()) {
			ParagraphModel paragraphModel = paragraphIt.next();
			Iterator<SentenceModel> sentenceIt = paragraphModel.iterator();
			while (sentenceIt.hasNext()) {
				SentenceModel sentenceModel = sentenceIt.next();
				Iterator<WordModel> wordIt = sentenceModel.iterator();
				while (wordIt.hasNext()) {
					stemming(wordIt.next());	
				}
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
	
	public String stemming(String word) {
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

