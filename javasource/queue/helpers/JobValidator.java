package queue.helpers;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;

import queue.proxies.Job;
import queue.repositories.QueueRepository;

public class JobValidator {
	
	private ILogNode logger;
	private MicroflowValidator microflowValidator;
	
	public JobValidator (ILogNode logger, MicroflowValidator microflowValidator) {
		this.logger = logger;
		this.microflowValidator = microflowValidator;
	}
	
	public boolean isValid (Job job) {
		
		return 
				checkQueue(job) &&
				checkMicroflowName(job) &&
				checkMaxRetries(job) &&
				checkDelay(job) &&
				checkRetry(job);
	}
	
	private boolean checkQueue(Job job) {
		String queue = job.getQueue();
		
		if (queue == "" || queue == null) {
			this.logger.error("Queue name is missing.");
			return false;
		}
		
		if (QueueRepository.queueExists(queue)) {
			this.logger.debug("Queue with name " + queue + " found.");
			return true;
		}
		
		this.logger.error("Queue with name " + queue +  " has not been initialized.");
		return false;
	}
	
	private boolean checkMicroflowName(Job job) {
		String microflowName = job.getMicroflowName();
		
		if (microflowName == "" || microflowName == null) {
			this.logger.error("Microflow name is missing.");
			return false;
		}
		
		if (this.microflowValidator.validate(microflowName)) {
			this.logger.debug("Microflow " + microflowName + " found.");
			return true;
		}
		
		String microflowSuggestion = this.microflowValidator.getClosestMatch(microflowName);
		
		if  (microflowSuggestion == "") {
			this.logger.error("Microflow " + microflowName + " could not be found.");
			return false;
		}
		
		this.logger.error("Microflow " + microflowName + " could not be found. Did you mean " + microflowSuggestion + "?");
		
		return false;
	}
	
	private boolean checkMaxRetries(Job job) {
		int maxRetries = job.getmaxRetries();
		
		if (maxRetries >= 0) {
			this.logger.debug("Max retries of " + maxRetries + " is valid");
			return true;
		}
		
		this.logger.error("Max retries of " + maxRetries + " is invalid and should be a number larger than or equal to 0.");
		return false;
	}
	
	private boolean checkDelay(Job job) {
		int delay = job.getDelay();
		
		if (delay >= 0) {
			this.logger.debug("Delay of " + delay + " is valid");
			return true;
		}
		
		this.logger.error("Delay of " + delay + " is invalid and should be a number larger than or equal to 0.");
		return false;
	}
	
	private boolean checkRetry(Job job) {
		int retry = job.getRetry();
		
		if (retry == 0) {
			this.logger.debug("Delay of 0 is valid");
			return true;
		}
		
		job.setRetry(0);
		try{
			job.commit();
		} catch (CoreException e) {
			this.logger.error("Could not commit job when retry is set to 0.");
			return false;
		}
		this.logger.warn("It is not allowed to set retry when adding a job to the queue. Will be set to 0.");
		return true;
	}
}
