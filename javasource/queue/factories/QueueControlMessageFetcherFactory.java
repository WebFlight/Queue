package queue.factories;

import com.mendix.core.Core;

import queue.helpers.QueueControlMessageFetcher;

public class QueueControlMessageFetcherFactory {
	
	public QueueControlMessageFetcherFactory() {
		
	}
	
	public QueueControlMessageFetcher<?> getQueueControlMessageFetcher() {
		return new QueueControlMessageFetcher<>(Core.createSystemContext());
	}

}
