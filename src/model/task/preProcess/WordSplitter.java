package model.task.preProcess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import exception.LacksOfFeatures;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import tools.Tools;

public class WordSplitter extends AbstractPreProcess {

	public WordSplitter(int id) {
		super(id);
	}
	
	@Override
	public void init() throws LacksOfFeatures {
	}

	@Override
	public void process() {
		Iterator<TextModel> textIt = getModel().getDocumentModels().iterator();
		while (textIt.hasNext()) {
			TextModel textModel = textIt.next();
			splitSentenceIntoWord(textModel);
		}
	}
	
	@Override
	public void finish() {
	}
	
	private void splitSentenceIntoWord(TextModel textModel) {
		Iterator<ParagraphModel> paragraphIt = textModel.iterator();
		while (paragraphIt.hasNext()) {
			ParagraphModel paragraphModel = paragraphIt.next();
			Iterator<SentenceModel> sentenceIt = paragraphModel.iterator();
			while (sentenceIt.hasNext()) {
				SentenceModel sentenceModel = sentenceIt.next();
				String[] words = sentenceModel.getSentence().split(" ");
				for (int i = 0;i<words.length;i++) {
					WordModel word = new WordModel();
					word.setSentence(sentenceModel);
					word.setWord(Tools.enleverPonctuation(words[i]));
					sentenceModel.add(word);		
				}
				if (sentenceModel.size() < 6) {
					sentenceIt.remove();
					paragraphModel.setNbSentence(paragraphModel.getNbSentence()-1);
				}
			}
		}
	}
	
	public static List<String> splitSentenceIntoWord(String sentence) {
		List<String> listOfWord = new ArrayList<String>();
		
		String[] words = sentence.split(" ");
		if (words.length > 5) {
			for (int i = 0;i<words.length;i++) {
				listOfWord.add(Tools.enleverPonctuation(words[i]));
			}
		}
		return listOfWord;
	}
}
