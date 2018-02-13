package liasd.asadera.model.task.process.indexBuilder.wordEmbeddings;

import java.util.List;

import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;

public class GraphWeightingWordToVec extends WordToVec {

	public GraphWeightingWordToVec(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		super.processIndex(listCorpus);
	}
}
