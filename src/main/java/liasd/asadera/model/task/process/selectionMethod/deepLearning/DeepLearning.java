package main.java.liasd.asadera.model.task.process.selectionMethod.deepLearning;

import java.util.ArrayList;
import java.util.List;

import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.WordModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public abstract class DeepLearning extends AbstractSelectionMethod implements IndexBasedIn<WordIndex> {

	private Index<WordIndex> index;
	protected boolean learning;
//	private Properties props;
//	private StanfordCoreNLP pipeline;
//	private String propStanfordNLP;
	
	public DeepLearning(int id) throws SupportADNException {
		super(id);
	}
	
	@Override
	public void initADN() throws Exception {
		super.initADN();
		
//		try {
			learning = true; //Boolean.getBoolean(getCurrentProcess().getModel().getProcessOption(id, "Learning"));
//		}
//		catch (LacksOfFeatures lf) {
//			learning = false;
//		}
	}
	
	public abstract void trainModel(List<Corpus> listCorpus) throws Exception;
	
//	@Override
//	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
//		// launch python script
//		Reader reader = new Reader("generated summary", false);
//		reader.open();
//		String textSummary = reader.read();
//		reader.close();
//		propStanfordNLP = "tokenize, ssplit, pos, lemma";
//		props = new Properties();
//		props.put("annotators", propStanfordNLP);
//		pipeline = new StanfordCoreNLP(props);
//		List<SentenceModel> summary = StanfordNLPPreProcess.liveProcessToListSentenceModel(propStanfordNLP, pipeline, textSummary);
//		updateIndex(summary);
//		return summary;
//	}
	
	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		if (learning) {
			trainModel(getCurrentMultiCorpus());
			System.exit(0);
		}
		return new ArrayList<SentenceModel>();
	}
	
	protected void updateIndex(List<SentenceModel> summary) {
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
