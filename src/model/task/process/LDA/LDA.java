package model.task.process.LDA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import exception.VectorDimensionException;
import jgibblda.LDACmdOption;
import jgibblda.Model;
import model.task.process.AbstractProcess;
import model.task.process.VectorCaracteristicBasedOut;
import optimize.parameter.Parameter;
import textModeling.Corpus;
import textModeling.MultiCorpus;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.LDA.WordLDA;

public class LDA extends AbstractProcess implements VectorCaracteristicBasedOut {
	
	static {
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("Alpha", Double.class);
		supportADN.put("Beta", Double.class);
	}

	public static enum InferenceLDA_Parameter {
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
	private Integer idMultiCorpusTrnModel = null;
	//private Model trnModel;
	
	private int nbSentence;
	private double[] theta;
	private int K;
	
	private Map<SentenceModel, double[]> sentenceCaracteristic;
	
	public LDA(int id) throws Exception {
		super(id);
	}
	
	/**
	 * Calcul des topics sur le/les documents à résumer
	 */
	@Override
	public void init() throws Exception {
		adn.putParameter(new Parameter<Double>(InferenceLDA_Parameter.alpha.getName(), Double.parseDouble(getModel().getProcessOption(id, InferenceLDA_Parameter.alpha.getName()))));
		adn.putParameter(new Parameter<Double>(InferenceLDA_Parameter.beta.getName(), Double.parseDouble(getModel().getProcessOption(id, InferenceLDA_Parameter.beta.getName()))));
	
		super.init();
		
		writeTempInputFile();
		LDACmdOption option = new LDACmdOption();
		option.est = false;
		option.estc = false;
		option.inf = true;
		option.dir = getModel().getOutputPath()  + File.separator + "modelLDA";
		option.dfile = "temp.txt.gz";
		option.niters = 100;
		option.twords = 20;
		K = (int) Math.round(Math.sqrt(getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()).getNbSentence()));
		option.K = K;
		option.alpha = adn.getParameterValue(Double.class, InferenceLDA_Parameter.alpha.getName()); //Double.parseDouble(getModel().getProcessOption(id, "Alpha"));
		option.beta = adn.getParameterValue(Double.class, InferenceLDA_Parameter.beta.getName()); //Double.parseDouble(getModel().getProcessOption(id, "Beta"));
		option.modelName = "LDA_model_"+option.alpha+"_"+option.beta;
		option.dir = "/home/valnyz/Documents" + File.separator + "modelLDA";
		option.dfile = "temp.txt.gz"; //TODO à changer
		
		MultiCorpus temp = new MultiCorpus(getModel().getCurrentMultiCorpus().getiD());
		temp.add(getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()));
		
		/*if (idMultiCorpusTrnModel == null || idMultiCorpusTrnModel != getModel().getCurrentMultiCorpus().getiD()) {
			//trnModel = LearningLDA.ldaModelLearning(option.K, option.alpha, option.beta, getModel().getOutputPath(), temp);
			//trnModel = LearningLDA.ldaModelLearning(option.K, option.alpha, option.beta, getModel().getOutputPath(), getModel().getCurrentMultiCorpus());
			idMultiCorpusTrnModel = getModel().getCurrentMultiCorpus().getiD();
		}*/
		//Inferencer inferencer = new Inferencer(option);
		
