package model.task.process.caracteristicBuilder.vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import model.task.process.caracteristicBuilder.SentenceCaracteristicBasedOut;
import model.task.process.indexBuilder.IndexBasedIn;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class TfIdfVectorSentence extends AbstractCaracteristicBuilder/*<double[]>*/ implements IndexBasedIn<WordTF_IDF>, SentenceCaracteristicBasedOut {

	protected Index<WordTF_IDF> index;
	protected Map<SentenceModel, Object> sentenceCaracteristic;
	
	public TfIdfVectorSentence(int id) throws SupportADNException {
		super(id);
		
		sentenceCaracteristic = new HashMap<SentenceModel, Object>();
		
		listParameterIn.add(new ParametrizedType(WordTF_IDF.class, Index.class, IndexBasedIn.class));
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

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) {
		for (Corpus corpus : listCorpus) {
			for (TextModel text : corpus) {
				Iterator<SentenceModel> sentenceIt = text.iterator();
				while (sentenceIt.hasNext()) {
					SentenceModel sentenceModel = sentenceIt.next();
					double[] tfIdfVector = new double[index.size()];
					Iterator<WordModel> wordIt = sentenceModel.iterator();
					while (wordIt.hasNext()) {
						WordModel wm = wordIt.next();
						if (!wm.isStopWord()) {
							WordTF_IDF word = index.get(wm.getmLemma());
							//System.out.println(word);
							tfIdfVector[word.getiD()]+=word.getTfCorpus(corpus.getiD())*word.getIdf();
						}
					}
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
	public void setIndex(Index<WordTF_IDF> index) {
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
