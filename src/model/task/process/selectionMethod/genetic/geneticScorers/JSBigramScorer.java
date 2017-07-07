package model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import model.task.process.selectionMethod.genetic.GeneticIndividual;
import model.task.process.selectionMethod.genetic.ScoringThread;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.smoothings.DirichletSmoothing;
import textModeling.smoothings.Smoothing;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.InvertedIndex;
import textModeling.wordIndex.NGram;
import textModeling.wordIndex.WordIndex;

public class JSBigramScorer extends GeneticIndividualScorer{

	private TreeMap <NGram, Double> sourceDistribution;
	private TreeMap <NGram, Double> sourceOccurences;
	private TreeMap <NGram, Integer> firstSentencesConcepts;
	private ScoringThread threads_tab[];
	
	private int nbBiGramsInSource;
	private Smoothing smoothing;
	private HashMap<SentenceModel, ArrayList<NGram>> ngrams_in_sentences;
	
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
	
	public void computeNGrams_in_sentences()
	{
		this.ngrams_in_sentences = new HashMap<SentenceModel, ArrayList<NGram>> ();
		
		for (SentenceModel p : ss)
		{
			this.ngrams_in_sentences.put(p, new ArrayList<NGram>(p.getNGrams(2, this.index, null)));
		}
	}
	
	
	/**
	 * Compute the occurences and distribution for the source documents
	 */
	private void computeSourceDistribution()
	{
		this.sourceDistribution = new TreeMap <NGram, Double>();
		this.sourceOccurences = new TreeMap <NGram, Double> ();
		this.firstSentencesConcepts = new TreeMap <NGram, Integer> ();
		this.nbBiGramsInSource = 0;
		double modified_nbBiGramsInSource = 0.;
		
		for (SentenceModel p : this.ngrams_in_sentences.keySet())
		{
			//System.out.println("phrase pos : "+p.getPosition());
			ArrayList<NGram> curr_ngrams_list = new ArrayList<NGram>(this.ngrams_in_sentences.get(p));
					//p.getNGrams(2, this.index, this.filter);
			//ArrayList<NGram> curr_ngrams_list = p.getBiGrams( this.index, this.filter);
			for (NGram ng : curr_ngrams_list)
			{
				if (!this.sourceOccurences.containsKey(ng)) { //If ng not already counted, put 1
					this.sourceOccurences.put(ng, 1.);
				}
				else //If ng already counted, add 1
					this.sourceOccurences.put(ng, this.sourceOccurences.get(ng) + 1.);
				this.nbBiGramsInSource++;
				
				if (p.getPosScore() == 1)
				{
					if (this.firstSentencesConcepts.containsKey(ng))
						this.firstSentencesConcepts.put(ng, this.firstSentencesConcepts.get(ng) + 1);
					else
						this.firstSentencesConcepts.put(ng, 1);
				}
			}
		}
		
		System.out.println(" Nombre de bigrams : "+this.nbBiGramsInSource);
		
		for (NGram ng : this.firstSentencesConcepts.keySet())
		{
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
		
		System.out.println(" Nombre de bigrams après filtrage : "+this.nbBiGramsInSource+" | "+modified_nbBiGramsInSource);
		for (NGram ng : this.sourceOccurences.keySet())
		{
			this.sourceDistribution.put(ng, (double)this.sourceOccurences.get(ng) / modified_nbBiGramsInSource );
		}
	}
	
	@Override
	public void computeScore(ArrayList<GeneticIndividual> population) {
		int cpt = 0;
		threads_tab = new ScoringThread[population.size()];
		for (GeneticIndividual gi : population)
		{
			threads_tab[cpt] = new ScoringThread(gi, ngrams_in_sentences, this.sourceDistribution, this.firstSentencesConcepts, this.index, this.firstSentenceConceptsFactor, this.delta);
			threads_tab[cpt].start();
			cpt++;
		}
		
		cpt = 0;
		
		for (ScoringThread st : threads_tab) {
			try {
				st.join();
			}catch (InterruptedException ie) {
				ie.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	private double jensenShanonDivergence(GeneticIndividual gi, TreeMap<NGram, Double> summDistrib)//, int summNbTokens)
	{
		double divergence = 0;
		//Double dProbSumm;
		double divider; //divider1 = summNbTokens + this.delta;//divider1 = summNbTokens + this.delta * 1.5 * this.sourceDistribution.size();
		double probSumm;
		double probSource;
		double log2 = Math.log(2);
		double sourceOp;
		double summOp;
		
		
		//System.out.println("summDistrib size : "+summDistrib.size());
		//System.out.println("sourceDist size : "+this.sourceDistribution.size());
		
		for (NGram ng : this.sourceDistribution.keySet())
		{
			probSource = this.sourceDistribution.get(ng);
			/*dProbSumm = summDistrib.get(ng);
			
			//probSumm = dProbSumm == null ? this.delta / divider1 : (dProbSumm + this.delta) / divider1;
			probSumm = dProbSumm == null ? this.delta * probSource / divider1 : (dProbSumm + this.delta * probSource) / divider1;
			System.out.print("probSumm : "+probSumm);*/
			//probSumm = kz.getSmoothedProb(ng);
			//System.out.println(" | "+probSumm);
			
			probSumm = this.smoothing.getSmoothedProb(ng);
			
			
			divider = probSource + probSumm;
			sourceOp = 2 * probSource / divider;
			summOp = 2 * probSumm / divider;
			
			divergence += probSumm * Math.log(summOp) / log2;
			divergence += probSource * Math.log(sourceOp) / log2;
		}
		
		return 1 - divergence / 2.;
	}

	
	public TreeMap<NGram, Double> getSourceDistribution() {
		return sourceDistribution;
	}

	public TreeMap<NGram, Double> getSourceOccurences() {
		return sourceOccurences;
	}

	public TreeMap<NGram, Integer> getFirstSentencesConcepts() {
		return firstSentencesConcepts;
	}

	@Override
	public double computeScore(GeneticIndividual gi) {
		TreeMap<NGram, Double> summDistrib = new TreeMap <NGram, Double > ();
		//this.computeIndividualDistribution(gi, summDistrib);
		
		//this.smoothing = new WeightedLaplace (2, this.delta, summDistrib.size(), gi.getGenes(), this.index, this.filter);
		this.smoothing = new DirichletSmoothing(2, this.delta, summDistrib.size(), ngrams_in_sentences, gi.getGenes(), this.index, this.sourceDistribution, this.firstSentencesConcepts, this.firstSentenceConceptsFactor);
		//this.smoothing = new GoodTuring(2, this.delta, summDistrib.size(), gi.getGenes(), this.index, this.filter);
			
		double jsd = this.jensenShanonDivergence (gi, summDistrib);
		
		return jsd;
	}
}