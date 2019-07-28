package main.java.liasd.asadera.model.task.process.selectionMethod;

import java.util.ArrayList;
import java.util.List;

import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;

public class LeadBased extends AbstractSelectionMethod  {

	private int nbSen = 8;
	private int size;
	
	public LeadBased(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		nbSen = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "NbSen"));
		size = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "Size"));
	}

	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		List<SentenceModel> summary = new ArrayList<SentenceModel>();
		int nbChar = 0;
		int i = 0;
		int nbDoc = 0;
		while (summary.size() < nbSen && nbChar < size) {
			for (Corpus corpus : listCorpus)
				for (TextModel text : corpus) {
					if (i >= text.size()) {
						nbDoc++;
						if (nbDoc == corpus.size())
							return summary;
						continue;
					}
					if (nbChar + text.get(i).getNbMot() > size)
						return summary;
					summary.add(text.get(i));
					nbChar += text.get(i).getNbMot();
				}
			i++;
		}
		return summary;
	}
	
	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		// TODO Auto-generated method stub

	}
}
