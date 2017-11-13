package liasd.asadera.model.task.preProcess.stanfordNLP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.valnyz.reader_writer.Writer;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import liasd.asadera.exception.LacksOfFeatures;
import liasd.asadera.model.task.preProcess.AbstractPreProcess;
import liasd.asadera.model.task.preProcess.GenerateTextModel;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.tools.Tools;
import liasd.asadera.tools.wordFilters.TrueFilter;
import liasd.asadera.tools.wordFilters.WordFilter;

/**
 * Utilisation de la lib StanfordNLPCore
 * Seulement SentenceSplitter and WordSplitter
 * @author Val
 *
 */
public class StanfordNLPSimplePreProcess extends AbstractPreProcess {

	private Properties props;
	private StanfordCoreNLP pipeline;
	private String propStanfordNLP;
	private WordFilter filter; 
	
	public StanfordNLPSimplePreProcess(int id) {
		super(id);
	}
	
	@Override
	public void init() throws LacksOfFeatures {
		// creates a StanfordCoreNLP object, with properties
		try {
			propStanfordNLP = getModel().getProcessOption(id, "PropStanfordNLP");
		}
		catch (LacksOfFeatures lof) {
			propStanfordNLP = "tokenize, ssplit, pos, lemma";
		}
		props = new Properties();
		props.put("annotators", propStanfordNLP);
		pipeline = new StanfordCoreNLP(props);
		
		if (getCurrentProcess() != null && getCurrentProcess().getClass() == GenerateTextModel.class)
			filter = ((GenerateTextModel) getCurrentProcess()).getFilter();
		else
			filter = new TrueFilter();
	}

	@Override
	public void process() throws Exception {
		int iD = 0;
		
		Iterator<Corpus> corpusIt = getCurrentMultiCorpus().iterator();
		while (corpusIt.hasNext()) {
			Iterator<TextModel> textIt = corpusIt.next().iterator();
			while (textIt.hasNext()) {
				TextModel textModel = textIt.next();
				// read some text in the text variable
				String text = textModel.getText();
				// create an empty Annotation just with the given text
				Annotation document = new Annotation(text);
				// run all Annotators on this text
				pipeline.annotate(document);
				// these are all the sentences in this document
				// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
				List<CoreMap> sentences = document.get(SentencesAnnotation.class);
				for(CoreMap sentence: sentences) {
					if (!sentence.toString().replace("_", "").isEmpty()) {
						String senText;
						if (sentence.toString().contains(" -- "))
							senText = sentence.toString().split(" -- ")[1].replace("\n",  "\t");
						else
							senText = sentence.toString().replace("\n",  "\t");
						SentenceModel sen = new SentenceModel(senText, iD, textModel);
						// traversing the words in the current sentence
						// a CoreLabel is a CoreMap with additional token-specific methods
						for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
							String w = token.get(TextAnnotation.class);
							if (!Tools.enleverPonctuation(w).isEmpty() && senText.contains(w)) {
								WordModel word = new WordModel();
								word.setmForm(w);
								word.setSentence(sen);
								if(propStanfordNLP.contains("pos"))
									word.setmPosTag(token.get(PartOfSpeechAnnotation.class));
								//System.out.println(token.get(LemmaAnnotation.class));
								if(propStanfordNLP.contains("lemma"))
									word.setmLemma(token.get(LemmaAnnotation.class).toLowerCase());
								else
									word.setmLemma(w.toLowerCase());
								word.setWord(w);
								sen.add(word);
							}
						}
						if (sen.getLength(filter) > 7)
							textModel.add(sen);
						iD++;
					}
				}
			}
		}
	}
	
	@Override
	public void finish() {
//		props = new Properties();
//		props.put("annotators", "tokenize,ssplit,pos,lemma");
//		pipeline = new StanfordCoreNLP(props);
	}
	
	/**
	 * 
	 * @param textToProcess
	 * @param writer
	 * @throws Exception 
	 */
	public static void liveProcessToFile(StanfordCoreNLP pipeline, String textToProcess, Writer writer) throws Exception {
		Annotation document = new Annotation(textToProcess);
		// run all Annotators on this text
		pipeline.annotate(document);
		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence: sentences) {
			if (!sentence.toString().replace("_", "").isEmpty()) {
				// traversing the words in the current sentence
				// a CoreLabel is a CoreMap with additional token-specific methods
				for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
					if (!Tools.enleverPonctuation(token.get(TextAnnotation.class)).isEmpty()) {
						writer.write(token.get(LemmaAnnotation.class).toLowerCase() + " ");
					}
				}
				writer.write("\n");
			}
		}
	}
	
	/**
	 * 
	 * @param textToProcess
	 * @param writer
	 */
	public static List<String> liveProcessToListString(StanfordCoreNLP pipeline, String textToProcess) {
		List<String> listSentence = new ArrayList<String>();
		
		Annotation document = new Annotation(textToProcess);
		// run all Annotators on this text
		pipeline.annotate(document);
		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence: sentences) {
			if (!sentence.toString().replace("_", "").isEmpty()) {
				// traversing the words in the current sentence
				// a CoreLabel is a CoreMap with additional token-specific methods
				String s= "";
				for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
					if (!Tools.enleverPonctuation(token.get(TextAnnotation.class)).isEmpty()) {
						s += token.get(LemmaAnnotation.class).toLowerCase() + " ";
					}
				}
				listSentence.add(s);
			}
		}
		return listSentence;
	}

	public String getLemma(String word) {
		Annotation tokenAnnotation = new Annotation(word);
		pipeline.annotate(tokenAnnotation);
		List<CoreMap> list = tokenAnnotation.get(SentencesAnnotation.class);
		if(!list.isEmpty()) {
			String tokenLemma = list.get(0).get(TokensAnnotation.class)
		                        	.get(0).get(LemmaAnnotation.class);
			return tokenLemma;
		}
		else
			return null;
	}
}
