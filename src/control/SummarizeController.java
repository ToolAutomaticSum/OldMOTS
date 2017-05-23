package control;

import java.util.ArrayList;
import java.util.List;

import model.AbstractModel;
import model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import model.task.process.indexBuilder.AbstractIndexBuilder;
import model.task.process.tempProcess.SummarizeProcess;
import model.task.process.tempScoringMethod.AbstractScoringMethod;
import model.task.process.tempSelectionMethod.AbstractSelectionMethod;
import view.AbstractView;

@SuppressWarnings("rawtypes")
public class SummarizeController extends Controller {

	private SummarizeProcess currentProcess;

	public SummarizeController(AbstractModel model, AbstractView view) {
		super(model, view);
	}

	@Override
	public void notifyProcessChanged(String processName) {
    	getModel().getProcessIDs().put(processName, processID);
    	Object o = dynamicConstructor("process.tempProcess.SummarizeProcess"/* + processName*/);
    	currentProcess = (SummarizeProcess) o;
		getModel().getProcess().add(currentProcess);
    }

	@Override
	public void notifyIndexBuilderChanged(String processName, String indexBuilder) {
    	Object o = dynamicConstructor("process.indexBuilder." + indexBuilder);
    	currentProcess.setIndexBuilder((AbstractIndexBuilder) o);
    }

	@Override
	public void notifyCaracteristicBuilderChanged(String processName, String caracteristicBuilder) {
		List<AbstractCaracteristicBuilder> listCaracBuilder;
		if (currentProcess.getCaracteristicBuilders() == null)
			listCaracBuilder = new ArrayList<AbstractCaracteristicBuilder>();
		else
			listCaracBuilder = currentProcess.getCaracteristicBuilders();
		Object o = dynamicConstructor("process.caracteristicBuilder." + caracteristicBuilder);
    	listCaracBuilder.add((AbstractCaracteristicBuilder) o);
    	currentProcess.setCaracteristicBuilders(listCaracBuilder);
    }

	@Override
	public void notifyScoringMethodChanged(String processName, String scoringMethod) {
		List<AbstractScoringMethod> listScoringMethod;
		if (currentProcess.getScoringMethods() == null)
			listScoringMethod = new ArrayList<AbstractScoringMethod>();
		else
			listScoringMethod = currentProcess.getScoringMethods();
		Object o = dynamicConstructor("process.tempScoringMethod." + scoringMethod);
    	listScoringMethod.add((AbstractScoringMethod) o);
    	currentProcess.setScoringMethods(listScoringMethod);
    }

	@Override
	public void notifySelectionMethodChanged(String processName, String selectionMethod) {
    	Object o = dynamicConstructor("process.tempSelectionMethod." + selectionMethod);
    	currentProcess.setSelectionMethod((AbstractSelectionMethod) o);
    }

}
