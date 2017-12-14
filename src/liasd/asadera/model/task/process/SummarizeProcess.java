package liasd.asadera.model.task.process;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import liasd.asadera.model.AbstractModel;
import liasd.asadera.model.task.preProcess.GenerateTextModel;
import liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import liasd.asadera.model.task.process.indexBuilder.AbstractIndexBuilder;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.scoringMethod.AbstractScoringMethod;
import liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.optimize.parameter.ADN;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.MultiCorpus;
import liasd.asadera.textModeling.SentenceModel;

@SuppressWarnings("rawtypes") 
public class SummarizeProcess extends AbstractProcess implements Runnable {

	private Thread t;
	private SummarizeProcess[] threads = null;
	int nbThreads = 0;
	
	protected List<AbstractIndexBuilder> indexBuilders;
	protected List<AbstractCaracteristicBuilder> caracteristicBuilders;
	protected List<AbstractScoringMethod> scoringMethods;
	protected AbstractSelectionMethod selectionMethod;
	
	//Matrice de résumé : Dimension = MultiCorpus et List Corpus à résumer
	protected Map<Integer, Map<Integer, List<SentenceModel>>> summary = new HashMap<Integer, Map<Integer, List<SentenceModel>>>();
	//private int sizeSummary = 8;

	public SummarizeProcess(int id) throws SupportADNException {
		super(id);
	}
	
	public SummarizeProcess makeCopy() throws Exception {
		SummarizeProcess p = new SummarizeProcess(id);
		initCopy(p);
		return p;
	}
	
	@Override
	protected final void initCopy(AbstractProcess p) throws Exception {
		super.initCopy(p);
		SummarizeProcess sp = (SummarizeProcess) p;
		
		List<AbstractIndexBuilder> listIndexBuilder = new ArrayList<AbstractIndexBuilder>();
		if (indexBuilders != null) {
			for (AbstractIndexBuilder builder : indexBuilders)
				listIndexBuilder.add(builder.makeCopy());
		}
		sp.setIndexBuilders(listIndexBuilder);
		
		List<AbstractCaracteristicBuilder> listBuilder = new ArrayList<AbstractCaracteristicBuilder>();
		if (caracteristicBuilders != null) {
			for (AbstractCaracteristicBuilder builder : caracteristicBuilders)
				listBuilder.add(builder.makeCopy());
		}
		sp.setCaracteristicBuilders(listBuilder);
		
		List<AbstractScoringMethod> listScoring = new ArrayList<AbstractScoringMethod>();
		if (scoringMethods != null) {
			for (AbstractScoringMethod scoring : scoringMethods)
				listScoring.add(scoring.makeCopy());
		}
		sp.setScoringMethods(listScoring);
		
		sp.setSelectionMethod(selectionMethod.makeCopy());
		sp.setSummary(summary);
	}

	public void initADN() throws Exception {
		if (indexBuilders != null) {
			for (AbstractIndexBuilder indexBuilder : indexBuilders)
				indexBuilder.setCurrentProcess(this);
		}
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

		if (indexBuilders != null) {
			for (AbstractIndexBuilder indexBuilder : indexBuilders)
				indexBuilder.initADN();
		}
		if (caracteristicBuilders != null) {
			for (AbstractCaracteristicBuilder caracteristicBuilder : caracteristicBuilders)
				caracteristicBuilder.initADN();
		}
		if (scoringMethods != null) {
			for (AbstractScoringMethod scoringMethod : scoringMethods)
				scoringMethod.initADN();
		}
		if (selectionMethod != null)
			selectionMethod.initADN();
		
		initCompatibility();		
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		corpusToSummarize = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", getCurrentMultiCorpus().get(getSummarizeCorpusId()), true);
		System.out.println("Corpus " + corpusToSummarize.getiD() + " read");
	}

