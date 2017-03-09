package model.task.process.LDA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import exception.LacksOfFeatures;
import jgibblda.Estimator;
import jgibblda.LDACmdOption;
import jgibblda.Model;
import model.SModel;
import model.task.process.AbstractProcess;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.MultiCorpus;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;

public class LearningLDA extends AbstractProcess {

	private LDACmdOption option = new LDACmdOption();
	private boolean newModel = true;
	int nbSentence = 0;
	
	private Estimator estimator;
	
	/**
	 * ProcessOption li� NbTopicsLDA, int.
	 * @throws SupportADNException 
	 * @throws LacksOfFeatures 
	 * @throws NumberFormatException 
	 */
	public LearningLDA(int id) throws SupportADNException, NumberFormatException, LacksOfFeatures {
		super(id);	
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
		option.dfile = "temp_"+option.alpha+"_"+option.beta;
		//estimator.init(option);
		
		option.modelName = "LDA_model_"+option.alpha+"_"+option.beta;
		if (newModel)
			option.est = true;
		else {
			option.modelName += "-final";
			option.estc = true;
		}
		
		super.init();
		writeTempInputFile(getModel().getCurrentMultiCorpus(), getModel().getOutputPath());
	}
	
	@Override
	public void process() throws FileNotFoundException, IOException {
		option.dfile = "temp.txt";
		option.est=true;
		option.estc=false;
		estimator = new Estimator(option);
		estimator.estimate();
	}
	
	@Override
	public void finish() throws Exception {
		super.finish();
		File f = new File(getModel().getOutputPath() + File.separator + "modelLDA" + File.separator + "temp.txt");
		f.delete();
	}
	
	private static void writeTempInputFile(MultiCorpus currentMultiCorpus, String outputPath) throws IOException {
		new File(outputPath + File.separator + "modelLDA").mkdir();
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(
                    new FileOutputStream(outputPath + File.separator + "modelLDA" + File.separator + "temp.txt")), "UTF-8"));

		writer.write(String.valueOf(currentMultiCorpus.size()) + "\n");
		Iterator<Corpus> corpusIt = currentMultiCorpus.iterator();
		while (corpusIt.hasNext()) {
			Iterator<TextModel> textIt = corpusIt.next().iterator();
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
		}
		writer.close();   
	}
	
	public static Model ldaModelLearning(SModel model, int K, int alpha, int beta, String outputPath, MultiCorpus currentMultiCorpus) throws IOException {
		LDACmdOption option = new LDACmdOption();
		Estimator estimator;
		
		option.inf = false;
		option.K = K;
		option.alpha = alpha;
		option.beta = beta;
		option.niters = 1000;
		//option.savestep = 500;
		option.twords = 15;
		option.dir = outputPath + File.separator + "modelLDA";
		option.dfile = "temp_"+option.alpha+"_"+option.beta;
		//estimator.init(option);
		
		option.modelName = "LDA_model_"+option.alpha+"_"+option.beta;

		option.est = true;

		writeTempInputFile(currentMultiCorpus, outputPath);
		
		option.dfile = "temp.txt";
		option.est=true;
		option.estc=false;
		estimator = new Estimator(option);
		estimator.estimate();
	}
}
