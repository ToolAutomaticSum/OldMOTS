package main.java.liasd.asadera.model.task.process.caracteristicBuilder.vector.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.Coreness;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.GraphBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedOut;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.vector.TfIdfVectorSentence;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.Query;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class KCoreQuery extends TfIdfVectorSentence
		implements GraphBasedIn<WordIndex, DefaultWeightedEdge>, QueryBasedOut {

	private Query query;

	private SimpleWeightedGraph<WordIndex, DefaultWeightedEdge> graph;

	public KCoreQuery(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(DefaultWeightedEdge.class, WordIndex.class, GraphBasedIn.class));
		listParameterOut.add(new ParameterizedType(null, double[].class, QueryBasedOut.class));
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		KCoreQuery p = new KCoreQuery(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		query = new Query();
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) throws Exception {
		super.processCaracteristics(listCorpus);

		double[] vector = new double[graph.vertexSet().size()];

		// TODO Strange that graph isn't reset but do we need to pass it via an
		// interface ?
		for (Corpus corpus : listCorpus) {
			Coreness<WordIndex, DefaultWeightedEdge> core = new Coreness<WordIndex, DefaultWeightedEdge>(graph);
			List<WordIndex> listMaxKCore = new ArrayList<WordIndex>();
			int maxCore = core.getDegeneracy();
			for (Entry<WordIndex, Integer> e : core.getScores().entrySet())
				if (e.getValue() == maxCore)
					listMaxKCore.add(e.getKey());

			for (WordIndex word : listMaxKCore)
				vector[word.getiD()] += word.getTfCorpus(corpus.getiD()) * word.getIdf(index.getNbDocument());
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
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return super.isOutCompatible(compatibleMethod) || compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(null, double[].class, QueryBasedIn.class));
	}
 
	@Override
	public void setCompatibility(ParameterizedMethod compatibleMethod) {
		super.setCompatibility(compatibleMethod);
		if (compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(null, double[].class, QueryBasedIn.class)))
			((QueryBasedIn) compatibleMethod).setQuery(query);
	}
}
