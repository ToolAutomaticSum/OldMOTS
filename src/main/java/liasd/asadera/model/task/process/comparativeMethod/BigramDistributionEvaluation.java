package main.java.liasd.asadera.model.task.process.comparativeMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import main.java.liasd.asadera.model.task.process.indexBuilder.ILP.SentenceNGramBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.tools.Pair;

public class BigramDistributionEvaluation extends AbstractComparativeMethod implements SentenceNGramBasedIn {

	private List<double[]> listCorpusDistribution;
	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	private Map<NGram, Double> sourceDistribution;
	private Map<NGram, Integer> firstSentencesConcepts;
//	private SimilarityMetric sim;
	private int nbBiGramsInSource;
	private double firstSentenceConceptsFactor;
//	private double delta;

	public BigramDistributionEvaluation(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(NGram.class, List.class, SentenceNGramBasedIn.class));
	}

	@Override
	public AbstractComparativeMethod makeCopy() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initADN() throws Exception {
//		delta = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "Delta"));
		firstSentenceConceptsFactor = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "Fsc"));

//		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");

//		sim = SimilarityMetric.instanciateSentenceSimilarity(similarityMethod);
	}

	@Override
	public List<Pair<SentenceModel, String>> calculateDifference(List<Corpus> listCorpus) throws Exception {
		computeCorpusDistributions(listCorpus);

		return null;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	private void computeCorpusDistributions(List<Corpus> listCorpus) {
		for (Corpus corpus : listCorpus) {
			sourceDistribution = new TreeMap<NGram, Double>();
			Map<NGram, Double> sourceOccurences = new TreeMap<NGram, Double>();
			firstSentencesConcepts = new TreeMap<NGram, Integer>();
			nbBiGramsInSource = 0;
			double modified_nbBiGramsInSource = 0.;

			for (TextModel text : corpus)
				for (SentenceModel sen : text) {
					List<NGram> curr_ngrams_list = new ArrayList<NGram>(ngrams_in_sentences.get(sen));
					nbBiGramsInSource += curr_ngrams_list.size();
					for (NGram ng : curr_ngrams_list) {
						if (!sourceOccurences.containsKey(ng))// If ng not already counted, put 1
							sourceOccurences.put(ng, 1.);
						else // If ng already counted, add 1
							sourceOccurences.put(ng, sourceOccurences.get(ng) + 1.);

						if (sen.getPosScore() == 1) {
							if (firstSentencesConcepts.containsKey(ng))
								firstSentencesConcepts.put(ng, firstSentencesConcepts.get(ng) + 1);
							else
								firstSentencesConcepts.put(ng, 1);
						}
					}
				}
			for (NGram ng : firstSentencesConcepts.keySet()) {
				double d = sourceOccurences.get(ng);
				modified_nbBiGramsInSource += firstSentenceConceptsFactor * d;
				sourceOccurences.put(ng, d + firstSentenceConceptsFactor * d);
			}

			modified_nbBiGramsInSource += nbBiGramsInSource;

			double[] corpusDistri = new double[sourceOccurences.size()];
			int j = 0;
			for (NGram ng : sourceOccurences.keySet()) {
				sourceDistribution.put(ng, (double) sourceOccurences.get(ng) / modified_nbBiGramsInSource);
				corpusDistri[j] = sourceDistribution.get(ng);
				j++;
			}
			listCorpusDistribution.add(corpusDistri);
		}
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSentenceNGram(Map<SentenceModel, Set<NGram>> ngrams_in_sentences) {
		// TODO Auto-generated method stub

	}

}
