package liasd.asadera.model.task.process.caracteristicBuilder.matrix;

import java.util.List;

import liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedIn;
import liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedOut;
import liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.Query;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.textModeling.wordIndex.WordIndex;
import liasd.asadera.textModeling.wordIndex.WordVector;

public class DocQueryMatrix extends ConcatMatrixSentence implements QueryBasedOut {

	private Query query;

	public DocQueryMatrix(int id) throws SupportADNException {
		super(id);

		query = new Query();

		listParameterOut.add(new ParameterizedType(null, double[][].class, QueryBasedOut.class));
	}

	@Override
	public DocQueryMatrix makeCopy() throws Exception {
		DocQueryMatrix p = new DocQueryMatrix(id);
		initCopy(p);
		return p;
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) {
		super.processCaracteristics(listCorpus);

		int nbMot = 0;
		for (Corpus corpus : listCorpus)
			for (TextModel text : corpus)
				for (SentenceModel sen : text)
					nbMot += sen.size();

		double[][] matrixDoc = new double[nbMot][dimension];
		int i = 0;
		for (Corpus corpus : listCorpus)
			for (TextModel text : corpus)
				for (SentenceModel s : text)
					for (WordIndex w : s) {
						matrixDoc[i] = ((WordVector) w).getWordVector();
						i++;
					}

		query.setQuery(matrixDoc);
	}

	@Override
	public void finish() {
		super.finish();
		query.clear();
	}

	@Override
	public Query getQuery() {
		return query;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		boolean a = super.isOutCompatible(compatibleMethod);
		boolean b = compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(null, double[][].class, QueryBasedIn.class));
		return a || (a && b);
	}

	@Override
	public void setCompatibility(ParameterizedMethod compatibleMethod) {
		super.setCompatibility(compatibleMethod);
		if (compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(null, double[][].class, QueryBasedIn.class)))
			((QueryBasedIn) compatibleMethod).setQuery(query);
	}
}
