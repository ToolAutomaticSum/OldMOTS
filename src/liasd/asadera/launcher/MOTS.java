package liasd.asadera.launcher;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import liasd.asadera.control.AbstractController;
import liasd.asadera.control.ComparativeController;
import liasd.asadera.control.LearningController;
import liasd.asadera.control.SummarizeController;
import liasd.asadera.model.AbstractModel;
import liasd.asadera.model.ComparativeModel;
import liasd.asadera.model.LearningModel;
import liasd.asadera.model.SummarizeModel;
import liasd.asadera.model.task.process.AbstractProcess;
import liasd.asadera.optimize.AlgoGenetique;
import liasd.asadera.view.CommandView;

public class MOTS {

	/**
	 * MOTS main command line launcher.
	 * -c : configuration's file path. REQUIRED
	 * -m : multicorpus configuration's file path RECOMMENDED
	 * -o : hyperparameter optimization mode with genetic algorithm
	 * -C : Comparative Summarization Mode EXPERIMENTAL
	 * -L : Model learning mode (for LDA, Word Embeddings)
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CommandLine commandLine;
		Option option_C = Option.builder("C").required(false).desc("Mode for comparative summarization.")
				.longOpt("ComparativeSummarization").build();
		Option option_L = Option.builder("L").required(false).desc("Mode for model learning.").longOpt("LearninModel")
				.build();
		Option option_c = Option.builder("c").required(true).hasArg().desc("The path of configuration file.")
				.longOpt("configurationFilePath").build();
		Option option_m = Option.builder("m").required(false).hasArg()
				.desc("The path of multicorpus configuration file.").longOpt("configurationMultiCorpusFilePath")
				.build();
		Option option_o = Option.builder("o").required(false)
				.desc("Run the genetic algorithm for parameter optimisation.").longOpt("optimization").build();
		Options options = new Options();
		CommandLineParser parser = new DefaultParser();

		options.addOption(option_C);
		options.addOption(option_L);
		options.addOption(option_c);
		options.addOption(option_m);
		options.addOption(option_o);

		CommandView view;
		AbstractModel model;
		AbstractController controller;

		try {
			commandLine = parser.parse(options, args);

			String configFileName = new File(commandLine.getOptionValue("c")).getName();
			String name = configFileName + File.separator + "UnknownMultiCorpus";
			if (commandLine.hasOption("m")) {
				String corpusFileName = new File(commandLine.getOptionValue("m")).getName();
				/*String name for the save of multicorpus preprocess and results*/
				name = configFileName.substring(7, configFileName.length() - 4) + File.separator
						+ corpusFileName.substring(0, corpusFileName.length() - 4);
				view = new CommandView(commandLine.getOptionValue("c"), commandLine.getOptionValue("m"));
			}
			else
				view = new CommandView(commandLine.getOptionValue("c"));

			if (commandLine.hasOption("C")) {
				model = new ComparativeModel();
				model.setName(name);
				controller = new ComparativeController(model, view);
				controller.displayView();
			} else if (commandLine.hasOption("L")) {
				model = new LearningModel();
				model.setName(name);
				controller = new LearningController(model, view);
				controller.displayView();
			} else {
				model = new SummarizeModel();
				model.setName(name);
				controller = new SummarizeController(model, view);
				if (commandLine.hasOption("o")) {
					view.init();
					model.loadMultiCorpusModels();
					for (AbstractProcess p : model.getProcess()) {
						p.setModel(model);
						p.initADN();
						AlgoGenetique ag;
						try {
							//TODO Genetic parameter hardcoded
							ag = new AlgoGenetique(0, 0.5, 0.50, 0, 50, 100, 0.14, p);
							ag.init();
							ag.optimize();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else
					controller.displayView();
			}
		} catch (ParseException exception) {
			System.err.print("Parse error: ");
			System.err.println(exception.getMessage());
		}
	}
}