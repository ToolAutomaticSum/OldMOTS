package test.java.liasd.asadera.tools.pythonWrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.tools.pythonWrapper.Word2VecPython;
import main.java.liasd.asadera.tools.pythonWrapper.Word2VecPythonBuilder;

public class Word2VecWrapperTest {

	private static Logger logger = LoggerFactory.getLogger(Word2VecWrapperTest.class);

	private Word2VecPython w2vp = null;
	private List<List<String>> sentences = new ArrayList<List<String>>();

	@Before
	public void init() {	
		String testText = "anarchism is a political philosophy that advocates self-governed societies based on voluntary institutions . these are often described as stateless societies although several authors have defined them more specifically as institutions based on non-hierarchical free associations . anarchism holds the state to be undesirable unnecessary and harmful . while anti-statism is central anarchism entails opposing authority or hierarchical organization in the conduct of all human relations including but not limited to the state system .";
		List<String> sentence = new ArrayList<String>();
		for (String word : testText.split(" ")) {
			if (word.equals(".")) {
				sentences.add(sentence);
				sentence = new ArrayList<String>();
			} else if (!word.isEmpty() || !word.contains(",") || !word.equals(" ")) {
				sentence.add(word);
			}
		}
	}

	@Test
	public void build() {
		Word2VecPythonBuilder factory = new Word2VecPythonBuilder();
		w2vp = factory.setSentences(sentences).setSize(200).setIter(1).setMin_count(1).build();
		assert (w2vp != null);
		logger.info(String.valueOf(w2vp.getVocabSize()));
		assert (w2vp.getVocabSize() == 59);
	}

	@Test
	public void build_vocab() throws IOException {
		build();
		w2vp.build_vocab(Arrays.asList(Arrays.asList("bonjour", "le", "Monde")), 10, true);
		assert (w2vp.isWordInVocab("bonjour"));
	}

	@Test
	public void train() throws IOException {
		build_vocab();
		w2vp.train(Arrays.asList(Arrays.asList("bonjour", "le", "Monde")), 1);
		try {
			logger.info(String.valueOf(w2vp.getVector("bonjour").toArray().length));
			assert (w2vp.getVector("bonjour") != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getVocab() throws IOException {
		train();
		assert (w2vp.getVocab().contains("anarchism"));
	}

	@Test
	public void getVectorVocab() throws IOException {
		train();
		logger.info(w2vp.getVectorVocab().get("bonjour").toArray()[0].toString());
		assert (w2vp.getVectorVocab().containsKey("bonjour"));
		assert (w2vp.getVectorVocab().get("bonjour").toArray()[0] instanceof Double);
	}

	@Test
	public void save() throws IOException {
		train();
		w2vp.save("Test.bin");
		assert (new File("Test.bin").exists());
	}

	@Test
	public void load() throws IOException {
		save();
		Word2VecPythonBuilder factory = new Word2VecPythonBuilder();
		w2vp = null;
		w2vp = factory.load("Test.bin");
		logger.info(w2vp.getVectorVocab().get("bonjour").toArray()[0].toString());
		assert (w2vp.getVectorVocab().containsKey("bonjour"));
	}
}
