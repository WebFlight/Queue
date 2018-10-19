package queue.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.utilities.CoreUtility;

public class QueueControlMessageFetcherExecutor {
	
	public void execute(IContext context, CoreUtility coreUtility, ILogNode logger) throws CoreException {
		List<IMendixObject> queueControlMessages = coreUtility.retrieveXPathQuery(context, "//Queue.QueueControlMessage[Queue.QueueControlMessage_XASInstance/System.XASInstance/XASId='" + coreUtility.getXASId() + "']");
		queueControlMessages.forEach(o ->
			{
				Map<String, Object> inputMap = new HashMap<>();
				inputMap.put("QueueControlMessage", o);
				try {
					coreUtility.executeAsync(context, "Queue.IVK_ProcessQueueControlMessage", true, inputMap);
				} catch (CoreException e) {
					logger.error("Could not process Queue Control Message with ID " + o.getId().toLong() + ".", e);
				}
			}
		);
	}

}