	private void initCompatibility() {
		List<ParametrizedMethod> listMethod = new ArrayList<ParametrizedMethod>();
		
		if (indexBuilders != null) {
			listMethod.addAll(indexBuilders);
		}
		if (caracteristicBuilders != null) {
			listMethod.addAll(caracteristicBuilders);
		}
		if (scoringMethods != null) {
			listMethod.addAll(scoringMethods);
		}
		if (selectionMethod != null)
			listMethod.add(selectionMethod);
		
		ListIterator<ParametrizedMethod> it = listMethod.listIterator();
		while (it.hasNext())
			for (ParametrizedMethod pm : it.next().getSubMethod())
				it.add(pm);
		
		for (ParametrizedMethod pm : listMethod) {
			for (ParametrizedMethod pm2 : listMethod) {
				if (pm != pm2 && pm.isOutCompatible(pm2)) {
					pm.setCompatibility(pm2);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void process() throws Exception {
		List<Corpus> listCorpus = new ArrayList<Corpus>();
		listCorpus.add(corpusToSummarize);
		if (indexBuilders != null) {
			for (AbstractIndexBuilder indexBuilder : indexBuilders)
				indexBuilder.processIndex(listCorpus);
		}
		if (caracteristicBuilders != null) {
			for (AbstractCaracteristicBuilder caracteristicBuilder : caracteristicBuilders)
				caracteristicBuilder.processCaracteristics(listCorpus);
		}
		if (scoringMethods != null) {
			for (AbstractScoringMethod scoringMethod : scoringMethods) {
				scoringMethod.init(this);
				scoringMethod.computeScores(listCorpus);
			}
		}
		if (selectionMethod != null) {
			//Nouveau MultiCorpus
			boolean test;
			List<SentenceModel> sum = selectionMethod.calculateSummary(listCorpus);
			//System.out.println("Summary size " + sum.size() + " corpus " + getSummarizeCorpusId() + " MultiCorpus " + currentMultiCorpus.getiD() + " is ok.");
			
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
		if (indexBuilders != null)
			for (AbstractIndexBuilder indexBuilder : indexBuilders)
				indexBuilder.finish();
		if (caracteristicBuilders != null)
			for (AbstractCaracteristicBuilder caracteristicBuilder : caracteristicBuilders)
				caracteristicBuilder.finish();
		if (scoringMethods != null)
			for (AbstractScoringMethod scoringMethod : scoringMethods)
				scoringMethod.finish();
	}
	
	public void start() throws Exception {
		System.out.println("Starting " +  this.getClass().getSimpleName() + " " + getSummarizeCorpusId());
		t = new Thread (this, this.getClass() + " " + getSummarizeCorpusId());
		t.start();
	}
	
	public void join() throws InterruptedException {
		t.join();
		getModel().setModelChanged();
		getModel().notifyObservers("Corpus " + getSummarizeCorpusId() + "\n" + SentenceModel.listSentenceModelToString(this.getSummary().get(currentMultiCorpus.getiD()).get(getSummarizeCorpusId())));
	}
	
	@Override
	public void run() {
		try {
			process();
			finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Map<Integer, Map<Integer, List<SentenceModel>>> getSummary() {
		return summary;
	}

	public List<AbstractIndexBuilder> getIndexBuilders() {
		return indexBuilders;
	}

	public void setIndexBuilders(List<AbstractIndexBuilder> indexBuilders) {
		this.indexBuilders = indexBuilders;
		for (AbstractIndexBuilder aib : indexBuilders) {
			if (aib.getSupportADN() != null)
				supportADN.putAll(aib.getSupportADN());
		}
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
		if (getModel().getMultiCorpusModels().size() != 1)
			throw new Exception("Too much MultiCorpus for MultiThreading Optimize");
		setCurrentMultiCorpus(getModel().getMultiCorpusModels().get(0));
		if (getModel().isMultiThreading()) {
			initCorpusToCompress();
			nbThreads = getListCorpusId().size();
			threads = new SummarizeProcess[nbThreads];

			threads[0] = this;
			for (int i=0; i<nbThreads; i++) {
				if (i != 0)
					threads[i] = makeCopy();
				threads[i].setADN(new ADN(getADN()));
				threads[i].setCurrentMultiCorpus(new MultiCorpus(currentMultiCorpus));
				threads[i].setModel(getModel());
				threads[i].setSummarizeIndex(getListCorpusId().get(i));
				threads[i].setCorpusToSummarize(getCurrentMultiCorpus().get(threads[i].getSummarizeCorpusId()));
				threads[i].initADN();
				threads[i].init();
			}
		}
		else {
			initCorpusToCompress();
			for (int i : getListCorpusId()) {
				setSummarizeIndex(i);
				init();
			}
		}
	}

	@Override
	public void optimize() throws Exception {		
//		Iterator<MultiCorpus> multiCorpusIt = getModel().getMultiCorpusModels().iterator();
//		while (multiCorpusIt.hasNext()) {
//			MultiCorpus multiCorpus = multiCorpusIt.next();
//			getModel().setCurrentMultiCorpus(multiCorpus);
			
			System.out.println("MultiCorpus : " + currentMultiCorpus.getiD());
			
			if (getModel().isMultiThreading()) {
				threads[0].setCorpusToSummarize(getCurrentMultiCorpus().get(0));
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
				for (int i : getListCorpusId()) {
//					setCurrentMultiCorpus(multiCorpus);
					setSummarizeIndex(i);
					setCorpusToSummarize(getCurrentMultiCorpus().get(getSummarizeCorpusId()));					
					process();
					finish();
				}
			}
			getModel().getEvalRouge().setModel(getModel());
			getModel().getEvalRouge().init();
			getModel().getEvalRouge().process();
			getModel().getEvalRouge().finish();
//		}
	}
	
	@Override
	public void setCurrentMultiCorpus(MultiCorpus currentMultiCorpus) {
		super.setCurrentMultiCorpus(currentMultiCorpus);
		if (indexBuilders != null) {
			for (AbstractIndexBuilder indexBuilder : indexBuilders)
				indexBuilder.setCurrentMultiCorpus(currentMultiCorpus);
		}
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
		if (indexBuilders != null) {
			for (AbstractIndexBuilder indexBuilder : indexBuilders)
				indexBuilder.setModel(model);
		}
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
