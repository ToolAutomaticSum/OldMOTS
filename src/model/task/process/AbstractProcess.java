package model.task.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.Model;
import model.task.AbstractTaskModel;
import model.task.postProcess.AbstractPostProcess;
import model.task.process.ILP.BiGramListBasedOut;
import model.task.process.LDA.LdaBasedOut;
import model.task.process.scoringMethod.AbstractScoringMethod;
import model.task.process.scoringMethod.ScoreBasedOut;
import model.task.process.scoringMethod.ILP.BiGramListBasedIn;
import model.task.process.scoringMethod.ILP.FileModelBasedOut;
import model.task.process.scoringMethod.LDA.LdaBasedIn;
import model.task.process.scoringMethod.LDA.TopicLdaBasedOut;
import model.task.process.summarizeMethod.AbstractSummarizeMethod;
import model.task.process.summarizeMethod.FileModelBasedIn;
import model.task.process.summarizeMethod.ScoreBasedIn;
import model.task.process.summarizeMethod.TopicLdaBasedIn;
import optimize.Optimize;
import optimize.SupportADNException;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.wordIndex.Dictionnary;
import tools.Tools;

public abstract class AbstractProcess extends Optimize implements AbstractTaskModel {

	protected Model model;
	protected Dictionnary dictionnary = new Dictionnary();
	protected Map<Integer, String> hashMapWord = new HashMap<Integer, String>();
	
	protected AbstractSummarizeMethod sentenceSelection;
	protected AbstractScoringMethod scoringMethod;
	protected List<AbstractPostProcess> postProcess = new ArrayList<AbstractPostProcess>();
	protected List<SentenceModel> allSentenceList = new ArrayList<SentenceModel>();
	protected List<List<SentenceModel>> summary = new ArrayList<List<SentenceModel>>();
	private int sizeSummary = 8;
	
	public AbstractProcess(int id) throws SupportADNException {
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
			allSentenceList.addAll(text.getSentence());
		}
		
		if (!(scoringMethod == null)) {
			scoringMethod.setCurrentProcess(this);
			scoringMethod.setModel(model);
			initCompatibilityProcess();
			scoringMethod.init(this, dictionnary, hashMapWord);
			scoringMethod.computeScores();
		}
		if (!(sentenceSelection == null)) {
			sentenceSelection.setCurrentProcess(this);
			sentenceSelection.setModel(model);
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
		if(scoringMethod != null) {
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
	
	@Override
	public void optimize() throws Exception {
		setModel(getModel());
		init();
		process();
		finish();
	}
	
	/**
	 * Nécessite utilisation de ROUGE
	 */
	@Override
	public double getScore() {
		if (!getModel().isbRougeEvaluation())
			return 0;
		else
			return score;
	}
	
	@Override
	public Model getModel() {
		return model;
	}

	@Override
	public void setModel(Model model) {
		this.model = model;
	}

	public Dictionnary getDictionnary() {
		return dictionnary;
	}

	public Map<Integer, String> getHashMapWord() {
		return hashMapWord;
	}
}
