package main.java.liasd.asadera.model.task.preProcess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.WordModel;
import main.java.liasd.asadera.tools.Tools;

public class WordSplitter extends AbstractPreProcess {

	public WordSplitter(int id) {
		super(id);
	}

	@Override
	public void init() throws LacksOfFeatures {
	}

	@Override
	public void process() {
		Iterator<Corpus> corpusIt = getCurrentMultiCorpus().iterator();
		while (corpusIt.hasNext()) {
			Iterator<TextModel> textIt = corpusIt.next().iterator();
			while (textIt.hasNext()) {
				TextModel textModel = textIt.next();
				splitSentenceIntoWord(textModel);
			}
		}
	}

	@Override
	public void finish() {
	}

	private void splitSentenceIntoWord(TextModel textModel) {
		Iterator<SentenceModel> sentenceIt = textModel.iterator();
		while (sentenceIt.hasNext()) {
			SentenceModel sentenceModel = sentenceIt.next();
			String[] words = sentenceModel.toString().split(" ");
			// System.out.println(sentenceModel.toString());
			// System.out.println(words.length);
			sentenceModel.setNbMot(words.length);
			for (int i = 0; i < words.length; i++) {
				WordModel word = new WordModel();
				word.setSentence(sentenceModel);
				String w = Tools.removePonctuation(words[i]);
				word.setmForm(w);
				word.setmLemma(w.toLowerCase());
				word.setWord(w);
				sentenceModel.getListWordModel().add(word);
				textModel.setTextSize(textModel.getTextSize() + 1);
			}
		}
	}

	public static List<String> splitSentenceIntoWord(String sentence) {
		List<String> listOfWord = new ArrayList<String>();

		String[] words = sentence.split(" ");
		if (words.length > 5) {
			for (int i = 0; i < words.length; i++) {
				listOfWord.add(Tools.removePonctuation(words[i]));
			}
		}
		return listOfWord;
	}
}
