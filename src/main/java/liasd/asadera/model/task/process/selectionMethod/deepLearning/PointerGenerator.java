package main.java.liasd.asadera.model.task.process.selectionMethod.deepLearning;

import java.util.List;

import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;

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
	public void trainModel(List<Corpus> listCorpus) {
	}
	
	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		return null;
	}

}
