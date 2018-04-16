package main.java.liasd.asadera.tools;

import java.util.Arrays;

public class Caracteristic {
	protected double d;
	protected double[] dTab;

	public Caracteristic() {
		super();
		d = 0;
		dTab = null;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	public double[] getdTab() {
		return dTab;
	}

	public void setdTab(double[] dTab) {
		this.dTab = dTab;
	}

	@Override
	public String toString() {
		return ((d == 0) ? "" : d) + ((dTab == null) ? "" : Arrays.toString(dTab));
	}

}
