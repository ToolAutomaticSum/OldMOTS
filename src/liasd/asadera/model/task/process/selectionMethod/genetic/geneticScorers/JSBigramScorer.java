package liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import liasd.asadera.model.task.process.selectionMethod.genetic.ScoringThread;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.smoothings.DirichletSmoothing;
import liasd.asadera.textModeling.smoothings.Smoothing;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.InvertedIndex;
import liasd.asadera.textModeling.wordIndex.NGram;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class JSBigramScorer extends GeneticIndividualScorer {

	private Map <NGram, Double> sourceDistribution;
	private Map <NGram, Double> sourceOccurences;
	private Map <NGram, Integer> firstSentencesConcepts;
	private ScoringThread threads_tab[];
	
	private int nbBiGramsInSource;
	private Smoothing smoothing;
	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	
	public JSBigramScorer(HashMap <GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus, InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, ss, corpus, null, index, null, delta, firstSentenceConceptsFactor,
				null, null);	
	}
	
	@Override
	public void init() {
		//System.out.println("JS Bigram scorer initialization.");
		this.computeNGrams_in_sentences();
		
		this.computeSourceDistribution ();
		//System.out.println("JS Bigram scorer initialized.");
		/*Writer w = new Writer("sourceDistribution.txt");
		w.open();
		for(Entry<NGram, Double>  ng : sourceOccurences.entrySet())
			w.write(ng.getKey() + "\t" + ng.getValue() + "\t" + sourceDistribution.get(ng.getKey()) + "\n");
		w.close();
		System.out.println("Source Distribution OK !!");*/
	}
	
	public void computeNGrams_in_sentences() {
		ngrams_in_sentences = new HashMap<SentenceModel, Set<NGram>> ();
		
		for (SentenceModel p : ss)
			ngrams_in_sentences.put(p, new TreeSet<NGram>(p.getNGrams(2, this.index, null)));
	}
	
	
	/**
	 * Compute the occurences and distribution for the source documents
	 */
	private void computeSourceDistribution() {
		this.sourceDistribution = new TreeMap <NGram, Double>();
		this.sourceOccurences = new TreeMap <NGram, Double> ();
		this.firstSentencesConcepts = new TreeMap <NGram, Integer> ();
		this.nbBiGramsInSource = 0;
		double modified_nbBiGramsInSource = 0.;
		
		for (SentenceModel p : this.ngrams_in_sentences.keySet()) {
			//System.out.println("phrase pos : "+p.getPosition());
			List<NGram> curr_ngrams_list = new ArrayList<NGram>(this.ngrams_in_sentences.get(p));
					//p.getNGrams(2, this.index, this.filter);
			//ArrayList<NGram> curr_ngrams_list = p.getBiGrams( this.index, this.filter);
			for (NGram ng : curr_ngrams_list) {
				if (!this.sourceOccurences.containsKey(ng))//If ng not already counted, put 1
					this.sourceOccurences.put(ng, 1.);
				else //If ng already counted, add 1
					this.sourceOccurences.put(ng, this.sourceOccurences.get(ng) + 1.);
				
				this.nbBiGramsInSource++;
				
				if (p.getPosScore() == 1) {
					if (this.firstSentencesConcepts.containsKey(ng))
						this.firstSentencesConcepts.put(ng, this.firstSentencesConcepts.get(ng) + 1);
					else
						this.firstSentencesConcepts.put(ng, 1);
				}
			}
		}
		
		System.out.println("Number of bigrams : "+this.nbBiGramsInSource);
		
		for (NGram ng : this.firstSentencesConcepts.keySet()) {
			double d = this.sourceOccurences.get(ng);
			modified_nbBiGramsInSource += this.firstSentenceConceptsFactor * d;
			this.sourceOccurences.put(ng, d + this.firstSentenceConceptsFactor * d);
		}
		
		modified_nbBiGramsInSource += this.nbBiGramsInSource;
		
		/*TreeMap <NGram, Double> sourceOcc_copy = new TreeMap <NGram, Double> ();
		sourceOcc_copy.putAll(this.sourceOccurences);
		for (NGram ng: sourceOcc_copy.keySet())
		{
			double curr_occ = this.sourceOccurences.get(ng);
			if (curr_occ < 2) {
				this.sourceOccurences.remove(ng);
				//this.nbBiGramsInSource += curr_occ;
				modified_nbBiGramsInSource -= curr_occ;
				//ng.printNGram();
				//System.out.println(" : "+this.sourceDistribution.get(ng)+" | "+this.sourceOccurences.get(ng));
			}
		}*/
		
		System.out.println("Number of bigrams after filtering : "+this.nbBiGramsInSource+" | "+modified_nbBiGramsInSource);
		for (NGram ng : this.sourceOccurences.keySet())	{
			this.sourceDistribution.put(ng, (double)this.sourceOccurences.get(ng) / modified_nbBiGramsInSource );
		}
	}
	
	@Override
	public void computeScore(ArrayList<GeneticIndividual> population) {
		int cpt = 0;
		threads_tab = new ScoringThread[population.size()];
		for (GeneticIndividual gi : population) {
			threads_tab[cpt] = new ScoringThread(gi, ngrams_in_sentences, this.sourceDistribution, this.firstSentencesConcepts, this.index, this.firstSentenceConceptsFactor, this.delta);
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
	
	private double jensenShanonDivergence(GeneticIndividual gi, Map<NGram, Double> summDistrib) {
		double divergence = 0;
		double divider; //divider1 = summNbTokens + this.delta;//divider1 = summNbTokens + this.delta * 1.5 * this.sourceDistribution.size();
		double probSumm;
		double probSource;
		double log2 = Math.log(2);
		double sourceOp;
		double summOp;

		for (NGram ng : this.sourceDistribution.keySet()) {
			probSource = this.sourceDistribution.get(ng);

			probSumm = this.smoothing.getSmoothedProb(ng);
			
			divider = probSource + probSumm;
			sourceOp = 2 * probSource / divider;
			summOp = 2 * probSumm / divider;
			
			divergence += probSumm * Math.log(summOp) / log2;
			divergence += probSource * Math.log(sourceOp) / log2;
		}
		
		return 1 - divergence / 2.;
	}

	
	public Map<NGram, Double> getSourceDistribution() {
		return sourceDistribution;
	}

	public Map<NGram, Double> getSourceOccurences() {
		return sourceOccurences;
	}

	public Map<NGram, Integer> getFirstSentencesConcepts() {
		return firstSentencesConcepts;
	}

	@Override
	public double computeScore(GeneticIndividual gi) {
		Map<NGram, Double> summDistrib = new TreeMap <NGram, Double > ();
		//this.computeIndividualDistribution(gi, summDistrib);
		
		//this.smoothing = new WeightedLaplace (2, this.delta, summDistrib.size(), gi.getGenes(), this.index, this.filter);
		this.smoothing = new DirichletSmoothing(2, this.delta, summDistrib.size(), ngrams_in_sentences, gi.getGenes(), this.sourceDistribution, this.firstSentencesConcepts, this.firstSentenceConceptsFactor);
		//this.smoothing = new GoodTuring(2, this.delta, summDistrib.size(), gi.getGenes(), this.index, this.filter);
			
		double jsd = this.jensenShanonDivergence (gi, summDistrib);
		
		return jsd;
	}
}