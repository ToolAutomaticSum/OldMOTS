package liasd.asadera.model.task.process.caracteristicBuilder.vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedOut;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.indexBuilder.ILP.SentenceNGramBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.NGram;

public class BiGramVectorSentence extends AbstractCaracteristicBuilder implements IndexBasedIn<NGram>, SentenceNGramBasedIn, SentenceCaracteristicBasedOut {

	protected Index<NGram> index;
	protected Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	protected Map<SentenceModel, Object> sentenceCaracteristic;
	
	public BiGramVectorSentence(int id) throws SupportADNException {
		super(id);
		
		sentenceCaracteristic = new HashMap<SentenceModel, Object>();
		
		listParameterIn.add(new ParametrizedType(NGram.class, Index.class, IndexBasedIn.class));
		listParameterIn.add(new ParametrizedType(NGram.class, List.class, SentenceNGramBasedIn.class));
		listParameterOut.add(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedOut.class));
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
				tfIdfVector[bg.getiD()]+=bg.getTfCorpus(sen.getText().getParentCorpus().getiD())*bg.getIdf();
			sentenceCaracteristic.put(sen, tfIdfVector);
		}
//		for (Corpus corpus : listCorpus) {
//			for (TextModel text : corpus) {
//				Iterator<SentenceModel> sentenceIt = text.iterator();
//				while (sentenceIt.hasNext()) {
//					SentenceModel sentenceModel = sentenceIt.next();
//					double[] tfIdfVector = new double[index.size()];
//					Iterator<NGram> biGramIt = sentenceModel.getNGrams().iterator();
//					while (biGramIt.hasNext()) {
//						NGram bg = biGramIt.next();
//						NGram indexBiGram = index.get(bg.getWord());
//							//System.out.println(word);
//							tfIdfVector[indexBiGram.getiD()]+=indexBiGram.getTfCorpus(corpus.getiD())*indexBiGram.getIdf();
//					}
//					sentenceCaracteristic.put(sentenceModel, tfIdfVector);
//				}
//			}
//		}
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
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
	}
	
	/**
	 * donne le/les paramètre(s) d'output en input à la class comp méthode
	 */
	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((SentenceCaracteristicBasedIn)compMethod).setCaracterisics(sentenceCaracteristic);
	}
}
