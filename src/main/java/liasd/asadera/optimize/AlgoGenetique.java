package main.java.liasd.asadera.optimize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import main.java.liasd.asadera.optimize.parameter.ADN;
import main.java.liasd.asadera.optimize.parameter.Parameter;
import main.java.liasd.asadera.tools.reader_writer.Writer;

public class AlgoGenetique extends Optimize {

	public static enum AGParameter {
		CHANCE_MUTATION("CHANCE_MUTATION"), POURCENTAGE_MEILLEURS("POURCENTAGE_MEILLEURS"), POURCENTAGE_RESTE(
				"POURCENTAGE_RESTE"), NB_POPULATION("NB_POPULATION"), NB_GENERATION_MAX(
						"NB_GENERATION_MAX"), NB_ELITE_A_GARDER("NB_ELITE_A_GARDER"), VAL_MIN("VAL_MIN");

		private String name;

		private AGParameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private Writer writer;
	private Optimize optimize;
	private HashMap<String, Class<?>> supportPopulationADN;
	private List<ADN> population = new ArrayList<ADN>();
	private List<ADN> solution = new ArrayList<ADN>();

	public AlgoGenetique(int id, double chanceMutation, double pourcentageMeilleur, double pourcentageReste,
			int taillePopulation, int nbGenerationMax, double valFitnessMin, Optimize opti) throws Exception {
		super(id);

		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("CHANCE_MUTATION", Double.class);
		supportADN.put("POURCENTAGE_MEILLEURS", Double.class);
		supportADN.put("POURCENTAGE_RESTE", Double.class);
		supportADN.put("NB_POPULATION", Integer.class);
		supportADN.put("NB_GENERATION_MAX", Integer.class);
		supportADN.put("NB_ELITE_A_GARDER", Integer.class);
		supportADN.put("VAL_MIN", Double.class);

		adn = new ADN(supportADN);
		adn.putParameter(new Parameter<Double>("CHANCE_MUTATION", chanceMutation));
		adn.putParameter(new Parameter<Double>("POURCENTAGE_MEILLEURS", pourcentageMeilleur));
		adn.putParameter(new Parameter<Double>("POURCENTAGE_RESTE", pourcentageReste));
		adn.putParameter(new Parameter<Integer>("NB_POPULATION", taillePopulation));
		adn.putParameter(new Parameter<Integer>("NB_GENERATION_MAX", nbGenerationMax));
		adn.putParameter(new Parameter<Integer>("NB_ELITE_A_GARDER", (int) (taillePopulation * pourcentageMeilleur)));
		adn.putParameter(new Parameter<Double>("CHANCE_MUTATION", chanceMutation));
		adn.putParameter(new Parameter<Double>("VAL_MIN", valFitnessMin));

		this.optimize = opti;
		supportPopulationADN = opti.getSupportADN();

		writer = new Writer("resultGenetique" + optimize.getClass().getSimpleName() + ".txt");
	}

	@Override
	public void initADN() throws Exception {
	}

	/**
	 * @throws Exception
	 */
	public void init() throws Exception {
		try {
			optimize.initOptimize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		writer.open(false);
		writer.write(adn.toString() + "\n" + adn.toValueString() + "\n");
		Random rand = new Random(System.currentTimeMillis());
		for (int i = 0; i < adn.getParameterValue(Integer.class, AGParameter.NB_POPULATION.getName()); i++) {
			ADN temp = optimize.generateAleaADN(rand);
			population.add(temp);
		}
	}

	public List<ADN> selection(Random random) {
		List<ADN> parents = new ArrayList<ADN>();
		parents.addAll(
				population.subList(0, adn.getParameterValue(Integer.class, AGParameter.NB_ELITE_A_GARDER.getName())));

		for (int i = adn.getParameterValue(Integer.class, AGParameter.NB_ELITE_A_GARDER.getName()); i < adn
				.getParameterValue(Integer.class, AGParameter.NB_POPULATION.getName()); i++) {
			if (random.nextDouble() < adn.getParameterValue(Double.class, AGParameter.POURCENTAGE_RESTE.getName()))
				parents.add(population.get(i));
		}

		return parents;
	}

	public void mutation(Random random, List<ADN> parents) {
		for (ADN individu : parents) {
			if (random.nextDouble() < adn.getParameterValue(Double.class, AGParameter.CHANCE_MUTATION.getName())) {
				String parameterToModify = (String) supportPopulationADN.keySet().toArray()[random
						.nextInt(supportPopulationADN.size())];
				individu.putParameter(Optimize.generateGenericAleaParameter(random,
						individu.getParameter(individu.getParameterClass(parameterToModify), parameterToModify)));
			}
		}
	}

	public void croisement(Random random, List<ADN> parents) {
		int nbEnfants = adn.getParameterValue(Integer.class, AGParameter.NB_POPULATION.getName()) - parents.size();
		List<ADN> children = new ArrayList<ADN>();
		while (children.size() < nbEnfants) {
			ADN pere = parents.get(random.nextInt(parents.size()));
			ADN maman = parents.get(random.nextInt(parents.size()));
			if (pere != maman)
				children.add(ADN.croisementADN(random, pere, maman));
		}
		parents.addAll(children);
	}

	public void evaluationPopulation() throws Exception {
		for (ADN individu : population) {
			optimize.setADN(individu);
			optimize.optimize();
			individu.setScore(optimize.getScore());
			writer.write(individu.toValueString() + individu.getScore() + "\n");
			System.out.println(individu.toString());
			System.out.println(individu.toValueString());
		}
	}

	public boolean evoluer() throws Exception {
		Random random = new Random(System.currentTimeMillis());
		evaluationPopulation();
		Collections.sort(population);
		Double fitness_moyen = 0.0;

		List<ADN> nextPopulation = new ArrayList<ADN>();
		int i = 0;
		for (ADN individu : population) {
			fitness_moyen += individu.getScore();
			nextPopulation.add(individu);
			if (individu.getScore() > adn.getParameterValue(Double.class, AGParameter.VAL_MIN.getName()))
				solution.add(individu);
			System.out.println(individu.toString());
			if (i < 20)
				System.out.println(i + "\t" + individu.toValueString() + individu.getScore());
			i++;
		}
		fitness_moyen /= adn.getParameterValue(Integer.class, AGParameter.NB_POPULATION.getName());
		System.out.println("Fitness moyen : " + fitness_moyen);

		if (solution.size() > 0)
			return true;

		List<ADN> parents = selection(random);
		mutation(random, parents);
		croisement(random, parents);
		population = parents;
		return false;
	}

	@Override
	public void optimize() throws Exception {
		int i = 0;
		boolean solutionFind = false;
		while ((!solutionFind) && i < adn.getParameterValue(Integer.class, AGParameter.NB_GENERATION_MAX.getName())) {
			long time = System.currentTimeMillis();
			writer.write(optimize.getADN().toString() + "Score\nIteration " + i + "\n");
			System.out.println("Iteration " + i);
			solutionFind = evoluer();
			writer.write(Long.toString(System.currentTimeMillis() - time) + "\n");
			i++;
		}

		for (int j = 0; j < solution.size(); j++)
			System.out.println(solution.get(j).toValueString());
		writer.close();
	}

	@Override
	public String toString() {
		String str = "";

		for (int j = 0; j < solution.size(); j++)
			str += solution.get(j).toString();
		return str;
	}

	@Override
	public double getScore() {
		return 0;
	}

	@Override
	public void initOptimize() throws Exception {
	}
}
