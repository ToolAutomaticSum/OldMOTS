package main.java.liasd.asadera.model.task.process.selectionMethod.deepLearning;

import java.io.File;
import java.io.IOException;
import java.util.List;

import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.WordModel;
import main.java.liasd.asadera.tools.reader_writer.Writer;

public class PointerGenerator extends DeepLearning {

	public PointerGenerator(int id) throws SupportADNException {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		return null;
	}

	@Override
	public void trainModel(List<Corpus> listCorpus) throws Exception {
		new File("temp").mkdirs();
		writeCorpusToFolder("temp");
	}
	
	/*
	 * Have to do this because Pointer-generator python code take binary file as input.
	 */
	public void writeCorpusToFolder(String folderName) throws IOException {
		
		for (Corpus corpus : getCurrentMultiCorpus())
			for (TextModel text : corpus) {
				Writer w = new Writer(folderName + File.separator + text.getTextName());
				w.open(true);
				for (SentenceModel sen : text) {
					if (sen.getLabels().contains("highlight"))
						w.write("@highlight");
					String s = "";
					for (WordModel word : sen.getListWordModel())
						s += word.toString() + " ";
					w.write(s + "\n");
				}
				w.close();
			}
	}
		
	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		return null;
	}
}
