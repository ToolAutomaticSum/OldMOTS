package model.task.process.old;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import exception.LacksOfFeatures;
import model.AbstractModel;
import model.task.AbstractTask;
import model.task.postProcess.AbstractPostProcess;
import model.task.preProcess.GenerateTextModel;
import model.task.process.old.ILP.BiGramListBasedOut;
import model.task.process.old.LDA.LdaBasedOut;
import model.task.process.old.scoringMethod.AbstractScoringMethod;
import model.task.process.old.summarizeMethod.AbstractSummarizeMethod;
import model.task.process.scoringMethod.ScoreBasedIn;
import model.task.process.scoringMethod.ScoreBasedOut;
import optimize.Optimize;
import optimize.SupportADNException;
import optimize.parameter.ADN;
import textModeling.Corpus;
import textModeling.MultiCorpus;
import textModeling.SentenceModel;
import textModeling.wordIndex.Index;
import tools.Tools;

public abstract class AbstractProcess extends Optimize implements AbstractTask, Runnable {

	private Thread t;
	private AbstractProcess[] threads = null;
	int nbThreads = 0;
	
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
	
	public abstract AbstractProcess makeCopy() throws Exception;
	
	protected final void initCopy(AbstractProcess p) throws Exception {
		p.setListCorpusId(new ArrayList<Integer>(listCorpusId));
		p.setReadStopWords(readStopWords);
		p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		//p.setADN(new ADN(p.getSupportADN()));
		p.setSentenceSelection(sentenceSelection.makeCopy());
		if (scoringMethod != null)
			p.setScoringMethod(scoringMethod.makeCopy());
		p.setPostProcess(postProcess);
		p.setSummary(summary);
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
		corpusToSummarize = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", getCurrentMultiCorpus().get(getSummarizeCorpusId()), readStopWords);
		System.out.println("Corpus " + corpusToSummarize.getiD() + " read");
	}
	
	public void start() {
		System.out.println("Starting " +  this.getClass().getSimpleName() + " " + getSummarizeCorpusId());
		t = new Thread (this, this.getClass() + " " + getSummarizeCorpusId());
		t.start();
   }
	
	public void join() throws InterruptedException {
		t.join();
	}
	
	@Override
	public void run() {
		try {
			init();
			process();
			finish();
			getModel().setModelChanged();
			getModel().notifyObservers("Corpus " + getSummarizeCorpusId() + "\n" + SentenceModel.listSentenceModelToString(this.getSummary().get(currentMultiCorpus.getiD()).get(getSummarizeCorpusId())));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Appel à super.process() une fois les process effectués.
	 */
	@Override
	public void process() throws Exception {
		//System.out.println("Process Corpus " + getSummarizeCorpusId());
		
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
			boolean test;
			List<SentenceModel> sum = sentenceSelection.calculateSummary();
			System.out.println("Summary size " + sum.size() + " corpus " + getSummarizeCorpusId() + " MultiCorpus " + currentMultiCorpus.getiD() + " is ok.");
			
			synchronized(summary) {
				test = summary.containsKey(getCurrentMultiCorpus().getiD());
			}
			if (!test) {
				Map<Integer, List<SentenceModel>> map = new HashMap<Integer, List<SentenceModel>>();
				map.put(getSummarizeCorpusId(), sum);
				synchronized(summary) {
					summary.put(getCurrentMultiCorpus().getiD(), map);
				}
			}
			else { //MultiCorpus en cours de travail, parcours de listCorpusId 
				Map<Integer, List<SentenceModel>> map;
				synchronized(summary) {
					map = summary.get(getCurrentMultiCorpus().getiD());
				}
				map.put(getSummarizeCorpusId(), sum);
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
			//((BiGramListBasedIn)scoringMethod).setBiGrams(((BiGramListBasedOut)this).getBiGrams());
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
			p.setCurrentMultiCorpus(currentMultiCorpus);
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
	public void initOptimize() throws Exception {
		this.initCorpusToCompress();
		if (getModel().isMultiThreading()) {
			if (getModel().getMultiCorpusModels().size() != 1)
				throw new Exception("Too much MultiCorpus for MultiThreading Optimize");
			else
				currentMultiCorpus = getModel().getMultiCorpusModels().get(0);

			nbThreads = getListCorpusId().size();
			threads = new AbstractProcess[nbThreads];

			threads[0] = this;
			for (int i=0; i<nbThreads; i++) {
				if (i != 0)
					threads[i] = this.makeCopy();
				threads[i].setADN(new ADN(this.getADN()));
				threads[i].setCurrentMultiCorpus(new MultiCorpus(currentMultiCorpus));
				threads[i].setModel(getModel());
				threads[i].setSummarizeIndex(this.getListCorpusId().get(i));
				threads[i].initADN();
			}
		}
	}

	@Override
	public void optimize() throws Exception {		
		Iterator<MultiCorpus> multiCorpusIt = getModel().getMultiCorpusModels().iterator();
		while (multiCorpusIt.hasNext()) {
			MultiCorpus multiCorpus = multiCorpusIt.next();
			getModel().setCurrentMultiCorpus(multiCorpus);
			
			System.out.println("MultiCorpus : " + multiCorpus.getiD());
			
			if (getModel().isMultiThreading()) {
				threads[0].start();
				for (int i=1; i<nbThreads; i++) {
					threads[i].setADN(new ADN(this.getADN()));
					threads[i].start();
				}
				for (int i=0; i<nbThreads; i++) {
					threads[i].join();
				}
			}
			else {
				for (int i : this.getListCorpusId()) {
					this.setCurrentMultiCorpus(multiCorpus);
					this.setSummarizeIndex(i);
					this.init();
					this.process();
					this.finish();
				}
			}
			getModel().setCurrentMultiCorpus(multiCorpus);
			getModel().getEvalRouge().setModel(getModel());
			getModel().getEvalRouge().init();
			getModel().getEvalRouge().process();
			getModel().getEvalRouge().finish();
		}
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
	
	
	public void setListCorpusId(List<Integer> listCorpusId) {
		this.listCorpusId = listCorpusId;
	}

	public void setReadStopWords(boolean readStopWords) {
		this.readStopWords = readStopWords;
	}
	
	@Override
	public void setModel(AbstractModel model) {
		super.setModel(model);
		if (scoringMethod != null)
			scoringMethod.setModel(model);
		if (sentenceSelection != null)
			sentenceSelection.setModel(model);
	}

	public void setSummary(Map<Integer, Map<Integer, List<SentenceModel>>> summary) {
		this.summary = summary;
	}
}
