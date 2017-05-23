package model;

import model.task.postProcess.EvaluationROUGE;

public class ComparativeModel extends AbstractModel {

	public ComparativeModel() {
		setbRougeEvaluation(false);
		setEvalRouge(null);
	}
	
	@Override
	public void run() {
	}
	
	@Override
	public boolean isbRougeEvaluation() {
		throw new NullPointerException("Can't use ROUGE toolkit for comparative summarization.");
	}
	
	@Override
	public void setbRougeEvaluation(boolean bRougeEvaluation) {
		throw new NullPointerException("Can't use ROUGE toolkit for comparative summarization.");
	}
	
	@Override
	public EvaluationROUGE getEvalRouge() {
		throw new NullPointerException("Can't use ROUGE toolkit for comparative summarization.");
	}

	@Override
	public void setEvalRouge(EvaluationROUGE evalRouge) {
		throw new NullPointerException("Can't use ROUGE toolkit for comparative summarization.");
	}
}
