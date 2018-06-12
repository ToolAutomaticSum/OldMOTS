package main.java.liasd.asadera.tools.pythonWrapper;

import java.io.IOException;
import java.util.List;

import jep.Jep;
import jep.JepException;

/**
 *
 * Object Factory that is used to coerce python module into a Java class
 */
public class Word2VecPythonBuilder {

	private static Jep jep;

	private List<List<String>> sentences = null;
	private int size = 100;
	private double alpha = 0.025;
	private int window = 5;
	private int min_count = 5;
	private Integer max_vocab_size = null;
	private double sample = 1e-3;
	private int seed = 1;
	private int workers = 3;
	private double min_alpha = 0.0001;
	private int sg = 0;
	private int hs = 0;
	private int negative = 5;
	private int cbow_mean = 1;
	private String hashfxn = "hash";
	private int iter = 5;
	private int null_word = 0;
	// trim_rule=None;
	private int sorted_vocab = 1;
	private int batch_words = 10000;

	/**
	 * Create a new PythonInterpreter object, then use it to execute some python
	 * code. In this case, we want to import the python module that we will coerce.
	 *
	 * Once the module is imported then we obtain a reference to it and assign the
	 * reference to a Java variable
	 * 
	 * @param <jep>
	 * @throws IOException
	 * @throws JepException
	 */
	public Word2VecPythonBuilder() {
		try {
			if (jep == null)
				jep = new Jep(false);
		} catch (JepException e) {
			e.printStackTrace();
		}
	}

	public Word2VecPython build() {
		try {
			jep.eval("import sys, logging, jep");
			jep.eval("sys.path.append('"
					+ System.getProperty("user.dir")
					+ "/src/main/python') if '" + System.getProperty("user.dir")
					+ "/src/main/python' not in sys.path else None");
			jep.eval("logger = logging.getLogger('w2v.log')");
			jep.eval("logging.basicConfig(format='%(asctime)s: %(levelname)s: %(message)s')");
			jep.eval("logging.root.setLevel(level=logging.INFO)");
			jep.eval("logging.log(logging.INFO, sys.path)");
			
			jep.eval("from pythonword2vecwrapper import *");

			jep.set("sentences", sentences);
			jep.set("size", size);
			jep.set("alpha", alpha);
			jep.set("window", window);
			jep.set("min_count", min_count);
			jep.set("max_vocab_size", max_vocab_size);
			jep.set("sample", sample);
			jep.set("seed", seed);
			jep.set("workers", workers);
			jep.set("min_alpha", min_alpha);
			jep.set("sg", sg);
			jep.set("hs", hs);
			jep.set("negative", negative);
			jep.set("cbow_mean", cbow_mean);
			jep.set("hashfxn", hashfxn);
			jep.set("iter", iter);
			jep.set("null_word", null_word);
			jep.set("sorted_vocab", sorted_vocab);
			jep.set("batch_words", batch_words);
			/*
			 * sentences, size, alpha, window," +
			 * "min_count, max_vocab_size, sample, seed, workers, min_alpha," + "sg, hs,
			 * negative, cbow_mean, hashfxn, iter, null_word, sorted_vocab, batch_words
			 */
			jep.eval("pw2vw = PythonWord2VecWrapper(sentences=sentences, size=size, alpha=alpha,"
					+ "window=window, min_count=min_count, max_vocab_size=max_vocab_size, sample=sample,"
					+ "seed=seed, workers=workers, min_alpha=min_alpha, sg=sg, hs=hs, negative=negative,"
					+ "cbow_mean=cbow_mean, iter=iter, null_word=null_word, sorted_vocab=sorted_vocab)");
			jep.eval("w2vp = jep.jproxy(pw2vw, ['main.java.liasd.asadera.tools.pythonWrapper.Word2VecPython'])");
			return (Word2VecPython) jep.getValue("w2vp");
		} catch (JepException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Word2VecPython load(String fname) {
		try {
			jep.eval("import sys, logging, jep");
			jep.eval("sys.path.append('" + System.getProperty("user.dir") + "/src/main/python')");
			jep.eval("from pythonword2vecwrapper import *");
			/*
			 * jep.eval("logger = logging.getLogger('w2v.log')"); jep.
			 * eval("logging.basicConfig(format='%(asctime)s: %(levelname)s: %(message)s')"
			 * ); jep.eval("logging.root.setLevel(level=logging.INFO)");
			 */

			jep.set("fname", fname);

			jep.eval("pw2vw = PythonWord2VecWrapper()");
			jep.eval("pw2vw.load(fname)");
			jep.eval("w2vp = jep.jproxy(pw2vw, ['main.java.liasd.asadera.tools.pythonWrapper.Word2VecPython'])");
			return (Word2VecPython) jep.getValue("w2vp");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Word2VecPythonBuilder setSentences(List<List<String>> sentences) {
		this.sentences = sentences;
		return this;
	}

	public Word2VecPythonBuilder setSize(int size) {
		this.size = size;
		return this;
	}

	public Word2VecPythonBuilder setAlpha(double alpha) {
		this.alpha = alpha;
		return this;
	}

	public Word2VecPythonBuilder setWindow(int window) {
		this.window = window;
		return this;
	}

	public Word2VecPythonBuilder setMin_count(int min_count) {
		this.min_count = min_count;
		return this;
	}

	public Word2VecPythonBuilder setMax_vocab_size(Integer max_vocab_size) {
		this.max_vocab_size = max_vocab_size;
		return this;
	}

	public Word2VecPythonBuilder setSample(double sample) {
		this.sample = sample;
		return this;
	}

	public Word2VecPythonBuilder setSeed(int seed) {
		this.seed = seed;
		return this;
	}

	public Word2VecPythonBuilder setWorkers(int workers) {
		this.workers = workers;
		return this;
	}

	public Word2VecPythonBuilder setMin_alpha(double min_alpha) {
		this.min_alpha = min_alpha;
		return this;
	}

	public Word2VecPythonBuilder setSg(int sg) {
		this.sg = sg;
		return this;
	}

	public Word2VecPythonBuilder setHs(int hs) {
		this.hs = hs;
		return this;
	}

	public Word2VecPythonBuilder setNegative(int negative) {
		this.negative = negative;
		return this;
	}

	public Word2VecPythonBuilder setCbow_mean(int cbow_mean) {
		this.cbow_mean = cbow_mean;
		return this;
	}

	public Word2VecPythonBuilder setHashfxn(String hashfxn) {
		this.hashfxn = hashfxn;
		return this;
	}

	public Word2VecPythonBuilder setIter(int iter) {
		this.iter = iter;
		return this;
	}

	public Word2VecPythonBuilder setNull_word(int null_word) {
		this.null_word = null_word;
		return this;
	}

	public Word2VecPythonBuilder setSorted_vocab(int sorted_vocab) {
		this.sorted_vocab = sorted_vocab;
		return this;
	}

	public Word2VecPythonBuilder setBatch_words(int batch_words) {
		this.batch_words = batch_words;
		return this;
	}
}
