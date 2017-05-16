package model.task.process.ILP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import exception.LacksOfFeatures;
import model.task.process.AbstractProcess;
import optimize.SupportADNException;
import optimize.parameter.Parameter;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.NGram;
import textModeling.wordIndex.WordIndex;

public class BiGram_ILP extends AbstractProcess implements BiGramListBasedOut{

	public static enum BiGramILP_Parameter {
		fscFactor("fscFactor"),
		minSenLength("minSenLength");

		private String name;

		private BiGramILP_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private HashMap <NGram, Double> bigram_weights;
	private ArrayList < TreeSet <NGram> > bigrams_in_sentence;

	private TreeMap <NGram, Integer> bigrams_ids;
	
	private ArrayList<NGram> bigrams;
	private double fscFactor = 1;
	private int minSenLength = 1;
	
	public BiGram_ILP(int id) throws SupportADNException, NumberFormatException, LacksOfFeatures {
		super(id);
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put(BiGramILP_Parameter.fscFactor.getName(), Double.class);
		supportADN.put(BiGramILP_Parameter.minSenLength.getName(), Integer.class);
	}
	
	@Override
	public AbstractProcess makeCopy() throws Exception {
		BiGram_ILP p = new BiGram_ILP(id);
		initCopy(p);
		return p;
	}
	
	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		adn.putParameter(new Parameter<Double>(BiGramILP_Parameter.fscFactor.getName(), Double.parseDouble(getModel().getProcessOption(id, BiGramILP_Parameter.fscFactor.getName()))));
		adn.putParameter(new Parameter<Integer>(BiGramILP_Parameter.minSenLength.getName(), Integer.parseInt(getModel().getProcessOption(id, BiGramILP_Parameter.minSenLength.getName()))));
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		fscFactor = adn.getParameterValue(Double.class, BiGramILP_Parameter.fscFactor.getName());
		minSenLength = adn.getParameterValue(Integer.class, BiGramILP_Parameter.minSenLength.getName());
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
		this.bigram_weights = new HashMap<NGram, Double>();
		this.bigrams_in_sentence = new ArrayList<TreeSet<NGram> > ();
		this.bigrams = new ArrayList<NGram> ();
		this.bigrams_ids = new TreeMap <NGram, Integer> ();
		
		TreeSet <NGram> curr_bg_set;
		TreeSet <NGram> curr_doc_bg_set;

		System.out.println("Construction du modèle");

		TreeSet <NGram> first_sentence_concepts = new TreeSet <NGram> ();
		
		for (TextModel text : corpusToSummarize)
		{
			curr_doc_bg_set = new TreeSet <NGram> ();
			for ( SentenceModel sen : text )
			{
				if (sen.getNbMot() >= minSenLength) {
					for (WordModel w : sen) {
						if (!index.containsKey(w.getmLemma()))
							index.put(w.getmLemma(), new WordIndex(w.getmLemma(), index));
						index.get(w.getmLemma()).add(w);
					}
					
					//On construit le set des bigrams dans la phrases
					curr_bg_set = new TreeSet<NGram> (sen.getNGrams(2, index));
					if ( sen.getPosition() == 1 )
					{
						first_sentence_concepts = new TreeSet <NGram> (curr_bg_set);
						for (NGram ng : first_sentence_concepts)
						{
							if ( this.bigram_weights.containsKey(ng) )
								this.bigram_weights.put(ng, this.bigram_weights.get(ng) + this.fscFactor);
							else
								this.bigram_weights.put(ng, this.fscFactor);
						}
					}
					else
					{
						curr_doc_bg_set.addAll(curr_bg_set);
					}
					this.bigrams_in_sentence.add(curr_bg_set);
				}
			}
			
			for (NGram ng : curr_doc_bg_set)
			{
				if ( this.bigram_weights.containsKey(ng) )
					this.bigram_weights.put(ng, this.bigram_weights.get(ng) + 1.);
				else
					this.bigram_weights.put(ng, 1.);
			}
		}
		
		//On donne un id à chaque ngram
		int i = 0;
		for (NGram ng : this.bigram_weights.keySet())
		{
			this.bigrams_ids.put(ng, i++);
		}
	}

	@Override
	public HashMap<NGram, Double> getBiGramWeights() {
		return bigram_weights;
	}

	@Override
	public ArrayList<TreeSet<NGram>> getBiGramsInSentence() {
		return bigrams_in_sentence;
	}

	@Override
	public ArrayList<NGram> getBiGrams() {
		return bigrams;
	}

	@Override
	public TreeMap<NGram, Integer> getBiGramsIds() {
		return bigrams_ids;
	}
}
