package queue.factories;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;

import queue.helpers.QueueControlMessageFetcherExecutor;
import queue.utilities.CoreUtility;
import queue.utilities.QueueControlMessageFetcher;

public class QueueControlMessageFetcherFactory {
	
	public QueueControlMessageFetcherFactory() {
		
	}
	
	public QueueControlMessageFetcher<?> getQueueControlMessageFetcher(ILogNode logger, CoreUtility coreUtility) {
		return new QueueControlMessageFetcher<>(logger, Core.createSystemContext(), coreUtility, new QueueControlMessageFetcherExecutor());
	}

}
