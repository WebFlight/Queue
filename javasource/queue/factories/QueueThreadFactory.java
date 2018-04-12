package queue.factories;

import java.util.concurrent.ThreadFactory;

import queue.entities.QueueConfiguration;

public class QueueThreadFactory implements ThreadFactory {
	
	private QueueConfiguration queueConfiguration;
	
	public QueueThreadFactory(QueueConfiguration queueConfiguration) {
		this.queueConfiguration = queueConfiguration;
	}
	
	@Override
	public Thread newThread(Runnable arg0) {
		Thread newThread = new Thread(arg0);
		newThread.setPriority(this.queueConfiguration.getPriority());
		return newThread;
	}
}
