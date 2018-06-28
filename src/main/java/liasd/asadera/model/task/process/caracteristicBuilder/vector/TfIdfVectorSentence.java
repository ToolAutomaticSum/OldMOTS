package main.java.liasd.asadera.model.task.process.caracteristicBuilder.vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedOut;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class TfIdfVectorSentence extends AbstractCaracteristicBuilder implements IndexBasedIn<WordIndex>, SentenceCaracteristicBasedOut {

	protected Index<WordIndex> index;
	protected Map<SentenceModel, Object> sentenceCaracteristic;

	public TfIdfVectorSentence(int id) throws SupportADNException {
		super(id);
		
		listParameterIn.add(new ParameterizedType(NGram.class, Index.class, IndexBasedIn.class));
		listParameterIn.add(new ParameterizedType(WordIndex.class, Index.class, IndexBasedIn.class));
		listParameterOut.add(new ParameterizedType(double[].class, Map.class, SentenceCaracteristicBasedOut.class));
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		TfIdfVectorSentence p = new TfIdfVectorSentence(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		sentenceCaracteristic = new HashMap<SentenceModel, Object>();
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) throws Exception {
		for (Corpus corpus : listCorpus) {
			for (TextModel text : corpus) {
				for (SentenceModel sentenceModel : text) {
//					int nbWord = 0;
					double[] tfIdfVector = new double[index.size()];
					for (WordIndex word : sentenceModel) {
						tfIdfVector[word.getiD()] += word.getTfCorpus(corpus.getiD())
								* word.getIdf(index.getNbDocument());
//						nbWord++;
					}
//					for (int i = 0; i < index.size(); i++)
//						tfIdfVector[i] /= nbWord;
					sentenceCaracteristic.put(sentenceModel, tfIdfVector);
				}
			}
		}
	}

	@Override
	public void finish() {
		sentenceCaracteristic.clear();
	}

	@Override
	public void setIndex(Index<WordIndex> index) {
		this.index = index;
	}

	@Override
	public Map<SentenceModel, Object> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
	}
 
	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		((SentenceCaracteristicBasedIn) compMethod).setCaracterisics(sentenceCaracteristic);
	}
}
