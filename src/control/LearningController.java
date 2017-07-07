package control;

import java.util.ArrayList;
import java.util.List;

import model.AbstractModel;
import model.task.process.LearningProcess;
import model.task.process.indexBuilder.LearningModelBuilder;
import view.AbstractView;

public class LearningController extends AbstractController {

	public LearningController(AbstractModel model, AbstractView view) {
		super(model, view);
	}

	private LearningProcess currentProcess;
	
	@Override
	public void notifyProcessChanged(String processName) {
		getModel().getProcessIDs().put("LearningProcess", processID);
    	Object o = dynamicConstructor("process.LearningProcess"/* + processName*/);
    	currentProcess = (LearningProcess) o;
		getModel().getProcess().add(currentProcess);
	}

	@Override
	public void notifyIndexBuilderChanged(String processName, String indexBuilder) {
		List<LearningModelBuilder> listModelBuilders;
		if (currentProcess.getModelBuilders() == null) {
			listModelBuilders = new ArrayList<LearningModelBuilder>();
		}
		else {
			listModelBuilders = currentProcess.getModelBuilders();
		}
		Object o = dynamicConstructor("process.indexBuilder." + indexBuilder);
		listModelBuilders.add((LearningModelBuilder) o);
    	currentProcess.setModelBuilders(listModelBuilders);
	}

	@Override
	public void notifyCaracteristicBuilderChanged(String processName, String caracteristicBuilder) {
		System.out.println("No CaracteristicBuilder required for learning !");
	}

	@Override
	public void notifyScoringMethodChanged(String processName, String scoringMethod) {
		System.out.println("No ScoringMethod required for learning !");
	}

	@Override
	public void notifySelectionMethodChanged(String processName, String summarizeMethod) {
		System.out.println("No SelectionMethod required for learning !");
	}

}
