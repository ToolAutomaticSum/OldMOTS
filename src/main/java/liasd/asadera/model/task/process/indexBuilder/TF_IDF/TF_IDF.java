package main.java.liasd.asadera.model.task.process.indexBuilder.TF_IDF;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.model.task.preProcess.GenerateTextModel;
import main.java.liasd.asadera.model.task.process.indexBuilder.AbstractIndexBuilder;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedOut;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.WordModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;
import main.java.liasd.asadera.tools.reader_writer.Reader;
import main.java.liasd.asadera.tools.reader_writer.Writer;
import main.java.liasd.asadera.tools.wordFilters.WordFilter;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

public class TF_IDF extends AbstractIndexBuilder<WordIndex> {

	private boolean generateIdf = false;
	private String idfFile = null;
	
	private boolean firstLoading = true;
	private Index<WordIndex> loadingIndex;
	
	public TF_IDF(int id) throws SupportADNException {
		super(id);

		listParameterOut.add(new ParameterizedType(WordIndex.class, Index.class, IndexBasedOut.class));
	}

	@Override
	public AbstractIndexBuilder<WordIndex> makeCopy() throws Exception {
		TF_IDF p = new TF_IDF(id);
		initCopy(p);
		return p;
	}
	
	@Override
	public void initADN() throws Exception {
		super.initADN();
		try {
			idfFile = getCurrentProcess().getModel().getProcessOption(id, "IdfFile");
			idfFile += ".idf";
		}
		catch (LacksOfFeatures e) {
			idfFile = null;
		}
		try {
			generateIdf = Boolean.parseBoolean(getCurrentProcess().getModel().getProcessOption(id, "GenerateIdf"));
			if (generateIdf && idfFile == null)
				throw new LacksOfFeatures("GenerateIdf option need IdfFile option.");
		}
		catch (LacksOfFeatures e) {
			generateIdf = false;
		}
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		super.processIndex(listCorpus);
		
		if (generateIdf) {
			TF_IDF.generateIdfFile(getCurrentMultiCorpus(), getCurrentProcess().getFilter(), idfFile);
			System.exit(0);
		}
		else {
			if (idfFile == null) {
				TF_IDF.generateDictionary(listCorpus, index, getCurrentProcess().getFilter());
				for (Corpus c : getCurrentMultiCorpus()) {
					if (!listCorpus.contains(c)) {
//						boolean clear = c.size() == 0;
//						Corpus temp = c;
//						if (clear)
//							temp = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", c,
//									true);
						TF_IDF.majIDFDictionnary(c, index, getCurrentProcess().getFilter());
//						if (clear)
//							temp.clear();
					}
				}
			}
			else {
				if (firstLoading) {
					loadingIndex = new Index<WordIndex>();
					TF_IDF.loadIdfFile(getCurrentMultiCorpus(), loadingIndex, getCurrentProcess().getFilter(), idfFile);
					firstLoading = false;
				}
				loadIdfFromLoadingIndex(listCorpus);
				//TF_IDF.loadIdfFile(listCorpus, index, getCurrentProcess().getFilter(), idfFile);
			}
		}
	}

	@SuppressWarnings("unlikely-arg-type")
	public static void generateDictionary(List<Corpus> listCorpus, Index<WordIndex> index, WordFilter filter) throws Exception {
		for (Corpus corpus : listCorpus) {
			boolean clear = false;
			if (corpus.getNbSentence() == 0) {
				clear = true;
				corpus = GenerateTextModel.readTempDocument("output" + File.separator + "temp", corpus, true);
			}
			index.setNbDocument(index.getNbDocument() + corpus.size());
			for (TextModel textModel : corpus) {
				for (SentenceModel sentenceModel : textModel) {
					List<WordIndex> listWordIndex = new ArrayList<WordIndex>();
					for (WordModel word : sentenceModel.getListWordModel())
						if (filter.passFilter(word)) {
							WordIndex w;
							if (!index.containsKey(word.getmLemma())) {
								w = new WordIndex(word.getmLemma());
								w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
								index.put(word.getmLemma(), w);
							} else {
								w = index.get(word.getmLemma());
								w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
							}
							listWordIndex.add(w);
						}
					sentenceModel.setN(1);
					sentenceModel.setListWordIndex(1, listWordIndex);
				}
			}
			index.putCorpusNbDoc(corpus.getiD(), corpus.size());
			if (clear)
				corpus.clear();
		}
	}

