package main.java.liasd.asadera.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import main.java.liasd.asadera.control.AbstractController;
import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.launcher.MOTS;
import main.java.liasd.asadera.model.task.postProcess.AbstractPostProcess;
import main.java.liasd.asadera.model.task.postProcess.EvaluationROUGE;
import main.java.liasd.asadera.model.task.preProcess.AbstractPreProcess;
import main.java.liasd.asadera.model.task.process.AbstractProcess;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.MultiCorpus;
import main.java.liasd.asadera.tools.wordFilters.WordFilter;

/**
 * Represent an automatic summarizatin system split in 3 steps :
 * - preProcess : list of preProcessing computation
 * - process : list of processing computation for summary's generation
 * - postProcess : list of postProcess
 * @author valnyz
 *
 */
public abstract class AbstractModel extends Observable {

	private AbstractController ctrl;

	private int taskID;
	private String name;

	private String language;

	private String outputPath;

	/**
	 * List of AbstractPreProcess for documents preProcessing.
	 */
	private List<AbstractPreProcess> preProcess = new ArrayList<AbstractPreProcess>();
	/**
	 * List of AbstractProcess for documents summarization
	 */
	private List<AbstractProcess> process = new ArrayList<AbstractProcess>();
	/**
	 * List of AbstractPostProcess for summary postProcessing.
	 */
	private List<AbstractPostProcess> postProcess = new ArrayList<AbstractPostProcess>();
	/**
	 * Map of AbstractMethod class ({@link AbstractProcess}, {@link AbstractPreProcess},
	 * {@link AbstractPostProcess} and their ids
	 * Use for passing option (parameter) from file to object
	 */
	private Map<String, Integer> processIDs = new HashMap<String, Integer>();

	/**
	 * List of Map of ids (from @processIDs)
	 */
	private List<Map<String, String>> processOption = new ArrayList<Map<String, String>>();
	
	/**
	 * Filter dertimining which words are to be considering as Stopwords
	 */
	private WordFilter filter;

	/**
	 * List of MultiCorpus
	 */
	private List<MultiCorpus> multiCorpusModels = new ArrayList<MultiCorpus>();

	protected MultiCorpus currentMultiCorpus;

	private boolean bMultiThreading = false;

	private boolean bRougeEvaluation = false;
	
	private boolean isVerbose = false;

	private EvaluationROUGE evalRouge;

	/**
	 * Launch the task whatever it is. Override by {@link SummarizeModel}, {@link ComparativeModel} and {@link LearningModel}
	 * @throws Exception 
	 */
	public abstract void run() throws Exception;

	/**
	 * Load Multicorpus files name into memory
	 * @see Corpus#loadDocumentModels()
	 */
	public void loadMultiCorpusModels() {
		for (MultiCorpus multiC : getMultiCorpusModels())
			for (Corpus c : multiC)
				c.loadDocumentModels();
	}

	/**
	 * Notify Observers that the Observable has changed.
	 * @see Observable#setChanged()
	 */
	public void setModelChanged() {
		setChanged();
	}

	/**
	 * Get the Controller from MVC pattern.
	 * @return {@link AbstractController}
	 */
	public AbstractController getCtrl() {
		return ctrl;
	}

	/**
	 * /**
	 * Set the Controller from MVC pattern.
	 * @param {@link AbstractController} ctrl
	 */
	public void setCtrl(AbstractController ctrl) {
		this.ctrl = ctrl;
	}

	/**
	 * Get input language
	 * @return String language, language of input texts 
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Set input language
	 * @param String language, language of input texts
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	public int getTaskID() {
		return taskID;
	}

	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	/**
	 * Get task's name. Set as Multicorpus config file name as default in {@link MOTS}
	 * @return String name, task's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set task's name.
	 * @param String name, task's name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get output path for saving preprocessed input text and results
	 * Set in config file by default.
	 * @return String ouputPath
	 */
	public String getOutputPath() {
		return outputPath;
	}

