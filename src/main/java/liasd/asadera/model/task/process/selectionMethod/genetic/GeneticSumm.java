package main.java.liasd.asadera.model.task.process.selectionMethod.genetic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers.GeneticIndividualScorer;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.InvertedIndex;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class GeneticSumm extends AbstractSelectionMethod implements IndexBasedIn<WordIndex> {

	private static Logger logger = LoggerFactory.getLogger(GeneticSumm.class);

	private Index<WordIndex> index;

	private GeneticIndividual bestSummary;
	private double bestSummaryScore;

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
	private Random rand;

	public GeneticSumm(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(WordIndex.class, Index.class, IndexBasedIn.class));
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		GeneticSumm p = new GeneticSumm(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		this.parentsNumber = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "ParentsNumber"));
		this.hybridationNumber = Integer
				.parseInt(getCurrentProcess().getModel().getProcessOption(id, "HybridationNumber"));
		this.mutationNumber = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "MutationNumber"));
		this.randomNumber = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "RandomNumber"));
		this.maxSummLength = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "MaxSummLength"));
		this.populationNb = parentsNumber + hybridationNumber + mutationNumber + randomNumber;
		this.generationsNb = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "GenerationsNb"));
		this.maxMutatedGenes = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "MaxMutatedGenes"));
	}

	public void init(List<Corpus> listCorpus) throws Exception {
		rand = new Random(System.currentTimeMillis());

		for (Corpus corpus : listCorpus) {
			for (TextModel doc : corpus) {
				for (SentenceModel sen : doc)
					if (sen.getNbMot() >= 10)
						this.ss.add(sen);
			}
		}
		this.scorer = instanciateGeneticScorer(
				getCurrentProcess().getModel().getProcessOption(id, "GeneticScorerMethod"));
		this.scorer.init();
		this.bestSummary = new GeneticIndividual(rand, this.ss, this.maxSummLength);
		this.bestSummaryScore = 0;

		this.population = new ArrayList<GeneticIndividual>();
		this.populationScore = new ArrayList<Double>();
	}

	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		init(listCorpus);
		this.initializePopulation();

		for (int curr_generation = 0; curr_generation < this.generationsNb; curr_generation++) {
			this.scoreCurrentPopulation();
			System.out.println("Best score at " + curr_generation + "th generation : " + this.bestSummaryScore);
			this.createNewGeneration();
		}

		DecimalFormat df = new DecimalFormat("0.000");
		try {
			FileOutputStream fw = new FileOutputStream("./fitness_scores.txt", true);
			OutputStreamWriter osr = new OutputStreamWriter(fw, "UTF-8");
			osr.write(df.format(this.generationsNb) + "\t" + this.bestSummaryScore + "\n");
			osr.flush();
			osr.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finish();
		return this.bestSummary.getGenes();
	}

	public void finish() {
		ss.clear();
	}

	private void initializePopulation() {
		for (int curr_indiv = 0; curr_indiv < this.populationNb; curr_indiv++) {
			GeneticIndividual gi = new GeneticIndividual(rand, this.ss, this.maxSummLength);
			this.population.add(gi);
		}

	}

	/**
	 * score the current population and sets the max to its new value if necessary
	 */
	private void scoreCurrentPopulation() {
		double maxScore = 0;
		int maxIndex = 0;
		int cpt = 0;
		double currScore = 0;
		this.populationScore.clear();
		scorer.computeScore(population);
		for (GeneticIndividual gi : this.population) {
			currScore = gi.getScore(); // this.scorer);
			if (currScore == 0) {
				for (SentenceModel p : gi.getGenes())
					logger.debug(p.toString());
			}
			if (currScore > maxScore) {
				maxScore = currScore;
				maxIndex = cpt;
			}
			this.populationScore.add(currScore);
			cpt++;
		}
		if (maxScore > this.bestSummaryScore) {
			this.bestSummaryScore = maxScore;
			this.bestSummary = new GeneticIndividual(rand, this.population.get(maxIndex));
		}
	}

	private void createNewGeneration() {
		ArrayList<GeneticIndividual> parents = this.selectParents();

		ArrayList<GeneticIndividual> hybrids = this.hybridation(parents);
		
		this.population.clear();
		this.population.addAll(parents);
		this.population.addAll(hybrids);

		ArrayList<GeneticIndividual> mutants = this.createMutants(this.population);
		
		this.population.addAll(mutants);
		this.eliminateDoublons();
		this.population.addAll(this.createRandom());

	}

	private ArrayList<GeneticIndividual> selectParents() {
		ArrayList<GeneticIndividual> parents = new ArrayList<GeneticIndividual>();
		ArrayList<ArrayList<Integer>> tournaments = new ArrayList<ArrayList<Integer>>();
		int random1, selec1;

		ArrayList<Integer> selectables = new ArrayList<Integer>();
		for (int i = 0; i < this.population.size(); i++)
			selectables.add(i);

		for (int i = 0; i < this.parentsNumber; i++) {
			tournaments.add(new ArrayList<Integer>());

			for (int j = 0; j < this.populationNb / this.parentsNumber; j++) {
				random1 = (int) (rand.nextDouble() * selectables.size());
				selec1 = selectables.get(random1);
				tournaments.get(i).add(selec1);
				selectables.remove(random1);
			}
		}
		for (int i = 0; i < this.parentsNumber; i++) {
			double maxScore = 0;
			int indexMaxScore = 0;
			int ind;
			for (int j = 0; j < tournaments.get(i).size(); j++) {
				ind = tournaments.get(i).get(j);
				if (this.populationScore.get(ind) > maxScore) {
					maxScore = this.populationScore.get(ind);
					indexMaxScore = ind;
				}
			}
			parents.add(this.population.get(indexMaxScore));

		}

		return parents;
	}

	private ArrayList<GeneticIndividual> hybridation(ArrayList<GeneticIndividual> parents) {
		int randomP1, randomP2;
		ArrayList<GeneticIndividual> hybrids = new ArrayList<GeneticIndividual>();

		for (int i = 0; i < this.hybridationNumber; i++) {
			randomP1 = (int) (rand.nextDouble() * parents.size());
			randomP2 = (int) (rand.nextDouble() * parents.size());
			hybrids.add(new GeneticIndividual(rand, this.ss, this.maxSummLength, parents.get(randomP1),
					parents.get(randomP2)));
		}

		return hybrids;
	}

	private ArrayList<GeneticIndividual> createMutants(ArrayList<GeneticIndividual> pop) {
		int randomI;
		int mutationRandomNb;
		ArrayList<GeneticIndividual> mutants = new ArrayList<GeneticIndividual>();
		for (int i = 0; i < this.mutationNumber; i++) {
			mutationRandomNb = (int) (rand.nextDouble() * (this.maxMutatedGenes)) + 1;
			randomI = (int) (rand.nextDouble() * pop.size());
			mutants.add(new GeneticIndividual(rand, this.ss, this.maxSummLength, pop.get(randomI), mutationRandomNb));
		}

		return mutants;
	}

	private ArrayList<GeneticIndividual> createRandom() {
		ArrayList<GeneticIndividual> randoms = new ArrayList<GeneticIndividual>();

		for (int i = this.population.size(); i <= this.populationNb; i++) {
			randoms.add(new GeneticIndividual(rand, this.ss, this.maxSummLength));
		}
		return randoms;
	}

	private void eliminateDoublons() {
		for (int i = 0; i < this.population.size() - 1; i++)
			for (int j = i + 1; j < this.population.size(); j++) {
				if (this.population.get(i).equals(this.population.get(j)) && i != j) {
					this.population.remove(j);
					j--;
				}
			}
	}

	public GeneticIndividualScorer instanciateGeneticScorer(String geneticScorer)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> cl;
		cl = Class.forName("main.java.liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers." + geneticScorer);
		// Class types = new Class{Integer.class};
		@SuppressWarnings("rawtypes")
		Constructor ct = cl.getConstructor(HashMap.class, ArrayList.class, Corpus.class, InvertedIndex.class,
				Index.class, Double.class, Double.class, Double.class, Integer.class, Double.class);

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
			firstSentenceConceptsFactor = Double
					.parseDouble(getModel().getProcessOption(id, "FirstSentenceConceptsFactor"));
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

		Object o = ct.newInstance(null, ss, getCurrentProcess().getCorpusToSummarize(),
				new InvertedIndex<WordIndex>(index), index, divWeight, delta, firstSentenceConceptsFactor, window,
				fsc_factor);
		return (GeneticIndividualScorer) o;
	}

	@Override
	public void setIndex(Index<WordIndex> index) {
		this.index = index;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
	}
}
