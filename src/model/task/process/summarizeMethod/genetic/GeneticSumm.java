package model.task.process.summarizeMethod.genetic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import exception.LacksOfFeatures;
import model.task.process.summarizeMethod.AbstractSummarizeMethod;
import model.task.process.summarizeMethod.genetic.geneticScorers.GeneticIndividualScorer;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.InvertedIndex;

public class GeneticSumm extends AbstractSummarizeMethod {

	static {
		supportADN = new HashMap<String, Class<?>>();
	}

	public static enum Genetic_Parameter {
		test("test");

		private String name;

		private Genetic_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private GeneticIndividual bestSummary;
	private double bestSummaryScore;
	
/*	private Index index;
	private Integer idClust;*/
	private int populationNb;
	private int generationsNb;
	private int hybridationNumber;
	private int parentsNumber;
	private int mutationNumber;
	private int randomNumber;
	private int maxMutatedGenes;
	private ArrayList<GeneticIndividual> population;
	private ArrayList<Double> populationScore;
	private GeneticIndividualScorer scorer;
	private ArrayList<SentenceModel> ss = new ArrayList<SentenceModel>();
	private int maxSummLength;
	private String idClust;
		
	public GeneticSumm (int id) throws SupportADNException {
		super(id);
	}
	
	public void init() throws Exception {
		this.parentsNumber = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "ParentsNumber"));
		this.hybridationNumber = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "HybridationNumber"));
		this.mutationNumber = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "MutationNumber"));
		this.randomNumber = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "RandomNumber"));
		this.maxSummLength = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "MaxSummLenght"));
		this.bestSummary = new GeneticIndividual(this.ss, this.maxSummLength);
		this.bestSummaryScore = 0;
		this.populationNb = parentsNumber + hybridationNumber + mutationNumber + randomNumber;
		this.generationsNb = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "GenerationsNb"));
		this.maxMutatedGenes = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "MaxMutatedGenes"));
		this.population = new ArrayList<GeneticIndividual>();
		this.populationScore = new ArrayList<Double>();
		
		this.scorer = instanciateGeneticScorer(getCurrentProcess().getModel().getProcessOption(id, "GeneticScorerMethod"));
	
	}
	
	@Override
	public ArrayList<SentenceModel> calculateSummary() throws Exception
	{
		init();
		this.initializePopulation();
		
		for (int curr_generation = 0; curr_generation < this.generationsNb; curr_generation++)
		{
			this.scoreCurrentPopulation();
			System.out.println("Meilleur score à la "+curr_generation+"ème génération : "+this.bestSummaryScore);
			/*System.out.println("**************************************************");
			
			for (Double d : this.populationScore)
			{
				System.out.println(d);
			}*/
			
			System.out.println("**************************************************");
			
			this.createNewGeneration();
		}
		
		DecimalFormat df = new DecimalFormat ("0.000");
		try{
		FileOutputStream fw = new FileOutputStream("./fitness_scores.txt", true);
		OutputStreamWriter osr = new OutputStreamWriter (fw, "UTF-8");
		//BufferedWriter output = new BufferedWriter(fw);
		double mean = 0;
		for (Double d : this.populationScore)
		{
			mean += d;
		}
		mean /= (double)this.populationScore.size();
		osr.write(df.format(this.generationsNb)+"\t"+this.bestSummaryScore+"\n");
		osr.flush();
		osr.close();
		}catch(IOException ioe){ioe.printStackTrace();}
		
		
		
		return this.bestSummary.getGenes();
	}
	
	private void initializePopulation ()
	{
		for (int curr_indiv = 0; curr_indiv < this.populationNb ; curr_indiv++)
		{
			for (TextModel doc : getModel().getDocumentModels())
				this.ss.addAll(doc.getSentence());
			GeneticIndividual gi = new GeneticIndividual(this.ss, this.maxSummLength);
			this.population.add(gi);
		}
		
	}
	
	/**
	 * score the current population and sets the max to its new value if necessary
	 */
	private void scoreCurrentPopulation ()
	{
		double maxScore = 0;
		int maxIndex = 0;
		int cpt = 0;
		double currScore = 0;
		this.populationScore.clear();
		for (GeneticIndividual gi : this.population)
		{
			currScore = gi.getScore(this.scorer);
			//System.out.println("Score indiv "+cpt+" : "+currScore);
			if (currScore == 0)
			{
				for (SentenceModel p : gi.getGenes())
					System.out.println(p);
			}
			if (currScore > maxScore)
			{
				maxScore = currScore;
				maxIndex = cpt;
			}
			this.populationScore.add(currScore);
			cpt++;
		}
		if (maxScore > this.bestSummaryScore)
		{
			this.bestSummaryScore = maxScore;
			this.bestSummary = new GeneticIndividual (this.population.get(maxIndex));
		}
	}
	
	
	private void createNewGeneration ()
	{
		ArrayList<GeneticIndividual> parents = this.selectParents();
		
		ArrayList<GeneticIndividual> hybrids = this.hybridation(parents);
		
		this.population.clear();
		this.population.addAll(parents);
		this.population.addAll(hybrids);
		
		ArrayList<GeneticIndividual> mutants = this.createMutants (this.population);
		//this.population.clear();
		this.population.addAll(mutants);
		this.eliminateDoublons();
		this.population.addAll(this.createRandom());
		
	}
	
	private ArrayList<GeneticIndividual> selectParents()
	{
		ArrayList<GeneticIndividual> parents = new ArrayList<GeneticIndividual>();
		ArrayList<ArrayList<Integer>> tournaments = new ArrayList<ArrayList<Integer>>();
		int random1, selec1;
		
		ArrayList<Integer> selectables = new ArrayList<Integer>();
		for (int i = 0; i < this.population.size(); i++)
			selectables.add(i);
		
		for (int i = 0; i < this.parentsNumber; i++)
		{
			tournaments.add(new ArrayList<Integer>());
			
			for (int j = 0; j < this.populationNb/this.parentsNumber; j++)
			{
				random1 = (int) (Math.random() * selectables.size());
				selec1 = selectables.get(random1);
				tournaments.get(i).add(selec1);
				selectables.remove(random1);
			}
			//System.out.println("Tournaments sizes for "+this.population.size()+": ");
			//for(int j = 0; j < tournaments.size(); j++)
				//System.out.print(""+tournaments.get(j).size()+" |Â ");
			//System.out.println("");
		}
		//System.out.println("idClust : "+this.idClust);
		for (int i = 0; i < this.parentsNumber; i++)
		{
			double maxScore = 0;
			int indexMaxScore = 0;
			int ind;
			for (int j = 0; j < tournaments.get(i).size(); j++)
			{
				ind = tournaments.get(i).get(j);
				if (this.populationScore.get(ind) > maxScore)
				{
					maxScore = this.populationScore.get(ind);
					indexMaxScore = ind;
				}
			}
			parents.add(this.population.get(indexMaxScore));
			
		}
		
		return parents;
	}
	
	private ArrayList<GeneticIndividual> hybridation (ArrayList<GeneticIndividual> parents)
	{
		int randomP1, randomP2;
		ArrayList<GeneticIndividual> hybrids = new ArrayList<GeneticIndividual> ();
		
		for (int i = 0; i < this.hybridationNumber; i++)
		{
			randomP1 = (int) (Math.random() * parents.size());
			randomP2 = (int) (Math.random() * parents.size());
			hybrids.add(new GeneticIndividual (this.ss, this.maxSummLength, parents.get(randomP1), parents.get(randomP2)));
		}
		
		return hybrids;
	}
	
	private ArrayList<GeneticIndividual> createMutants (ArrayList<GeneticIndividual> pop)
	{
		int randomI;
		int mutationRandomNb;
		ArrayList<GeneticIndividual> mutants = new ArrayList<GeneticIndividual> ();
		for (int i = 0; i < this.mutationNumber; i++)
		{
			ss.clear();
			for (TextModel doc : getModel().getDocumentModels())
				this.ss.addAll(doc.getSentence());
			mutationRandomNb = (int) (Math.random() * (this.maxMutatedGenes)) + 1;
			randomI = (int) (Math.random() * pop.size());
			mutants.add(new GeneticIndividual (this.ss, this.maxSummLength, pop.get(randomI), mutationRandomNb));
		}
	
		return mutants;
	}
	
	private ArrayList<GeneticIndividual> createRandom()
	{
		ArrayList<GeneticIndividual> randoms = new ArrayList<GeneticIndividual> ();
		
		for (int i = this.population.size(); i <= this.populationNb; i ++)
		{
			for (TextModel doc : getModel().getDocumentModels())
				this.ss.addAll(doc.getSentence());
			randoms.add(new GeneticIndividual (this.ss, this.maxSummLength));
		}
		return randoms;
	}
	
	private void eliminateDoublons()
	{
		for (int i = 0; i < this.population.size() - 1; i++)
			for (int j = i + 1; j < this.population.size(); j++)
			{
				if(this.population.get(i).equals(this.population.get(j) ) && i != j)
				{
					this.population.remove(j);
					j--;
				}
			}
	}
	
	public GeneticIndividualScorer instanciateGeneticScorer(String geneticScorer) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> cl;
		cl = Class.forName("model.task.process.summarizeMethod.genetic.geneticScorers." + geneticScorer);
	    //Class types = new Class{Integer.class};
	    @SuppressWarnings("rawtypes")
		Constructor ct = cl.getConstructor(HashMap.class, Corpus.class, HashMap.class, InvertedIndex.class, Dictionnary.class, Double.class, Double.class, Double.class, Integer.class, Double.class);
	    
	    Double divWeight = null;
	    Double delta = null;
	    Double firstSentenceConceptsFactor = null;
		Double window = null;
	    Double fsc_factor = null;
	    try {
			divWeight = Double.parseDouble(getModel().getProcessOption(id, "DivWeight"));
	    } catch (LacksOfFeatures e) {
	    	
	    }
	    try {
	    	delta = Double.parseDouble(getModel().getProcessOption(id, "Delta"));
	    } catch (LacksOfFeatures e) {
	    	
	    }
	    try {
	    	firstSentenceConceptsFactor = Double.parseDouble(getModel().getProcessOption(id, "FirstSentenceConceptsFactor"));
		} catch (LacksOfFeatures e) {
			
		}
	    try {
	    	window = Double.parseDouble(getModel().getProcessOption(id, "Window"));
		} catch (LacksOfFeatures e) {
			
		}
	    try {
	    	fsc_factor = Double.parseDouble(getModel().getProcessOption(id, "Fsc_factor"));
	    } catch (LacksOfFeatures e) {
	    	
	    }
	    
	    Object o = ct.newInstance(null, getModel().getDocumentModels(), getCurrentProcess().getHashMapWord(), new InvertedIndex(getCurrentProcess().getDictionnary()), getCurrentProcess().getDictionnary(), divWeight, delta, firstSentenceConceptsFactor, window, fsc_factor);
	    return (GeneticIndividualScorer) o;
	}
}
