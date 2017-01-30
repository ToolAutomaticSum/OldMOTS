package model.task.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.task.AbstractTask;
import model.task.VectorCaracteristicBasedIn;
import model.task.VectorCaracteristicBasedOut;
import model.task.postProcess.AbstractPostProcess;
import model.task.process.ILP.BiGramListBasedOut;
import model.task.process.LDA.LdaBasedOut;
import model.task.scoringMethod.AbstractScoringMethod;
import model.task.scoringMethod.ScoreBasedOut;
import model.task.scoringMethod.ILP.BiGramListBasedIn;
import model.task.scoringMethod.ILP.FileModelBasedOut;
import model.task.scoringMethod.LDA.LdaBasedIn;
import model.task.scoringMethod.LDA.TopicLdaBasedOut;
import model.task.summarizeMethod.AbstractSummarizeMethod;
import model.task.summarizeMethod.FileModelBasedIn;
import model.task.summarizeMethod.ScoreBasedIn;
import model.task.summarizeMethod.TopicLdaBasedIn;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.wordIndex.Dictionnary;
import tools.Tools;

public abstract class AbstractProcess extends AbstractTask {

	protected Dictionnary dictionnary = new Dictionnary();
	protected Map<Integer, String> hashMapWord = new HashMap<Integer, String>();
	
	protected AbstractSummarizeMethod sentenceSelection;
	protected AbstractScoringMethod scoringMethod;
	protected List<AbstractPostProcess> postProcess = new ArrayList<AbstractPostProcess>();
	protected List<SentenceModel> allSentenceList = new ArrayList<SentenceModel>();
	protected List<List<SentenceModel>> summary = new ArrayList<List<SentenceModel>>();
	private int sizeSummary = 8;
	
	public AbstractProcess(int id) {
		super(id);	
	}
	
	@Override
	public void init() throws Exception {
		/*Iterator<AbstractPreProcess> preProIt = preProcess.iterator();
		while (preProIt.hasNext()) {
			AbstractPreProcess p = preProIt.next();
			p.setModel(getModel());
			p.setCurrentProcess(this);
			p.init();
		}
		
		preProIt = preProcess.iterator();
		while (preProIt.hasNext()) {
			AbstractPreProcess p = preProIt.next();
			p.process();
		}
		
		preProIt = preProcess.iterator();
		while (preProIt.hasNext()) {
			AbstractPreProcess p = preProIt.next();
			p.finish();
		}*/
	}
	
	/**
	 * Appel à super.process() une fois les process effectués.
	 */
	@Override
	public void process() throws Exception {
		for (TextModel text : getModel().getDocumentModels())
		{
			for ( ParagraphModel p : text )
			{
				for ( SentenceModel s : p )
				{
					allSentenceList.add(s);
				}
			}
		}
		
		if (!(scoringMethod == null)) {
			scoringMethod.setCurrentProcess(this);
			initCompatibilityProcess();
			scoringMethod.init(this, dictionnary, hashMapWord);
			scoringMethod.computeScores();
		}
		if (!(sentenceSelection == null)) {
			sentenceSelection.setCurrentProcess(this);
			initCompatibilityScoring();
			summary.add(sentenceSelection.calculateSummary());
		}
	}
	
	protected void initCompatibilityProcess() {
		for (Class<?> process : Tools.getInheritance(this.getClass())) {
			for (Class<?> scoring : Tools.getInheritance(scoringMethod.getClass())) {
				if (process == LdaBasedOut.class && scoring == LdaBasedIn.class) {
					((LdaBasedIn)scoringMethod).setK(((LdaBasedOut)this).getK());
					((LdaBasedIn)scoringMethod).setTheta(((LdaBasedOut)this).getTheta());
					((LdaBasedIn)scoringMethod).setNbSentence(((LdaBasedOut)this).getNbSentence());
				}
				else if (process == VectorCaracteristicBasedOut.class && scoring == VectorCaracteristicBasedIn.class) {
					((VectorCaracteristicBasedIn)scoringMethod).setVectorCaracterisic(((VectorCaracteristicBasedOut)this).getVectorCaracterisic());
				}
				else if (process == BiGramListBasedOut.class && scoring == BiGramListBasedIn.class) {
					((BiGramListBasedIn)scoringMethod).setBiGramsInSentence(((BiGramListBasedOut)this).getBiGramsInSentence());
					((BiGramListBasedIn)scoringMethod).setBiGrams(((BiGramListBasedOut)this).getBiGrams());
					((BiGramListBasedIn)scoringMethod).setBiGramWeights(((BiGramListBasedOut)this).getBiGramWeights());
				}
			}
		}
	}
	
