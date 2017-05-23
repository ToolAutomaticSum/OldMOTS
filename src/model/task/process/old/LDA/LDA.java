package model.task.process.old.LDA;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.VectorDimensionException;
import jgibblda.LDACmdOption;
import jgibblda.Model;
import model.task.process.AbstractProcess;
import model.task.process.old.VectorCaracteristicBasedOut;
import optimize.parameter.Parameter;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.WordVector;

public class LDA extends AbstractProcess implements VectorCaracteristicBasedOut {

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
	
	protected double[] averageVector;
	//Vecteur de la phrases obtenue par moyenne des vecteurs des mots de la phrases
	protected double[][] matSentenceTopic; //Sentence/Topic
	
	//private Inferencer inferencer;
	private Model newModel;
	//private Model trnModel;
	
	private String modelName;
	private int nbSentence;
	private double[] theta;
	private int K;
	
	private Map<SentenceModel, double[]> sentenceCaracteristic;
	
	public LDA(int id) throws Exception {
		super(id);
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("K", Integer.class);
		supportADN.put("Alpha", Double.class);
		supportADN.put("Beta", Double.class);
	}
	
	@Override
	public AbstractProcess makeCopy() throws Exception {
		LDA p = new LDA(id);
		initCopy(p);
		return p;
	}
	
	@Override
	public void initADN() throws Exception {
		super.initADN();
		int tempK = Integer.parseInt(getModel().getProcessOption(id, InferenceLDA_Parameter.K.getName()));
		adn.putParameter(new Parameter<Integer>(InferenceLDA_Parameter.K.getName(), tempK));
		adn.getParameter(Integer.class, InferenceLDA_Parameter.K.getName()).setMaxValue(4*tempK);
		adn.getParameter(Integer.class, InferenceLDA_Parameter.K.getName()).setMinValue(2);	
		adn.putParameter(new Parameter<Double>(InferenceLDA_Parameter.alpha.getName(), Double.parseDouble(getModel().getProcessOption(id, InferenceLDA_Parameter.alpha.getName()))));
		adn.getParameter(Double.class, InferenceLDA_Parameter.alpha.getName()).setMaxValue(1.0);
		adn.getParameter(Double.class, InferenceLDA_Parameter.alpha.getName()).setMinValue(0.01);	
		adn.putParameter(new Parameter<Double>(InferenceLDA_Parameter.beta.getName(), Double.parseDouble(getModel().getProcessOption(id, InferenceLDA_Parameter.beta.getName()))));
		adn.getParameter(Double.class, InferenceLDA_Parameter.beta.getName()).setMaxValue(1.0);
		adn.getParameter(Double.class, InferenceLDA_Parameter.beta.getName()).setMinValue(0.01);
	
	}
	
	/**
	 * Calcul des topics sur le/les documents à résumer
	 */
	@Override
	public void init() throws Exception {
		super.init();
		
		LDACmdOption option = new LDACmdOption();
		option.est = false;
		option.estc = false;
		option.inf = true;
		option.dir = getModel().getOutputPath()  + File.separator + "modelLDA";
		option.niters = 400;
		option.twords = 20;
		option.K = adn.getParameterValue(Integer.class, InferenceLDA_Parameter.K.getName());
		option.alpha = adn.getParameterValue(Double.class, InferenceLDA_Parameter.alpha.getName()); //Double.parseDouble(getModel().getProcessOption(id, "Alpha"));
		option.beta = adn.getParameterValue(Double.class, InferenceLDA_Parameter.beta.getName()); //Double.parseDouble(getModel().getProcessOption(id, "Beta"));
		modelName = "LDA_model_"+option.K+"_"+option.alpha+"_"+option.beta;
		option.modelName = modelName;
		option.dfile = "tempCorpus" + corpusToSummarize.getiD() + ".gz";
		
		List<Corpus> listCorpus = new ArrayList<Corpus>();
		for (Corpus c : getCurrentMultiCorpus())
				listCorpus.add(c);
		newModel = LearningLDA.ldaModelLearning(modelName, "-all", listCorpus, readStopWords, option.K,  option.alpha, option.beta, getModel().getOutputPath());//inferencer.inference(false);
	}
	
