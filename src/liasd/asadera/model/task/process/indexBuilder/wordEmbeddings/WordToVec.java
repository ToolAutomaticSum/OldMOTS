package liasd.asadera.model.task.process.indexBuilder.wordEmbeddings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.valnyz.reader_writer.Writer;

import liasd.asadera.model.task.preProcess.GenerateTextModel;
import liasd.asadera.model.task.process.indexBuilder.AbstractIndexBuilder;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.WordIndex;
import liasd.asadera.textModeling.wordIndex.WordVector;
import liasd.asadera.tools.pythonWrapper.Word2VecPython;
import liasd.asadera.tools.pythonWrapper.Word2VecPythonBuilder;
import liasd.asadera.tools.vector.ToolsVector;

public class WordToVec extends AbstractIndexBuilder<WordVector> {
	
	private int dimension;
	private boolean modelLoad = false;
	private Word2VecPythonBuilder pythonBuilder;
	private Word2VecPython w2vModel;

	public WordToVec(int id) throws SupportADNException {
		super(id);
		
		listParameterOut.add(new ParameterizedType(WordVector.class, Index.class, IndexBasedIn.class));
	}

	@Override
	public WordToVec makeCopy() throws Exception {
		WordToVec p = new WordToVec(id);
		initCopy(p);
		p.setDimension(dimension);
		p.setModelLoad(modelLoad);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		pythonBuilder = new Word2VecPythonBuilder();
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		super.processIndex(listCorpus);
		if (!modelLoad) {
			System.out.println("Prepare loading ... ");
			w2vModel = pythonBuilder.load(getModel().getProcessOption(id, "ModelPath"));
			System.out.println("Model size : " + w2vModel.getVocabSize());

			List<Corpus> tempList = new ArrayList<Corpus>(listCorpus);
			List<Boolean> tempClear = new ArrayList<Boolean>();
			tempClear.add(false);

			for (Corpus c : getCurrentMultiCorpus()) {
				if (!listCorpus.contains(c)) {
					boolean clear = c.size() == 0;
					Corpus temp = c;
					if (clear)
						temp = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp",
								c, true);
					tempList.add(temp);
					tempClear.add(clear);
				}
			}

			writeTempInputFile(tempList);
			System.out.println("Up-Training");
			WordToVec.learnWordToVec(w2vModel, getModel().getOutputPath() + File.separator + "tempWord2Vec.temp", 1,
					true);
			System.out.println("Model size : " + String.valueOf(w2vModel.getVocabSize()));
			new File(getModel().getOutputPath() + File.separator + "tempWord2Vec.temp").delete();
			for (int i = 0; i < tempList.size(); i++) {
				if (tempClear.get(i))
					tempList.get(i).clear();
			}
			modelLoad = true;
		}

		boolean bDimension = true;

		int nbMotWE = 0;
		for (Corpus c : listCorpus)
			for (TextModel text : c) {
				for (SentenceModel sentenceModel : text) {
					List<WordIndex> listWordIndex = new ArrayList<WordIndex>();
					for (WordModel wm : sentenceModel.getListWordModel()) {
						if ((getCurrentProcess().getFilter().passFilter(wm)) && !index.containsKey(wm.getmLemma())) {
							WordVector wv;
							if (w2vModel.isWordInVocab(wm.getmLemma())) {
								nbMotWE++;
								if (bDimension) {
									dimension = w2vModel.getVector(wm.getmLemma()).size();
									bDimension = false;
								}
								wv = new WordVector(wm.getmLemma(),
										ToolsVector.ListToArray(w2vModel.getVector(wm.getmLemma())));
								index.put(wv);
							} else {
								System.out.println("Model don't have the word " + wm.getmLemma());
								wv = new WordVector(wm.getmLemma(), new double[dimension]);
								index.put(wv);
							}
							listWordIndex.add(wv);
						}
					}
					sentenceModel.setN(1);
					sentenceModel.setListWordIndex(1, listWordIndex);
				}
			}
		System.err.println(index.size());
		System.err.println(nbMotWE);
	}

	public static void learnWordToVec(Word2VecPython vec, String fname, int min_count, boolean update) {
		vec.build_vocab_file(fname, min_count, 10000, update);
		vec.train_file(fname, 5);
	}

	private void writeTempInputFile(List<Corpus> listCorpus) throws Exception {
		Writer w = new Writer(getModel().getOutputPath() + File.separator + "tempWord2Vec.temp");
		w.open(false);
		for (Corpus corpus : listCorpus)
			for (TextModel text : corpus)
				for (SentenceModel sen : text) {
					for (WordModel word : sen.getListWordModel())
						if (getCurrentProcess().getFilter().passFilter(word))
							w.write(word.getmLemma() + " ");
					w.write("\n");
				}
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public void setModelLoad(boolean modelLoad) {
		this.modelLoad = modelLoad;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(WordVector.class, Index.class, IndexBasedIn.class));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		((IndexBasedIn<WordVector>) compMethod).setIndex(index);
	}
}
