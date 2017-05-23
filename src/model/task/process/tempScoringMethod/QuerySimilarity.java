package model.task.process.tempScoringMethod;

import java.util.Map;

import model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import model.task.process.caracteristicBuilder.queryBuilder.QueryBasedIn;
import model.task.process.processCompatibility.ParametrizedMethod;
import optimize.SupportADNException;
import textModeling.SentenceModel;

public class QuerySimilarity extends AbstractScoringMethod implements QueryBasedIn<double[]>, SentenceCaracteristicBasedIn<double[]>{

	public QuerySimilarity(int id) throws SupportADNException {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractScoringMethod makeCopy() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initADN() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void computeScores() throws Exception {
		// TODO Auto-generated method stub
		
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

	@Override
	public void setCaracterisics(Map<SentenceModel, double[]> sentenceCaracteristic) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setQuery(double[] query) {
		// TODO Auto-generated method stub
		
	}

}
