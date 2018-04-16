package main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.featurer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.ReinforcementLearning;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class BigramFeatures extends Featurer implements IndexBasedIn<NGram> {

	private Index<NGram> indexNG;
	private List<WordIndex> topTfIdf;
	private int maxLength;
	private int nbWord;

	public BigramFeatures(ReinforcementLearning rl) throws SupportADNException {
		super(rl);

		listParameterIn.add(new ParameterizedType(NGram.class, Index.class, IndexBasedIn.class));
	}

	@Override
	public void init(int maxLength) throws Exception {
		nbWord = Integer.parseInt(rl.getCurrentProcess().getModel().getProcessOption(rl.getId(), "NbWord"));
		int corpusId = rl.getCurrentProcess().getCorpusToSummarize().getiD();
		this.maxLength = maxLength;

		topTfIdf = new ArrayList<WordIndex>(indexNG.values());
		Collections.sort(topTfIdf,
				(a, b) -> -Double.compare(a.getTfCorpus(corpusId) * a.getIdf(indexNG.getNbDocument()),
						b.getTfCorpus(corpusId) * b.getIdf(indexNG.getNbDocument())));
		topTfIdf = topTfIdf.subList(0, nbWord - 1);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public double[] getFeatures(List<SentenceModel> summary) {
		double[] features = instanciateVector();
		double[] redundancy = new double[nbWord];
		int length = 0;
		for (SentenceModel sen : summary) {
			for (WordIndex ng : sen) {
				int indexOf = topTfIdf.indexOf(indexNG.get(ng.getWord()));
				if (indexOf != -1) {
					features[indexOf]++;
					redundancy[indexOf]++;
					features[nbWord]++;
				}
			}
			length += sen.getNbMot();
			features[nbWord + 3] += 1.0 / (double) sen.getPosition();
		}
		for (int i = 0; i < nbWord; i++)
			features[nbWord + 1] += 2 * (redundancy[i] - Math.min(redundancy[i], 1));
		features[nbWord + 2] = length / maxLength;
		return features;
	}

	@Override
	public double[] instanciateVector() {
		return new double[nbWord + 4 + 1];
	}

	@Override
	public void setIndex(Index<NGram> index) {
		this.indexNG = index;
	}
}
