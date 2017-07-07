package model.task.process.caracteristicBuilder.matrix;

import java.util.List;

import model.task.process.caracteristicBuilder.QueryBasedIn;
import model.task.process.caracteristicBuilder.QueryBasedOut;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.Query;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;

public class DocQueryMatrix extends ConcatMatrixSentence implements QueryBasedOut {

	private Query query;
	
	public DocQueryMatrix(int id) throws SupportADNException {
		super(id);
		
		query = new Query();
		
		listParameterOut.add(new ParametrizedType(null, double[].class, QueryBasedOut.class));
	
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
		for (Corpus corpus: listCorpus)
			for (TextModel text : corpus)
				nbMot += text.getNbWord();
		
		double[][] matrixDoc = new double[nbMot][dimension];
		int i = 0;
		for (Corpus corpus : listCorpus)
			for (TextModel text : corpus)
				for (SentenceModel s : text)
					for (WordModel w : s) {
						matrixDoc[i] = index.get(w.getmLemma()).getWordVector();
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
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return super.isOutCompatible(compatibleMethod) || super.isOutCompatible(compatibleMethod) && compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(null, double[][].class, QueryBasedIn.class));
	}

	/**
	 * donne le/les paramètre(s) d'output en input à la class comp méthode
	 */
	@Override
	public void setCompatibility(ParametrizedMethod compatibleMethod) {
		super.setCompatibility(compatibleMethod);
		if (compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(null, double[][].class, QueryBasedIn.class)))
			((QueryBasedIn)compatibleMethod).setQuery(query);
	}
}
