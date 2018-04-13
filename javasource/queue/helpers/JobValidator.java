package queue.helpers;

import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import queue.proxies.ENU_TimeUnit;
import queue.proxies.Job;
import queue.repositories.QueueRepository;

public class JobValidator {
	
	private ILogNode logger;
	private MicroflowValidator microflowValidator;
	
	public JobValidator (ILogNode logger, MicroflowValidator microflowValidator) {
		this.logger = logger;
		this.microflowValidator = microflowValidator;
	}
	
	public boolean isValid (IContext context, QueueRepository queueRepository, Job job) {
		return 
				checkQueue(context, queueRepository, job) &&
				checkMicroflowName(context, job) &&
				checkMaxRetries(context, job) &&
				checkCurrentDelay(context, job) &&
				checkBaseDelay(context, job) &&
				checkRetry(context, job) &&
				checkDelayUnit(context, job);
	}
	
	private boolean checkQueue(IContext context, QueueRepository queueRepository, Job job) {
		String queue = job.getQueue(context);
		
		boolean validString = checkIfStringExistsAndNotEmpty(queue, "Queue");
		
		if (validString) {
			if (queueRepository.queueExists(queue)) {
				this.logger.debug("Queue with name " + queue + " found.");
				return true;
			}
			
			this.logger.error("Queue with name " + queue +  " has not been initialized.");
			return false;
		}
		
		return false;
	}
	
	private boolean checkMicroflowName(IContext context, Job job) {
		String microflowName = job.getMicroflowName(context);
		
		boolean validString = checkIfStringExistsAndNotEmpty(microflowName, "MicroflowName");
				
		if (validString) {
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
		
		return false;
	}
	
	private boolean checkMaxRetries(IContext context, Job job) {
		int maxRetries = job.getMaxRetries(context);
		return checkIfIntegerPositiveOrEqualToZero(maxRetries, "MaxRetries");
	}
	
	private boolean checkCurrentDelay(IContext context, Job job) {
		int currentDelay = job.getCurrentDelay(context);
		return checkIfIntegerPositiveOrEqualToZero(currentDelay, "CurrentDelay");
	}
	
	private boolean checkBaseDelay(IContext context, Job job) {
		int baseDelay = job.getBaseDelay(context);
		return checkIfIntegerPositiveOrEqualToZero(baseDelay, "BaseDelay");
	}
	
	private boolean checkRetry(IContext context, Job job) {
		int retry = job.getRetry(context);
		return checkIfIntegerPositiveOrEqualToZero(retry, "Retry");
	}
	
	private boolean checkDelayUnit(IContext context, Job job) {
		ENU_TimeUnit delayUnit = job.getDelayUnit(context);
		if (delayUnit == null) {
			this.logger.error("DelayUnit cannot be empty.");
			return false;
		}
		this.logger.debug("DelayUnit is valid.");
		return true;
	}
	
	private boolean checkIfIntegerPositiveOrEqualToZero(int number, String attributeName) {
		if (number >= 0) {
			this.logger.debug(attributeName + " of " + number + " is valid");
			return true;
		}
		this.logger.error(attributeName + " of " + number + " is invalid and should be a number larger than or equal to 0.");
		return false;
	}
	
	private boolean checkIfStringExistsAndNotEmpty(String text, String attributeName) {
		if (text == null) {
			this.logger.error(attributeName + " is missing");
			return false;
		}
		if (text.equals("")) {
			this.logger.error(attributeName + " is missing");
			return false;
		}
		this.logger.debug(attributeName + " of " + text + " is valid");
		return true;
	}
}
