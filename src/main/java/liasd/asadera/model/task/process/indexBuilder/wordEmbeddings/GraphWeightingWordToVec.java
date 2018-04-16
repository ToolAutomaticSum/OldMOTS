package main.java.liasd.asadera.model.task.process.indexBuilder.wordEmbeddings;

import java.util.List;

import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;

public class GraphWeightingWordToVec extends WordToVec {

	public GraphWeightingWordToVec(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		super.processIndex(listCorpus);
	}
}
