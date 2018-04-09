package queue.helpers;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.mendix.logging.ILogNode;

import queue.repositories.QueueRepository;

public class QueueController {
	
	private ILogNode logger;
	
	public QueueController (ILogNode logger) {
		this.logger = logger;
	}

	public boolean shutdown(QueueRepository queueRepository, String name, boolean gracefully, boolean awaitTermination, int terminationTimeout) {
		if (queueRepository.queueExists(name) == false) {
			logger.error("Queue with name " + name + " has not been initialized and therefore cannot be terminated.");
			return false;
		}
		
		ScheduledExecutorService queue = queueRepository.getQueue(name);
		
		if (gracefully) {
			return shutdownGracefully(queue, awaitTermination, terminationTimeout);
		}
		
		return shutdownForced(queue, awaitTermination, terminationTimeout);
	}
	
	private boolean shutdownForced(ScheduledExecutorService queue, boolean awaitTermination, int terminationTimeout) {
		queue.shutdownNow();
		
		if (awaitTermination) {
			return awaitTermination(queue, terminationTimeout);
		}
		
		return true;
	}
	
	private boolean shutdownGracefully(ScheduledExecutorService queue, boolean awaitTermination, int terminationTimeout) {
		queue.shutdown();
		
		if (awaitTermination) {
			return awaitTermination(queue, terminationTimeout);
		}
		
		return true;
	}
	
	private boolean awaitTermination (ScheduledExecutorService queue, int terminationTimeout) {
		try {
			if(queue.awaitTermination(terminationTimeout, TimeUnit.SECONDS)) {
				this.logger.info("Queue has been terminated.");
				return true;
			} else {
				this.logger.error("Queue could not be terminated within timeout of " + terminationTimeout + " seconds.");
				return false;
			}
			
		} catch (InterruptedException e) {
			this.logger.error("Queue has been shutdown unexpectedly (thread interrupted).");
			return true;
		}
	}
}
