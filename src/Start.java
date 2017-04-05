import control.Controller;
import model.SModel;
import model.task.process.AbstractProcess;
import optimize.AlgoGenetique;
import optimize.SupportADNException;
import view.CommandView;

public class Start {

	public static void main(String[] args) throws Exception {
		if (args.length == 2) {
			CommandView view = new CommandView(args);
			SModel model = new SModel();
			Controller controller = new Controller(model, view);
			controller.displayView();
		} else {
			CommandView view = new CommandView(args);
			SModel model = new SModel();
			Controller controller = new Controller(model, view);
			view.init();
			model.loadMultiCorpusModels();
			for (AbstractProcess p : model.getProcess()) {
				p.setModel(model);
				p.initADN();
				AlgoGenetique ag;
				try {
					ag = new AlgoGenetique(0, 0.66, 0.75, 0, 320, 1000, 0.9, p);
					ag.init();
					ag.optimize();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
