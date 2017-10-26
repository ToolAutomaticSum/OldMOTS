package liasd.asadera.model.task.process.indexBuilder.wordEmbeddings;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import com.valnyz.reader_writer.Writer;

import liasd.asadera.model.task.process.indexBuilder.AbstractIndexBuilder;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.indexBuilder.LearningModelBuilder;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.WordVector;
import liasd.asadera.tools.vector.ToolsVector;
import pythonWrapper.Word2VecPython;
import pythonWrapper.Word2VecPythonBuilder;

public class WordToVec extends AbstractIndexBuilder<WordVector> implements LearningModelBuilder {

	private int dimension;
	private boolean modelLoad = false;
	private Word2VecPythonBuilder pythonBuilder;
	private Word2VecPython w2vModel;
	
	public WordToVec(int id) throws SupportADNException {
		super(id);
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
		/**
		 * Demande trop de ram, à tester sur serveur ou avec plus de ram.
		 */
		if (!modelLoad) {
			System.out.println("Prepare loading ... ");
			w2vModel = pythonBuilder.load(getModel().getProcessOption(id, "ModelPath"));
		    System.out.println("Model size : " + String.valueOf(w2vModel.getVocabSize()));
		    
		    modelLoad = true;
		}

	    writeTempInputFile(listCorpus);

	    System.out.println("Up-Training");
	    WordToVec.learnWordToVec(w2vModel, getModel().getOutputPath() + File.separator + "tempWord2Vec.temp", 1, true);
	    System.out.println("Model size : " + String.valueOf(w2vModel.getVocabSize()));
	    new File(getModel().getOutputPath() + File.separator + "tempWord2Vec.temp").delete();
	    
	    boolean bDimension = true;

	    int nbMotText = 0;
	    int nbMotWE = 0;
		//Construire index à partir de Word2Vec object
	    for (Corpus c : listCorpus)
		    for (TextModel text : c) {
				Iterator<SentenceModel> sentenceIt = text.iterator();
				while (sentenceIt.hasNext()) {
					SentenceModel sentenceModel = sentenceIt.next();
					Iterator<WordModel> wordIt = sentenceModel.iterator();
					while (wordIt.hasNext()) {
						WordModel wm = wordIt.next();
						nbMotText++;
						if ((getCurrentProcess().getFilter().passFilter(wm)) && !index.containsKey(wm.getmLemma())) {
							if (w2vModel.isWordInVocab(wm.getmLemma())) {
								nbMotWE++;
								if (bDimension) {
									dimension = w2vModel.getVector(wm.getmLemma()).size();
									bDimension = false;
								}
								index.put(new WordVector(wm.getmLemma(), index, ToolsVector.ListToArray(w2vModel.getVector(wm.getmLemma()))));
							}
							else {
								System.out.println("Model don't have the word " + wm.getmLemma());
								index.put(new WordVector(wm.getmLemma(), index, new double[dimension]));
							}
						}
					}
				}
		    }
	    System.out.println("Modèle chargé !");
	    System.out.println(nbMotText);
	    System.out.println(nbMotWE);
	}
	
	@Override
	public void learn(List<Corpus> listCorpus, String modelName) throws Exception {
		//vec = learnFromRawWordToVecMultiCorpus(listCorpus);
		 //WordVectorSerializer.writeWord2VecModel(vec, modelName + ".bin.gz");
	}
	
	@Override
	public void liveLearn(List<String> listStringSentence, String modelName) {
		/*File modelFile = new File(modelName + ".bin.gz");
		if (vec == null && modelFile.exists() && !modelFile.isDirectory())
			vec = WordVectorSerializer.readWord2VecModel(modelFile, true);
		long time = System.currentTimeMillis();
		vec = learnWordToVecListString(vec, listStringSentence);
		System.out.println("Learning : " + (System.currentTimeMillis() - time));
		System.out.println("Vocab size: " + vec.getVocab().numWords());
		WordVectorSerializer.writeWord2VecModel(vec, modelName + ".bin.gz");
		System.out.println("Learning + writing : " + (System.currentTimeMillis() - time));*/
	}
	
	public static Word2VecPython learnFromRawWordToVecMultiCorpus(List<Corpus> multiCorpus) {
		/*ListCorpusSentenceIterator sentenceIterator = new ListCorpusSentenceIterator(multiCorpus);
		sentenceIterator.setPreProcessor(new SentencePreProcess());
		
        MyTokenizerFactory tokenizerFactory = new MyTokenizerFactory();

        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(1)
                .iterations(1)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(sentenceIterator)
                .tokenizerFactory(tokenizerFactory)
                .build();
        vec.fit();
        
        return vec;*/
		return null;
	}
	
	/*public static Word2Vec learnWordToVecListString(Word2Vec vec, List<String> listStringSentence) {
		ListStringSentenceIterator sentenceIterator = new ListStringSentenceIterator(listStringSentence);
		sentenceIterator.setPreProcessor(new SentencePreProcess());
		
		TokenizerFactory tokenizerFactory = new MyTokenizerFactory();
		tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        if (vec == null) {
        	vec = new Word2Vec.Builder()
	                .minWordFrequency(1)
	                .iterations(1)
	                .layerSize(100)
	                .seed(42)
	                .windowSize(5)
	                .iterate(sentenceIterator)
	                .tokenizerFactory(tokenizerFactory)
	                .build();
        }
        else {
        	vec.setTokenizerFactory(tokenizerFactory); 
            vec.setSentenceIter(sentenceIterator);
        }
        
        vec.fit();
        
        return vec;
	}*/
	
	public static void learnWordToVec(Word2VecPython vec, String fname, int min_count, boolean update) {
		vec.build_vocab_file(fname, min_count, 10000, update);
		vec.train_file(fname, 5);
	}
	
	private void writeTempInputFile(List<Corpus> listCorpus) throws Exception {
		Writer w = new Writer(getModel().getOutputPath() + File.separator + "tempWord2Vec.temp");
		w.open();
		for (Corpus corpus : listCorpus)
			for (TextModel text : corpus)
				for (SentenceModel sen : text) {
					for (WordModel word : sen)
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
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(WordVector.class, Index.class, IndexBasedIn.class));
	}
	
	/**
	 * donne le/les paramètre(s) d'output en input à la class comp méthode
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((IndexBasedIn<WordVector>)compMethod).setIndex(index);
	}
}
