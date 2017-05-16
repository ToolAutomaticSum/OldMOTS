package model.task.process.LDA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import exception.LacksOfFeatures;
import jgibblda.Estimator;
import jgibblda.LDACmdOption;
import jgibblda.Model;
import model.task.preProcess.GenerateTextModel;
import model.task.process.AbstractProcess;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;

public class LearningLDA extends AbstractProcess {

	private LDACmdOption option = new LDACmdOption();
	private boolean newModel = true;
	int nbSentence = 0;
	
	private Estimator estimator;
	
	/**
	 * ProcessOption lié NbTopicsLDA, int.
	 * @throws SupportADNException 
	 * @throws LacksOfFeatures 
	 * @throws NumberFormatException 
	 */
	public LearningLDA(int id) throws SupportADNException, NumberFormatException, LacksOfFeatures {
		super(id);	
	}
	
	@Override
	public AbstractProcess makeCopy() throws Exception {
		throw new Exception("No copy allowed !");
	}
	
	@Override
	public void init() throws Exception {		
		option.inf = false;
		option.K = Integer.parseInt(getModel().getProcessOption(id, "NbTopicsLDA"));
		option.alpha = Double.parseDouble(getModel().getProcessOption(id, "Alpha"));
		option.beta = Double.parseDouble(getModel().getProcessOption(id, "Beta"));
		option.niters = 1000;
		//option.savestep = 500;
		option.twords = 15;
		option.dir = getModel().getOutputPath() + File.separator + "modelLDA";
		option.dfile = "tempCorpus-all.gz";
		//estimator.init(option);
		
		option.modelName = "LDA_model_"+option.K+"_"+option.alpha+"_"+option.beta;
		System.out.println(option.modelName);
		if (newModel)
			option.est = true;
		else {
			option.modelName += "-final";
			option.estc = true;
		}
		
		super.init();
		writeTempInputFile(option.modelName, "-all", getCurrentMultiCorpus(), readStopWords, getModel().getOutputPath());
	}
	
	@Override
	public void process() throws FileNotFoundException, IOException {
		option.dfile = "temp" + option.modelName + ".gz";
		option.est=true;
		option.estc=false;
		estimator = new Estimator(option);
		estimator.estimate(true);
	}

	@Override
	public void finish() throws Exception {
		super.finish();
		//File f = new File(getModel().getOutputPath() + File.separator + "modelLDA" + File.separator + "temp" + option.modelName + ".gz");
		//f.delete();
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
	        trnModel.init(false);
			return trnModel;
		}
		
		if (!new File("tempCorpus" + corpusId + ".gz").exists())
			writeTempInputFile(modelName, corpusId, listCorpus, readStopWords, outputPath);
		
		estimator = new Estimator(option);
		return estimator.estimate(true);
	}
}
