package queue.helpers;

import java.util.List;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.repositories.QueueRepository;
import queue.utilities.CoreUtility;

public class QueueInfoUpdaterExecutor {
	
	public void execute(IContext context, ILogNode logger, QueueRepository queueRepository, CoreUtility coreUtility) throws CoreException {
		
		List<IMendixObject> queueInfoObjects = queueRepository.getQueueInfos(context);
		List<IMendixObject> oldQueueInfos = null;
		
		long instanceIndex = coreUtility.getInstanceIndex();
		
		try {
			oldQueueInfos = coreUtility.retrieveXPathQuery(context, "//Queue.QueueInfo[InstanceIndex=" + instanceIndex + "]");
		} catch (CoreException e) {  
			throw new CoreException("Could not retrieve QueueInfo objects for Instance Index " + instanceIndex + " from database.", e);
		}
		
		coreUtility.delete(context, oldQueueInfos);
		
		for (IMendixObject queueInfoObject : queueInfoObjects) {
			queueInfoObject.setValue(context, "InstanceIndex", instanceIndex);
		}

		coreUtility.commit(context, queueInfoObjects);
	}

}
