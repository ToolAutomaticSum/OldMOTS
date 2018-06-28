package main.java.liasd.asadera.model.task.process.caracteristicBuilder.vector.query;

import java.util.List;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedOut;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.vector.TfIdfVectorSentence;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.Query;
import main.java.liasd.asadera.tools.vector.ToolsVector;

public class TfIdfDocument extends TfIdfVectorSentence implements QueryBasedOut {

	private Query query;

	public TfIdfDocument(int id) throws SupportADNException {
		super(id);

		listParameterOut.add(new ParameterizedType(null, double[].class, QueryBasedOut.class));
	}

	@Override
	public TfIdfDocument makeCopy() throws Exception {
		TfIdfDocument p = new TfIdfDocument(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		query = new Query();
	}
	
	@Override
	public void processCaracteristics(List<Corpus> listCorpus) throws Exception {
		super.processCaracteristics(listCorpus);

		int dimension = ((double[]) sentenceCaracteristic.values().iterator().next()).length;
		double[] docVector = new double[dimension];

		for (Object vector : sentenceCaracteristic.values())
			docVector = ToolsVector.somme(docVector, (double[]) vector);
		for (int i = 0; i < dimension; i++)
			docVector[i] /= sentenceCaracteristic.size();
		query.setQuery(docVector);
	}

	@Override
	public Query getQuery() {
		return query;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return super.isOutCompatible(compatibleMethod) || compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(null, double[].class, QueryBasedIn.class));
	}
 
	@Override
	public void setCompatibility(ParameterizedMethod compatibleMethod) {
		super.setCompatibility(compatibleMethod);
		if (compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(null, double[].class, QueryBasedIn.class)))
			((QueryBasedIn) compatibleMethod).setQuery(query);
	}
}