	/**
	 * @throws Exception 
	 * génère le dictionnaire des mots avec leurs probabilités par topic
	 * et attribut un score à chaque phrase
	 */
	@Override
	public void process() throws Exception {

		System.gc();
		//File f = new File(getModel().getOutputPath() + File.separator + "modelLDA" + File.separator + "temp" + modelName + ".gz");
		//f.delete();		//Suppression de tempInputFile
		
		nbSentence = 0;
		K = newModel.K;

		theta = new double[K];
		for (int k = 0; k<K; k++) {
			for (int m = 0; m<newModel.M; m++) {
				theta[k] += newModel.theta[m][k];
			}
			theta[k]/=newModel.M;
		}
		generateDictionary();
		
		sentenceCaracteristic = new HashMap<SentenceModel, double[]>();
		
		generateMatSentenceTopic();
		
		super.process(); //Appel à scoringMethod puis summarizeMethod via AbstractProcess
	}
	
	/**
	 * 
	 */
	@Override
	public void finish() throws Exception {
		super.finish();
	}
	
	private void generateDictionary() {
		//int n = 0; //nbWords;
		//Construction du dictionnaire
		for (TextModel t : corpusToSummarize) {
			for (SentenceModel s : t) {
				for (WordModel word : s) {
					if (!word.isStopWord()) {
						if(!index.containsKey(word.getmLemma())) {
							WordVector w = new WordVector(word.getmLemma(), index, K);
							index.put(word.getmLemma(), w, newModel.data.localDict.getID(word.getmLemma()));
						}
						//index.get(word.getmLemma()).add(word); //Ajout au wordIndex des WordModel correspondant
					}
				}
			}
		}
		
		//Ajout dans le dictionnaire des IDs et des Topic Distribution
		for (int w = 0; w < newModel.V; w++){ //Boucle sur le nombre de mot du vocabulaire local
			String word = newModel.data.localDict.getWord(w);
			WordVector wLDA;
			if(index.containsKey(word)) {
				wLDA = (WordVector) index.get(w);
				//double temp = 0;
				for (int k = 0; k < newModel.K; k++){ //Boucle sur le nombre de Topic
					//System.out.println(k + "\t" + theta[k] + "\t" + newModel.phi[k][w] + "\t" + wLDA.size() + "\t" + n);
					wLDA.getWordVector()[k] = newModel.phi[k][w];
					//temp+=wLDA.getTopicDistribution()[k];
				}
				//for (int k = 0; k < newModel.K; k++)
				//	wLDA.getTopicDistribution()[k]/=temp;
			}
		}
	}

	/**
	 * Ajout des caractéristiques de la phrase comme étant la moyenne des vecteurs de chaque mot la composant
	 * @throws VectorDimensionException
	 */
	private void generateMatSentenceTopic() throws VectorDimensionException {
		nbSentence = corpusToSummarize.getNbSentence();
		matSentenceTopic = new double[nbSentence][K];
		
		int i = 0; //sentence variable
		int l = 1; //optional parameter to configure handicap for long sentences

		for (TextModel t : corpusToSummarize) {
			for (SentenceModel sentenceModel : t) {
				int n = 0; //length of the sentence
				//matSentenceTopic[i] = new double[K];
				for (WordModel word : sentenceModel) {
					if (!word.isStopWord()) {
						WordVector wordLDA = (WordVector) index.get(word.getmLemma());
						for (int k = 0; k<K; k++)
							matSentenceTopic[i][k] += wordLDA.getWordVector()[k];
						n++;
					}
				}
				for (int k = 0; k<K; k++) //topic loop
					matSentenceTopic[i][k] = matSentenceTopic[i][k]*theta[k]/(Math.pow(n,l));
				sentenceCaracteristic.put(sentenceModel,matSentenceTopic[i]);
				i++;
			}
		}
	}

	public void setTheta(double[] theta) {
		this.theta = theta;
	}

	public void setK(int k) {
		K = k;
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}
}
