package control;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.Model;
import model.task.postProcess.AbstractPostProcess;
import model.task.postProcess.EvaluationROUGE;
import model.task.preProcess.AbstractPreProcess;
import model.task.process.AbstractProcess;
import model.task.process.scoringMethod.AbstractScoringMethod;
import model.task.process.summarizeMethod.AbstractSummarizeMethod;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.MultiCorpus;
import view.AbstractView;

public class Controller {

	private final Model model;
    private final AbstractView view;

	//private int taskID;
	private int processID = 0;
	protected String language;
	protected String inputDir;
	protected List<Corpus> corpusList = new ArrayList<Corpus>();
	protected String outputDir;
	protected List<String> preProcess = new ArrayList<String>();
	protected String processName;
	protected List<Map<String, String>> processOption = new ArrayList<Map<String, String>>();
	protected List<String> postProcess = new ArrayList<String>();
	protected EvaluationROUGE evalRouge;
	protected MultiCorpus currentMultiCorpus;
	/*protected boolean bRougeEvaluation = false;
    protected String modelRoot;
    protected String peerRoot;*/
    
    public Controller(Model model, AbstractView view) {
        this.model = model;
        this.view = view;
        model.setCtrl(this);
        view.setCtrl(this);
        
        model.addObserver(view);
    }
    
    public void displayView(){
    	view.display();
	}
 
	public void closeView(){
		view.close();
	}
    
	public void run() {
		model.run();
	}

	private  Object dynamicConstructor(String className) {
		Class<?> cl;
		try {
			cl = Class.forName("model.task." + className);
		    //Class types = new Class{Integer.class};
		    Constructor<?> ct = cl.getConstructor(int.class);
		    Object o = ct.newInstance(processID);
		    processID++;
		    return o;
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	}
    
    public void notifyTaskChanged(int taskID) {
    	if (getModel().getTaskID() != taskID) {
    		processID = 0;
    		corpusList.clear();
    		processName = null;
    		processOption.clear();
    		preProcess.clear();
    		postProcess.clear();
    		getModel().clear();
        	getModel().setTaskID(taskID);
    	}
    }
    
    public void notifyLanguageChanged(String language) {
    	this.language = language;
    	getModel().setLanguage(language);
    }
    
    /*public void notifyInputPathChanged(String inputDir) {
    	this.inputDir = inputDir;
    	getModel().setInputPath(inputDir);
    }*/
    
    public void notifyMultiCorpusChanged() {
    	currentMultiCorpus = new MultiCorpus(getModel().getMultiCorpusModels().size());
    	getModel().getMultiCorpusModels().add(currentMultiCorpus);
    }
    
    public void notifyCorpusChanged(String summaryInputPath, List<String> summaryNames, String inputCorpusPath, List<String> docNames) {
    	int i = 0;
    	boolean notAll  = true;
    	while (notAll && i < docNames.size()) {
    		if (docNames.get(i).equals("ALL")) {
    			notAll = false;
    		}
    		i++;	
    	}
    	//if (!one_Summary_Per_Doc) {
	    	Corpus corpus = new Corpus(currentMultiCorpus.size());
	    	//this.docNames = docNames;
	    	if (!notAll) { /** Si tous les documents txt du dossier */
	    		docNames.clear();
	    		File f = new File(inputCorpusPath);
	        	File[] lf = f.listFiles();
	        	for (int j = 0; j<lf.length; j++) {
	        		//if (Tools.getFileExtension(lf[j]).equals("txt"))
	        		docNames.add(lf[j].getName());
	        	}
	    	}
	    	corpus.setModel(model);
	    	corpus.setDocNames(docNames);
	    	corpus.setInputPath(inputCorpusPath);
	    	corpus.setSummaryNames(summaryNames);
	    	corpus.setSummaryPath(summaryInputPath);
    	    currentMultiCorpus.add(corpus);
    }

    public void notifyOutputPathChanged(String outputDir) {
    	this.outputDir = outputDir;
    	getModel().setOutputPath(outputDir);
    }
    
    public void notifyPreProcessChanged(String preProcessName) {
    	Object o = dynamicConstructor("preProcess." + preProcessName);
    	getModel().getPreProcess().add((AbstractPreProcess) o);
    }
    
    public void notifyProcessChanged(String processName) {
    	getModel().getProcessIDs().put(processName, processID);
    	Object o = dynamicConstructor("process." + processName);
		getModel().getProcess().add((AbstractProcess) o);
    }
    
    public void notifyProcessOptionChanged(Map<String, String> processOption) {
    	this.processOption.add(processOption);
		getModel().setProcessOption(this.processOption);
    }
    
    public void notifyScoringMethodChanged(String processName, String scoringMethod) {
    	Object o = dynamicConstructor("process.scoringMethod." + scoringMethod);
    	getModel().getProcessByID(getModel().getProcessIDs().get(processName)).setScoringMethod((AbstractScoringMethod)o);
    }
    
    public void notifySummarizeMethodChanged(String processName, String summarizeMethod) {
    	Object o = dynamicConstructor("process.summarizeMethod." + summarizeMethod);
    	getModel().getProcessByID(getModel().getProcessIDs().get(processName)).setSentenceSelection((AbstractSummarizeMethod)o);
    }
    
    public void notifyPostProcessChanged(String processName, String postProcessName) {
    	Object o = dynamicConstructor("postProcess." + postProcessName);
    	getModel().getProcessByID(getModel().getProcessIDs().get(processName)).getPostProcess().add((AbstractPostProcess) o);
    }
    
    public void notifyRougeEvaluationChanged(boolean bRougeEvaluation) throws SupportADNException {
    	if(bRougeEvaluation)
    		evalRouge = new EvaluationROUGE(incrementProcessID());
    	else
    		evalRouge = null;
    	getModel().setEvalRouge(evalRouge);
    	getModel().setbRougeEvaluation(bRougeEvaluation);
    }
    
    public void notifyRougeMeasureChanged(String rougeMeasure) {
    	List<String> listRougeMeasure = new ArrayList<String>();
    	for (String s : rougeMeasure.split("\t")) {
    		listRougeMeasure.add(s);
		}
    	evalRouge.setRougeMeasure(listRougeMeasure);
    }
    
    public void notifyRougePathChanged(String rougePath) {
    	evalRouge.setRougePath(rougePath);
    }
    
    public void notifyModelRootChanged(String modelRoot) {
    	evalRouge.setModelRoot(modelRoot);
    }
    
    public void notifyPeerRootChanged(String peerRoot) {
    	evalRouge.setPeerRoot(peerRoot);
    }
    
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getInputDir() {
		return inputDir;
	}

	public void setInputDir(String inputDir) {
		this.inputDir = inputDir;
	}

	public List<String> getPreProcess() {
		return preProcess;
	}

	public void setPreProcess(List<String> preProcess) {
		this.preProcess = preProcess;
	}

	public String getProcess() {
		return processName;
	}

	public void setProcess(String process) {
		this.processName = process;
	}

	public List<Map<String, String>> getProcessOption() {
		return processOption;
	}

	public void setProcessOption(List<Map<String, String>> processOption) {
		this.processOption = processOption;
	}

	public List<String> getPostProcess() {
		return postProcess;
	}

	public void setPostProcess(List<String> postProcess) {
		this.postProcess = postProcess;
	}

	public Model getModel() {
		return model;
	}

	public AbstractView getView() {
		return view;
	}
	
	public int incrementProcessID() {
		return processID++;
	}
	
	public int decrementProcessID() {
		return processID--;
	}
}
