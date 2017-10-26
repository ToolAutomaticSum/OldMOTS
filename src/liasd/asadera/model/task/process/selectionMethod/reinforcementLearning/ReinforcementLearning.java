package liasd.asadera.model.task.process.selectionMethod.reinforcementLearning;

import java.util.List;

import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;

public class ReinforcementLearning extends AbstractSelectionMethod {

	public ReinforcementLearning(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initADN() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		// TODO Auto-generated method stub
		
	}

}
