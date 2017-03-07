package model.task.process.summarizeMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import optimize.SupportADNException;
import textModeling.SentenceModel;
import tools.PairSentenceScore;

public class BestIsBetter extends AbstractSummarizeMethod implements ScoreBasedIn {

	private TreeSet<PairSentenceScore> sentenceScore;
	private boolean nbCharSizeOrNbSentenceSize;
	private int maxSummLength;
	private int nbSentenceInSummary;
	
	public BestIsBetter(int id) throws SupportADNException {
		super(id);
	}

	public void init() throws Exception {
		nbCharSizeOrNbSentenceSize = Boolean.parseBoolean(getCurrentProcess().getModel().getProcessOption(id, "CharLimitBoolean"));
		int size = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "Size"));
	
		if (nbCharSizeOrNbSentenceSize)
			this.maxSummLength = size;
		else
			this.nbSentenceInSummary = size;
	}
	
	@Override
	public List<SentenceModel> calculateSummary() throws Exception {
		init();
		
		List<SentenceModel> summary = new ArrayList<SentenceModel>();
		
		if (nbCharSizeOrNbSentenceSize) {
			int size = 0;
			Iterator<PairSentenceScore> senIt = sentenceScore.iterator();
			while (senIt.hasNext() && size < maxSummLength) {
				SentenceModel sen = senIt.next().getPhrase();
				size+=sen.size();
				if (size < maxSummLength)
					summary.add(sen);
				else
					size -= sen.size();
			}
		}
		else {
			int i = 0;
			Iterator<PairSentenceScore> senIt = sentenceScore.iterator();
			while (senIt.hasNext() && i < nbSentenceInSummary) {
				summary.add(senIt.next().getPhrase());
				i++;
			}
		}
		
		return summary;
	}

	@Override
	public void setScore(TreeSet<PairSentenceScore> score) {
		this.sentenceScore = score;
	}
}
