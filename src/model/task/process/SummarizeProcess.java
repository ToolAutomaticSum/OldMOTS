package model.task.process.tempProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import exception.LacksOfFeatures;
import model.AbstractModel;
import model.task.preProcess.GenerateTextModel;
import model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import model.task.process.indexBuilder.AbstractIndexBuilder;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.tempScoringMethod.AbstractScoringMethod;
import model.task.process.tempSelectionMethod.AbstractSelectionMethod;
import optimize.SupportADNException;
import optimize.parameter.ADN;
import textModeling.MultiCorpus;
import textModeling.SentenceModel;

@SuppressWarnings("rawtypes") 
public class SummarizeProcess extends AbstractProcess implements Runnable {

	private Thread t;
	private SummarizeProcess[] threads = null;
	int nbThreads = 0;
	
	protected AbstractIndexBuilder indexBuilder;
	protected List<AbstractCaracteristicBuilder> caracteristicBuilders;
	protected List<AbstractScoringMethod> scoringMethods;
	protected AbstractSelectionMethod selectionMethod;
	
	//Matrice de résumé : Dimension = MultiCorpus et List Corpus à résumer
	protected Map<Integer, Map<Integer, List<SentenceModel>>> summary = new HashMap<Integer, Map<Integer, List<SentenceModel>>>();
	//private int sizeSummary = 8;

	public SummarizeProcess(int id) throws SupportADNException {
		super(id);

		supportADN = new HashMap<String, Class<?>>();
	}
	
	public SummarizeProcess makeCopy() throws Exception {
		return this;
	}
	
	@Override
	protected final void initCopy(AbstractProcess p) throws Exception {
		super.initCopy(p);
		SummarizeProcess sp = (SummarizeProcess) p;
		sp.setIndexBuilder(indexBuilder.makeCopy());
		List<AbstractCaracteristicBuilder> listBuilder = new ArrayList<AbstractCaracteristicBuilder>();
		for (AbstractCaracteristicBuilder builder : caracteristicBuilders)
			listBuilder.add(builder.makeCopy());
		sp.setCaracteristicBuilders(listBuilder);
		sp.setSelectionMethod(selectionMethod.makeCopy());
		List<AbstractScoringMethod> listScoring = new ArrayList<AbstractScoringMethod>();
		for (AbstractScoringMethod scoring : scoringMethods)
			listScoring.add(scoring.makeCopy());
		sp.setScoringMethods(listScoring);
		sp.setSummary(summary);
	}

