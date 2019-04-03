package main.java.liasd.asadera.model.task.process.selectionMethod.deepLearning;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.model.task.preProcess.GenerateTextModel;
import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.tools.reader_writer.Writer;

public class PointerGenerator extends DeepLearning {

	private static Logger logger = LoggerFactory.getLogger(PointerGenerator.class);
	
	public PointerGenerator(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		return null;
	}

	@Override
	public void trainModel(List<Corpus> listCorpus) throws Exception {
		new File("temp").mkdirs();
		writeCorpusToFolder("temp");
		
		logger.info("Launching python serialization.");
		String command = "python src/main/python/pointer_generator/serialize_data.py temp";
		
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
		command = "rm temp/*.story";
		
		p = Runtime.getRuntime().exec(command);
		p.waitFor();
		logger.info("*.bin in temp/serialized.");
		
		command = "rm temp/*.story";
		
		p = Runtime.getRuntime().exec(command);
		p.waitFor();
		logger.info("*.bin in temp/serialized.");
	}
	
	/*
	 * Have to do this because Pointer-generator python code take binary file as input.
	 */
	public void writeCorpusToFolder(String folderName) throws Exception {
		
		for (Corpus corpus : getCurrentMultiCorpus()) {
			boolean clear = false;
			if (corpus.getNbSentence() == 0) {
				clear = true;
				corpus = GenerateTextModel.readTempDocument("output" + File.separator + "temp", corpus, true);
			}
			for (TextModel text : corpus) {
				File f = new File(folderName + File.separator + text.getTextName());
				if (f.exists())
					f.delete();
				Writer w = new Writer(folderName + File.separator + text.getTextName());
				w.open(true);
				for (SentenceModel sen : text) {
					if (sen.getLabels().contains("highlight"))
						w.write("@highlight\n");
//					String s = "";
//					for (WordModel word : sen.getListWordModel())
//						s += word.toString() + " ";
//						//s += word.getWord() + " ";
//					w.write(s + ".\n");
					w.write(sen.toString() + "\n");
				}
				w.close();
			}
			if (clear)
				corpus.clear();
		}
	}
		
	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		return super.calculateSummary(listCorpus);
	}
}
