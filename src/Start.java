import control.Controller;
import model.SModel;
import view.CommandView;

public class Start {

	public static void main(String[] args) {
		CommandView view = new CommandView(args);
		SModel model = new SModel();
		Controller controller = new Controller(model, view);
		controller.displayView();
	}
}
