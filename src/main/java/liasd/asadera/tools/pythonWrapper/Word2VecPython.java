package main.java.liasd.asadera.tools.pythonWrapper;

import java.util.List;
import java.util.Map;

public interface Word2VecPython {

	public void save(String fname);

	public String test(String test);

	public void build_vocab(List<List<String>> sentences, int progress_per, boolean update);

	public void build_vocab_file(String fname, int progress_per, boolean update);

	public void build_vocab_file(String fname, int min_count, int progress_per, boolean update);

	public boolean isWordInVocab(String word);

	public void train(List<List<String>> sentences, int epochs, double start_alpha, double end_alpha, int word_count,
			int queue_factor, double report_delay);

	public void train(List<List<String>> sentences, int epochs);

	public void train_file(String fname, int epochs, double start_alpha, double end_alpha, int word_count,
			int queue_factor, double report_delay);

	public void train_file(String fname, int epochs);

	public Long getVocabSize();

	public List<String> getVocab();

	public Map<String, List<Double>> getVectorVocab();

	public List<Double> getVector(String word);
}
