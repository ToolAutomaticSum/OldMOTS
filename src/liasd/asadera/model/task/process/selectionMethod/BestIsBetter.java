package liasd.asadera.model.task.process.selectionMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.model.task.process.scoringMethod.ScoreBasedIn;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.tools.PairSentenceScore;

public class BestIsBetter extends AbstractSelectionMethod implements ScoreBasedIn {

	private ArrayList<PairSentenceScore> sentenceScore;
	private boolean nbCharSizeOrNbSentenceSize;
	private int maxSummLength;
	private int nbSentenceInSummary;
	
	public BestIsBetter(int id) throws SupportADNException {
		super(id);
		
		listParameterIn = new ArrayList<ParametrizedType>();
		listParameterIn.add(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedIn.class));
		listParameterOut = new ArrayList<ParametrizedType>();
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		BestIsBetter p = new BestIsBetter(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		nbCharSizeOrNbSentenceSize = Boolean.parseBoolean(getCurrentProcess().getModel().getProcessOption(id, "CharLimitBoolean"));
		int size = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "Size"));
	
		if (nbCharSizeOrNbSentenceSize)
			this.maxSummLength = size;
		else
			this.nbSentenceInSummary = size;
	}

	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		List<SentenceModel> summary = new ArrayList<SentenceModel>();
		
		if (nbCharSizeOrNbSentenceSize) {
			int size = 0;
			Iterator<PairSentenceScore> senIt = sentenceScore.iterator();
			while (senIt.hasNext() && size < maxSummLength) {
				SentenceModel sen = senIt.next().getPhrase();
				size+=sen.getNbMot();
				if (size < maxSummLength)
					summary.add(sen);
				else
					size -= sen.getNbMot();
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
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
	}

	@Override
	public void setScore(ArrayList<PairSentenceScore> score) {
		this.sentenceScore = score;
	}

}