	/**
	 * @param corpus
	 * @param dictionnary
	 * @throws Exception 
	 */
	public static void majIDFDictionnary(Corpus corpus, Index<WordIndex> index, WordFilter filter) throws Exception {
		boolean clear = false;
		if (corpus.getNbSentence() == 0) {
			clear = true;
			corpus = GenerateTextModel.readTempDocument("output" + File.separator + "temp", corpus, true);
		}
		index.setNbDocument(index.getNbDocument() + corpus.getNbDocument());
		for (TextModel text : corpus) {
			for (SentenceModel sentenceModel : text) {
				for (WordModel word : sentenceModel.getListWordModel()) {
					if (filter.passFilter(word) && index.containsKey(word.getmLemma())) {
						@SuppressWarnings("unlikely-arg-type")
						WordIndex w = (WordIndex) index.get(word.getmLemma());
						w.addDocumentOccurence(corpus.getiD(), text.getiD());
					}
				}
			}
		}
		index.putCorpusNbDoc(corpus.getiD(), corpus.size());
		if (clear)
			corpus.clear();
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public static void generateIdfFile(List<Corpus> listCorpus, WordFilter filter, String idfName) throws Exception {
		new File("output" + File.separator + "modelIDF").mkdirs();
		Index <WordIndex> index = new Index<WordIndex>();
		try (ProgressBar pb = new ProgressBar("Generate index idf ", listCorpus.size(), ProgressBarStyle.ASCII)) {
			for (Corpus corpus : listCorpus) {
				if (corpus.getNbSentence() == 0)
					corpus = GenerateTextModel.readTempDocument("output" + File.separator + "temp", corpus,
																true);
				index.setNbDocument(index.getNbDocument() + corpus.size());
				for (TextModel textModel : corpus) {
					for (SentenceModel sentenceModel : textModel) {
						for (WordModel word : sentenceModel.getListWordModel())
							if (filter.passFilter(word)) {
								WordIndex w;
								if (!index.containsKey(word.getmLemma())) {
									w = new WordIndex(word.getmLemma());
									w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
									index.put(word.getmLemma(), w);
								} else {
									w = index.get(word.getmLemma());
									w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
								}
							}
					}
				}
				index.putCorpusNbDoc(corpus.getiD(), corpus.size());
				corpus.clear();
				pb.step();
			}
		}
		String outputPath = "output" + File.separator + "modelIDF" + File.separator + idfName;
		File f = new File(outputPath);
		if (f.exists())
			f.delete();
		Writer w = new Writer(outputPath);
		w.open(false);
		w.write(index.getNbDocument() + "\n");
		for(WordIndex word : index.values()) {
			w.write(word.toString() + "\t" + + word.getNbDocumentWithWordSeen() + "\t" + word.getIdf(index.getNbDocument()) + "\n");
		}
		w.close();
	}
	
	@SuppressWarnings("unlikely-arg-type")
	protected void loadIdfFromLoadingIndex(List<Corpus> listCorpus) throws Exception {
		TF_IDF.generateDictionary(listCorpus, index, getCurrentProcess().getFilter());
		for (WordIndex word : index.values()) {
			word.setNbDocumentWithWordSeen(loadingIndex.get(word.toString()).getNbDocumentWithWordSeen());
			word.setIdf(loadingIndex.get(word.toString()).getIdf());
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public static void loadIdfFile(List<Corpus> listCorpus, Index<WordIndex> index, WordFilter filter, String idfName) throws Exception {
		String path = "output" + File.separator + "modelIDF" + File.separator + idfName;
		if (!new File(path).exists())
			throw new FileNotFoundException(path + " not found.");
		
		TF_IDF.generateDictionary(listCorpus, index, filter);
		
		Reader r = new Reader(path, true);
		r.open();
		String line = r.read();
		index.setNbDocument(Integer.parseInt(line));
		line = r.read();
		while (line != null) {
			String[] split = line.split("\t");
			WordIndex word = index.get(split[0]);
			if (word != null) {
				word.setNbDocumentWithWordSeen(Integer.parseInt(split[1]));
				word.setIdf(Float.parseFloat(split[2]));
			}
			line = r.read();
		}
		r.close();
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return super.isOutCompatible(compatibleMethod) || compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(WordIndex.class, Index.class, IndexBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		if (super.isOutCompatible(compMethod))
			super.setCompatibility(compMethod);
		if (compMethod.getParameterTypeIn()
				.contains(new ParameterizedType(WordIndex.class, Index.class, IndexBasedIn.class)))
			((IndexBasedIn<WordIndex>) compMethod).setIndex(index);
	}
}
