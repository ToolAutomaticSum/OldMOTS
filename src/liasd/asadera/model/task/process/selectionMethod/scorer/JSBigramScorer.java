package liasd.asadera.model.task.process.selectionMethod.scorer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import liasd.asadera.model.task.process.indexBuilder.ILP.SentenceNGramBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.Summary;
import liasd.asadera.textModeling.smoothings.DirichletSmoothing;
import liasd.asadera.textModeling.wordIndex.NGram;
import liasd.asadera.tools.sentenceSimilarity.SentenceSimilarityMetric;

public class JSBigramScorer extends Scorer implements SentenceNGramBasedIn {

	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	private Map<NGram, Double> sourceDistribution;
	private Map<NGram, Integer> firstSentencesConcepts;
	private SentenceSimilarityMetric sim;
	private int nbBiGramsInSource;
	private double firstSentenceConceptsFactor;
	private double delta;
	private double[] corpusDistri;
	
	public JSBigramScorer(AbstractSelectionMethod method) throws SupportADNException {
		super(method);

		listParameterIn.add(new ParametrizedType(NGram.class, List.class, SentenceNGramBasedIn.class));
	}

	@Override
	public void init() throws Exception {
		delta = Double.parseDouble(method.getCurrentProcess().getModel().getProcessOption(id, "Delta"));
		firstSentenceConceptsFactor = Double.parseDouble(method.getCurrentProcess().getModel().getProcessOption(id, "Fsc"));
		
		String similarityMethod = method.getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");
		
		sim = SentenceSimilarityMetric.instanciateSentenceSimilarity(similarityMethod);
		
		computeSourceDistribution();
	}

	@Override
	public double getScore(Summary summary) throws Exception {		
		if (summary.size() != 0) {
			if (summary.getScore() != 0)
				return summary.getScore();
			else {
				DirichletSmoothing smoothing = new DirichletSmoothing(2, delta, sourceDistribution.size(), ngrams_in_sentences, summary, sourceDistribution, firstSentencesConcepts, firstSentenceConceptsFactor);
				double[] distri = smoothing.getSmoothedDistrib();
				double jsd = sim.computeSimilarity(distri, corpusDistri); //this.jensenShanonDivergence (gi, summDistrib);
				//double jsd2 = jensenShannon(corpusDistri, distri);
				summary.setScore(jsd);
				return jsd;
			}
		}
		else
			return 0;
	}
	
	private void computeSourceDistribution() {
		sourceDistribution = new TreeMap <NGram, Double>();
		Map<NGram, Double> sourceOccurences = new TreeMap <NGram, Double> ();
		firstSentencesConcepts = new TreeMap <NGram, Integer> ();
		nbBiGramsInSource = 0;
		double modified_nbBiGramsInSource = 0.;
		
		for (SentenceModel p : ngrams_in_sentences.keySet()) {
			//System.out.println("phrase pos : "+p.getPosition());
			List<NGram> curr_ngrams_list = new ArrayList<NGram>(ngrams_in_sentences.get(p));
			nbBiGramsInSource += curr_ngrams_list.size();
					//p.getNGrams(2, this.index, this.filter);
			//ArrayList<NGram> curr_ngrams_list = p.getBiGrams( this.index, this.filter);
			for (NGram ng : curr_ngrams_list) {
				if (!sourceOccurences.containsKey(ng))//If ng not already counted, put 1
					sourceOccurences.put(ng, 1.);
				else //If ng already counted, add 1
					sourceOccurences.put(ng, sourceOccurences.get(ng) + 1.);
				
				if (p.getPosScore() == 1) {
					if (firstSentencesConcepts.containsKey(ng))
						firstSentencesConcepts.put(ng, firstSentencesConcepts.get(ng) + 1);
					else
						firstSentencesConcepts.put(ng, 1);
				}
			}
		}
		
		System.out.println("Number of bigrams : " + nbBiGramsInSource);
		
		for (NGram ng : firstSentencesConcepts.keySet()) {
			double d = sourceOccurences.get(ng);
			modified_nbBiGramsInSource += firstSentenceConceptsFactor * d;
			sourceOccurences.put(ng, d + firstSentenceConceptsFactor * d);
		}
		
		modified_nbBiGramsInSource += nbBiGramsInSource;
		
		corpusDistri = new double[sourceOccurences.size()];
		int i = 0;
		System.out.println("Number of bigrams after filtering : " + nbBiGramsInSource + " | " + modified_nbBiGramsInSource);
		for (NGram ng : sourceOccurences.keySet())	{
			sourceDistribution.put(ng, (double) sourceOccurences.get(ng) / modified_nbBiGramsInSource );
			corpusDistri[i] = sourceDistribution.get(ng);
			i++;
		}
	}
	
	@Override
	public void setSentenceNGram(Map<SentenceModel, Set<NGram>> ngrams_in_sentences) {
		this.ngrams_in_sentences = ngrams_in_sentences;
	}
}
