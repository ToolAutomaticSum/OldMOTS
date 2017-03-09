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
import jgibblda.Inferencer;
import jgibblda.LDACmdOption;
import jgibblda.Model;
import model.task.process.AbstractProcess;
import optimize.parameter.Parameter;
import textModeling.Corpus;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.LDA.WordLDA;

public class LDA extends AbstractProcess implements LdaBasedOut {
	
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
	
	private Inferencer inferencer;
	private Model newModel;
	private Integer idMultiCorpusTrnModel = null;
	private Model trnModel;
	
	private int nbSentence;
	private double[][] theta;
	private int K;
	
	private Map<SentenceModel, double[]> sentenceCaracteristic;
	
	public LDA(int id) throws Exception {
		super(id);
	}
	
	/**
	 * Calcul des topics sur le/les documents � r�sumer
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
		option.dfile = "temp.txt.gz"; //TODO à changer
		option.twords = 20;
		K = getModel().getCurrentMultiCorpus().size();
		option.K = K;
		option.alpha = adn.getParameterValue(Double.class, InferenceLDA_Parameter.alpha.getName()); //Double.parseDouble(getModel().getProcessOption(id, "Alpha"));
		option.beta = adn.getParameterValue(Double.class, InferenceLDA_Parameter.beta.getName()); //Double.parseDouble(getModel().getProcessOption(id, "Beta"));

		if (idMultiCorpusTrnModel == null || idMultiCorpusTrnModel != getModel().getCurrentMultiCorpus().getiD()) {	
			trnModel = LearningLDA.ldaModelLearning(option.K, option.alpha, option.beta, getModel().getOutputPath(), getModel().getCurrentMultiCorpus());
			idMultiCorpusTrnModel = getModel().getCurrentMultiCorpus().getiD();
		}
		inferencer = new Inferencer(option, trnModel);
		
		newModel = inferencer.inference(false);
	}
	
	/**
	 * @throws Exception 
	 * génère le dictionnaire des mots avec leurs probabilités par topic
	 * et attribut un score à chaque phrase
	 */
	@Override
	public void process() throws Exception {
		sentenceCaracteristic = new HashMap<SentenceModel, double[]>();
		
		generateDictionary();
		File f = new File(getModel().getOutputPath() + File.separator + "modelLDA" + File.separator + "temp.txt");
		f.delete();		//Suppression de tempInputFile
		
		double valeurLimiteTopic = 0.01;
		int t = 0; //document variable
		nbSentence = 0;
		K = newModel.K;
		theta = newModel.theta;
		Iterator<TextModel> textIt = getModel().getCurrentMultiCorpus().get(getSummarizeCorpusId()).iterator();
		while (textIt.hasNext()) {
			nbSentence+=textIt.next().getNbSentence();
			//Parcours de theta, si valeur topic < X => valeur topic = 0;
			for (int topic = 0; topic < theta[t].length; topic++) {
				if (theta[t][topic] < valeurLimiteTopic)
					theta[t][topic] = 0;
			}
			t++;
		}
		
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
		//Construction du dictionnaire
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
					while (wordIt.hasNext()) {
						WordModel word = wordIt.next();
						if(!index.containsKey(word.getmLemma())) {
							index.put(word.getmLemma(), new WordLDA(word.getmLemma(), index, newModel.K));
						}
						index.get(word.getmLemma()).add(word);
					}
				}
			}
		}
		
		//Ajout dans le dictionnaire des IDs et des Topic Distribution
		for (int w = 0; w < newModel.V; w++){ //Boucle sur le nombre de mot du vocabulaire local
			String word = newModel.data.localDict.getWord(w);
			WordLDA wordEmbeddings;
			if(index.containsKey(word)) {
				wordEmbeddings = (WordLDA) index.get(word);
				wordEmbeddings.setId(w);
			}
			else {
				wordEmbeddings = new WordLDA(word, index, newModel.K);
				index.put(word, wordEmbeddings);
			}
			for (int k = 0; k < newModel.K; k++){ //Boucle sur le nombre de Topic
				if (newModel.data.localDict.contains(w)){
					wordEmbeddings.getTopicDistribution()[k] = newModel.phi[k][w];
				}
			}//end foreach word
		}
	}
	
	private void writeTempInputFile() throws IOException {
		new File(getModel().getOutputPath() + File.separator + "modelLDA").mkdir();
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(
                    new FileOutputStream(getModel().getOutputPath() + File.separator + "modelLDA" + File.separator + "temp.txt.gz")), "UTF-8"));

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
		matSentenceTopic = new double[nbSentence][K];
		
		int t = 0; //document variable
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
					int j = 0; //length of the sentence
					while (wordIt.hasNext()) {
						WordModel word = wordIt.next();
						WordLDA wordLDA = (WordLDA) index.get(word.getmLemma());
						for (int k = 0; k<K; k++)
							matSentenceTopic[i][k] += wordLDA.getTopicDistribution()[k]*theta[t][k];
						j++;
					}
					
					for (int k = 0; k<K; k++) //topic loop
						matSentenceTopic[i][k] /= j^l;
					sentenceCaracteristic.put(sentenceModel,matSentenceTopic[i]);
					i++;
				}
			}
			t++;
		}
	}
	
	@Override
	public double[][] getTheta() {
		return theta;
	}

	@Override
	public int getK() {
		return K;
	}

	@Override
	public int getNbSentence() {
		return nbSentence;
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}
}
