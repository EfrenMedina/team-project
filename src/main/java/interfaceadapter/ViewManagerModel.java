package interfaceadapter;

/** Holds the name of the screen currently on show. The {@code ViewManager} listens to it. */
public class ViewManagerModel extends ViewModel<String> {

  /** Creates the model with no screen selected yet. */
  public ViewManagerModel() {
    super("view manager");
    setState("");
  }
}
