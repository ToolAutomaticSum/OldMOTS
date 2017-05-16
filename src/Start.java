import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import control.Controller;
import model.SModel;
import model.task.process.AbstractProcess;
import optimize.AlgoGenetique;
import view.CommandView;

public class Start {

	public static void main(String[] args) throws Exception {
	        CommandLine commandLine;
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

	        options.addOption(option_c);
	        options.addOption(option_o);

	        try
	        {
	            commandLine = parser.parse(options, args);
	            CommandView view;
	            SModel model;
	            Controller controller;
	            if (commandLine.hasOption("c"))
	            {
	            	view = new CommandView(commandLine.getOptionValue("c"));
	    			model = new SModel();
	    			controller = new Controller(model, view);
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
	        catch (ParseException exception)
	        {
	            System.err.print("Parse error: ");
	            System.err.println(exception.getMessage());
	        }
	}
}
