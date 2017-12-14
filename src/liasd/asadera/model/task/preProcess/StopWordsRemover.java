package liasd.asadera.model.task.preProcess;

import java.io.File;
import java.util.Iterator;

import liasd.asadera.exception.LacksOfFeatures;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.tools.wordFilters.WordStopListFilter;

public class StopWordsRemover extends AbstractPreProcess {

	WordStopListFilter filter;
	
	/**
	 * ProcessOption lié StopWordListPath, String.
	 */
	public StopWordsRemover(int id) {
		super(id);
	}
	
	@Override
	public void init() throws LacksOfFeatures {
			filter = new WordStopListFilter(getModel().getProcessOption(id, "StopWordListPath") + File.separator + getModel().getLanguage() + "StopWords.txt");
	}
	
	@Override
	public void process() {
		Iterator<Corpus> corpusIt = getCurrentMultiCorpus().iterator();
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
		for (SentenceModel sen : textModel)
			for (WordModel word : sen.getListWordModel())
				if (filter.passFilter(word))
					word.setStopWord(true);
	}
}
