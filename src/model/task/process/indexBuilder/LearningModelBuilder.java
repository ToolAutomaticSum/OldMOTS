package model.task.process.indexBuilder;

import java.util.HashMap;
import java.util.List;

import model.AbstractModel;
import textModeling.Corpus;
import textModeling.MultiCorpus;

public interface LearningModelBuilder {
	public void learn(List<Corpus> listCorpus, String modelName) throws Exception;
	
	public void liveLearn(List<String> listStringSentence, String modelName) throws Exception;
	
	public void setCurrentMultiCorpus(MultiCorpus currentMultiCorpus);
	
	public void setModel(AbstractModel model);
	
	public HashMap<String, Class<?>> getSupportADN();
}
