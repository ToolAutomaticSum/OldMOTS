package view;

import java.util.Observable;
import java.util.Observer;

import control.Controller;

public abstract class AbstractView implements Observer {
	protected Controller ctrl;
	
	public abstract void display();
	public abstract void close();
	
	public Controller getCtrl() {
		return ctrl;
	}
	public void setCtrl(Controller ctrl) {
		this.ctrl = ctrl;
	}
	
	public abstract void update(Observable o, Object arg);
}
