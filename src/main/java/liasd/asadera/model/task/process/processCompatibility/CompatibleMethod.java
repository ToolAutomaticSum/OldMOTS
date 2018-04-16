package main.java.liasd.asadera.model.task.process.processCompatibility;

public abstract interface CompatibleMethod {
	public boolean isCompatible(CompatibleMethod compMethod);

	public void setCompatibility(CompatibleMethod compMethod, boolean out);
}
