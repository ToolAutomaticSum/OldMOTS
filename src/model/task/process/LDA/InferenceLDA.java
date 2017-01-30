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
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.LDA.WordLDA;

public class InferenceLDA extends AbstractProcess implements LdaBasedOut {
	
	protected double[] averageVector;
	//Vecteur de la phrases obtenue par moyenne des vecteurs des mots de la phrases
	protected double[][] matSentenceTopic; //Sentence/Topic
	
	private Inferencer inferencer;
	private Model newModel;
	
	private int nbSentence;
	private double[][] theta;
	private int K;
	
	private Map<SentenceModel, double[]> sentenceCaracteristic = new HashMap<SentenceModel, double[]>();
	
	public InferenceLDA(int id) {
		super(id);	
	}
	
	/**
	 * Calcul des topics sur le/les documents à résumer
	 */
	@Override
	public void init() throws Exception {
		super.init();
		
		writeTempInputFile();
		LDACmdOption option = new LDACmdOption();
		option.est = false;
		option.estc = false;
		option.inf = true;
		//option..savestep = 10;
		option.twords = 20;
		option.K = Integer.parseInt(getModel().getProcessOption(id, "NbTopicsLDA"));
		option.alpha = Double.parseDouble(getModel().getProcessOption(id, "Alpha"));
		option.beta = Double.parseDouble(getModel().getProcessOption(id, "Beta"));
		option.modelName = "LDA_model_"+option.alpha+"_"+option.beta;
		option.dir = getModel().getProcessOption(id, "PathModel") + "\\modelLDA";
		option.dfile = "temp.txt.gz"; //TODO à changer
		inferencer = new Inferencer(option);
		//inferencer.init(option);
		newModel = inferencer.inference();
	}
	
	/**
	 * @throws Exception 
	 * génère le dictionnaire des mots avec leurs probabilités par topic
	 * et attribut un score à chaque phrase
	 */
	@Override
	public void process() throws Exception {
		generateDictionary();
		File f = new File(getModel().getOutputPath() + "\\modelLDA\\temp.txt");
		f.delete();		//Suppression de tempInputFile
		
		double valeurLimiteTopic = 0.01;
		int t = 0; //document variable
		nbSentence = 0;
		K = newModel.K;
		theta = newModel.theta;
		Iterator<TextModel> textIt = getModel().getDocumentModels().iterator();
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
		
		/*AbstractScoringMethodLDA s = (AbstractScoringMethodLDA) scoringMethod;
		s.init(this, getModel().getDictionnary(), newModel.theta, newModel.K, nbSentence);
		*/
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
		Iterator<TextModel> textIt = getModel().getDocumentModels().iterator();
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
						if(!dictionnary.containsKey(word.getmLemma())) {
							dictionnary.put(word.getmLemma(), new WordLDA(word.getmLemma(), dictionnary, -1, newModel.K));
						}
						dictionnary.get(word.getmLemma()).add(word);
					}
				}
			}
		}
		
		//Ajout dans le dictionnaire des IDs et des Topic Distribution
		for (int w = 0; w < newModel.V; w++){ //Boucle sur le nombre de mot du vocabulaire local
			String word = newModel.data.localDict.getWord(w);
			WordLDA wordEmbeddings;
			if(dictionnary.containsKey(word)) {
				wordEmbeddings = (WordLDA) dictionnary.get(word);
				wordEmbeddings.setId(w);
				hashMapWord.put(w, word);
			}
			else {
				wordEmbeddings = new WordLDA(word, dictionnary, w, newModel.K);
				hashMapWord.put(w, word);
				dictionnary.put(word, wordEmbeddings);
			}
			for (int k = 0; k < newModel.K; k++){ //Boucle sur le nombre de Topic
				if (newModel.data.localDict.contains(w)){
					wordEmbeddings.getTopicDistribution()[k] = newModel.phi[k][w];
				}
			}//end foreach word
		}
	}
	
	private void writeTempInputFile() throws IOException {
		new File(getModel().getOutputPath() + "\\modelLDA").mkdir();
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(
                    new FileOutputStream(getModel().getOutputPath() + "\\modelLDA\\temp.txt.gz")), "UTF-8"));

		//writer.write(String.valueOf(getModel().getDocumentModels().size()) + "\n");
		Iterator<TextModel> textIt = getModel().getDocumentModels().iterator();
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
							//System.out.println(word);
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
		//TreeSet<PairSentenceScore> sentencesScores = new TreeSet<PairSentenceScore>();
		
		matSentenceTopic = new double[nbSentence][K];
		
		int t = 0; //document variable
		int i = 0; //sentence variable
		int l = 1; //optional parameter to configure handicap for long sentences

		Iterator<TextModel> textIt = getModel().getDocumentModels().iterator();
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
						WordLDA wordLDA = (WordLDA) dictionnary.get(word.getmLemma());
						for (int k = 0; k<K; k++)
							matSentenceTopic[i][k] += wordLDA.getTopicDistribution()[k]*theta[t][k];
						j++;
					}
					
					for (int k = 0; k<K; k++) //topic loop
						matSentenceTopic[i][k] /= j^l;
					sentenceCaracteristic.put(sentenceModel,matSentenceTopic[i]);
					//sentenceModel.setScore(Tools.cosineSimilarity(matSentenceTopic[i],averageVector)); //Ajout du score à la phrase
					//sentenceModel.getCaracteristic().setdTab(matSentenceTopic[i]);
					//sentencesScores.add(new PairSentenceScore(sentenceModel, sentenceModel.getScore()));
					i++;
				}
			}
			t++;
		}
		
		//return sentencesScores;
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
