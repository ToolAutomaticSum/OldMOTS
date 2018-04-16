package main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.action;

import main.java.liasd.asadera.exception.StateException;
import main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.State;

public class Finish extends Action {

	@Override
	public void doAction(State state) throws StateException {
		state.setFinish(true);
		state.addAction(this);
	}

	@Override
	public void undoAction(State state) throws StateException {
		state.setFinish(false);
		state.removeAction(this);
	}
}
