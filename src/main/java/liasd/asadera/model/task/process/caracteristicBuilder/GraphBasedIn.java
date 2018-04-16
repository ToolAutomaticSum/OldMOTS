package main.java.liasd.asadera.model.task.process.caracteristicBuilder;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public interface GraphBasedIn<V extends WordIndex, E extends DefaultWeightedEdge> {

	public void setGraph(Graph<V, E> graph);
}
