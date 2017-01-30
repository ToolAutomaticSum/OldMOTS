package model.task.process.ILP;

import java.util.ArrayList;
import java.util.HashMap;

import model.task.process.AbstractProcess;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.WordIndex;
import tools.Tools;
import tools.wordFilters.WordFilter;
import tools.wordFilters.WordStopListFilter;

public class BiGram_ILP extends AbstractProcess implements BiGramListBasedOut{

	private ArrayList<Double> bigram_weights;
	private ArrayList<ArrayList<Integer>> bigrams_in_sentence;
	private ArrayList<NGram> bigrams; 
	private WordFilter filter;
	private double fsc_factor = 1;
	
	public BiGram_ILP(int id) {
		super(id);
	}

	@Override
	public void init() throws Exception {
		super.init();
		
		filter = new WordStopListFilter();
	}
	
	@Override
	public void process() throws Exception {
		this.buildWeightsAndSentences();
		//this.buildModel();
		//this.writeModelToTmpFile();
		//this.runGLPK();	
		
		super.process();
	}
	
	@Override
	public void finish() throws Exception {
		super.finish();
	}
	
	private void buildWeightsAndSentences()
	{
		int wordId = 0;
		this.bigram_weights = new ArrayList<Double> ();
		this.bigrams_in_sentence = new ArrayList<ArrayList<Integer> > ();
		this.bigrams = new ArrayList<NGram> ();
		int ind;
		ArrayList <NGram> curr_bg_arr;
		ArrayList <Integer> curr_doc_bg_arr;
		//HashMap <NGram, Integer> firstSentencesConcepts = new HashMap <NGram, Integer> ();
	
		System.out.println("Construction du modèle");
		
		for (TextModel text : getModel().getDocumentModels())
		{
			HashMap <NGram, Integer> firstSentencesConcepts = new HashMap <NGram, Integer> ();
			curr_doc_bg_arr = new ArrayList <Integer> ();
			for ( ParagraphModel p : text )
			{
				for ( SentenceModel s : p )
				{
					for (WordModel w : s) {
						if (!dictionnary.containsKey(w.getmLemma()))
							dictionnary.put(w.getmLemma(), new WordIndex(w.getmLemma(), dictionnary, wordId));
						dictionnary.get(w.getmLemma()).add(w);
						wordId++;
					}
					//On construit le set des bigrams dans la phrase
					ArrayList <Integer> sentence_bigrams = new ArrayList <Integer> ();
					curr_bg_arr = generateBiGrams(dictionnary, s, filter);
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
						
						if (p.get(0).equals(s))
							firstSentencesConcepts.put(ng, 1);
					}
					//On ajoute le set des bigrams de la phrase à bigrams_in_sentence
					bigrams_in_sentence.add(sentence_bigrams);
					//ss.add(s);
					//On ajoute le set des bigrams de la phrase au document 
					curr_doc_bg_arr.addAll(sentence_bigrams);
					Tools.toSet(curr_doc_bg_arr);
				}
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
			//On ajoute this.fsc_factor au poids des bigrammes de la première phrase du document
			for (NGram ng : firstSentencesConcepts.keySet())
			{
				Integer i = this.bigrams.indexOf(ng);
				this.bigram_weights.set( i, this.bigram_weights.get(i) + (this.fsc_factor) );
			}
		}		
	}
	
	public static ArrayList<NGram> generateBiGrams(Dictionnary dico, SentenceModel sentence, WordFilter filter) {
		ArrayList<NGram> ngrams_list = new ArrayList<NGram> ();
		WordModel w1, w2;
		for (int i = 0; i < sentence.size() - 1; i++)
		{
			w1 = sentence.get(i);
			w2 = sentence.get(i+1);
			
			if (filter.passFilter(w1) || filter.passFilter(w2) )
			{
				NGram ng = new NGram();
				ng.addGram(dico.get(w1.getmLemma()).getId());
				ng.addGram(dico.get(w2.getmLemma()).getId());
				//if (! ngrams_list.contains(ng));
				ngrams_list.add(ng);
				//System.out.println("Pas Filtrée !");
			}
			else
			{
				//System.out.println("Filtrée ! ");
			}
		}
		
		return ngrams_list;
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
