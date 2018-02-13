package liasd.asadera.model.task.process.scoringMethod;

import java.util.List;

import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class SumWeigth extends AbstractScoringMethod {

	public SumWeigth(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public AbstractScoringMethod makeCopy() throws Exception {
		return null;
	}

	@Override
	public void initADN() throws Exception {
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
