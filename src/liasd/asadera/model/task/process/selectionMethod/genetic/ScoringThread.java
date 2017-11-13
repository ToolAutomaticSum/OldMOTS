package liasd.asadera.model.task.process.selectionMethod.genetic;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.NGram;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class ScoringThread extends Thread implements Runnable {
	private GeneticIndividual gi;
	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	private Map <NGram, Double> sourceDistribution;
	private Map <NGram, Integer> firstSentencesConcepts;
	private double delta;
	//private Index index;
	private double fsc;
	
	public ScoringThread (GeneticIndividual gi, Map<SentenceModel, Set<NGram>> ngrams_in_sentences2, Map<NGram, Double> sourceDistribution2, Map<NGram, Integer> firstSentencesConcepts2, Index<WordIndex> index, double fsc, double delta) {
		this.gi = gi;
		this.ngrams_in_sentences = ngrams_in_sentences2;
		this.sourceDistribution = sourceDistribution2;
		this.firstSentencesConcepts = firstSentencesConcepts2;
		//this.index = index;
		this.fsc = fsc;
		this.delta = delta;
	}

	public void run() {
		//double t = System.currentTimeMillis();
		double js = jensenShannon();
		gi.setScore(js);
		//System.out.println(t-System.currentTimeMillis() + "\t" + "JensenShannon");
	}
	
	private double jensenShannon() {
		double divergence = 0.;
		
		TreeMap <NGram, Double> distribGI;
		distribGI = computeNGram_GI_distrib();
		
		double divider;
		double probSumm;
		double probSource;
		double log2 = Math.log(2);
		
		for (NGram ng : this.sourceDistribution.keySet())
		{
			probSource = this.sourceDistribution.get(ng);
			
			probSumm = distribGI.get(ng);
			
			
			divider = (probSource + probSumm)/2.;
			//sourceOp = 2 * probSource / divider;
			//summOp = 2 * probSumm / divider;
			
			divergence += probSumm * Math.log(probSumm/divider) / log2;
			divergence += probSource * Math.log(probSource/divider) / log2;
		}
		return 1. - divergence / 2.;
	}
	
	/*private TreeMap <NGram, Double> computeNGram_GI_occs () {
		TreeMap <NGram, Double> distrib = new TreeMap <NGram, Double> ();
		double nb_bi_grams_gi = 0.;
		for (SentenceModel p : gi.getGenes()) {
			ArrayList <NGram> curr_ngrams_list = p.getNGrams(2, index);
			for (NGram ng : curr_ngrams_list) {
				
				if (distrib.containsKey(ng)) {
					distrib.put(ng, distrib.get(ng) + 1.);
				}
				else {
					distrib.put(ng, 1.);
				}
				nb_bi_grams_gi += 1.;
			}
		}
		for (NGram ng : distrib.keySet()) {
			if (this.firstSentencesConcepts.containsKey(ng)) {
				double d = distrib.get(ng) ;
				double dMult = d * fsc;
				nb_bi_grams_gi += dMult;
				d += dMult;
				distrib.put (ng, d);
				
			}
		}

		return distrib;		
	}*/
	
	private TreeMap <NGram, Double> computeNGram_GI_distrib () {
		TreeMap <NGram, Double> distrib = new TreeMap <NGram, Double> ();
		//System.out.println("distrib : "+distrib);
		double nb_bi_grams_gi = 0.;
		for (SentenceModel p : this.gi.getGenes()) {
			Set<NGram> curr_ngrams_list = ngrams_in_sentences.get(p);
			for (NGram ng : curr_ngrams_list) {
				//TODO : GROSSE APPROXIMATION : il faut parcourir les bigrams récupérés de la phrase, et non
				//ngrams in sentence, car là on a toujours 1 comme valeur pour un ngram même si présent plusieurs fois.
				if (distrib.containsKey(ng)) {
					distrib.put(ng, distrib.get(ng) + 1.);
				}
				else {
					distrib.put(ng, 1.);
				}
				nb_bi_grams_gi += 1.;
			}
		}
		for (NGram ng : distrib.keySet()) {
			if (this.firstSentencesConcepts.containsKey(ng)) {
				double d = distrib.get(ng) ;
				double dMult = d * fsc;
				nb_bi_grams_gi += dMult;
				d += dMult;
				distrib.put (ng, d);
				
			}
		}
		
		//Smoothing
		/*TreeSet <NGram> corpusDistribMinusGIDistrib = new TreeSet <NGram> ();
		corpusDistribMinusGIDistrib.addAll(this.sourceDistribution.keySet());
		corpusDistribMinusGIDistrib.removeAll(distrib.keySet());*/
		
		double divider = nb_bi_grams_gi + this.delta;
		
		for (NGram ng : this.sourceDistribution.keySet()) {
			double probSource = this.sourceDistribution.get(ng);
			Double dProb = distrib.get(ng);
			if (dProb == null)
				dProb = 0.;
			distrib.put(ng,(dProb + this.delta * probSource) / divider);
		}
		
		/*for (NGram ng : corpusDistribMinusGIDistrib) {
			double probSource = this.sourceDistribution.get(ng);
			distrib.put(ng,this.delta * probSource / divider); 
		}*/
		
		return distrib;
	}
	

}
