package main.java.liasd.asadera.model.task.preProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.exception.UnknownLanguage;
import main.java.liasd.asadera.model.task.preProcess.stanfordNLP.StanfordNLPProperties;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.WordModel;
import main.java.liasd.asadera.tools.Tools;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

public class StanfordNLPPreProcess extends AbstractPreProcess {
		
	private static Logger logger = LoggerFactory.getLogger(StanfordNLPPreProcess.class);

	private Properties props;
	private StanfordCoreNLP pipeline;
	private String propStanfordNLP;
	
	public StanfordNLPPreProcess(int id) {
		super(id);
	}

	@Override
	public void init() throws LacksOfFeatures, UnknownLanguage {
		if (pipeline == null) {
			String language = getModel().getLanguage();
			if (!StanfordNLPProperties.languageAbbr.containsKey(language))
				throw new UnknownLanguage(language);
			// creates a StanfordCoreNLP object, with properties
			try {
				propStanfordNLP = getModel().getProcessOption(id, "PropStanfordNLP");
			} catch (LacksOfFeatures lof) {
				propStanfordNLP = "tokenize, ssplit, pos, lemma";
			}
			props = new StanfordNLPProperties(language);
			props.put("annotators", propStanfordNLP);
			props.put("tokenize.language", null);
			props.put("pos.model", null);
			pipeline = new StanfordCoreNLP(props);
		}
	}

	@Override
	public void process() throws Exception {
		logger.trace("StanfordNLP pipeline starting");
		try (ProgressBar pb = new ProgressBar("Preprocessing documents ", getModel().getNbDoc(), ProgressBarStyle.ASCII)) {
			int iD = 0;
			boolean highlight = false;
			for (Corpus corpus : getCurrentMultiCorpus()) {
				for (TextModel textModel : corpus) {
					// read some text in the text variable
					String text = textModel.getText();
					// create an empty Annotation just with the given text
					Annotation document = new Annotation(text);
					// run all Annotators on this text
					pipeline.annotate(document);
					// these are all the sentences in this document
					// a CoreMap is essentially a Map that uses class objects as keys and has values
					// with custom types
					List<CoreMap> sentences = document.get(SentencesAnnotation.class);
					for (CoreMap sentence : sentences) {
						if (!sentence.toString().replace("_", "").replace(".", "").isEmpty()) {
							String senText;
							if (sentence.toString().contains(" -- "))
								senText = sentence.toString().split(" -- ")[1].replace("\n", "\t");
							else
								senText = sentence.toString().replace("\n", "\t");
							if (senText.startsWith("@highlight")) {
								highlight = true;
								continue;
							}
							SentenceModel sen = new SentenceModel(senText, iD, textModel);
							// traversing the words in the current sentence
							// a CoreLabel is a CoreMap with additional token-specific methods
							for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
								String w = token.get(TextAnnotation.class);
								if (!Tools.removePonctuation(w).isEmpty() && senText.contains(w)) {
									WordModel word = new WordModel();
									word.setmForm(w);
									word.setSentence(sen);
									if (propStanfordNLP.contains("pos"))
										word.setmPosTag(token.get(PartOfSpeechAnnotation.class));
									if (propStanfordNLP.contains("lemma"))
										word.setmLemma(token.get(LemmaAnnotation.class).toLowerCase());
									else
										word.setmLemma(w.toLowerCase());
									word.setWord(w);
									sen.getListWordModel().add(word);
								}
							}
							//if (sen.getLength(getModel().getFilter()) > 7)
							textModel.add(sen);
							iD++;
							if (highlight) {
								logger.debug(sen.toString());
								sen.getLabels().add("highlight");
								highlight = false;
							}
						}
					}
					if (getModel().isWritePerFile()) {
						String outputPath = getModel().getOutputPath() + File.separator + "temp" + File.separator + corpus.getCorpusName();
						GenerateTextModel.writeTempText(model, corpus.size(), textModel, outputPath);
					}
					pb.step();
				}
			}
		}
	}

	@Override
	public void finish() {
	}

	/**
	 * 
	 * @param textToProcess
	 * @param writer
	 */
	public static List<SentenceModel> liveProcessToListSentenceModel(String propStanfordNLP, StanfordCoreNLP pipeline, String textToProcess) {
		List<SentenceModel> listSentence = new ArrayList<SentenceModel>();

		Annotation document = new Annotation(textToProcess);
		// run all Annotators on this text
		pipeline.annotate(document);
		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and has values
		// with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			if (!sentence.toString().replace("_", "").isEmpty()) {
				String senText = sentence.toString();
				SentenceModel sen = new SentenceModel(senText);
				// traversing the words in the current sentence
				// a CoreLabel is a CoreMap with additional token-specific methods
				for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
					String w = token.get(TextAnnotation.class);
					if (!Tools.removePonctuation(w).isEmpty() && senText.contains(w)) {
						WordModel word = new WordModel();
						word.setmForm(w);
						word.setSentence(sen);
						if (propStanfordNLP.contains("pos"))
							word.setmPosTag(token.get(PartOfSpeechAnnotation.class));
						if (propStanfordNLP.contains("lemma"))
							word.setmLemma(token.get(LemmaAnnotation.class).toLowerCase());
						else
							word.setmLemma(w.toLowerCase());
						word.setWord(w);
						sen.getListWordModel().add(word);
					}
				}
				listSentence.add(sen);
			}
		}
		return listSentence;
	}
	
	public static List<String> liveProcessToListString(StanfordCoreNLP pipeline, String textToProcess) {
		List<String> listSentence = new ArrayList<String>();
		Annotation document = new Annotation(textToProcess);
		// run all Annotators on this text
		pipeline.annotate(document);
		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and has values
		// with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			if (!sentence.toString().replace("_", "").isEmpty()) {
				// traversing the words in the current sentence
				// a CoreLabel is a CoreMap with additional token-specific methods
				String s = "";
				for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
					if (!Tools.removePonctuation(token.get(TextAnnotation.class)).isEmpty()) {
						s += token.get(LemmaAnnotation.class).toLowerCase() + " ";
					}
				}
				listSentence.add(s);
			}
		}
		return listSentence;

	}
}
