package main.java.liasd.asadera.model.task.process.selectionMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.scoringMethod.ScoreBasedIn;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.Summary;

public class BestIsBetter extends AbstractSelectionMethod implements ScoreBasedIn {

	private static Logger logger = LoggerFactory.getLogger(BestIsBetter.class);

	private Map<SentenceModel, Double> sentencesScore;
	private boolean nbCharSizeOrNbSentenceSize;
	private int maxSummLength;
	private int nbSentenceInSummary;

	public BestIsBetter(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(Double.class, Map.class, ScoreBasedIn.class));
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		BestIsBetter p = new BestIsBetter(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		nbCharSizeOrNbSentenceSize = Boolean
				.parseBoolean(getCurrentProcess().getModel().getProcessOption(id, "CharLimitBoolean"));
		int size = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "Size"));

		if (nbCharSizeOrNbSentenceSize)
			this.maxSummLength = size;
		else
			this.nbSentenceInSummary = size;
	}

	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		Summary summary = new Summary();

		List<SentenceModel> list = new ArrayList<SentenceModel>(sentencesScore.keySet());
		Collections.sort(list, (a, b) -> Double.compare(sentencesScore.get(b), sentencesScore.get(a)));
		if (nbCharSizeOrNbSentenceSize) {
			int size = 0;
			Iterator<SentenceModel> senIt = list.iterator();
			while (senIt.hasNext() && size < maxSummLength) {
				SentenceModel sen = senIt.next();
				size += sen.getNbMot();
				if (size < maxSummLength)
					summary.add(sen);
				else
					size -= sen.getNbMot();
			}
		} else {
			int i = 0;
			Iterator<SentenceModel> senIt = list.iterator();
			while (senIt.hasNext() && i < nbSentenceInSummary) {
				summary.add(senIt.next());
				i++;
			}
		}
		double s = 0.0;
		for (SentenceModel sen : summary)
			s += sentencesScore.get(sen);
		logger.info(String.valueOf(s));
		return summary;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
	}

	@Override
	public void setScore(Map<SentenceModel, Double> sentencesScore) {
		this.sentencesScore = sentencesScore;
	}
}
