package model.task.process.indexBuilder.LDA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import exception.LacksOfFeatures;
import jgibblda.Estimator;
import jgibblda.LDACmdOption;
import jgibblda.Model;
import model.task.preProcess.GenerateTextModel;
import model.task.process.indexBuilder.AbstractIndexBuilder;
import model.task.process.indexBuilder.IndexBasedIn;
import model.task.process.indexBuilder.LearningModelBuilder;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import optimize.parameter.Parameter;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordVector;

public class LDA extends AbstractIndexBuilder<WordVector> implements LearningModelBuilder {

	private Model newModel;	
	private String modelName;
	//private int nbSentence;
	protected double[] theta;
	private int K;
	
	public static enum InferenceLDA_Parameter {
		K("K"),
		alpha("Alpha"),
		beta("Beta");

		private String name;

		private InferenceLDA_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	
	public LDA(int id) throws SupportADNException {
		super(id);
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("K", Integer.class);
		supportADN.put("Alpha", Double.class);
		supportADN.put("Beta", Double.class);
	}
	
	@Override
	public AbstractIndexBuilder<WordVector> makeCopy() throws Exception {
		LDA p = new LDA(id);
		initCopy(p);
		return p;
	}
	
	@Override
	public void initADN() throws Exception {
		int tempK = Integer.parseInt(getModel().getProcessOption(id, InferenceLDA_Parameter.K.getName()));
		getCurrentProcess().getADN().putParameter(new Parameter<Integer>(InferenceLDA_Parameter.K.getName(), tempK));
		getCurrentProcess().getADN().getParameter(Integer.class, InferenceLDA_Parameter.K.getName()).setMaxValue(4*tempK);
		getCurrentProcess().getADN().getParameter(Integer.class, InferenceLDA_Parameter.K.getName()).setMinValue(2);	
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(InferenceLDA_Parameter.alpha.getName(), Double.parseDouble(getModel().getProcessOption(id, InferenceLDA_Parameter.alpha.getName()))));
		getCurrentProcess().getADN().getParameter(Double.class, InferenceLDA_Parameter.alpha.getName()).setMaxValue(1.0);
		getCurrentProcess().getADN().getParameter(Double.class, InferenceLDA_Parameter.alpha.getName()).setMinValue(0.01);	
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(InferenceLDA_Parameter.beta.getName(), Double.parseDouble(getModel().getProcessOption(id, InferenceLDA_Parameter.beta.getName()))));
		getCurrentProcess().getADN().getParameter(Double.class, InferenceLDA_Parameter.beta.getName()).setMaxValue(1.0);
		getCurrentProcess().getADN().getParameter(Double.class, InferenceLDA_Parameter.beta.getName()).setMinValue(0.01);
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		LDACmdOption option = new LDACmdOption();
		option.est = false;
		option.estc = false;
		option.inf = true;
		option.dir = getModel().getOutputPath()  + File.separator + "modelLDA";
		option.niters = 400;
		option.twords = 20;
		option.K = getCurrentProcess().getADN().getParameterValue(Integer.class, InferenceLDA_Parameter.K.getName());
		option.alpha = getCurrentProcess().getADN().getParameterValue(Double.class, InferenceLDA_Parameter.alpha.getName()); //Double.parseDouble(getModel().getProcessOption(id, "Alpha"));
		option.beta = getCurrentProcess().getADN().getParameterValue(Double.class, InferenceLDA_Parameter.beta.getName()); //Double.parseDouble(getModel().getProcessOption(id, "Beta"));
		modelName = "LDA_model_"+option.K+"_"+option.alpha+"_"+option.beta;
		option.modelName = modelName;
		option.dfile = "tempCorpus" + getCurrentProcess().getCorpusToSummarize().getiD() + ".gz";
		
		List<Corpus> listAllCorpus = new ArrayList<Corpus>();
		for (Corpus c : getCurrentMultiCorpus())
			listAllCorpus.add(c);
		newModel = LDA.ldaModelLearning(modelName, "-all", listAllCorpus, readStopWords, option.K,  option.alpha, option.beta, getModel().getOutputPath());//inferencer.inference(false);

		//nbSentence = 0;
		K = newModel.K;