	protected void initCompatibilityScoring() {
		for (Class<?> scoring : Tools.getInheritance(scoringMethod.getClass())) {
			for (Class<?> selection : Tools.getInheritance(sentenceSelection.getClass())) {
				if (scoring == ScoreBasedOut.class && selection == ScoreBasedIn.class) {
					((ScoreBasedIn)sentenceSelection).setScore(((ScoreBasedOut)scoringMethod).getScore());
				}
				else if (scoring == TopicLdaBasedOut.class && selection == TopicLdaBasedIn.class) {
					((TopicLdaBasedIn)sentenceSelection).setListTopicLda(((TopicLdaBasedOut)scoringMethod).getListTopicLda());
				}
				else if (scoring == VectorCaracteristicBasedOut.class && selection == VectorCaracteristicBasedIn.class) {
					((VectorCaracteristicBasedIn)sentenceSelection).setVectorCaracterisic(((VectorCaracteristicBasedOut)scoringMethod).getVectorCaracterisic());
				}
				else if (scoring == FileModelBasedOut.class && selection == FileModelBasedIn.class) {
					((FileModelBasedIn)sentenceSelection).setFileModel(((FileModelBasedOut)scoringMethod).getFileModel());
				}
			}
		}
	}
	
	@Override
	public void finish() throws Exception {
		Iterator<AbstractPostProcess> postProIt = postProcess.iterator();
		while (postProIt.hasNext()) {
			AbstractPostProcess p = postProIt.next();
			p.setModel(getModel());
			p.setCurrentProcess(this);
			p.init();
		}
		
		postProIt = postProcess.iterator();
		while (postProIt.hasNext()) {
			AbstractPostProcess p = postProIt.next();
			p.process();
		}
		
		postProIt = postProcess.iterator();
		while (postProIt.hasNext()) {
			AbstractPostProcess p = postProIt.next();
			p.finish();
		}
		
		dictionnary.clear();
		allSentenceList.clear();
	}
	
	/*public List<AbstractPreProcess> getPreProcess() {
		return preProcess;
	}
	public void setPreProcess(List<AbstractPreProcess> preProcess) {
		this.preProcess = preProcess;
	}*/
	
	public List<AbstractPostProcess> getPostProcess() {
		return postProcess;
	}
	
	public void setPostProcess(List<AbstractPostProcess> postProcess) {
		this.postProcess = postProcess;
	}

	public List<List<SentenceModel>> getSummary() {
		return summary;
	}

	public void setSummary(List<List<SentenceModel>> summary) {
		this.summary = summary;
	}

	public int getSizeSummary() {
		return sizeSummary;
	}

	public void setSizeSummary(int sizeSummary) {
		this.sizeSummary = sizeSummary;
	}

	public AbstractSummarizeMethod getSentenceSelection() {
		return sentenceSelection;
	}

	public void setSentenceSelection(AbstractSummarizeMethod sentenceSelection) {
		this.sentenceSelection = sentenceSelection;
	}

	public AbstractScoringMethod getScoringMethod() {
		return scoringMethod;
	}

	public void setScoringMethod(AbstractScoringMethod scoringMethod) {
		this.scoringMethod = scoringMethod;
	}

	public List<SentenceModel> getSentenceList() {
		return allSentenceList;
	}

	public void setSentenceList(List<SentenceModel> allSentenceList) {
		this.allSentenceList = allSentenceList;
	}
}
