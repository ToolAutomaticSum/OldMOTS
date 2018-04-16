package main.java.liasd.asadera.model.task;

public interface AbstractTask {
	public abstract void init() throws Exception;

	public abstract void process() throws Exception;

	public abstract void finish() throws Exception;
}
