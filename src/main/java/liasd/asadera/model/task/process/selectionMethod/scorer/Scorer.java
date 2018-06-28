package main.java.liasd.asadera.model.task.process.selectionMethod.scorer;

import java.lang.reflect.Constructor;

import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Summary;

public abstract class Scorer extends ParameterizedMethod {

	protected AbstractSelectionMethod method;

	public Scorer(AbstractSelectionMethod method) throws SupportADNException {
		super(method.getId());
		this.method = method;
		this.method.getSubMethod().add(this);
	}

	public abstract void init() throws Exception;

	public abstract double getScore(Summary summary) throws Exception;

	public static Scorer instanciateScorer(AbstractSelectionMethod method, String scorer) throws Exception {
		Class<?> cl;
		cl = Class.forName("main.java.liasd.asadera.model.task.process.selectionMethod.scorer." + scorer);
		@SuppressWarnings("rawtypes")
		Constructor ct = cl.getConstructor(AbstractSelectionMethod.class);
		Scorer o = (Scorer) ct.newInstance(method);
		return (Scorer) o;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
	}

}
