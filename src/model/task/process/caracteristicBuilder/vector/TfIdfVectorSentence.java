package model.task.process.caracteristicBuilder.TF_IDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import model.task.process.caracteristicBuilder.SentenceCaracteristicBasedOut;
import model.task.process.indexBuilder.IndexBasedIn;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class TfIdfVectorSentence extends AbstractCaracteristicBuilder<double[]> implements IndexBasedIn<WordTF_IDF>, SentenceCaracteristicBasedOut<double[]> {

	private Index<WordTF_IDF> index;
	
	public TfIdfVectorSentence(int id) throws SupportADNException {
		super(id);
		listParameterIn = new ArrayList<ParametrizedType>();
		listParameterIn.add(new ParametrizedType(WordTF_IDF.class, Index.class, IndexBasedIn.class));
		listParameterOut = new ArrayList<ParametrizedType>();
		listParameterOut.add(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedOut.class));
	}

	@Override
	public AbstractCaracteristicBuilder<double[]> makeCopy() throws Exception {
		return null;
	}

	@Override
	public void initADN() throws Exception {
		sentenceCaracteristic = new HashMap<SentenceModel, double[]>();
	}

	@Override
	public void processCaracteristics() {
		sentenceCaracteristic.clear();
		for (TextModel text : getCurrentProcess().getCorpusToSummarize()) {
			Iterator<SentenceModel> sentenceIt = text.iterator();
			while (sentenceIt.hasNext()) {
				SentenceModel sentenceModel = sentenceIt.next();
				double[] tfIdfVector = new double[index.size()];
				Iterator<WordModel> wordIt = sentenceModel.iterator();
				while (wordIt.hasNext()) {
					WordModel wm = wordIt.next();
					if (!wm.isStopWord()) {
						WordTF_IDF word = (WordTF_IDF) index.get(wm.getmLemma());
						tfIdfVector[word.getId()]+=word.getTfCorpus(getCurrentProcess().getCorpusToSummarize().getiD())*word.getIdf();
					}
				}
				sentenceCaracteristic.put(sentenceModel, tfIdfVector);
			}
		}
	}

	@Override
	public void setIndex(Index<WordTF_IDF> index) {
		this.index = index;
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
	}
	
	/**
	 * donne le/les paramètre(s) d'output en input à la class comp méthode
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((SentenceCaracteristicBasedIn<double[]>)compMethod).setCaracterisics(sentenceCaracteristic);
	}
}
