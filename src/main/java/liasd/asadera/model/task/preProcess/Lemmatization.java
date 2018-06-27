package main.java.liasd.asadera.model.task.preProcess;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.exception.UnknownLanguage;
import main.java.liasd.asadera.model.task.preProcess.lemmatizer.AhmetAkerLemmatizer;
import main.java.liasd.asadera.model.task.preProcess.stanfordNLP.StanfordNLPProperties;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.WordModel;

public class Lemmatization extends AbstractPreProcess {
	
	private static Logger logger = LoggerFactory.getLogger(Lemmatization.class);
	
	public static String getPOSTag (String pos) {
		String returnPos = pos;
		switch (pos.charAt(0)) {
		case 'A':
			if (pos.length() > 2 && pos.charAt(2) == 'V')
				returnPos = "ADV";
			else if (pos.length() > 2 && pos.charAt(2) == 'J')
				returnPos = "ADJ";
			else
				returnPos = "ADJ";
			break;
		case 'N':
			returnPos = "NOUN";
			break;
		case 'C':
			returnPos = "CONJ";
			break;
		case 'P':
			if (pos.length() > 2 && pos.charAt(2) == 'O')
				returnPos = "PRON";
			else
				returnPos = "PREP";
			break;
		case 'V':
			returnPos = "VERB";
			break;
		default:
			break;
		}
		return returnPos;
	}

	private String language;
	private AhmetAkerLemmatizer lemmatizer;

	public Lemmatization(int id) {
		super(id);
	}

	@Override
	public void init() throws UnknownLanguage, IOException {
		language = getModel().getLanguage();
		if (!StanfordNLPProperties.languageAbbr.containsKey(language))
			throw new UnknownLanguage(language);
		language = StanfordNLPProperties.languageAbbr.get(language);
		lemmatizer = new AhmetAkerLemmatizer("docs", language);
	}

	@Override
	public void process() {
		logger.trace("Lemmatization");
		for (Corpus corpus : getCurrentMultiCorpus())
			for (TextModel text : corpus)
				for (SentenceModel sentence : text)
					for (WordModel word : sentence.getListWordModel()) {
						String lemma = lemmatizer.getLemma(word.getWord(), language, getPOSTag(word.getmPosTag()));
						if (lemma != null)
							word.setmLemma(lemma);
					}
	}

	@Override
	public void finish() {
	}
}
