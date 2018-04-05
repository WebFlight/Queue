package system;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.mendix.core.actionmanagement.IActionRegistrator;

@Component(immediate = true)
public class UserActionsRegistrar
{
  @Reference
  public void registerActions(IActionRegistrator registrator)
  {
    registrator.bundleComponentLoaded();
    registrator.registerUserAction(queue.actions.AddJobToQueue.class);
    registrator.registerUserAction(queue.actions.GetQueueOverview.class);
    registrator.registerUserAction(queue.actions.InitializeQueue.class);
    registrator.registerUserAction(queue.actions.RemoveJob.class);
    registrator.registerUserAction(queue.actions.ShutdownQueue.class);
    registrator.registerUserAction(system.actions.VerifyPassword.class);
  }
}
