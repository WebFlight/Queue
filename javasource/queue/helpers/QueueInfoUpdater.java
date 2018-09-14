package queue.helpers;

import java.util.List;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.actionmanagement.CoreAction;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.proxies.constants.Constants;
import queue.repositories.QueueRepository;

public class QueueInfoUpdater<R> extends CoreAction<R> {
	
	private static ILogNode logger = Core.getLogger(Constants.getLOGNODE()); 
	
	public QueueInfoUpdater(IContext arg0) {
		super(arg0);
	}

	@Override
	public R execute() {
		IContext context = this.getContext();
		
		List<IMendixObject> queueInfoObjects = QueueRepository.getInstance().getQueueInfos(context);
		List<IMendixObject> xasInstances = null;
		List<IMendixObject> oldQueueInfos = null;
		
		try {
			xasInstances = Core.retrieveXPathQuery(context, "//System.XASInstance[XASId='" + Core.getXASId() + "']");
		} catch (CoreException e) {
			logger.error("Could not retrieve XAS Instance from database.", e);
		}
		IMendixObject xasInstance = xasInstances.get(0);
		
		try {
			oldQueueInfos = Core.retrieveXPathQuery(context, "//Queue.QueueInfo[Queue.QueueInfo_XASInstance=" + xasInstance.getId().toLong() + "]");
		} catch (CoreException e) {  
			logger.error("Could not retrieve QueueInfo objects for XAS Instance from database.", e);
		}
		
		Core.delete(context, oldQueueInfos);
		
		for (IMendixObject queueInfoObject : queueInfoObjects) {
			queueInfoObject.setValue(context, "Queue.QueueInfo_XASInstance", xasInstance.getId());
		}

		Core.commit(context, queueInfoObjects);
		
		return null;
	}

}
