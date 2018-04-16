package main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.action;

import main.java.liasd.asadera.exception.StateException;
import main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.State;
import main.java.liasd.asadera.textModeling.SentenceModel;

public class Insert extends Action {

	private SentenceModel sentence;

	public Insert(SentenceModel sentence) {
		this.sentence = sentence;
	}

	public SentenceModel getSentence() {
		return sentence;
	}

	@Override
	public void doAction(State state) throws StateException {
		if (!state.isFinish()) {
			state.addAction(this);
			state.insertSentence(sentence);
		} else
			throw new StateException("Can't insert when in finish state.");
	}

	@Override
	public void undoAction(State state) throws StateException {
		state.removeAction(this);
		state.removeSentence(sentence);
	}
}
