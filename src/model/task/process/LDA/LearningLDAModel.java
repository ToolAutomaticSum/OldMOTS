package model.task.process.LDA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import jgibblda.Estimator;
import jgibblda.LDACmdOption;
import model.task.preProcess.AbstractPreProcess;
import model.task.preProcess.SentenceSplitter;
import model.task.preProcess.StopWordsRemover;
import model.task.preProcess.TextStemming;
import model.task.preProcess.WordSplitter;
import model.task.process.AbstractProcess;
import optimize.SupportADNException;
import reader_writer.Reader;
import textModeling.Corpus;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;

public class LearningLDAModel extends AbstractProcess {

	private LDACmdOption option = new LDACmdOption();
	private boolean liveProcess = false;
	private boolean newModel = true;
	int nbSentence = 0;
	
	private Estimator estimator;
	
	/**
	 * ProcessOption lié NbTopicsLDA, int.
	 * @throws SupportADNException 
	 */
	public LearningLDAModel(int id) throws SupportADNException {
		super(id);	
	}
	
	@Override
	public void init() throws Exception {
		liveProcess = Boolean.parseBoolean(getModel().getProcessOption(id, "LiveProcess"));
		newModel = Boolean.parseBoolean(getModel().getProcessOption(id, "NewModel"));
		
		option.inf = false;
		option.K = Integer.parseInt(getModel().getProcessOption(id, "NbTopicsLDA"));
		option.alpha = Double.parseDouble(getModel().getProcessOption(id, "Alpha"));
		option.beta = Double.parseDouble(getModel().getProcessOption(id, "Beta"));
		option.niters = 1000;
		//option.savestep = 500;
		option.twords = 15;
		option.dir = getModel().getOutputPath() + "\\modelLDA";
		option.dfile = "temp_"+option.alpha+"_"+option.beta;
		//estimator.init(option);
		
		option.modelName = "LDA_model_"+option.alpha+"_"+option.beta;
		if (newModel)
			option.est = true;
		else {
			option.modelName += "-final";
			option.estc = true;
		}
		
		if (liveProcess)
			liveInit();
		else {
			super.init();
			writeTempInputFile();
		}
	}
	
	/**
	 * If file temp.txt exists, do nothing,
	 * else, write it reading inputTextModel and doing preProcess
	 * @throws Exception
	 */
	private void liveInit() throws Exception {
		StopWordsRemover stopWordsProcess = null;
		TextStemming textStemmer = null;
		
		/**
		 * Initializing preProcess
		 */
		Iterator<AbstractPreProcess> preProcIt = getModel().getPreProcess().iterator();
		while (preProcIt.hasNext()) {
			AbstractPreProcess p = preProcIt.next();
			if (p.getClass().equals(StopWordsRemover.class)) {
				stopWordsProcess = (StopWordsRemover) p;
				p.setModel(getModel());
				p.setCurrentProcess(this);
				p.init();
			}
			else if (p.getClass().equals(TextStemming.class)) {
				textStemmer = (TextStemming) p;
				p.setModel(getModel());
				p.setCurrentProcess(this);
				p.init();
			}
		}
		
		int i = 0;
		nbSentence = 0;
		Iterator<Corpus> corpusIt = getModel().getCurrentMultiCorpus().iterator();
		while (corpusIt.hasNext()) {
			Iterator<TextModel> textIt = corpusIt.next().iterator();
			while (textIt.hasNext()) {
				TextModel textModel = textIt.next();
				
				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
		                new GZIPOutputStream(
		                    new FileOutputStream(getModel().getOutputPath() + "\\modelLDA\\temp" + i + "_"+option.alpha+"_"+option.beta)), "UTF-8"));
	
				Reader r = new Reader(textModel.getDocumentFilePath(), true);
				r.open();
				String text = r.read();
	
				while (text != null)
		        {
					w.write(liveTextProcess(text, stopWordsProcess, textStemmer));
					text = r.read();
		        }
				w.close();
				i++;
			}
		}
		/*RandomAccessFile raf = new RandomAccessFile(new File(getModel().getOutputPath() + "\\modelLDA\\temp.txt"), "rw");
		raf.seek(0);
		String value = String.valueOf(nbSentence) + "\n";
		ByteBuffer buf = ByteBuffer.allocate(16);
		buf.put(value.getBytes("UTF-8"));
		for (int i = 0; i<(16-value.length()); i++)
			buf.put(raf.readByte());
		raf.seek(0);
		//String oldValue = raf.read(arg0);
		raf.write(buf.array(),0,buf.capacity());
		raf.close();*/
	}
	
	private String liveTextProcess(String text, StopWordsRemover stopWordsProcess, TextStemming textStemmer) {
		String returnText = "";
		List<String> listOfSentence = SentenceSplitter.splitTextIntoSentence(text);
		
		Iterator<String> senIt = listOfSentence.iterator();
		while (senIt.hasNext()) {
			String sentence = senIt.next();
			if (!sentence.equals("")) {
				int nbWordToWrite = 0;
				List<String> listOfWord = WordSplitter.splitSentenceIntoWord(sentence);
				listOfWord = stopWordsProcess.removeGramWords(listOfWord);
				Iterator<String> wordIt = listOfWord.iterator();
				while (wordIt.hasNext()) {
					String word = wordIt.next();
					word = textStemmer.stemming(word);
					returnText += word + " ";
					nbWordToWrite++;
				}
				if (nbWordToWrite != 0) {
					returnText += "\n";
					nbSentence++;
				}
			}
		}
		
		return returnText;
	}
	
	@Override
	public void process() throws FileNotFoundException, IOException {
		if (liveProcess) {
			option.dfile = "temp0_"+option.alpha+"_"+option.beta;
			estimator = new Estimator(option);
			estimator.estimate();
			Iterator<Corpus> corpusIt = getModel().getCurrentMultiCorpus().iterator();
			while (corpusIt.hasNext()) {
				for (int i = 1; i <  corpusIt.next().size(); i++) {
					option.dfile = "temp" + i+"_"+option.alpha+"_"+option.beta;
					option.est=false;
					option.estc=true;
					estimator = new Estimator(option);
					estimator.estimate();
				}
			}
		}
		else {
			option.dfile = "temp.txt";
			option.est=true;
			option.estc=false;
			estimator = new Estimator(option);
			estimator.estimate();
		}
	}
	
	@Override
	public void finish() throws Exception {
		super.finish();
		if (!liveProcess) {
			File f = new File(getModel().getOutputPath() + "\\modelLDA\\temp.txt");
			f.delete();
		}
	}
	
	private void writeTempInputFile() throws IOException {
		new File(getModel().getOutputPath() + "\\modelLDA").mkdir();
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(
                    new FileOutputStream(getModel().getOutputPath() + "\\modelLDA\\temp.txt")), "UTF-8"));

		writer.write(String.valueOf(getModel().getCurrentMultiCorpus().size()) + "\n");
		Iterator<Corpus> corpusIt = getModel().getCurrentMultiCorpus().iterator();
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
}
