package liasd.asadera.model.task.process.caracteristicBuilder.vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedOut;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.NGram;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class TfIdfVectorSentence extends AbstractCaracteristicBuilder/*<double[]>*/ implements IndexBasedIn<WordIndex>, SentenceCaracteristicBasedOut {

	protected Index<WordIndex> index;
	protected Map<SentenceModel, Object> sentenceCaracteristic;
	
	public TfIdfVectorSentence(int id) throws SupportADNException {
		super(id);
		
		sentenceCaracteristic = new HashMap<SentenceModel, Object>();

		listParameterIn.add(new ParametrizedType(NGram.class, Index.class, IndexBasedIn.class));
		listParameterIn.add(new ParametrizedType(WordIndex.class, Index.class, IndexBasedIn.class));
		listParameterOut.add(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedOut.class));
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		TfIdfVectorSentence p = new TfIdfVectorSentence(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
	}

	//@SuppressWarnings("unlikely-arg-type")
	@Override
	public void processCaracteristics(List<Corpus> listCorpus) throws Exception {
		for (Corpus corpus : listCorpus) {
			for (TextModel text : corpus) {
				for (SentenceModel sentenceModel : text) {
					int nbWord = 0;
					double[] tfIdfVector = new double[index.size()];
					for (WordIndex word : sentenceModel) {
						//if (getCurrentProcess().getFilter().passFilter(wm)) {
						//	WordIndex word = index.get(wm.getmLemma());
							//System.out.println(word);
							tfIdfVector[word.getiD()]+=word.getTfCorpus(corpus.getiD())*word.getIdf(index.getNbDocument());
							nbWord++;
						//}
					}
					for (int i=0; i<index.size(); i++)
						tfIdfVector[i]/=nbWord;
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
