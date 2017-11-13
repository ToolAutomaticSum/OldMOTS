package liasd.asadera.model.task.process.caracteristicBuilder.matrix;

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
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.WordVector;

public class ConcatMatrixSentence extends AbstractCaracteristicBuilder implements IndexBasedIn<WordVector>, SentenceCaracteristicBasedOut {

	protected int dimension;
	protected Index<WordVector> index;
	protected Map<SentenceModel, Object> sentenceCaracteristic;
	
	public ConcatMatrixSentence(int id) throws SupportADNException {
		super(id);

		sentenceCaracteristic = new HashMap<SentenceModel, Object>();
		
		listParameterIn.add(new ParametrizedType(WordVector.class, Index.class, IndexBasedIn.class));
		listParameterOut.add(new ParametrizedType(double[][].class, Map.class, SentenceCaracteristicBasedOut.class));
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		ConcatMatrixSentence p = new ConcatMatrixSentence(id);
		initCopy(p);
		p.setDimension(dimension);
		return p;
	}

	@Override
	public void initADN() throws Exception {
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) {
		dimension = index.values().iterator().next().getDimension();
		
		for (Corpus corpus : listCorpus) {
			for (TextModel text : corpus) {
				for (SentenceModel sentenceModel : text) {
					int nbWord = 0;
					double[][] sentenceMatrix = new double[sentenceModel.getNbMot()][];
					for (WordModel wm : sentenceModel) {
						@SuppressWarnings("unlikely-arg-type")
						WordVector word = index.get(wm.getmLemma());
						sentenceMatrix[nbWord] = word.getWordVector();
						nbWord++;
					}
					sentenceCaracteristic.put(sentenceModel, sentenceMatrix);
				}
			}
		}
	}
	
	public void finish() {
		sentenceCaracteristic.clear();
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	@Override
	public void setIndex(Index<WordVector> index) {
		if (index == null)
			throw new NullPointerException("Index is null.");
		this.index = index;
	}
	
	@Override
	public Map<SentenceModel, Object> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(null, Map.class, SentenceCaracteristicBasedIn.class));
	}

	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((SentenceCaracteristicBasedIn)compMethod).setCaracterisics(sentenceCaracteristic);;
	}
}
