package model.task.process.ILP;

import java.util.ArrayList;
import java.util.HashMap;

import exception.LacksOfFeatures;
import model.task.process.AbstractProcess;
import optimize.SupportADNException;
import optimize.parameter.Parameter;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.NGram;
import textModeling.wordIndex.WordIndex;
import tools.Tools;

public class BiGram_ILP extends AbstractProcess implements BiGramListBasedOut{

	public static enum BiGramILP_Parameter {
		fscFactor("fscFactor");

		private String name;

		private BiGramILP_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	
	private ArrayList<Double> bigram_weights;
	private ArrayList<ArrayList<Integer>> bigrams_in_sentence;
	private ArrayList<NGram> bigrams;
	private double fsc_factor = 1;
	
	public BiGram_ILP(int id) throws SupportADNException, NumberFormatException, LacksOfFeatures {
		super(id);
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("fscFactor", Double.class);
	}
	
	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		adn.putParameter(new Parameter<Double>(BiGramILP_Parameter.fscFactor.getName(), Double.parseDouble(getModel().getProcessOption(id, BiGramILP_Parameter.fscFactor.getName()))));
	}

	@Override
	public void process() throws Exception {
		this.buildWeightsAndSentences();
		
		super.process();
	}
	
	@Override
	public void finish() throws Exception {
		super.finish();
		bigram_weights = null;
		bigrams_in_sentence = null;
		bigrams = null;
	}
	
	private void buildWeightsAndSentences()
	{
		this.bigram_weights = new ArrayList<Double> ();
		this.bigrams_in_sentence = new ArrayList<ArrayList<Integer> > ();
		this.bigrams = new ArrayList<NGram> ();
		int ind;
		ArrayList <NGram> curr_bg_arr;
		ArrayList <Integer> curr_doc_bg_arr;

		System.out.println("Construction du modèle");
		
		/*for (TextModel t : corpusToSummarize) {
			HashMap <NGram, Integer> firstSentencesConcepts = new HashMap <NGram, Integer> ();
			curr_doc_bg_arr = new ArrayList <Integer> ();
			for ( SentenceModel s : t) {
				for (WordModel w : s) {
					if (!index.containsKey(w.getmLemma()))
						index.put(w.getmLemma(), new WordIndex(w.getmLemma(), index));
					index.get(w.getmLemma()).add(w);
				}
				//On construit le set des bigrams dans la phrase
				ArrayList <Integer> sentence_bigrams = new ArrayList <Integer> ();
				curr_bg_arr = s.getNGrams(2, index); //generateBiGrams(index, s, filter);
				Tools.toSet(curr_bg_arr);
				for (NGram ng : curr_bg_arr)
				{
					if ( ( ind = this.bigrams.indexOf(ng)) != -1 )
						sentence_bigrams.add(ind);
					else
					{
						this.bigrams.add(ng);
						sentence_bigrams.add(this.bigrams.size() - 1 );
					}
					
					if (s.getPosition() == 1)
						firstSentencesConcepts.put(ng, 1);
				}
				//On ajoute le set des bigrams de la phrase � bigrams_in_sentence
				bigrams_in_sentence.add(sentence_bigrams);
				//ss.add(s);
				//On ajoute le set des bigrams de la phrase au document 
				curr_doc_bg_arr.addAll(sentence_bigrams);
				Tools.toSet(curr_doc_bg_arr);
			}
			//On ajoute 1 au poids des bigrams de curr_doc_bg_arr dans bigram_weights
			for (Integer i : curr_doc_bg_arr)
			{
				if ( this.bigram_weights.size() - 1 >= i )
				{
					this.bigram_weights.set(i, this.bigram_weights.get(i) + 1.);
				}
				else
				{
					if ( this.bigram_weights.size() == i)
					{
						this.bigram_weights.add(1.);
					}
					else System.out.println("Pas bon");
				}
			}
			//On ajoute this.fsc_factor au poids des bigrammes de la premi�re phrase du document
			for (NGram ng : firstSentencesConcepts.keySet())
			{
				Integer i = this.bigrams.indexOf(ng);
				this.bigram_weights.set( i, this.bigram_weights.get(i) + (this.fsc_factor) );
			}
		}*/
		
		for (TextModel text : corpusToSummarize)
		{
			HashMap <NGram, Integer> firstSentencesConcepts = new HashMap <NGram, Integer> ();
			curr_doc_bg_arr = new ArrayList <Integer> ();
			for ( SentenceModel sen : text )
			{
				for (WordModel w : sen) {
					if (!index.containsKey(w.getmLemma()))
						index.put(w.getmLemma(), new WordIndex(w.getmLemma(), index));
					index.get(w.getmLemma()).add(w);
				}
				
				//On construit le set des bigrams dans la phrase
				ArrayList <Integer> sentence_bigrams = new ArrayList <Integer> ();
				curr_bg_arr = sen.getNGrams(2, index);
				Tools.toSet(curr_bg_arr);
				for (NGram ng : curr_bg_arr)
				{
					if ( ( ind = this.bigrams.indexOf(ng)) != -1 )
						sentence_bigrams.add(ind);
					else
					{
						this.bigrams.add(ng);
						sentence_bigrams.add(this.bigrams.size() - 1 );
					}
					
					if (sen.getPosition() == 1)
						firstSentencesConcepts.put(ng, 1);
				}
				//On ajoute le set des bigrams de la phrase Ã  bigrams_in_sentence
				bigrams_in_sentence.add(sentence_bigrams);
				//On ajoute le set des bigrams de la phrase au document 
				curr_doc_bg_arr.addAll(sentence_bigrams);
				Tools.toSet(curr_doc_bg_arr);
			}
			//On ajoute 1 au poids des bigrams de curr_doc_bg_arr dans bigram_weights
			for (Integer i : curr_doc_bg_arr)
			{
				if ( this.bigram_weights.size() - 1 >= i )
				{
					this.bigram_weights.set(i, this.bigram_weights.get(i) + 1.);
				}
				else
				{
					if ( this.bigram_weights.size() == i)
					{
						this.bigram_weights.add(1.);
					}
					else System.out.println("Pas bon");
				}
			}
			//On ajoute this.fsc_factor au poids des bigrammes de la premiÃ¨re phrase du document
			for (NGram ng : firstSentencesConcepts.keySet())
			{
				Integer i = this.bigrams.indexOf(ng);
				this.bigram_weights.set( i, this.bigram_weights.get(i) + (this.fsc_factor) );
			}
			
			
		}
	}

	@Override
	public ArrayList<Double> getBiGramWeights() {
		return bigram_weights;
	}

	@Override
	public ArrayList<ArrayList<Integer>> getBiGramsInSentence() {
		return bigrams_in_sentence;
	}

	@Override
	public ArrayList<NGram> getBiGrams() {
		return bigrams;
	}
}
