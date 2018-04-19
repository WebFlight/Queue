package queue.helpers;

import com.mendix.logging.ILogNode;

import queue.repositories.QueueRepository;

public class QueueValidator {

	ILogNode logger;
	
	public QueueValidator (ILogNode logger) {
		this.logger = logger;
	}
	
	public boolean isValid (QueueRepository queueRepository, String name, int poolSize, int priority) {
		return checkName(queueRepository, name) &&
				checkPoolSize(poolSize) &&
				checkPriority(priority);
	}
	
	private boolean checkName(QueueRepository queueRepository, String name) {
		if(name == null) {
			this.logger.error("Queue name is missing.");
			return false;
		}
		
		if (name.equals("")) {
			this.logger.error("Queue name is missing.");
			return false;
		}
		
		if (queueRepository.queueExists(name)) {
			this.logger.error("Queue " + name + " has already been initialized.");
			return false;
		}
		
		return true;
	}
	
	private boolean checkPoolSize (int poolSize) {
		if (poolSize > 0) {
			this.logger.debug("Pool size " + poolSize + " is a valid number.");
			return true;
		}
		
		this.logger.error("Pool size of " + poolSize + " is not valid. Number should be greater than 0.");
		return false;
	}
	
	private boolean checkPriority (int priority) {
		if (priority >= Thread.MIN_PRIORITY && priority <= Thread.MAX_PRIORITY) {
			this.logger.debug("Thread priority " + priority + " is a valid number.");
			return true;
		}
		
		this.logger.error("Thread priority " + priority + " is not valid, should be a number between " + Thread.MIN_PRIORITY + " and " + Thread.MAX_PRIORITY + ".");
		return false;
	}
}
