package main.java.liasd.asadera.model.task.process.selectionMethod;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.selectionMethod.scorer.Scorer;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.Summary;

public class Knapsack extends AbstractSelectionMethod {

	private static Logger logger = LoggerFactory.getLogger(Knapsack.class);

	private int K;
	private Scorer scorer;

	public void setScorer(Scorer scorer) {
		this.scorer = scorer;
	}

	public Knapsack(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		Knapsack p = new Knapsack(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		K = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "Size"));

		String sco = getCurrentProcess().getModel().getProcessOption(id, "ScoreMethod");
		scorer = Scorer.instanciateScorer(this, sco);
		getSubMethod().add(scorer);
	}

	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		List<SentenceModel> listSen = new ArrayList<SentenceModel>();
		for (Corpus corpus : listCorpus)
			listSen.addAll(corpus.getAllSentence());
		int n = listSen.size();

		scorer.init();

		Summary[][] s = new Summary[n + 1][K + 1];
		for (int i = 0; i <= n; i++)
			s[i][0] = new Summary();
		for (int k = 0; k <= K; k++)
			s[0][k] = new Summary();

		for (int i = 1; i <= n; i++) {
			for (int k = 1; k <= K; k++) {
				int li = listSen.get(i - 1).getNbMot();
				if (li > k)
					s[i][k] = s[i - 1][k];
				else {
					Summary sp = new Summary(s[i - 1][k]);
					Summary spp = new Summary(s[i - 1][k - li]);
					spp.add(listSen.get(i - 1));
					if (scorer.getScore(sp) > scorer.getScore(spp))
						s[i][k] = sp;
					else
						s[i][k] = spp;
				}
			}
		}

		double bestScore = 0;
		int bestK = 0;
		for (int k = 1; k <= K; k++) {
			double score = scorer.getScore(s[n][k]);
			if (score > bestScore) {
				bestScore = score;
				bestK = k;
			}
		}
		logger.trace(String.valueOf(bestScore));
		return s[n][bestK];
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.isOutCompatible(scorer);
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
	}
}
