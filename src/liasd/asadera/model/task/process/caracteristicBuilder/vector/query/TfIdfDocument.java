package liasd.asadera.model.task.process.caracteristicBuilder.vector.query;

import java.util.List;

import liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedIn;
import liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedOut;
import liasd.asadera.model.task.process.caracteristicBuilder.vector.TfIdfVectorSentence;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.Query;
import liasd.asadera.tools.vector.ToolsVector;

public class TfIdfDocument extends TfIdfVectorSentence implements QueryBasedOut {
	
	private Query query;
	
	public TfIdfDocument(int id) throws SupportADNException {
		super(id);
		
		query = new Query();
		
		listParameterOut.add(new ParametrizedType(null, double[].class, QueryBasedOut.class));
	}

	@Override
	public TfIdfDocument makeCopy() throws Exception {
		TfIdfDocument p = new TfIdfDocument(id);
		initCopy(p);
		return p;
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) throws Exception {
		super.processCaracteristics(listCorpus);
		
		int dimension = ((double[])sentenceCaracteristic.values().iterator().next()).length;
		double[] docVector = new double[dimension];
		
		for (Object vector : sentenceCaracteristic.values())
			docVector = ToolsVector.somme(docVector, (double[]) vector);
		for (int i=0; i<dimension; i++)
			docVector[i] /= sentenceCaracteristic.size();
		query.setQuery(docVector);
	}

	@Override
	public Query getQuery() {
		return query;
	}
	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return super.isOutCompatible(compatibleMethod) || compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(null, double[].class, QueryBasedIn.class));
	}

	/**
	 * donne le/les paramètre(s) d'output en input à la class comp méthode
	 */
	@Override
	public void setCompatibility(ParametrizedMethod compatibleMethod) {
		super.setCompatibility(compatibleMethod);
		if (compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(null, double[].class, QueryBasedIn.class)))
			((QueryBasedIn)compatibleMethod).setQuery(query);
	}
}
