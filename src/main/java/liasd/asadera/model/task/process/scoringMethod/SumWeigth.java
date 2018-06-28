package main.java.liasd.asadera.model.task.process.scoringMethod;

import java.util.List;

import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class SumWeigth extends AbstractScoringMethod {

	public SumWeigth(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public AbstractScoringMethod makeCopy() throws Exception {
		SumWeigth p = new SumWeigth(id);
		initCopy(p);
		return p;
	}

	@Override
	public void computeScores(List<Corpus> listCorpus) throws Exception {
		for (Corpus corpus : listCorpus) {
			for (TextModel textModel : corpus) {
				for (SentenceModel sentenceModel : textModel) {
					double sumWeigth = 0.0;
					for (WordIndex w : sentenceModel)
						sumWeigth += w.getWeight();
					sentencesScore.put(sentenceModel, sumWeigth);
				}
			}
		}
	}
}
