package main.java.liasd.asadera.view;

import java.util.Observable;
import java.util.Observer;

import main.java.liasd.asadera.control.AbstractController;

public abstract class AbstractView implements Observer {
	protected AbstractController ctrl;

	public abstract void display() throws Exception;

	public abstract void close();

	public AbstractController getCtrl() {
		return ctrl;
	}

	public void setCtrl(AbstractController ctrl) {
		this.ctrl = ctrl;
	}

	public abstract void update(Observable o, Object arg);
}
