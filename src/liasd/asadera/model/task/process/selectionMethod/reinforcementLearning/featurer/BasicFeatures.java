package liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.featurer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.ReinforcementLearning;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class BasicFeatures extends Featurer implements IndexBasedIn<WordIndex>{

	private List<WordIndex> topTfIdf;
	private Index<WordIndex> index;
	private int maxLength;
	private int nbWord;
	
	public BasicFeatures(ReinforcementLearning rl) throws SupportADNException {
		super(rl);
		
		listParameterIn.add(new ParametrizedType(WordIndex.class, Index.class, IndexBasedIn.class));
	}
	
	@Override
	public void init(int maxLength) throws Exception {
		nbWord = index.size(); //Integer.parseInt(rl.getCurrentProcess().getModel().getProcessOption(rl.getId(), "NbWord"));
		int corpusId = rl.getCurrentProcess().getCorpusToSummarize().getiD();
		this.maxLength = maxLength;
//		List<Pair<WordIndex, Double>> list = new ArrayList<Pair<WordIndex, Double>>();
//		for (WordIndex word : index.values())
//			list.add(new Pair(word, word.getTf()*word.getIdf()));
		topTfIdf = new ArrayList<WordIndex>(index.values());
//		Collections.sort(list);
		Collections.sort(topTfIdf, (a, b) -> -Double.compare(a.getTfCorpus(corpusId)*a.getIdf(), b.getTfCorpus(corpusId)*b.getIdf()));
//		for (int i=0; i<100; i++)
//			topTfIdf.add(list.get(i).getKey());
		topTfIdf = topTfIdf.subList(0, nbWord-1);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public double[] getFeatures(List<SentenceModel> summary) {
		double[] features = instanciateVector();
		double[] redundancy = new double[nbWord];
		int length = 0;
		for (SentenceModel sen : summary) {
			for (WordModel word : sen) {
				int indexOf = topTfIdf.indexOf(index.get(word.getmLemma()));
				if(indexOf != -1) {
					features[indexOf]++;
					redundancy[indexOf]++;
					features[nbWord]++;
				}
			}
			length += sen.getNbMot();
			features[nbWord + 3] += 1.0/(double)sen.getPosition();
		}
		for (int i=0; i<nbWord; i++)
			features[nbWord + 1] += 2*(redundancy[i] - Math.min(redundancy[i], 1));
		features[nbWord + 2] = length/maxLength;
		return features;
	}
	
	@Override
	public double[] instanciateVector() {
		return new double[nbWord + 4 + 1];
	}

	@Override
	public void setIndex(Index<WordIndex> index) {
		this.index = index;
	}

}
