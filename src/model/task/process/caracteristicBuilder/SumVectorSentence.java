package model.task.process.caracteristicBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import model.task.process.indexBuilder.IndexBasedIn;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordVector;

public class SumVectorSentence extends AbstractCaracteristicBuilder<double[]> implements IndexBasedIn<WordVector> {

	private int dimension;
	private Index<WordVector> index;
	
	public SumVectorSentence(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public AbstractCaracteristicBuilder<double[]> makeCopy() throws Exception {
		SumVectorSentence p = new SumVectorSentence(id);
		initCopy(p);
		p.setDimension(dimension);
		return p;
	}

	@Override
	public void initADN() throws Exception {
	}

	@Override
	public void processCaracteristics() {
		sentenceCaracteristic = new HashMap<SentenceModel, double[]>();
		for (TextModel text : getCurrentProcess().getCorpusToSummarize()) {
			for (SentenceModel sentenceModel : text) {
				int nbWord = 0;
				double[] wordVector = new double[dimension];
				Iterator<WordModel> wordIt = sentenceModel.iterator();
				while (wordIt.hasNext()) {
					WordModel wm = wordIt.next();
					if (!wm.isStopWord()) {
						WordVector word = index.get(wm.getmLemma());
						for (int i=0; i<dimension; i++)
							wordVector[i]+=word.getWordVector()[i];
						nbWord++;
					}
				}
				for (int i=0; i<dimension; i++)
					wordVector[i]/=nbWord;
				sentenceCaracteristic.put(sentenceModel, wordVector);
			}
		}
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
		dimension = index.values().iterator().next().getDimension();
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((SentenceCaracteristicBasedIn<double[]>)compMethod).setCaracterisics(sentenceCaracteristic);;
	}
}
