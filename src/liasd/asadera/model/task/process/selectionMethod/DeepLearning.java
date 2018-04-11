package liasd.asadera.model.task.process.selectionMethod;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import liasd.asadera.model.task.preProcess.stanfordNLP.StanfordNLPSimplePreProcess;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.WordIndex;
import liasd.asadera.tools.reader_writer.Reader;

public class DeepLearning extends AbstractSelectionMethod implements IndexBasedIn<WordIndex> {

	private Index<WordIndex> index;
	private Properties props;
	private StanfordCoreNLP pipeline;
	private String propStanfordNLP;
	
	public DeepLearning(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		return null;
	}

	@Override
	public void initADN() throws Exception {
	}

	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		// launch python script
		Reader reader = new Reader("generated summary", false);
		reader.open();
		String textSummary = reader.read();
		reader.close();
		propStanfordNLP = "tokenize, ssplit, pos, lemma";
		props = new Properties();
		props.put("annotators", propStanfordNLP);
		pipeline = new StanfordCoreNLP(props);
		List<SentenceModel> summary = StanfordNLPSimplePreProcess.liveProcessToListSentenceModel(propStanfordNLP, pipeline, textSummary);
		updateIndex(summary);
		return summary;
	}
	
	private void updateIndex(List<SentenceModel> summary) {
		for (SentenceModel sent : summary)
			for (WordModel word : sent.getListWordModel()) {
				if (!index.containsKey(word.getmLemma()))
					index.put(new WordIndex(word.getmLemma()));
			}
	}
	
	@Override
	public void setIndex(Index<WordIndex> index) {
		this.index = index;
	}



	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
	}

}
