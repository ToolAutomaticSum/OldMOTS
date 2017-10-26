package liasd.asadera.model.task.process.caracteristicBuilder.graphBased;

import java.util.List;

import liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;

public class GraphOfWordsBuilder extends AbstractCaracteristicBuilder {

	public GraphOfWordsBuilder(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		return null;
	}

	@Override
	public void initADN() throws Exception {
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) throws Exception {
	}

	@Override
	public void finish() {
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
	}

}
