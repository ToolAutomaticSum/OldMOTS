package main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.action.Action;
import main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.action.Finish;
import main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.action.Insert;
import main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.featurer.Featurer;
import main.java.liasd.asadera.model.task.process.selectionMethod.scorer.Scorer;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.Summary;
import main.java.liasd.asadera.tools.Pair;
import main.java.liasd.asadera.tools.vector.ToolsVector;

public class State {

	private double gamma;

	private double penalty;
	private Scorer scorer;
	private Featurer featurer;
	private List<Action> available;
	private List<Action> temp;
	private List<Action> listAction;
	private Summary summary;
	private int maxLength;
	private int currentLength;
	private boolean bFinish;
	private Random rand;

	public State(int maxLength, double penalty, Scorer scorer, Featurer featurer) {
		this.scorer = scorer;
		this.featurer = featurer;
		this.penalty = penalty;
		this.maxLength = maxLength;
		rand = new Random();
	}

	public void init(List<SentenceModel> listSentence) {
		available = new ArrayList<Action>();
		temp = new ArrayList<Action>();
		for (SentenceModel sen : listSentence)
			available.add(new Insert(sen));
		available.add(new Finish());
		listAction = new ArrayList<Action>();
		summary = new Summary();
		currentLength = 0;
		bFinish = false;
	}

	public Action selectActionWithCurrentPolicy(double[] theta, double temperature) throws Exception {
		double div = 0;
		List<Action> list = new ArrayList<Action>(available);
		for (Action avail : list)
			div += Math.exp(actionValueFunction(avail, theta) / temperature);

		double[] listPolicy = new double[available.size()];
		double policy = 0;
		for (int i = 0; i < available.size(); i++) {
			policy += policy(div, available.get(i), theta, temperature);
			listPolicy[i] = policy;
		}
		listPolicy[available.size() - 1] = 1.0;
		int i = 0;
		double select = rand.nextDouble();
		while (listPolicy[i] <= select)
			i++;
		return available.get(i);
	}

	public Action selectBestAction(double[] theta) throws Exception {
		List<Pair<Action, Double>> listAction = new ArrayList<Pair<Action, Double>>();
		List<Action> list = new ArrayList<Action>(available);
		for (Action a : list)
			listAction.add(new Pair<Action, Double>(a, actionValueFunction(a, theta)));
		Collections.sort(listAction);
		return listAction.get(0).getKey();
	}

	protected double reward() throws Exception {
		if (lastAction().getClass().equals(Finish.class)) {
			if (isTooLong())
				return penalty;
			else
				return scorer.getScore(summary);
		} else
			return 0.0;
	}

	protected double stateValueFunction(double[] theta) throws Exception {
		return ToolsVector.scalar(theta, getFeatures());
	}

	protected double actionValueFunction(Action action, double[] theta) throws Exception {
		action.doAction(this);
		double score = reward() + gamma * stateValueFunction(theta);
		action.undoAction(this);
		return score;
	}

	protected double policy(double div, Action action, double[] theta, double temperature) throws Exception {
		return Math.exp(actionValueFunction(action, theta) / temperature) / div;
	}

	public void availableAction() {
		temp.clear();
		temp.addAll(available);
		if (bFinish || isTooLong())
			available.removeIf(a -> a.getClass().equals(Insert.class));
		else
			available.remove(lastAction());
	}

	public double[] getFeatures() throws Exception {
		double[] features;
		if (isTooLong()) {
			features = featurer.instanciateVector();
			features[features.length - 1] = 1;
		} 
		else {
			features = featurer.getFeatures(summary);
			features[features.length - 1] = 0;
		}
		return features;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	protected Action lastAction() {
		if (listAction.size() != 0)
			return listAction.get(listAction.size() - 1);
		else
			return null;
	}

	public boolean isTooLong() {
		return maxLength < currentLength;
	}

	public boolean isFinish() {
		return bFinish;
	}

	public void setFinish(boolean b) {
		bFinish = b;
	}

	public void addAction(Action action) {
		listAction.add(action);
		availableAction();
	}

	public void removeAction(Action action) {
		listAction.remove(action);
		available.clear();
		available.addAll(temp);
	}

	public void insertSentence(SentenceModel sentence) {
		summary.add(sentence);
		currentLength += sentence.getNbMot();
	}

	public void removeSentence(SentenceModel sentence) {
		summary.remove(sentence);
		currentLength -= sentence.getNbMot();
	}

	public List<SentenceModel> getSummary() {
		return summary;
	}
}
