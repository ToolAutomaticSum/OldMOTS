import java.util.ArrayList;
import java.util.List;

import control.Controller;
import model.SModel;
import model.task.process.AbstractProcess;
import optimize.AlgoGenetique;
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
					ag = new AlgoGenetique(0, 0.66, 0.75, 0, 100, 100, 0.14, p);
					ag.init();
					ag.optimize();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static List<String> parseArgs(String[] args) throws Exception{
		if (args.length == 0){
			throw new NullPointerException("No argument !");
		}
		List<String> listReturn = new ArrayList<String>();
		List<String> listArg = new ArrayList<String>();
		for (String str : args)
			listArg.add(str);
		
		while (listArg.size() != 0) {
			if (listArg.contains("-nbc")) {
				listReturn.add(listArg.get(listArg.indexOf("-nbc")+1));
				listArg.remove(listArg.indexOf("-nbc")+1);
				listArg.remove(listArg.indexOf("-nbc"));
			} else if (listArg.contains("-test_path")) {
				listReturn.add(listArg.get(listArg.indexOf("-test_path")+1));
				listArg.remove(listArg.indexOf("-test_path")+1);
				listArg.remove(listArg.indexOf("-test_path"));
			} else if (listArg.contains("-language_path")) {
				listReturn.add(listArg.get(listArg.indexOf("-language_path")+1));
				listArg.remove(listArg.indexOf("-language_path")+1);
				listArg.remove(listArg.indexOf("-language_path"));
			} else
				throw new NullPointerException("Out of valid parameter !");
		}
		return listReturn;
	}
}
