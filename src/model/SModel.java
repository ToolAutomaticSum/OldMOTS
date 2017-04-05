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
	//protected String inputPath;
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
	 * Map des noms des AbstractMethod (Process, PreProcess, PostProcess, ScoringMethod, SummarizeMethod) (utiliser .class à la place ?) et de leurs IDs.
	 */
	protected Map<String,Integer> processIDs = new HashMap<String, Integer>();
	/**
	 * Liste des options associés à chaque AbstractMethod, une Map par AbstractMethod (Map sous la forme String (nom de l'option) String (valeur sous forme de String))
	 */
	protected List<Map<String, String>> processOption = new ArrayList<Map<String, String>>();
	//protected List<String> docNames;
	/**
	 * Liste des MultiCorpus sur lesquels appliqués les AbstractProcess
	 */
	protected List<MultiCorpus> multiCorpusModels = new ArrayList<MultiCorpus>();
	/**
	 * MultiCorpus en cours de résumé
	 */
	protected MultiCorpus currentMultiCorpus;
	//protected List<String> summary = new ArrayList<String>();
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
				//p.setCurrentProcess(this);
				p.init();
			}
			
			//dictionnary.clear();
			loadMultiCorpusModels();
			
			Iterator<MultiCorpus> multiCorpusIt = multiCorpusModels.iterator();
			while (multiCorpusIt.hasNext()) {
				currentMultiCorpus = multiCorpusIt.next();
				preProIt = preProcess.iterator();
				while (preProIt.hasNext()) {
					AbstractPreProcess p = preProIt.next();
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
					AbstractProcess p = proIt.next();
					p.setModel(this);
					p.initCorpusToCompress();
					p.initADN();
					for (int i : p.getListCorpusId()) {
						p.setSummarizeIndex(i);
						p.init();
						p.process();
						p.finish();
						setChanged();
						
						if (p.getSummary().get(currentMultiCorpus.getiD()) != null) {
							List<SentenceModel> summary = p.getSummary().get(currentMultiCorpus.getiD()).get(p.getSummarizeCorpusId());
							//Collections.sort(summary);
							notifyObservers(SentenceModel.listSentenceModelToString(summary));
						}
					}
				}
			}
			if (bRougeEvaluation) {
				evalRouge.setModel(this);
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
	
	public void loadMultiCorpusModels() {
		Iterator<MultiCorpus> multiCorpusIt = multiCorpusModels.iterator();
		while (multiCorpusIt.hasNext()) {
			Iterator<Corpus> corpusIt = multiCorpusIt.next().iterator();
			while (corpusIt.hasNext()) {
				corpusIt.next().loadDocumentModels();
			}
		}
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

	/*public Map<String, WordEmbeddings> getDictionnary() {
		return dictionnary;
	}

	public void setDictionnary(Map<String, WordEmbeddings> dictionnary) {
		this.dictionnary = dictionnary;
	}*/

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
		//inputPath = "";
		outputPath = "";
		
		process.clear();
		preProcess.clear();
		processIDs.clear();
		processOption.clear();
		multiCorpusModels.clear();
		//dictionnary.clear();
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

	/*public String getInputPath() {
		return inputPath;
	}

	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}*/

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	/*public List<String> getDocNames() {
		return docNames;
	}

	public void setDocNames(List<String> docNames) {
		this.docNames = docNames;
	}*/

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

	public MultiCorpus getCurrentMultiCorpus() {
		return currentMultiCorpus;
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
}
