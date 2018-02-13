package liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.action;

import liasd.asadera.exception.StateException;
import liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.State;

public abstract class Action {

	public abstract void doAction(State state) throws StateException;

	public abstract void undoAction(State state) throws StateException;

	public String toString() {
		return this.getClass().getSimpleName();
	}
}
