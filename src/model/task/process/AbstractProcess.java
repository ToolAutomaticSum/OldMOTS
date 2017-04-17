package model.task.process;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import exception.LacksOfFeatures;
import model.SModel;
import model.task.AbstractTaskModel;
import model.task.postProcess.AbstractPostProcess;
import model.task.preProcess.GenerateTextModel;
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
import optimize.parameter.ADN;
import textModeling.Corpus;
import textModeling.MultiCorpus;
import textModeling.SentenceModel;
import textModeling.wordIndex.Index;
import tools.Tools;

public abstract class AbstractProcess extends Optimize implements AbstractTaskModel {

	protected int summarizeIndex = 0;
	protected List<Integer> listCorpusId;
	protected Index index;
	
	protected Corpus corpusToSummarize;
	protected boolean readStopWords = false;
	
	protected AbstractSummarizeMethod sentenceSelection;
	protected AbstractScoringMethod scoringMethod;
	protected List<AbstractPostProcess> postProcess = new ArrayList<AbstractPostProcess>();
	
	//Matrice de résumé : Dimension = MultiCorpus et List Corpus à résumer
	protected Map<Integer, Map<Integer, List<SentenceModel>>> summary = new HashMap<Integer, Map<Integer, List<SentenceModel>>>();
	private int sizeSummary = 8;
	
	public AbstractProcess(int id) throws SupportADNException {
		super(id);

		index = new Index();
		supportADN = new HashMap<String, Class<?>>();
	}
	
	public final void initCorpusToCompress() throws NumberFormatException, LacksOfFeatures {
		listCorpusId = new ArrayList<Integer>();
		for (String corpusId : getModel().getProcessOption(id, "CorpusIdToSummarize").split("\t"))
			listCorpusId.add(Integer.parseInt(corpusId));
	}
	
	public void initADN() throws Exception {
		if (scoringMethod != null)
			scoringMethod.setCurrentProcess(this);
		if (sentenceSelection != null)
			sentenceSelection.setCurrentProcess(this);

		adn = new ADN(supportADN);

		if (scoringMethod != null)
			scoringMethod.initADN();

		if (sentenceSelection != null)
			sentenceSelection.initADN();
	}
	
	@Override
	public void init() throws Exception {
		try {
			readStopWords = Boolean.parseBoolean(getModel().getProcessOption(id, "ReadStopWords"));
		}
		catch (LacksOfFeatures e) {
			System.out.println("ReadStopWords = false");
			readStopWords = false;
		}
		corpusToSummarize = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()), readStopWords);
	}
	
	/**
	 * Appel � super.process() une fois les process effectu�s.
	 */
	@Override
	public void process() throws Exception {
		System.out.println("Process Corpus " + getSummarizeCorpusId());
		
		if (scoringMethod != null) {
			scoringMethod.setCurrentProcess(this);
			scoringMethod.setModel(model);
			initCompatibilityProcess();
			scoringMethod.init(this, index);
			scoringMethod.computeScores();
		}
		if (sentenceSelection != null) {
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

	/**
	 * TODO Ajouter test et throw error si incompatibility
	 */
	protected void initCompatibilityProcess() {
		Set<Class<?>> classProcess = Tools.getInheritance(this.getClass());
		Set<Class<?>> classScoring = Tools.getInheritance(scoringMethod.getClass());
		if (classProcess.contains(LdaBasedOut.class) && classScoring.contains(LdaBasedIn.class)) {
			((LdaBasedIn)scoringMethod).setK(((LdaBasedOut)this).getK());
		}
		if (classProcess.contains(VectorCaracteristicBasedOut.class) && classScoring.contains(VectorCaracteristicBasedIn.class)) {
			((VectorCaracteristicBasedIn)scoringMethod).setVectorCaracterisic(((VectorCaracteristicBasedOut)this).getVectorCaracterisic());
		}
		if (classProcess.contains(BiGramListBasedOut.class) && classScoring.contains(BiGramListBasedIn.class)) {
			((BiGramListBasedIn)scoringMethod).setBiGramsIds(((BiGramListBasedOut)this).getBiGramsIds());
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
		corpusToSummarize.clear();
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
		if (sentenceSelection != null) {
			this.sentenceSelection = sentenceSelection;
			if (sentenceSelection.getSupportADN() != null)
				supportADN.putAll(sentenceSelection.getSupportADN());
		}
	}

	public AbstractScoringMethod getScoringMethod() {
		return scoringMethod;
	}

	public void setScoringMethod(AbstractScoringMethod scoringMethod) {
		if (scoringMethod != null) {
			this.scoringMethod = scoringMethod;
			if (scoringMethod.getSupportADN() != null)
				supportADN.putAll(scoringMethod.getSupportADN());
		}
	}
	
	public Corpus getCorpusToSummarize() {
		return corpusToSummarize;
	}

	@Override
	public void optimize() throws Exception {		
		Iterator<MultiCorpus> multiCorpusIt = getModel().getMultiCorpusModels().iterator();
		while (multiCorpusIt.hasNext()) {
			getModel().setCurrentMultiCorpus(multiCorpusIt.next());		
			System.out.println("MultiCorpus : " + getModel().getCurrentMultiCorpus().getiD());

			this.initCorpusToCompress();
			for (int i : this.getListCorpusId()) {
				this.setSummarizeIndex(i);
				
				this.init();
				this.process();
				this.finish();
			}
		}
		getModel().getEvalRouge().setModel(getModel());
		getModel().getEvalRouge().init();
		getModel().getEvalRouge().process();
		getModel().getEvalRouge().finish();
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
	
	@Override
	public void setModel(SModel model) {
		super.setModel(model);
		if (scoringMethod != null)
			scoringMethod.setModel(model);
		if (sentenceSelection != null)
			sentenceSelection.setModel(model);
	}
}
