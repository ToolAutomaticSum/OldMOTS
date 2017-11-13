package liasd.asadera.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import liasd.asadera.control.AbstractController;
import liasd.asadera.exception.LacksOfFeatures;
import liasd.asadera.model.task.postProcess.AbstractPostProcess;
import liasd.asadera.model.task.postProcess.EvaluationROUGE;
import liasd.asadera.model.task.preProcess.AbstractPreProcess;
import liasd.asadera.model.task.process.AbstractProcess;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.MultiCorpus;

public abstract class AbstractModel extends Observable {

	private AbstractController ctrl;

	private int taskID;
	private String name;
	
	private String language;

	private String outputPath;
	
	/**
	 * Liste des AbstractPreProcess a appliquer aux documents avant de résumer.
	 */
	private List<AbstractPreProcess> preProcess = new ArrayList<AbstractPreProcess>();
	/**
	 * Liste des méthodes de résumé automatique.
	 */
	private List<AbstractProcess> process = new ArrayList<AbstractProcess>();
	/**
	 * Liste des AbstractPostProcess a appliquer aux documents après le résumer.
	 */
	private List<AbstractPostProcess> postProcess = new ArrayList<AbstractPostProcess>();
	/**
	 * Matrice des résumés : Dimension = MultiCorpus et ListCorpus à résumer
	 */
	//protected Map<Integer, Map<Integer, List<SentenceModel>>> summary = new HashMap<Integer, Map<Integer, List<SentenceModel>>>();
	/**
	 * Map des noms des AbstractMethod (Process, PreProcess, PostProcess, ScoringMethod, SummarizeMethod) (utiliser .class à la place ?) et de leurs IDs.
	 */
	private Map<String,Integer> processIDs = new HashMap<String, Integer>();
	/**
	 * Liste des options associés à chaque AbstractMethod, une Map par AbstractMethod (Map sous la forme String (nom de l'option) String (valeur sous forme de String))
	 */
	private List<Map<String, String>> processOption = new ArrayList<Map<String, String>>();
	/**
	 * Liste des MultiCorpus sur lesquels appliqués les AbstractProcess
	 */
	private List<MultiCorpus> multiCorpusModels = new ArrayList<MultiCorpus>();
	/**
	 * MultiCorpus en cours de traitement
	 */
	protected MultiCorpus currentMultiCorpus;
	/**
	 * boolean, true if multiThreading
	 * Multithreading define as one thread for each corpus in the currentMulticorpus
	 */
	private boolean bMultiThreading = false;
	/**
	 * boolean, true if ROUGE evaluation
	 */
	private boolean bRougeEvaluation = false;
	/**
	 * PostProcess permettant le calcul du score ROUGE
	 */
	private EvaluationROUGE evalRouge;
	
	public abstract void run();
	
	/**
	 * Instanciation des TextModels dans les corpus
	 */
	public void loadMultiCorpusModels() {
		for (MultiCorpus multiC : getMultiCorpusModels())
			for (Corpus c : multiC)
				c.loadDocumentModels();
	}
	
	/**
	 * Getter & Setter 
	 */
	public void setModelChanged() {
		setChanged();
	}
	
	public AbstractController getCtrl() {
		return ctrl;
	}
	
	public void setCtrl(AbstractController ctrl) {
		this.ctrl = ctrl;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	public int getTaskID() {
		return taskID;
	}

	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	
	public String getProcessOption(int processId, String optionName) throws LacksOfFeatures {
		if (processOption.get(processId) != null && !processOption.get(processId).isEmpty() && processOption.get(processId).containsKey(optionName)) {
			return processOption.get(processId).get(optionName);
		}
		else
			throw new LacksOfFeatures(optionName);
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
	
	public List<AbstractProcess> getProcess() {
		return process;
	}
	public void setProcess(List<AbstractProcess> process) {
		this.process = process;
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

	public List<AbstractPreProcess> getPreProcess() {
		return preProcess;
	}

	public void setPreProcess(List<AbstractPreProcess> preProcess) {
		this.preProcess = preProcess;
	}

	public List<AbstractPostProcess> getPostProcess() {
		return postProcess;
	}


	public void setPostProcess(List<AbstractPostProcess> postProcess) {
		this.postProcess = postProcess;
	}


	public List<MultiCorpus> getMultiCorpusModels() {
		return multiCorpusModels;
	}

	public void setCorpusModels(List<MultiCorpus> multiCorpusModels) {
		this.multiCorpusModels = multiCorpusModels;
	}

	public void setCurrentMultiCorpus(MultiCorpus currentMultiCorpus) {
		this.currentMultiCorpus = currentMultiCorpus;
		for (AbstractPreProcess pre : preProcess)
			pre.setCurrentMultiCorpus(currentMultiCorpus);
		for (AbstractProcess p : process)
			p.setCurrentMultiCorpus(currentMultiCorpus);
		for (AbstractPostProcess post : postProcess)
			post.setCurrentMultiCorpus(currentMultiCorpus);
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
}
