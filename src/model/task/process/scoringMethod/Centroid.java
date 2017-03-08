package model.task.process.scoringMethod;

import exception.LacksOfFeatures;
import optimize.SupportADNException;

public class Centroid extends AbstractScoringMethod {

	protected double[] centroid;
	protected int nbMaxWordInCluster;
	
	public Centroid(int id) throws SupportADNException {
		super(id);
	}
	
	private void init() throws NumberFormatException, LacksOfFeatures {
		nbMaxWordInCluster = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "NbMaxWordInCluster"));
	}

	@Override
	public void computeScores() throws Exception {
		calculateCentroid();
	}
	
	private void calculateCentroid() throws NumberFormatException, LacksOfFeatures {
		init();
		
		centroid = new double[dictionnary.size()];
		
	}

}
