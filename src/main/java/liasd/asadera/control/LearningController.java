package main.java.liasd.asadera.control;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.model.AbstractModel;
import main.java.liasd.asadera.model.task.process.LearningProcess;
import main.java.liasd.asadera.model.task.process.indexBuilder.LearningModelBuilder;
import main.java.liasd.asadera.view.AbstractView;

public class LearningController extends AbstractController {

	private static Logger logger = LoggerFactory.getLogger(LearningController.class);

	public LearningController(AbstractModel model, AbstractView view) {
		super(model, view);
	}

	private LearningProcess currentProcess;

	@Override
	public void notifyProcessChanged(String processName) throws ClassNotFoundException {
		Object o = dynamicConstructor("process.LearningProcess");
		currentProcess = (LearningProcess) o;
		getModel().getProcess().add(currentProcess);
	}

	@Override
	public void notifyIndexBuilderChanged(String processName, String indexBuilder) throws ClassNotFoundException {
		List<LearningModelBuilder> listModelBuilders;
		if (currentProcess.getModelBuilders() == null) {
			listModelBuilders = new ArrayList<LearningModelBuilder>();
		} else {
			listModelBuilders = currentProcess.getModelBuilders();
		}
		Object o = dynamicConstructor("process.indexBuilder." + indexBuilder);
		listModelBuilders.add((LearningModelBuilder) o);
		currentProcess.setModelBuilders(listModelBuilders);
	}

	@Override
	public void notifyCaracteristicBuilderChanged(String processName, String caracteristicBuilder) {
		logger.warn("No CaracteristicBuilder required for learning !");
	}

	@Override
	public void notifyScoringMethodChanged(String processName, String scoringMethod) {
		logger.warn("No ScoringMethod required for learning !");
	}

	@Override
	public void notifySelectionMethodChanged(String processName, String summarizeMethod) {
		logger.warn("No SelectionMethod required for learning !");
	}

}
