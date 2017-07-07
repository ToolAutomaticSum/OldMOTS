package launcher;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import control.ComparativeController;
import control.AbstractController;
import control.LearningController;
import control.SummarizeController;
import model.AbstractModel;
import model.ComparativeModel;
import model.LearningModel;
import model.SummarizeModel;
import model.task.process.AbstractProcess;
import optimize.AlgoGenetique;
import view.CommandView;

public class Start {

	public static void main(String[] args) throws Exception {
        CommandLine commandLine;
        Option option_C = Option.builder("C")
	            .required(false)
	            .desc("Mode for comparative summarization.")
	            .longOpt("ComparativeSummarization")
	            .build();
        Option option_L = Option.builder("L")
	            .required(false)
	            .desc("Mode for model learning.")
	            .longOpt("LearninModel")
	            .build();
        Option option_c = Option.builder("c")
            .required(true)
            .hasArg()
            .desc("The path of configuration file.")
            .longOpt("configurationFilePath")
            .build();
        Option option_o = Option.builder("o")
            .required(false)
            .desc("Run the genetic algorithm for parameter optimisation.")
            .longOpt("optimization")
            .build();
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();

        options.addOption(option_C);
        options.addOption(option_L);
        options.addOption(option_c);
        options.addOption(option_o);
        
        CommandView view;
        AbstractModel model;
        AbstractController controller;

        try
        {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption("C"))
            {
            	view = new CommandView(commandLine.getOptionValue("c"));
    			model = new ComparativeModel();
    			controller = new ComparativeController(model, view);
    			controller.displayView();
            }
            else if (commandLine.hasOption("L")) {
            	view = new CommandView(commandLine.getOptionValue("c"));
    			model = new LearningModel();
    			controller = new LearningController(model, view);
    			controller.displayView();
            }
            else {
	            if (commandLine.hasOption("c"))
	            {
	            	view = new CommandView(commandLine.getOptionValue("c"));
	    			model = new SummarizeModel();
	    			controller = new SummarizeController(model, view);
	            }
	            else
	            	throw new NullPointerException("Need configuration file path to make first initialization !");
	            if (commandLine.hasOption("o"))
	            {
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
	            else
	    			controller.displayView();
            }
            	
        }
        catch (ParseException exception)
        {
            System.err.print("Parse error: ");
            System.err.println(exception.getMessage());
        }
	}
}
