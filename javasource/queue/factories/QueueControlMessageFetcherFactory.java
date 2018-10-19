package queue.factories;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;

import queue.helpers.QueueControlMessageFetcher;

public class QueueControlMessageFetcherFactory {
	
	public QueueControlMessageFetcherFactory() {
		
	}
	
	public QueueControlMessageFetcher<?> getQueueControlMessageFetcher(ILogNode logger) {
		return new QueueControlMessageFetcher<>(logger, Core.createSystemContext());
	}

}
