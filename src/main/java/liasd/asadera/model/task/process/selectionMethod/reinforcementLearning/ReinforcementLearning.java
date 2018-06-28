package main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning;

import java.util.ArrayList;
import java.util.List;

import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.action.Action;
import main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.featurer.Featurer;
import main.java.liasd.asadera.model.task.process.selectionMethod.scorer.Scorer;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.tools.vector.ToolsVector;

public class ReinforcementLearning extends AbstractSelectionMethod {

	public static enum ASRL_Parameter {
		Temp("Temp"), Gamma("Gamma"), Alpha("Alpha");

		private String name;

		private ASRL_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private State currentState;
	private double[] theta;
	double temp;
	double gamma;
	double alpha;
	private Scorer s;
	private Featurer f;

	public ReinforcementLearning(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public ReinforcementLearning makeCopy() throws Exception {
		ReinforcementLearning p = new ReinforcementLearning(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		temp = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "Temperature"));
		gamma = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "Gamma"));
		alpha = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "Alpha"));

		String scorer = getCurrentProcess().getModel().getProcessOption(id, "ScoreMethod");
		s = Scorer.instanciateScorer(this, scorer);
		getSubMethod().add(s);
		String featurer = getCurrentProcess().getModel().getProcessOption(id, "FeatureMethod");
		f = Featurer.instanciateFeaturer(this, featurer);
		getSubMethod().add(f);
	}

	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		int size = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "Size"));
		double penalty = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "Penalty"));

		List<SentenceModel> listSentence = new ArrayList<SentenceModel>();
		for (Corpus corpus : listCorpus)
			for (TextModel text : corpus)
				for (SentenceModel sen : text)
					listSentence.add(sen);

		s.init();
		f.init(size);

		theta = f.instanciateVector();

		currentState = new State(size, penalty, s, f);
		currentState.setGamma(gamma);

		theta = learn(listSentence);

		currentState.init(listSentence);
		while (!currentState.isFinish()) {
			Action a = currentState.selectBestAction(theta);
			a.doAction(currentState);
		}
		return currentState.getSummary();
	}

	private double[] learn(List<SentenceModel> listSentence) throws Exception {
		int nbIter = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "NbIteration"));

		for (int i = 1; i <= nbIter; i++) {
			System.out.print(".");
			if (i % 50 == 0)
				System.out.println(" " + i);

			currentState.init(listSentence);
			double[] elig = f.instanciateVector();
			alpha = alpha * 101 / (100 + Math.pow(i, 1.1));
			temp = temp * Math.pow(0.987, i - 1);
			while (!currentState.isFinish()) {
				Action a = currentState.selectActionWithCurrentPolicy(theta, temp);
				double[] lastPhi = currentState.getFeatures();
				a.doAction(currentState);
				double delta = currentState.reward() + gamma * ToolsVector.scalar(theta, currentState.getFeatures())
						- ToolsVector.scalar(theta, lastPhi);
				elig = ToolsVector.scalarVector(gamma, ToolsVector.somme(elig, lastPhi));
				theta = ToolsVector.somme(theta, ToolsVector.scalarVector(alpha * delta, elig));
			}
		}

		return theta;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
	}
}