	/**
	 * Set output path for saving preprocessed input text and results
	 * Set in config file by default.
	 * @param outputPath
	 */
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	/**
	 * Get value of a process parameter (option) as a String. Be sure to know the type of the parameter when using this.
	 * @param int processId, id of parameter's process
	 * @param String optionName, parameter's name as write in config file
	 * @return String value, the parameter's value, may need a cast.
	 * @throws LacksOfFeatures if the parameter's name is not in the parameter's map of the process. i.e. has not be seen in config file.
	 */
	public String getProcessOption(int processId, String optionName) throws LacksOfFeatures {
		if (processOption.get(processId) != null && !processOption.get(processId).isEmpty()
				&& processOption.get(processId).containsKey(optionName)) {
			return processOption.get(processId).get(optionName);
		} else
			throw new LacksOfFeatures(optionName + " wasn't found for the process. Be sure that it is spelled right.");
	}

	/**
	 * Set the process option lists.
	 * You shouldn't use this method unless you override the {@link AbstractController#notifyProcessOptionChanged(Map)}
	 * @param String processOption
	 */
	public void setProcessOption(List<Map<String, String>> processOption) {
		this.processOption = processOption;
	}

	/**
	 * Get the AbstractProcess's list. i.e. Comparative/Learning/Summarize 
	 * @return List<AbstractProcess>
	 * @see AbstractProcess
	 */
	public List<AbstractProcess> getProcess() {
		return process;
	}

	public WordFilter getFilter() {
		return filter;
	}

	public void setFilter(WordFilter filter) {
		this.filter = filter;
	}

	/**
	 * Specify if the tool must be used with multithreading.
	 * Multithreading is implemented as one thread per corpus from the multicorpus.
	 * If your multicorpus has only one corpus, it is useless.
	 * @param boolean multiThreading
	 */
	public void setMultiThreading(boolean multiThreading) {
		this.bMultiThreading = multiThreading;
	}

	/**
	 * Test if it is request to perform the ROUGE evaluation on generated summary.
	 * @return
	 */
	public boolean isRougeEvaluation() {
		return bRougeEvaluation;
	}

	/**
	 * 
	 * @param bRougeEvaluation
	 */
	public void setRougeEvaluation(boolean bRougeEvaluation) {
		this.bRougeEvaluation = bRougeEvaluation;
	}

	public List<AbstractPreProcess> getPreProcess() {
		return preProcess;
	}

	public List<AbstractPostProcess> getPostProcess() {
		return postProcess;
	}

	public List<MultiCorpus> getMultiCorpusModels() {
		return multiCorpusModels;
	}
	
	/**
	 * Get EvaluationROUGE's instance to evaluate generated summaries
	 * @return {@link EvaluationROUGE} evalRouge
	 * @see EvaluationROUGE
	 */
	public EvaluationROUGE getEvalRouge() {
		return evalRouge;
	}

	/** Set EvaluationROUGE's instance to evaluate generated summaries
	 * 
	 * @param {@link EvaluationROUGE} evalRouge
	 * @see EvaluationROUGE
	 */
	public void setEvalRouge(EvaluationROUGE evalRouge) {
		this.evalRouge = evalRouge;
	}

	/**
	 * Test if the tool must be used with multithreading.
	 * Multithreading is implemented as one thread per {@link Corpus} from the {@link Multicorpus}.
	 * If your multicorpus has only one corpus, it is useless.
	 * @param boolean multiThreading
	 */
	public boolean isMultiThreading() {
		return bMultiThreading;
	}

	public boolean isVerbose() {
		return isVerbose;
	}

	public void setVerbose(boolean isVerbose) {
		this.isVerbose = isVerbose;
	}

	/** Clear instance's attribute.
	 * Useful especially for genetic algorithm.
	 */
	public void clear() {
		taskID = -1;
		language = "";
		outputPath = "";

		process.clear();
		preProcess.clear();
		processOption.clear();
		multiCorpusModels.clear();
	}
}
