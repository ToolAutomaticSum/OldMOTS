package model.task.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import exception.LacksOfFeatures;
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
import textModeling.wordIndex.Index;
import tools.Tools;

public abstract class AbstractProcess extends Optimize implements AbstractTaskModel {

	protected int summarizeIndex = 0;
	protected List<Integer> listCorpusId;
	protected Index index;
	
	protected AbstractSummarizeMethod sentenceSelection;
	protected AbstractScoringMethod scoringMethod;
	protected List<AbstractPostProcess> postProcess = new ArrayList<AbstractPostProcess>();
	protected List<SentenceModel> allSentenceList = new ArrayList<SentenceModel>();
	
	//Matrice de résumé : Dimension = MultiCorpus et List Corpus à résumer
	protected Map<Integer, Map<Integer, List<SentenceModel>>> summary = new HashMap<Integer, Map<Integer, List<SentenceModel>>>();
	private int sizeSummary = 8;
	
	public AbstractProcess(int id) throws SupportADNException {
		super(id);
	}
	
	public final void initCorpusToCompress() throws NumberFormatException, LacksOfFeatures {
		listCorpusId = new ArrayList<Integer>();
		for (String corpusId : getModel().getProcessOption(id, "CorpusIdToSummarize").split("\t"))
			listCorpusId.add(Integer.parseInt(corpusId));
	}
	
	@Override
	public void init() throws Exception {
		index = new Index();
	}
	
	/**
	 * Appel � super.process() une fois les process effectu�s.
	 */
	@Override
	public void process() throws Exception {
		for (TextModel text : getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId())) {
			allSentenceList.addAll(text.getSentence());
		}
		
		if (!(scoringMethod == null)) {
			scoringMethod.setCurrentProcess(this);
			scoringMethod.setModel(model);
			initCompatibilityProcess();
			scoringMethod.init(this, index);
			scoringMethod.computeScores();
		}
		if (!(sentenceSelection == null)) {
			sentenceSelection.setCurrentProcess(this);
			sentenceSelection.setModel(model);
			initCompatibilityScoring();
			//Nouveau MultiCorpus
			if (!summary.containsKey(getModel().getCurrentMultiCorpus().getiD())) {
				Map<Integer, List<SentenceModel>> map = new HashMap<Integer, List<SentenceModel>>();
				map.put(getSummarizeCorpusId(), sentenceSelection.calculateSummary());
				summary.put(getModel().getCurrentMultiCorpus().getiD(), map);
			}
			else { //MultiCorpus en cours de travail, parcours de listCorpusId 
				Map<Integer, List<SentenceModel>> map = summary.get(getModel().getCurrentMultiCorpus().getiD());
				map.put(getSummarizeCorpusId(), sentenceSelection.calculateSummary());
			}
		}
	}
	
	public int getSummarizeIndex() {
		return summarizeIndex;
	}

	public void setSummarizeIndex(int summarizeIndex) {
		this.summarizeIndex = summarizeIndex;
	}

	protected void initCompatibilityProcess() {
		Set<Class<?>> classProcess = Tools.getInheritance(this.getClass());
		Set<Class<?>> classScoring = Tools.getInheritance(scoringMethod.getClass());
		if (classProcess.contains(LdaBasedOut.class) && classScoring.contains(LdaBasedIn.class)) {
			((LdaBasedIn)scoringMethod).setK(((LdaBasedOut)this).getK());
			((LdaBasedIn)scoringMethod).setTheta(((LdaBasedOut)this).getTheta());
			((LdaBasedIn)scoringMethod).setNbSentence(((LdaBasedOut)this).getNbSentence());
		}
		if (classProcess.contains(VectorCaracteristicBasedOut.class) && classScoring.contains(VectorCaracteristicBasedIn.class)) {
			((VectorCaracteristicBasedIn)scoringMethod).setVectorCaracterisic(((VectorCaracteristicBasedOut)this).getVectorCaracterisic());
		}
		if (classProcess.contains(BiGramListBasedOut.class) && classScoring.contains(BiGramListBasedIn.class)) {
			((BiGramListBasedIn)scoringMethod).setBiGramsInSentence(((BiGramListBasedOut)this).getBiGramsInSentence());
			((BiGramListBasedIn)scoringMethod).setBiGrams(((BiGramListBasedOut)this).getBiGrams());
			((BiGramListBasedIn)scoringMethod).setBiGramWeights(((BiGramListBasedOut)this).getBiGramWeights());
		}
	}
	
	protected void initCompatibilityScoring() {
		if(scoringMethod != null) {
			Set<Class<?>> classSelection = Tools.getInheritance(sentenceSelection.getClass());
			Set<Class<?>> classScoring = Tools.getInheritance(scoringMethod.getClass());
			if (classScoring.contains(ScoreBasedOut.class) && classSelection.contains(ScoreBasedIn.class)) {
				((ScoreBasedIn)sentenceSelection).setScore(((ScoreBasedOut)scoringMethod).getScore());
			}
			if (classScoring.contains(TopicLdaBasedOut.class) && classSelection.contains(TopicLdaBasedIn.class)) {
				((TopicLdaBasedIn)sentenceSelection).setListTopicLda(((TopicLdaBasedOut)scoringMethod).getListTopicLda());
			}
			if (classScoring.contains(VectorCaracteristicBasedOut.class) && classSelection.contains(VectorCaracteristicBasedIn.class)) {
				((VectorCaracteristicBasedIn)sentenceSelection).setVectorCaracterisic(((VectorCaracteristicBasedOut)scoringMethod).getVectorCaracterisic());
			}
			if (classScoring.contains(FileModelBasedOut.class) && classSelection.contains(FileModelBasedIn.class)) {
				((FileModelBasedIn)sentenceSelection).setFileModel(((FileModelBasedOut)scoringMethod).getFileModel());
			}
			if (Tools.getInheritance(this.getClass()).contains(VectorCaracteristicBasedOut.class) && classSelection.contains(VectorCaracteristicBasedIn.class)) {
				((VectorCaracteristicBasedIn)sentenceSelection).setVectorCaracterisic(((VectorCaracteristicBasedOut)this).getVectorCaracterisic());
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
		
		index.clear();
		allSentenceList.clear();
	}
	
	public List<AbstractPostProcess> getPostProcess() {
		return postProcess;
	}
	
	public void setPostProcess(List<AbstractPostProcess> postProcess) {
		this.postProcess = postProcess;
	}

	public Map<Integer, Map<Integer, List<SentenceModel>>> getSummary() {
		return summary;
	}

	public int getSizeSummary() {
		return sizeSummary;
	}

	public List<Integer> getListCorpusId() {
		return listCorpusId;
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
	 * N�cessite utilisation de ROUGE
	 */
	@Override
	public double getScore() {
		if (!getModel().isbRougeEvaluation())
			return 0;
		else
			return score;
	}
	
	public Index getIndex() {
		return index;
	}

	/**
	 * Retourne l'Id du corpus résumé
	 * @return
	 */
	public Integer getSummarizeCorpusId() {
		return listCorpusId.get(summarizeIndex);
	}
}
