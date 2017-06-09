package model.task.process.indexBuilder.LDA;

import java.util.List;

import model.task.process.caracteristicBuilder.queryBuilder.QueryBasedIn;
import model.task.process.caracteristicBuilder.queryBuilder.QueryBasedOut;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.SentenceQuery;

public class QueryLDA extends LDA implements QueryBasedOut<double[]> {

	private SentenceQuery<double[]> query;
	
	public QueryLDA(int id) throws SupportADNException {
		super(id);
		
		listParameterOut.add(new ParametrizedType(null, double[].class, QueryBasedOut.class));
		
		query = new SentenceQuery<double[]>();
	}
	
	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		super.processIndex(listCorpus);
			
		query.setQuery(theta);
	}
	
	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return super.isOutCompatible(compatibleMethod) || compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(null, double[].class, QueryBasedIn.class));
	}

	/**
	 * donne le/les paramètre(s) d'output en input à la class comp méthode
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParametrizedMethod compatibleMethod) {
		if (super.isOutCompatible(compatibleMethod))
			super.setCompatibility(compatibleMethod);
		if (compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(null, double[].class, QueryBasedIn.class)))
			((QueryBasedIn<double[]>)compatibleMethod).setQuery(query);
	}

	@Override
	public SentenceQuery<double[]> getQuery() {
		return query;
	}

}
