package main.java.liasd.asadera.model.task.process.caracteristicBuilder.graphBased;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.GraphBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.GraphBasedOut;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class GraphOfWordsBuilder extends AbstractCaracteristicBuilder
		implements IndexBasedIn<WordIndex>, GraphBasedOut<WordIndex, DefaultWeightedEdge> {

	private SimpleWeightedGraph<WordIndex, DefaultWeightedEdge> graph;
	private Index<WordIndex> index;
	private int slidingWindow = 2;

	public GraphOfWordsBuilder(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(NGram.class, Index.class, IndexBasedIn.class));
		listParameterIn.add(new ParameterizedType(WordIndex.class, Index.class, IndexBasedIn.class));
		listParameterOut.add(new ParameterizedType(DefaultWeightedEdge.class, WordIndex.class, GraphBasedOut.class));
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		GraphOfWordsBuilder p = new GraphOfWordsBuilder(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		slidingWindow = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "Window"));
		
		graph = new SimpleWeightedGraph<WordIndex, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) throws Exception {
		for (WordIndex word : index.values())
			graph.addVertex(word);

		for (Corpus corpus : listCorpus) {
			for (TextModel text : corpus)
				for (SentenceModel sen : text) {
					int n = sen.getN();
					for (int i = 0; i < sen.size(); i++)
						for (int j = i + n; j < Math.min(i + slidingWindow + 1, sen.size()); j++) {
							WordIndex w1 = sen.get(i);
							WordIndex w2 = sen.get(j);
							if (i != j && w1 != w2) {
								if (!graph.containsVertex(w1))
									graph.addVertex(w1);
								if (!graph.containsVertex(w2))
									graph.addVertex(w2);
								if (!graph.containsEdge(w1, w2))
									graph.addEdge(w1, w2);
								else {
									DefaultWeightedEdge edge = graph.getEdge(w1, w2);
									graph.setEdgeWeight(edge, graph.getEdgeWeight(edge) + 1);
								}
							}
						}
				}
		}
	}

	@Override
	public void finish() {
		graph.removeAllEdges(new ArrayList<DefaultWeightedEdge>(graph.edgeSet()));
		graph.removeAllVertices(new ArrayList<WordIndex>(graph.vertexSet()));
	}

	@Override
	public void setIndex(Index<WordIndex> index) {
		this.index = index;
	}

	@Override
	public Graph<WordIndex, DefaultWeightedEdge> getGraph() {
		return graph;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(DefaultWeightedEdge.class, WordIndex.class, GraphBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParameterizedMethod compatibleMethod) {
		((GraphBasedIn<WordIndex, DefaultWeightedEdge>) compatibleMethod).setGraph(graph);
	}
}
