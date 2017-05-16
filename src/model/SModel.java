package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import control.Controller;
import exception.LacksOfFeatures;
import model.task.postProcess.EvaluationROUGE;
import model.task.preProcess.AbstractPreProcess;
import model.task.process.AbstractProcess;
import textModeling.Corpus;
import textModeling.MultiCorpus;
import textModeling.SentenceModel;

public class SModel extends Observable {
	protected Controller ctrl;
	
	protected int taskID;
	
	protected String language;

	protected String outputPath;
	
	/**
	 * Liste des AbstractPreProcess a appliquer aux documents avant de résumer.
	 */
	protected List<AbstractPreProcess> preProcess = new ArrayList<AbstractPreProcess>();
	/**
	 * Liste des méthodes de résumé automatique.
	 */
	protected List<AbstractProcess> process = new ArrayList<AbstractProcess>();
	/**
	 * Matrice des résumés : Dimension = MultiCorpus et ListCorpus à résumer
	 */
	//protected Map<Integer, Map<Integer, List<SentenceModel>>> summary = new HashMap<Integer, Map<Integer, List<SentenceModel>>>();
	/**
	 * Map des noms des AbstractMethod (Process, PreProcess, PostProcess, ScoringMethod, SummarizeMethod) (utiliser .class à la place ?) et de leurs IDs.
	 */
	protected Map<String,Integer> processIDs = new HashMap<String, Integer>();
	/**
	 * Liste des options associés à chaque AbstractMethod, une Map par AbstractMethod (Map sous la forme String (nom de l'option) String (valeur sous forme de String))
	 */
	protected List<Map<String, String>> processOption = new ArrayList<Map<String, String>>();
	/**
	 * Liste des MultiCorpus sur lesquels appliqués les AbstractProcess
	 */
	protected List<MultiCorpus> multiCorpusModels = new ArrayList<MultiCorpus>();
	/**
	 * MultiCorpus en cours de traitement
	 */
	protected MultiCorpus currentMultiCorpus;
	/**
	 * boolean, true if multiThreading
	 * Multithreading define as one thread for each corpus in the currentMulticorpus
	 */
	protected boolean bMultiThreading = false;
	/**
	 * boolean, true if ROUGE evaluation
	 */
	protected boolean bRougeEvaluation = false;
	/**
	 * PostProcess permettant le calcul du score ROUGE
	 */
	protected EvaluationROUGE evalRouge;
	
	/**
	 * Constructeur simple
	 */
	public SModel() {
		super();
	}