	public void initADN() throws Exception {
		if (indexBuilder != null)
			indexBuilder.setCurrentProcess(this);
		if (caracteristicBuilders != null) {
			for (AbstractCaracteristicBuilder caracteristicBuilder : caracteristicBuilders)
				caracteristicBuilder.setCurrentProcess(this);
		}
		if (scoringMethods != null) {
			for (AbstractScoringMethod scoringMethod : scoringMethods)
				scoringMethod.setCurrentProcess(this);
		}
		if (selectionMethod != null)
			selectionMethod.setCurrentProcess(this);

		adn = new ADN(supportADN);

		if (indexBuilder != null)
			indexBuilder.initADN();
		if (caracteristicBuilders != null) {
			for (AbstractCaracteristicBuilder caracteristicBuilder : caracteristicBuilders)
				caracteristicBuilder.initADN();;
		}
		if (scoringMethods != null) {
			for (AbstractScoringMethod scoringMethod : scoringMethods)
				scoringMethod.initADN();
		}
		if (selectionMethod != null)
			selectionMethod.initADN();
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

	private void initCompatibility() {
		List<ParametrizedMethod> listMethod = new ArrayList<ParametrizedMethod>();
		
		listMethod.add(indexBuilder);
		if (caracteristicBuilders != null) {
			listMethod.addAll(caracteristicBuilders);
		}
		if (scoringMethods != null) {
			listMethod.addAll(scoringMethods);
		}
		if (selectionMethod != null)
			listMethod.add(selectionMethod);
		
		for (ParametrizedMethod pm : listMethod) {
			for (ParametrizedMethod pm2 : listMethod) {
				if (pm != pm2 && pm.isOutCompatible(pm2)) {
					pm.setCompatibility(pm2);
				}
			}
		} 
	}
	
	@Override
	public void process() throws Exception {
		initCompatibility();
		if (indexBuilder != null) {
			indexBuilder.processIndex();
		}
		if (caracteristicBuilders != null) {
			for (AbstractCaracteristicBuilder caracteristicBuilder : caracteristicBuilders)
				caracteristicBuilder.processCaracteristics();
		}
		if (scoringMethods != null) {
			for (AbstractScoringMethod scoringMethod : scoringMethods) {
				scoringMethod.init(this);
				scoringMethod.computeScores();
			}
		}
		if (selectionMethod != null) {
			//Nouveau MultiCorpus
			boolean test;
			List<SentenceModel> sum = selectionMethod.calculateSummary();
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

	@Override
	public void finish() throws Exception {
		
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
	
	/*public int getSummarizeIndex() {
		return summarizeIndex;
	}*/

	public void setSummarizeIndex(int summarizeIndex) {
		this.summarizeIndex = summarizeIndex;
	}
	
	public Map<Integer, Map<Integer, List<SentenceModel>>> getSummary() {
		return summary;
	}

	public AbstractIndexBuilder getIndexBuilder() {
		return indexBuilder;
	}

	public void setIndexBuilder(AbstractIndexBuilder indexBuilder) {
		this.indexBuilder = indexBuilder;
		if (indexBuilder.getSupportADN() != null)
			supportADN.putAll(indexBuilder.getSupportADN());
	}

	public List<AbstractCaracteristicBuilder> getCaracteristicBuilders() {
		return caracteristicBuilders;
	}

	public void setCaracteristicBuilders(List<AbstractCaracteristicBuilder> caracteristicBuilders) {
		this.caracteristicBuilders = caracteristicBuilders;
		for (AbstractCaracteristicBuilder acb : caracteristicBuilders) {
			if (acb.getSupportADN() != null)
				supportADN.putAll(acb.getSupportADN());
		}
	}

	public AbstractSelectionMethod getSelectionMethod() {
		return selectionMethod;
	}

	public void setSelectionMethod(AbstractSelectionMethod sentenceSelection) {
		this.selectionMethod = sentenceSelection;
		if (selectionMethod.getSupportADN() != null)
			supportADN.putAll(selectionMethod.getSupportADN());
	}

	public List<AbstractScoringMethod> getScoringMethods() {
		return scoringMethods;
	}

	public void setScoringMethods(List<AbstractScoringMethod> scoringMethods) {
		this.scoringMethods = scoringMethods;
		for (AbstractScoringMethod acm : scoringMethods) {
			if (acm.getSupportADN() != null)
				supportADN.putAll(acm.getSupportADN());
		}
	}
	
	public void setSummary(Map<Integer, Map<Integer, List<SentenceModel>>> summary) {
		this.summary = summary;
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
			threads = new SummarizeProcess[nbThreads];

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
	
	@Override
	public void setCurrentMultiCorpus(MultiCorpus currentMultiCorpus) {
		super.setCurrentMultiCorpus(currentMultiCorpus);
		if (indexBuilder != null)
			indexBuilder.setCurrentMultiCorpus(currentMultiCorpus);
		if (caracteristicBuilders != null) {
			for (AbstractCaracteristicBuilder caracteristicBuilders : caracteristicBuilders)
				caracteristicBuilders.setCurrentMultiCorpus(currentMultiCorpus);
		}
		if (scoringMethods != null) {
			for (AbstractScoringMethod scoringMethod : scoringMethods)
				scoringMethod.setCurrentMultiCorpus(currentMultiCorpus);
		}
		if (selectionMethod != null)
			selectionMethod.setCurrentMultiCorpus(currentMultiCorpus);
	}
	
	@Override
	public void setModel(AbstractModel model) {
		super.setModel(model);
		if (indexBuilder != null)
			indexBuilder.setModel(model);
		if (caracteristicBuilders != null) {
			for (AbstractCaracteristicBuilder caracteristicBuilders : caracteristicBuilders)
				caracteristicBuilders.setModel(model);
		}
		if (scoringMethods != null) {
			for (AbstractScoringMethod scoringMethod : scoringMethods)
				scoringMethod.setModel(model);
		}
		if (selectionMethod != null)
			selectionMethod.setModel(model);
	}
}
