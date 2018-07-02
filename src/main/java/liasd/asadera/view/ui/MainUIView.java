package main.java.liasd.asadera.view.ui;

import java.util.Observable;

import main.java.liasd.asadera.view.AbstractView;

public class MainUIView extends AbstractView {

	private StartFrame start;
	
	public MainUIView() {
		start = new StartFrame();
	}


	@Override
	public void init() throws Exception {
	}
	
	@Override
	public void display() throws Exception {
		start.setVisible(true);
	}

	@Override
	public void close() {
		System.exit(0);
	}

	@Override
	public void update(Observable o, Object arg) {
	}
}