	/**
	 * Applique les PreProcess {@link #preProcess} sur les MultiCorpus {@link #multiCorpusModels}
	 * Lance l'exécution des AbstractProcess dans {@link #process} sur les MultiCorpus {@link #multiCorpusModels}
	 */
	public void run() {
		try {
			Iterator<AbstractPreProcess> preProIt = preProcess.iterator();
			while (preProIt.hasNext()) {
				AbstractPreProcess p = preProIt.next();
				p.setModel(this);
				p.init();
			}
			
			loadMultiCorpusModels();
			
			Iterator<MultiCorpus> multiCorpusIt = multiCorpusModels.iterator();
			while (multiCorpusIt.hasNext()) {
				currentMultiCorpus = multiCorpusIt.next();
				preProIt = preProcess.iterator();
				while (preProIt.hasNext()) {
					AbstractPreProcess p = preProIt.next();
					p.setCurrentMultiCorpus(currentMultiCorpus);
					p.process();
				}
				preProIt = preProcess.iterator();
				while (preProIt.hasNext()) {
					AbstractPreProcess p = preProIt.next();
					p.finish();
				}				

				System.out.println(currentMultiCorpus);
			}
			
			multiCorpusIt = multiCorpusModels.iterator();
			while (multiCorpusIt.hasNext()) {
				currentMultiCorpus = multiCorpusIt.next();		
				System.out.println("MultiCorpus : " + currentMultiCorpus.getiD());
				
				Iterator<AbstractProcess> proIt = process.iterator();
				while (proIt.hasNext()) {
					long time = System.currentTimeMillis();
					AbstractProcess p = proIt.next();
					p.setModel(this);
					p.initCorpusToCompress();
					p.initADN();
					runProcess(currentMultiCorpus, p);
					System.out.println(System.currentTimeMillis() - time);
				}
			}
			if (bRougeEvaluation) {
				evalRouge.setModel(this);
				evalRouge.setCurrentMultiCorpus(currentMultiCorpus);
				evalRouge.init();
				evalRouge.process();
				evalRouge.finish();
			}
		}
		catch (LacksOfFeatures e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Instanciation des TextModels dans les corpus
	 */
	public void loadMultiCorpusModels() {
		for (MultiCorpus multiC : multiCorpusModels)
			for (Corpus c : multiC)
				c.loadDocumentModels();
	}
	
	/**
	 * Traitement du AbstractProcess p sur le MultiCorpus
	 * @param multiCorpus
	 * @param p
	 * @throws Exception
	 */
	public void runProcess(MultiCorpus multiCorpus, AbstractProcess p) throws Exception {
		if (bMultiThreading) {
			int nbThreads = p.getListCorpusId().size();
			
			AbstractProcess[] threads = new AbstractProcess[nbThreads];

			threads[0] = p;
			for (int i=0; i<nbThreads; i++) {
				if (i != 0)
					threads[i] = p.makeCopy();
				threads[i].setCurrentMultiCorpus(new MultiCorpus(multiCorpus));
				threads[i].setModel(this);
				threads[i].setSummarizeIndex(p.getListCorpusId().get(i));
				threads[i].initADN();
			}
			for (int i=0; i<nbThreads; i++) {
				threads[i].start();
			}
			for (int i=0; i<nbThreads; i++) {
				threads[i].join();
			}
			/*for (int i : p.getListCorpusId()) {
				setChanged();
				notifyObservers("Corpus " + i + "\n" + SentenceModel.listSentenceModelToString(p.getSummary().get(multiCorpus.getiD()).get(i)));
			}*/
		}
		else {
			for (int i : p.getListCorpusId()) {
				p.setCurrentMultiCorpus(multiCorpus);
				p.setSummarizeIndex(i);
				p.init();
				p.process();
				p.finish();
				setChanged();
				notifyObservers("Corpus " + i + "\n" + SentenceModel.listSentenceModelToString(p.getSummary().get(multiCorpus.getiD()).get(i)));
			}
		}
	}
	
	public void setModelChanged() {
		setChanged();
	}
	
	public Controller getCtrl() {
		return ctrl;
	}
	
	public void setCtrl(Controller ctrl) {
		this.ctrl = ctrl;
	}
	public List<AbstractProcess> getProcess() {
		return process;
	}
	public void setProcess(List<AbstractProcess> process) {
		this.process = process;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getProcessOption(int processId, String optionName) throws LacksOfFeatures {
		if (getProcessOption().get(processId) != null && !getProcessOption().get(processId).isEmpty() && getProcessOption().get(processId).containsKey(optionName)) {
			return getProcessOption().get(processId).get(optionName);
		}
		else
			throw new LacksOfFeatures(optionName);
	}
	
	public List<Map<String, String>> getProcessOption() {
		return processOption;
	}

	public void setProcessOption(List<Map<String, String>> processOption) {
		this.processOption = processOption;
	}

	public Map<String, Integer> getProcessIDs() {
		return processIDs;
	}

	public void setProcessIDs(Map<String, Integer> processIDs) {
		this.processIDs = processIDs;
	}
	
	public AbstractProcess getProcessByID(int ID) {
		AbstractProcess returnProcess = null;
		boolean notFind = true;
		Iterator<AbstractProcess> it = process.iterator();
		while (notFind && it.hasNext()) {
			AbstractProcess p = it.next();
			if (p.getId() == ID) {
				returnProcess = p;
				notFind = false;
			}
		}
		return returnProcess;
	}
	
	public void clear() {
		taskID = -1;
		language = "";
		outputPath = "";
		
		process.clear();
		preProcess.clear();
		processIDs.clear();
		processOption.clear();
		multiCorpusModels.clear();
	}

	public void setMultiThreading(boolean multiThreading) {
		this.bMultiThreading = multiThreading;
	}

	public boolean isbRougeEvaluation() {
		return bRougeEvaluation;
	}

	public void setbRougeEvaluation(boolean bRougeEvaluation) {
		this.bRougeEvaluation = bRougeEvaluation;
	}

	public int getTaskID() {
		return taskID;
	}

	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public List<AbstractPreProcess> getPreProcess() {
		return preProcess;
	}

	public void setPreProcess(List<AbstractPreProcess> preProcess) {
		this.preProcess = preProcess;
	}

	public List<MultiCorpus> getMultiCorpusModels() {
		return multiCorpusModels;
	}

	public void setCorpusModels(List<MultiCorpus> multiCorpusModels) {
		this.multiCorpusModels = multiCorpusModels;
	}

	public void setCurrentMultiCorpus(MultiCorpus currentMultiCorpus) {
		this.currentMultiCorpus = currentMultiCorpus;
	}

	public EvaluationROUGE getEvalRouge() {
		return evalRouge;
	}

	public void setEvalRouge(EvaluationROUGE evalRouge) {
		this.evalRouge = evalRouge;
	}

	public boolean isMultiThreading() {
		return bMultiThreading;
	}
}
