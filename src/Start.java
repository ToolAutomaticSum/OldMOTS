import control.Controller;
import model.Model;
import view.CommandView;

public class Start {

	public static void main(String[] args) {
		CommandView view = new CommandView(args);
		Model model = new Model();
		Controller controller = new Controller(model, view);
		controller.displayView();
	}
}
