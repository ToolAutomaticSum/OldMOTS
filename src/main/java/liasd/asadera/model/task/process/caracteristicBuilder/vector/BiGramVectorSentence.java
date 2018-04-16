package main.java.liasd.asadera.model.task.process.caracteristicBuilder.vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedOut;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.indexBuilder.ILP.SentenceNGramBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;

@Deprecated
public class BiGramVectorSentence extends AbstractCaracteristicBuilder
		implements IndexBasedIn<NGram>, SentenceNGramBasedIn, SentenceCaracteristicBasedOut {

	protected Index<NGram> index;
	protected Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	protected Map<SentenceModel, Object> sentenceCaracteristic;

	public BiGramVectorSentence(int id) throws SupportADNException {
		super(id);

		sentenceCaracteristic = new HashMap<SentenceModel, Object>();

		listParameterIn.add(new ParameterizedType(NGram.class, Index.class, IndexBasedIn.class));
		listParameterIn.add(new ParameterizedType(NGram.class, List.class, SentenceNGramBasedIn.class));
		listParameterOut.add(new ParameterizedType(double[].class, Map.class, SentenceCaracteristicBasedOut.class));
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		BiGramVectorSentence p = new BiGramVectorSentence(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) {
		for (SentenceModel sen : ngrams_in_sentences.keySet()) {
			double[] tfIdfVector = new double[index.size()];
			for (NGram bg : ngrams_in_sentences.get(sen))
				tfIdfVector[bg.getiD()] += bg.getTfCorpus(sen.getText().getParentCorpus().getiD())
						* bg.getIdf(index.getNbDocument());
			sentenceCaracteristic.put(sen, tfIdfVector);
		}
	}

	@Override
	public void finish() {
		sentenceCaracteristic.clear();
	}

	@Override
	public void setIndex(Index<NGram> index) {
		this.index = index;
	}

	@Override
	public void setSentenceNGram(Map<SentenceModel, Set<NGram>> ngrams_in_sentences) {
		this.ngrams_in_sentences = ngrams_in_sentences;
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
