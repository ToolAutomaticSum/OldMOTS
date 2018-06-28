package main.java.liasd.asadera.model.task.process.selectionMethod.ILP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.scoringMethod.FileNameBasedIn;
import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;

public class ILP extends AbstractSelectionMethod implements FileNameBasedIn {

	private static Logger logger = LoggerFactory.getLogger(ILP.class);

	private String fileName = "";

	private String fileIn = "";
	private String fileOut = "";

	public ILP(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(null, String.class, FileNameBasedIn.class));
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		ILP p = new ILP(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
	}

	@Override
	public ArrayList<SentenceModel> calculateSummary(List<Corpus> listCorpus) {
		List<SentenceModel> listSentence = new ArrayList<SentenceModel>();
		for (Corpus c : listCorpus)
			listSentence.addAll(c.getAllSentence());
		
		runGLPK();

		ArrayList<Integer> ind_selected_sentences;
		logger.info("Summary computation");
		ind_selected_sentences = this.getSentencesFromGLPKSol();
		ArrayList<SentenceModel> summary = new ArrayList<SentenceModel>();

		for (Integer i : ind_selected_sentences) {
			if (i < listSentence.size())
				summary.add(listSentence.get(i));
		}

//		eraseTmpFiles();
		return summary;
	}

	private ArrayList<Integer> getSentencesFromGLPKSol() {
		ArrayList<Integer> ind_sentences = new ArrayList<Integer>();
		String line = "";
		int ind;
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(fileOut), "ASCII");
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				if ((ind = this.decodeLine(line)) != -1) {
					ind_sentences.add(ind);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ind_sentences;
	}

	/**
	 * Decode a line from glpk sol
	 * 
	 * @param line
	 *            the line to decode
	 * @return -1 if not a line containing a selected sentence, the index of the
	 *         selected sentence elsewhere
	 */
	private int decodeLine(String line) {
		int i = -1;
		Pattern pattern = Pattern.compile("^[\\s]+[0-9]+[\\s]+s([0-9]+)[ \\*]+1");
		Matcher match = pattern.matcher(line);

		if (match.find()) {
			return new Integer(match.group(1)).intValue();
		}

		return i;
	}

	@SuppressWarnings("unused")
	private void eraseTmpFiles() {
		File file = new File(fileName);
		if (!file.delete())
			System.err.println(file.getName() + " not deleted!");
	}

	private void runGLPK() {
		GLPLauncher glp = new GLPLauncher(fileIn);
		glp.runGLP(fileOut);
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
	}

	@Override
	public void setFileName(String fileName) {
		this.fileName = fileName;
		this.fileIn = fileName + ".ilp_in";
		this.fileOut = fileName + ".ilp_out";
	}
}
