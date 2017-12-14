package liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import liasd.asadera.model.task.process.selectionMethod.genetic.ScoringThread;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.smoothings.DirichletSmoothing;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.InvertedIndex;
import liasd.asadera.textModeling.wordIndex.NGram;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class JSBigramScorer extends GeneticIndividualScorer {

	private Map <WordIndex, Double> sourceDistribution;
	private Map <WordIndex, Double> sourceOccurences;
	private Map <WordIndex, Integer> firstSentencesConcepts;
	private ScoringThread threads_tab[];
	
	private int nbBiGramsInSource;
	private DirichletSmoothing smoothing;
	private List<SentenceModel> listSen;
//	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	
	public JSBigramScorer(HashMap <GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus, InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, ss, corpus, null, index, null, delta, firstSentenceConceptsFactor,
				null, null);	
	}
	
	@Override
	public void init() {
		//System.out.println("JS Bigram scorer initialization.");
		computeNGrams_in_sentences();
		
		computeSourceDistribution ();
		//System.out.println("JS Bigram scorer initialized.");
		/*Writer w = new Writer("sourceDistribution.txt");
		w.open();
		for(Entry<NGram, Double>  ng : sourceOccurences.entrySet())
			w.write(ng.getKey() + "\t" + ng.getValue() + "\t" + sourceDistribution.get(ng.getKey()) + "\n");
		w.close();
		System.out.println("Source Distribution OK !!");*/
	}
	
	public void computeNGrams_in_sentences() {
//		ngrams_in_sentences = new HashMap<SentenceModel, Set<NGram>>();
		listSen = new ArrayList<SentenceModel>();
		for (SentenceModel p : ss) {
			Set<NGram> set = new HashSet<NGram>();
			for (WordIndex wi : p.getListWordIndex(2))
				set.add((NGram) wi);
			listSen.add(p);
		}
	}
	
	
	/**
	 * Compute the occurences and distribution for the source documents
	 */
	private void computeSourceDistribution() {
		sourceDistribution = new TreeMap <WordIndex, Double>();
		sourceOccurences = new TreeMap <WordIndex, Double> ();
		firstSentencesConcepts = new TreeMap <WordIndex, Integer> ();
		nbBiGramsInSource = 0;
		double modified_nbBiGramsInSource = 0.;
		
		for (SentenceModel p : listSen) {
			//System.out.println("phrase pos : "+p.getPosition());
			List<WordIndex> curr_ngrams_list = p;
					//p.getNGrams(2, index, filter);
			//ArrayList<NGram> curr_ngrams_list = p.getBiGrams( index, filter);
			for (WordIndex ng : curr_ngrams_list) {
				if (!sourceOccurences.containsKey(ng))//If ng not already counted, put 1
					sourceOccurences.put(ng, 1.);
				else //If ng already counted, add 1
					sourceOccurences.put(ng, sourceOccurences.get(ng) + 1.);
				
				nbBiGramsInSource++;
				
				if (p.getPosScore() == 1) {
					if (firstSentencesConcepts.containsKey(ng))
						firstSentencesConcepts.put(ng, firstSentencesConcepts.get(ng) + 1);
					else
						firstSentencesConcepts.put(ng, 1);
				}
			}
		}
		
		System.out.println("Number of bigrams : "+nbBiGramsInSource);
		
		for (WordIndex ng : firstSentencesConcepts.keySet()) {
			double d = sourceOccurences.get(ng);
			modified_nbBiGramsInSource += firstSentenceConceptsFactor * d;
			sourceOccurences.put(ng, d + firstSentenceConceptsFactor * d);
		}
		
		modified_nbBiGramsInSource += nbBiGramsInSource;
		
		/*TreeMap <NGram, Double> sourceOcc_copy = new TreeMap <NGram, Double> ();
		sourceOcc_copy.putAll(sourceOccurences);
		for (NGram ng: sourceOcc_copy.keySet())
		{
			double curr_occ = sourceOccurences.get(ng);
			if (curr_occ < 2) {
				sourceOccurences.remove(ng);
				//nbBiGramsInSource += curr_occ;
				modified_nbBiGramsInSource -= curr_occ;
				//ng.printNGram();
				//System.out.println(" : "+sourceDistribution.get(ng)+" | "+sourceOccurences.get(ng));
			}
		}*/
		
		System.out.println("Number of bigrams after filtering : "+nbBiGramsInSource+" | "+modified_nbBiGramsInSource);
		for (WordIndex ng : sourceOccurences.keySet())	{
			sourceDistribution.put(ng, (double)sourceOccurences.get(ng) / modified_nbBiGramsInSource );
		}
	}
	
	@Override
	public void computeScore(ArrayList<GeneticIndividual> population) {
		int cpt = 0;
		threads_tab = new ScoringThread[population.size()];
		for (GeneticIndividual gi : population) {
			threads_tab[cpt] = new ScoringThread(gi, sourceDistribution, firstSentencesConcepts, index, firstSentenceConceptsFactor, delta);
			threads_tab[cpt].start();
			cpt++;
		}
		
		cpt = 0;
		
		for (ScoringThread st : threads_tab) {
			try {
				st.join();
			} 
			catch (InterruptedException ie) {
				ie.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	private double jensenShanonDivergence(GeneticIndividual gi, Map<WordIndex, Double> summDistrib) {
		double divergence = 0;
		double divider; //divider1 = summNbTokens + delta;//divider1 = summNbTokens + delta * 1.5 * sourceDistribution.size();
		double probSumm;
		double probSource;
		double log2 = Math.log(2);
		double sourceOp;
		double summOp;

		for (WordIndex ng : sourceDistribution.keySet()) {
			probSource = sourceDistribution.get(ng);

			probSumm = smoothing.getSmoothedProb(ng);
			
			divider = probSource + probSumm;
			sourceOp = 2 * probSource / divider;
			summOp = 2 * probSumm / divider;
			
			divergence += probSumm * Math.log(summOp) / log2;
			divergence += probSource * Math.log(sourceOp) / log2;
		}
		
		return 1 - divergence / 2.;
	}

	
	public Map<WordIndex, Double> getSourceDistribution() {
		return sourceDistribution;
	}

	public Map<WordIndex, Double> getSourceOccurences() {
		return sourceOccurences;
	}

	public Map<WordIndex, Integer> getFirstSentencesConcepts() {
		return firstSentencesConcepts;
	}

	@Override
	public double computeScore(GeneticIndividual gi) {
		Map<WordIndex, Double> summDistrib = new TreeMap<WordIndex, Double>();
		//computeIndividualDistribution(gi, summDistrib);
		
		//smoothing = new WeightedLaplace (2, delta, summDistrib.size(), gi.getGenes(), index, filter);
		smoothing = new DirichletSmoothing(2, delta, summDistrib.size(), gi.getGenes(), sourceDistribution, firstSentencesConcepts, firstSentenceConceptsFactor);
		//smoothing = new GoodTuring(2, delta, summDistrib.size(), gi.getGenes(), index, filter);
			
		double jsd = jensenShanonDivergence (gi, summDistrib);
		
		return jsd;
	}
}