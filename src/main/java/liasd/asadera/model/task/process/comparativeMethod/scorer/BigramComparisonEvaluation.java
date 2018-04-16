package main.java.liasd.asadera.model.task.process.comparativeMethod.scorer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import main.java.liasd.asadera.model.task.process.indexBuilder.ILP.SentenceNGramBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.model.task.process.selectionMethod.scorer.Scorer;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.Summary;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;

public class BigramComparisonEvaluation extends Scorer implements SentenceNGramBasedIn {

	private List<double[]> listCorpusOcc;
	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;

	public BigramComparisonEvaluation(AbstractSelectionMethod method) throws SupportADNException {
		super(method);

		listParameterIn.add(new ParameterizedType(NGram.class, List.class, SentenceNGramBasedIn.class));
	}

	@Override
	public void init() throws Exception {
		computeCorpusDistributions();
	}

	@Override
	public double getScore(Summary summary) throws Exception {
		if (summary.size() != 0) {
			if (summary.getScore() != 0)
				return summary.getScore();
			else {
				Map<NGram, Double> occ = new TreeMap<NGram, Double>();
				for (SentenceModel sent : summary) {
					Set<NGram> curr_ngrams_list = ngrams_in_sentences.get(sent);
					for (NGram ng : curr_ngrams_list)
						if (occ.containsKey(ng))
							occ.put(ng, occ.get(ng) + 1.);
						else
							occ.put(ng, 1.);
				}

				double score = 0;
				summary.setScore(score);
				return score;
			}
		} else
			return 0;
	}

	private void computeCorpusDistributions() {
		for (Corpus corpus : method.getCurrentProcess().getCurrentMultiCorpus()) {
			Map<NGram, Double> sourceOccurences = new TreeMap<NGram, Double>();

			for (TextModel text : corpus)
				for (SentenceModel sen : text) {
					List<NGram> curr_ngrams_list = new ArrayList<NGram>(ngrams_in_sentences.get(sen));
					for (NGram ng : curr_ngrams_list)
						if (!sourceOccurences.containsKey(ng))// If ng not already counted, put 1
							sourceOccurences.put(ng, 1.);
						else // If ng already counted, add 1
							sourceOccurences.put(ng, sourceOccurences.get(ng) + 1.);
				}
			double[] corpusOcc = new double[sourceOccurences.size()];
			int i = 0;
			for (NGram ng : sourceOccurences.keySet()) {
				corpusOcc[i] = sourceOccurences.get(ng);
				i++;
			}
			listCorpusOcc.add(corpusOcc);
		}
	}

	@Override
	public void setSentenceNGram(Map<SentenceModel, Set<NGram>> ngrams_in_sentences) {
		this.ngrams_in_sentences = ngrams_in_sentences;
	}
}
