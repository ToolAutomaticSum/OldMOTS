package model.task.preProcess;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import exception.LacksOfFeatures;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;

public class SentenceSplitter extends AbstractPreProcess {

	public SentenceSplitter(int id) {
		super(id);
	}
	
	@Override
	public void init() throws LacksOfFeatures {		
	}
	
	@Override
	public void process() {
		splitParagraphIntoSentence();
	}
	
	@Override
	public void finish() {		
	}
	
	private void splitParagraphIntoSentence() {
		Iterator<Corpus> corpusIt = getModel().getCurrentMultiCorpus().iterator();
		while (corpusIt.hasNext()) {
			int iD = 0;
			Iterator<TextModel> textIt = corpusIt.next().iterator();
			while (textIt.hasNext()) {
				TextModel textModel = textIt.next();
				BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
				iterator.setText(textModel.getText());
				int start = iterator.first();
				for (int end = iterator.next();
				    end != BreakIterator.DONE;
				    start = end, end = iterator.next()) {
					textModel.add(new SentenceModel(textModel.getText().substring(start,end), iD, textModel));
					iD++;
				}
			}
		}
	}
	
	public static List<String> splitTextIntoSentence(String textToSplit) {
		List<String> listOfSentence = new ArrayList<String>();
		
		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
		iterator.setText(textToSplit);
		int start = iterator.first();
		for (int end = iterator.next();
		    end != BreakIterator.DONE;
		    start = end, end = iterator.next()) {
			listOfSentence.add(textToSplit.substring(start,end).toLowerCase());		  	
		}
		return listOfSentence;
	}
}
