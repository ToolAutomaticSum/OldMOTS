package liasd.asadera.control;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import liasd.asadera.model.AbstractModel;
import liasd.asadera.model.task.postProcess.AbstractPostProcess;
import liasd.asadera.model.task.postProcess.EvaluationROUGE;
import liasd.asadera.model.task.preProcess.AbstractPreProcess;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.MultiCorpus;
import liasd.asadera.view.AbstractView;

public abstract class AbstractController {

	private final AbstractModel model;
    private final AbstractView view;
    
    private static final Logger logger = Logger.getLogger("AbstractController"); 

	//private int taskID;
	protected static int processID = 0;
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
	//protected boolean summarize;
	/*protected boolean bRougeEvaluation = false;
    protected String modelRoot;
    protected String peerRoot;*/
    
    public AbstractController(AbstractModel model, AbstractView view) {
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

	protected Object dynamicConstructor(String className) {
		Class<?> cl;
		try {
			cl = Class.forName("liasd.asadera.model.task." + className);
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
    
    public void notifyMultiThreadBoolChanged(boolean multithread) {
    	System.out.println("Multithread " + multithread);
    	getModel().setMultiThreading(multithread);
    }
    
    public void notifyMultiCorpusChanged() {
    	currentMultiCorpus = new MultiCorpus(getModel().getMultiCorpusModels().size());
    	getModel().getMultiCorpusModels().add(currentMultiCorpus);
    }
    
    public void notifyCorpusChanged(String summaryInputPath, List<String> summaryNames, String inputCorpusPath, List<String> docNames) {
    	Set<String> set_docNames = new TreeSet<String>();
    	Set<String> set_summaryNames = new TreeSet<String>();
    	
    	File f = new File(inputCorpusPath);
    	List<String> lf = Arrays.asList(f.list());
    	for (String doc : docNames) {
    		Pattern pattern = Pattern.compile(doc);
    		set_docNames.addAll(lf.stream().filter(pattern.asPredicate()).collect(Collectors.toSet()));
    	}
    	docNames.clear();
    	docNames.addAll(set_docNames);
    	
    	Corpus corpus = new Corpus(currentMultiCorpus.size());
    	
    	
    	f = new File(summaryInputPath);
    	lf = Arrays.asList(f.list());
    	for (String doc : summaryNames) {
    		Pattern pattern = Pattern.compile(doc);
    		set_summaryNames.addAll(lf.stream().filter(pattern.asPredicate()).collect(Collectors.toSet()));
    	}
    	summaryNames.clear();
    	summaryNames.addAll(set_summaryNames);
    	
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
    
    public abstract void notifyProcessChanged(String processName);
    
    public void notifyProcessOptionChanged(Map<String, String> processOption) {
    	this.processOption.add(processOption);
		getModel().setProcessOption(this.processOption);
    }

    public abstract void notifyIndexBuilderChanged(String processName, String indexBuilder);

    public abstract void notifyCaracteristicBuilderChanged(String processName, String caracteristicBuilder);
    
    public abstract void notifyScoringMethodChanged(String processName, String scoringMethod);
    
    public abstract void notifySelectionMethodChanged(String processName, String summarizeMethod);
    
    public void notifyPostProcessChanged(String processName, String postProcessName) {
    	Object o = dynamicConstructor("postProcess." + postProcessName);
    	getModel().getPostProcess().add((AbstractPostProcess) o);
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

	public AbstractModel getModel() {
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
