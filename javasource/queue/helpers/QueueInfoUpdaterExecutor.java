package queue.helpers;

import java.util.List;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.repositories.QueueRepository;
import queue.utilities.CoreUtility;

public class QueueInfoUpdaterExecutor {
	
	public void execute(IContext context, ILogNode logger, QueueRepository queueRepository, CoreUtility coreUtility) {
		
		List<IMendixObject> queueInfoObjects = queueRepository.getQueueInfos(context);
		List<IMendixObject> xasInstances = null;
		List<IMendixObject> oldQueueInfos = null;
		
		try {
			xasInstances = coreUtility.retrieveXPathQuery(context, "//System.XASInstance[XASId='" + coreUtility.getXASId() + "']");
		} catch (CoreException e) {
			logger.error("Could not retrieve XAS Instance from database.", e);
		}
		IMendixObject xasInstance = xasInstances.get(0);
		
		try {
			oldQueueInfos = coreUtility.retrieveXPathQuery(context, "//Queue.QueueInfo[Queue.QueueInfo_XASInstance=" + xasInstance.getId().toLong() + "]");
		} catch (CoreException e) {  
			logger.error("Could not retrieve QueueInfo objects for XAS Instance from database.", e);
		}
		
		coreUtility.delete(context, oldQueueInfos);
		
		for (IMendixObject queueInfoObject : queueInfoObjects) {
			queueInfoObject.setValue(context, "Queue.QueueInfo_XASInstance", xasInstance.getId());
		}

		coreUtility.commit(context, queueInfoObjects);
	}

}
