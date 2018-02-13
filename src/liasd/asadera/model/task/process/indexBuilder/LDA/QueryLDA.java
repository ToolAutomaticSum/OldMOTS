package liasd.asadera.model.task.process.indexBuilder.LDA;

import java.util.List;

import liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedIn;
import liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedOut;
import liasd.asadera.model.task.process.indexBuilder.AbstractIndexBuilder;
import liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.Query;
import liasd.asadera.textModeling.wordIndex.WordVector;

public class QueryLDA extends LDA implements QueryBasedOut {

	private Query query;

	public QueryLDA(int id) throws SupportADNException {
		super(id);

		listParameterOut.add(new ParameterizedType(null, double[].class, QueryBasedOut.class));

		query = new Query();
	}

	@Override
	public AbstractIndexBuilder<WordVector> makeCopy() throws Exception {
		QueryLDA p = new QueryLDA(id);
		initCopy(p);
		return p;
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		super.processIndex(listCorpus);

		query.setQuery(theta);
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return super.isOutCompatible(compatibleMethod) || compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(null, double[].class, QueryBasedIn.class));
	}
 
	@Override
	public void setCompatibility(ParameterizedMethod compatibleMethod) {
		if (super.isOutCompatible(compatibleMethod))
			super.setCompatibility(compatibleMethod);
		if (compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(null, double[].class, QueryBasedIn.class)))
			((QueryBasedIn) compatibleMethod).setQuery(query);
	}

	@Override
	public Query getQuery() {
		return query;
	}
}
