package etomica.virial.cluster2.mvc;

public interface WizardPageView extends View {

  public void configure(State state);

  public void attach(String key, Object object);

  public void attachDone();

  public void detach(String key, Object object);

  public void detachDone();

  // sets the object to call back to upon valid view completion
  public void setResponseListener(ViewResponseListener listener);
}