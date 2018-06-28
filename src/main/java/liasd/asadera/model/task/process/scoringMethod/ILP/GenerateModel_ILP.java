package main.java.liasd.asadera.model.task.process.scoringMethod.ILP;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.indexBuilder.ILP.SentenceNGramBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.scoringMethod.AbstractScoringMethod;
import main.java.liasd.asadera.model.task.process.scoringMethod.FileNameBasedIn;
import main.java.liasd.asadera.model.task.process.scoringMethod.FileNameBasedOut;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.tools.reader_writer.Writer;

public class GenerateModel_ILP extends AbstractScoringMethod
		implements IndexBasedIn<NGram>, SentenceNGramBasedIn, FileNameBasedOut {

	private static Logger logger = LoggerFactory.getLogger(GenerateModel_ILP.class);

	private static int ilp_nb = 0;
	private final int ilp_id;

	private Index<NGram> index;
	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;

	private String model;
	private String fileName = new File("").getAbsolutePath();

	private Integer maxSummLength;

	public GenerateModel_ILP(int id) throws SupportADNException {
		super(id);
		ilp_id = ilp_nb;
		ilp_nb++;
		fileName += "/tempILP" + ilp_id;
		listParameterIn.add(new ParameterizedType(NGram.class, Index.class, IndexBasedIn.class));
		listParameterIn.add(new ParameterizedType(NGram.class, List.class, SentenceNGramBasedIn.class));
		listParameterOut.add(new ParameterizedType(null, String.class, FileNameBasedOut.class));
	}

	@Override
	public AbstractScoringMethod makeCopy() throws Exception {
		GenerateModel_ILP p = new GenerateModel_ILP(id);
		initCopy(p);
		return p;
	}

	@Override
	public void computeScores(List<Corpus> listCorpus) throws Exception {
		maxSummLength = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "SummarySize"));

		buildModel(listCorpus);
		writeModelToTmpFile();
	}

	public void buildModel(List<Corpus> listCorpus) {
		List<SentenceModel> listSentence = new ArrayList<SentenceModel>();
		listSentence.addAll(ngrams_in_sentences.keySet());
		Collections.sort(listSentence);
		
		String texte = "Maximize\n";
		String objective = "score: ";
		for (NGram ng : index.values()) {
			double bg_weight = ng.getWeight();
			int id_bg = ng.getiD();
			objective += "+ " + bg_weight + " c" + id_bg + " ";
		}
		int i = 0;
		for (SentenceModel sen : listSentence) {
			double length = sen.getNbMot() / 1000.;
			objective += "- " + length + " s" + i + " ";
			i++;
		}
		texte += objective + "\n\nSubject To\n";
		boolean first;
		for (NGram ng : index.values()) {
			first = true;
			String contrainte = "index_" + ng.getiD() + ": ";
			int j = 0;
			for (SentenceModel sen : listSentence) {
				for (NGram ng1 : ngrams_in_sentences.get(sen)) {
					if (ng1.equals(ng)) {
						if (first) {
							contrainte += "s" + j + " ";
							first = false;
						} else
							contrainte += "+ " + "s" + j + " ";
					}
				}
				j++;
			}
			contrainte += "- c" + ng.getiD() + " >= 0\n";
			texte += contrainte;
		}
		String length_constraint = "length: ";
		i = 0;
		for (SentenceModel sen : listSentence) {
			int length = sen.getNbMot();
			if (i == 0)
				length_constraint += length + " s" + i;
			else
				length_constraint += " + " + length + " s" + i;
			i++;
		}
		length_constraint += " <= " + maxSummLength + "\n";

		texte += length_constraint + "\n\nBinary\n";

		for (NGram ng : index.values())
			texte += "c" + ng.getiD() + "\n";

		for (int j = 0; j < listSentence.size(); j++) {
			texte += "s" + j + "\n";
		}
		texte += "End";

		model = texte;
	}

	private void writeModelToTmpFile() {
		try {
//			File file = new File("tempILP" + ilp_id + ".ilp_out");
//			file.delete();
			Writer w = new Writer(fileName + ".ilp_in");
			w.open(false);
//			FileOutputStream fw = new FileOutputStream("tempILP" + ilp_id + ".ilp_out");
//			OutputStreamWriter osr = new OutputStreamWriter(fw, "UTF-8");
			logger.trace("Model length : " + model.length());
//			System.out.println(model);
			try {
				w.write(model);
			} catch (Exception e) {
				e.printStackTrace();
			}
			w.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(null, String.class, FileNameBasedIn.class));
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		((FileNameBasedIn) compMethod).setFileName(getFileName());
	}

	@Override
	public void setSentenceNGram(Map<SentenceModel, Set<NGram>> ngrams_in_sentences) {
		this.ngrams_in_sentences = ngrams_in_sentences;
	}

	@Override
	public void setIndex(Index<NGram> index) {
		this.index = index;
	}

	@Override
	public String getFileName() {
		return fileName;
	}
}
