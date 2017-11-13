package liasd.asadera.model.task.process.caracteristicBuilder.vector.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.Coreness;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import liasd.asadera.model.task.process.caracteristicBuilder.GraphBasedIn;
import liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedIn;
import liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedOut;
import liasd.asadera.model.task.process.caracteristicBuilder.vector.TfIdfVectorSentence;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.Query;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class KCoreQuery extends TfIdfVectorSentence implements GraphBasedIn<WordIndex, DefaultWeightedEdge>, QueryBasedOut {

	private Query query;

	private SimpleWeightedGraph<WordIndex, DefaultWeightedEdge> graph;
	
	public KCoreQuery(int id) throws SupportADNException {
		super(id);

		listParameterOut.add(new ParametrizedType(null, double[].class, QueryBasedOut.class));
		
		query = new Query();
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		KCoreQuery p = new KCoreQuery(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) throws Exception {
		super.processCaracteristics(listCorpus);
		
		double[] vector = new double[graph.vertexSet().size()];
		
		//TODO Strange that graph isn't reset but do we need to pass it via an interface ?
		for (Corpus corpus : listCorpus) {
			Coreness<WordIndex, DefaultWeightedEdge> core = new Coreness<WordIndex, DefaultWeightedEdge>(graph);
			List<WordIndex> listMaxKCore = new ArrayList<WordIndex>();
			int maxCore = core.getDegeneracy();
			for (Entry<WordIndex, Integer> e : core.getScores().entrySet())
				if (e.getValue() == maxCore)
					listMaxKCore.add(e.getKey());
			System.out.println("Degeneracy = " + maxCore + "\nList key word from max core : ");
			System.out.println(listMaxKCore);
			for (WordIndex word : listMaxKCore)
				vector[word.getiD()] += word.getTfCorpus(corpus.getiD())*word.getIdf();
		}
		query.setQuery(vector);
	}

	@Override
	public void finish() {
		query.clear();
	}

	@Override
	public void setGraph(Graph<WordIndex, DefaultWeightedEdge> graph) {
		this.graph = (SimpleWeightedGraph<WordIndex, DefaultWeightedEdge>) graph;
	}

	@Override
	public Query getQuery() {
		return query;
	}
	
	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return super.isOutCompatible(compatibleMethod) || compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(null, double[].class, QueryBasedIn.class));
	}

	/**
	 * donne le/les paramètre(s) d'output en input à la class comp méthode
	 */
	@Override
	public void setCompatibility(ParametrizedMethod compatibleMethod) {
		super.setCompatibility(compatibleMethod);
		if (compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(null, double[].class, QueryBasedIn.class)))
			((QueryBasedIn)compatibleMethod).setQuery(query);
	}
}