		newModel = LearningLDA.ldaModelLearning(option.K,  option.alpha, option.beta, getModel().getOutputPath(), temp);//inferencer.inference(false);
	}
	
	/**
	 * @throws Exception 
	 * génère le dictionnaire des mots avec leurs probabilités par topic
	 * et attribut un score à chaque phrase
	 */
	@Override
	public void process() throws Exception {

		System.gc();
		File f = new File(getModel().getOutputPath() + File.separator + "modelLDA" + File.separator + "temp.txt");
		f.delete();		//Suppression de tempInputFile
		
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
		Corpus corpus = getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId());
		Iterator<TextModel> textIt = corpus.iterator();
		while (textIt.hasNext()) {
			TextModel textModel = textIt.next();
			Iterator<ParagraphModel> paragraphIt = textModel.iterator();
			while (paragraphIt.hasNext()) {
				ParagraphModel paragraphModel = paragraphIt.next();
				Iterator<SentenceModel> sentenceIt = paragraphModel.iterator();
				while (sentenceIt.hasNext()) {
					SentenceModel sentenceModel = sentenceIt.next();
					Iterator<WordModel> wordIt = sentenceModel.iterator();
					while (wordIt.hasNext()) {
						WordModel word = wordIt.next();
						//TODO ajouter filtre à la place de getmLemma
						if (!word.isStopWord()) {
							if(!index.containsKey(word.getmLemma())) {
								WordLDA w = new WordLDA(word.getmLemma(), index, newModel.K, theta);
								index.put(word.getmLemma(), w, newModel.data.localDict.getID(word.getmLemma()));
							}
							index.get(word.getmLemma()).add(word); //Ajout au wordIndex des WordModel correspondant
						}
					}
				}
			}
		}
		
		//Ajout dans le dictionnaire des IDs et des Topic Distribution
		for (int w = 0; w < newModel.V; w++){ //Boucle sur le nombre de mot du vocabulaire local
			String word = newModel.data.localDict.getWord(w);
			WordLDA wLDA;
			if(index.containsKey(word)) {
				wLDA = (WordLDA) index.get(w);
				//double temp = 0;
				for (int k = 0; k < newModel.K; k++){ //Boucle sur le nombre de Topic
					//System.out.println(k + "\t" + theta[k] + "\t" + newModel.phi[k][w] + "\t" + wLDA.size() + "\t" + n);
					wLDA.getTopicDistribution()[k] = newModel.phi[k][w];
					//temp+=wLDA.getTopicDistribution()[k];
				}
				//for (int k = 0; k < newModel.K; k++)
				//	wLDA.getTopicDistribution()[k]/=temp;
			}
		}
	}
	
	private void writeTempInputFile() throws IOException {
		new File("/home/valnyz/Documents" + File.separator + "modelLDA").mkdir();
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(
                    new FileOutputStream("/home/valnyz/Documents" + File.separator + "modelLDA" + File.separator + "temp.txt.gz")), "UTF-8"));

		Iterator<TextModel> textIt = getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()).iterator();
		while (textIt.hasNext()) {
			Iterator<ParagraphModel> parIt = textIt.next().iterator();
			while (parIt.hasNext()) {
				Iterator<SentenceModel> senIt = parIt.next().iterator();
				while (senIt.hasNext()) {
					Iterator<WordModel> wordIt = senIt.next().iterator();
					while (wordIt.hasNext()) {
						String word = wordIt.next().toString();
						if (!word.isEmpty()) {
							writer.write(word + " ");
						}
					}
				}
			}
			writer.write("\n");
		}
		writer.close();   
	}

	/**
	 * Ajout des caractéristiques de la phrase comme étant la moyenne des vecteurs de chaque mot la composant
	 * @throws VectorDimensionException
	 */
	private void generateMatSentenceTopic() throws VectorDimensionException {
		nbSentence = getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()).getNbSentence();
		matSentenceTopic = new double[nbSentence][K];
		
		int i = 0; //sentence variable
		int l = 1; //optional parameter to configure handicap for long sentences

		Iterator<TextModel> textIt = getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()).iterator();
		while (textIt.hasNext()) {
			TextModel textModel = textIt.next();
			Iterator<ParagraphModel> paragraphIt = textModel.iterator();
			while (paragraphIt.hasNext()) {
				ParagraphModel paragraphModel = paragraphIt.next();
				Iterator<SentenceModel> sentenceIt = paragraphModel.iterator();
				while (sentenceIt.hasNext()) {
					SentenceModel sentenceModel = sentenceIt.next();
					Iterator<WordModel> wordIt = sentenceModel.iterator();
					int n = 0; //length of the sentence
					while (wordIt.hasNext()) {
						WordModel word = wordIt.next();
						if (!word.isStopWord()) {
							WordLDA wordLDA = (WordLDA) index.get(word.getmLemma());
							for (int k = 0; k<K; k++)
								matSentenceTopic[i][k] += wordLDA.getTopicDistribution()[k];
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
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}
}