		theta = new double[K];
		for (int k = 0; k<K; k++) {
			for (int m = 0; m<newModel.M; m++) {
				theta[k] += newModel.theta[m][k];
			}
			theta[k]/=newModel.M;
		}
		generateIndex(listCorpus);
	}
	
	public final void learn(List<Corpus> listCorpus, String modelName) throws Exception {
		int K;
		double alpha;
		double beta;
		try {
			K = Integer.parseInt(getModel().getProcessOption(id, InferenceLDA_Parameter.K.getName()));
			alpha = Double.parseDouble(getModel().getProcessOption(id, InferenceLDA_Parameter.alpha.getName()));
			beta = Double.parseDouble(getModel().getProcessOption(id, InferenceLDA_Parameter.beta.getName()));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (LacksOfFeatures e) {
			e.printStackTrace();
		} finally {
			K = 10;
			alpha = 50/K;
			beta = 0.01;
		}
		
		this.modelName = modelName + K+"_"+alpha+"_"+beta;
		
		ldaModelLearning(this.modelName, "Learning", listCorpus, readStopWords, K, alpha, beta, getCurrentProcess().getModel().getOutputPath());
	}
	
	@Override
	public void liveLearn(List<String> listStringSentence, String modelName) throws Exception {
		int K;
		double alpha;
		double beta;
		try {
			K = Integer.parseInt(getModel().getProcessOption(id, InferenceLDA_Parameter.K.getName()));
			alpha = Double.parseDouble(getModel().getProcessOption(id, InferenceLDA_Parameter.alpha.getName()));
			beta = Double.parseDouble(getModel().getProcessOption(id, InferenceLDA_Parameter.beta.getName()));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (LacksOfFeatures e) {
			e.printStackTrace();
		} finally {
			K = 10;
			alpha = 50/K;
			beta = 0.01;
		}
		
		this.modelName = modelName+K+"_"+alpha+"_"+beta;
		
		ldaModelLearning(this.modelName, modelName, listStringSentence, K, alpha, beta, getCurrentProcess().getModel().getOutputPath());
	}
	
	private void generateIndex(List<Corpus> listCorpus) {
		for (Corpus corpus : listCorpus) {
			//Construction du dictionnaire
			for (TextModel t : corpus) {
				for (SentenceModel s : t) {
					for (WordModel word : s) {
						if (readStopWords || !word.isStopWord()) {
							if(!index.containsKey(word.getmLemma())) {
								WordVector w = new WordVector(word.getmLemma(), index, K);
								index.put(word.getmLemma(), w/*, newModel.data.localDict.getID(word.getmLemma())*/);
							}
						}
					}
				}
			}
			
			//Ajout dans le dictionnaire des IDs et des Topic Distribution
			for (int w = 0; w < newModel.V; w++){ //Boucle sur le nombre de mot du vocabulaire local
				String word = newModel.data.localDict.getWord(w);
				if(index.containsKey(word)) {
					WordVector wLDA = index.get(word);
					//double temp = 0;
					for (int k = 0; k < newModel.K; k++){ //Boucle sur le nombre de Topic
						wLDA.getWordVector()[k] = newModel.phi[k][w]*theta[k];
					}
				}
			}
		}
	}

	public static void writeTempInputFile(String modelName, String corpusId, List<Corpus> listCorpus, boolean readStopWords, String outputPath) throws IOException {
		new File(outputPath + File.separator + "modelLDA").mkdir();

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(
                    new FileOutputStream(outputPath + File.separator + "modelLDA" + File.separator + "tempCorpus" + corpusId + ".gz")), "UTF-8"));
		int nbDoc = 0;
		for (Corpus c : listCorpus)
			nbDoc += c.getNbDocument();
		
		writer.write(String.valueOf(nbDoc) + "\n");
		for (Corpus c : listCorpus) {
			c = GenerateTextModel.readTempDocument(outputPath + File.separator + "temp", c, readStopWords);
			for (TextModel t : c) {
				for (SentenceModel s : t) {
					for (WordModel word : s) {
						if (!word.isStopWord()) {
							writer.write(word.getmLemma() + " ");
						}
					}
				}
				writer.write("\n");
			}
		}
		writer.close();   
	}
	
	public static void writeTempInputFile(String modelName, String corpusId, List<String> listSentence, String outputPath) throws IOException {
		new File(outputPath + File.separator + "modelLDA").mkdir();

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(
                    new FileOutputStream(outputPath + File.separator + "modelLDA" + File.separator + "tempCorpus" + corpusId + ".gz")), "UTF-8"));
		
		writer.write(listSentence.size() + "\n");
		for (String sentence : listSentence)
			writer.write(sentence + "\n");
		writer.close();   
	}
	
	public synchronized static Model ldaModelLearning(String modelName, String corpusId, List<Corpus> listCorpus, boolean readStopWords, int K, double alpha, double beta, String outputPath) throws IOException {
		LDACmdOption option = new LDACmdOption();
		Estimator estimator;

		option.inf = false;
		option.K = K;
		option.alpha = alpha;
		option.beta = beta;
		option.niters = 500;
		option.twords = 15;
		option.dir = outputPath + File.separator + "modelLDA";
		option.dfile = "tempCorpus" + corpusId + ".gz";
		
		option.modelName = modelName;//"LDA_model_"+option.K+"_"+option.alpha+"_"+option.beta;

		option.est = true;
		option.estc = false;
		if (new File(outputPath + File.separator + "modelLDA" + File.separator + modelName + ".wordmap.gz").exists()) {
			System.out.println("Model already exist, don't need to relearn it !");
			Model trnModel = new Model(option);
	        trnModel.loadCompleteModel();
			return trnModel;
		}
		
		if (!new File("tempCorpus" + corpusId + ".gz").exists())
			writeTempInputFile(modelName, corpusId, listCorpus, readStopWords, outputPath);
		
		estimator = new Estimator(option);
		return estimator.estimate(true);
	}
	
	public synchronized static Model ldaModelLearning(String modelName, String fileName, List<String> listSentence, int K, double alpha, double beta, String outputPath) throws IOException {
		LDACmdOption option = new LDACmdOption();
		Estimator estimator;

		option.inf = false;
		option.K = K;
		option.alpha = alpha;
		option.beta = beta;
		option.niters = 500;
		option.twords = 15;
		option.dir = outputPath + File.separator + "modelLDA";
		option.dfile = fileName + ".gz";
		
		option.modelName = modelName;//"LDA_model_"+option.K+"_"+option.alpha+"_"+option.beta;

		option.est = true;
		option.estc = false;
		/*if (new File(outputPath + File.separator + "modelLDA" + File.separator + modelName + ".wordmap.gz").exists()) {
			System.out.println("Model already exist, don't need to relearn it !");
			Model trnModel = new Model(option);
	        trnModel.loadCompleteModel();
			return trnModel;
		}*/
		
		if (!new File(fileName + ".gz").exists())
			writeTempInputFile(modelName, fileName, listSentence, outputPath);
		
		estimator = new Estimator(option);
		return estimator.estimate(true);
	}
	
	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(WordVector.class, Index.class, IndexBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((IndexBasedIn<WordVector>)compMethod).setIndex(index);
	}
}
