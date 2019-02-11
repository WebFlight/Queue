package queue.usecases;

import java.util.HashMap;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.helpers.JobToQueueAdder;
import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.JobRepository;
import queue.repositories.MicroflowRepository;
import queue.repositories.QueueRepository;
import queue.repositories.ScheduledJobRepository;

public class QueueHandler implements Runnable {
	
	private IMendixIdentifier jobId;
	private ILogNode logger;
	private JobToQueueAdder jobToQueueAdder;
	private ScheduledJobRepository scheduledJobRepository;
	private QueueRepository queueRepository;
	private JobRepository jobRepository;
	private MicroflowRepository microflowRepository;
	private int retry = 0;
	
	public QueueHandler (ILogNode logger, JobToQueueAdder jobToQueueAdder, ScheduledJobRepository scheduledJobRepository, QueueRepository queueRepository, JobRepository jobRepository, MicroflowRepository microflowRepository, IMendixIdentifier jobId) {
		this.jobId = jobId;
		this.logger = logger;
		this.jobToQueueAdder = jobToQueueAdder;
		this.scheduledJobRepository = scheduledJobRepository;
		this.queueRepository = queueRepository;
		this.jobRepository = jobRepository;
		this.microflowRepository = microflowRepository;
	}

	@Override
	public void run() {
		
		Job job = null;
		IContext context = null;
		IMendixObject jobObject = null;
		
		// Retrieve Job from database. Retry is implemented, because it could happen that the Job has not been committed yet. 
		try {
			context = queueRepository.getSystemContext();
			jobToQueueAdder.setTimeZone(context, logger);
			
			int retries = 0;
			int maxRetries = 10;
			while (retries < maxRetries) {
				logger.debug("Trying to retrieve job object. Attempt " + (retries + 1) + " of 10.");
				jobObject = jobRepository.getJob(context, jobId);
				
				if (jobObject != null) {
					logger.debug("Job object found.");
					break;
				}
				logger.debug("Job object not found.");
				
				if (jobObject == null && retries == (maxRetries - 1)) {
					throw new CoreException();
				}
				
				try {
					jobRepository.sleep(jobToQueueAdder.getExponentialBackoffCalculator().calculate(200, retries));
				} catch (InterruptedException e) {
					logger.error("While executing job, could bring Thread to sleep when retrieving job object.");
				}
				retries++;
			}
			
			job = jobRepository.initialize(context, jobObject);
			this.retry = job.getRetry(context);
			
			} catch (CoreException e) {
			logger.error("Could not retrieve job object. Job will not be executed.", e);
			return;
		}
		
		String microflowName = null;
		
		// Execute the microflow.
		try {
			microflowName = job.getMicroflowName(context);
			HashMap<String, Object> jobInput = microflowRepository.getJobInput(jobObject, microflowName);
			job.setStatus(context, ENU_JobStatus.Running);
			job.commit(context);
			logger.debug("Job status set to Running.");
			logger.debug("Starting execution of microflow " + microflowName + ".");
			jobRepository.executeJob(context, microflowName, true, jobInput);
			logger.debug("Finished execution of microflow " + microflowName + ".");
			job.setStatus(context, ENU_JobStatus.Done);
			job.commit(context);
			logger.debug("Job status set to Done.");
			scheduledJobRepository.remove(context, jobObject, retry);
		} catch (Exception e) {
			
			// Handle exceptions during execution of the microflow. Create a new system context to prevent issues with modeling errors in the microflow.
			try {
				IContext errorContext = queueRepository.getSystemContext();
				scheduledJobRepository.remove(errorContext, jobObject, retry);
				Throwable t = e.getCause();
				
				// Look for InterruptedException. This occurs when a job is aborted.
				while(true) {
					t = t.getCause();
					if (t == null) {
						break;
					}
					
					if(t instanceof InterruptedException) {
						logger.warn("Microflow " + microflowName + " has been interrupted. Status will be set to Cancelled.");
						job.setStatus(errorContext, ENU_JobStatus.Cancelled);
						job.commit(errorContext);
						return;
					}
				}
				
				logger.error("Job " + job.getIdJob(errorContext) + ": Error during execution of microflow " + microflowName + ".", e);
				if (this.retry < job.getMaxRetries(errorContext)) {
					logger.debug("Retry " + (this.retry + 1) + " of " + job.getMaxRetries(errorContext) + " will be scheduled for job with microflow " + job.getMicroflowName(errorContext) + ".");
					jobToQueueAdder.addRetry(errorContext, logger, queueRepository, jobRepository, scheduledJobRepository, job);
					logger.debug("Job rescheduled and status set to Queued.");
				} else {
					job.setStatus(errorContext, ENU_JobStatus.Error);
					job.commit(errorContext);
					logger.debug("Max retries reached, status is set to Error.");
				}
			} catch (Exception errorException) {
				logger.error("Exception during error handling of Job.", errorException);
			}
		} 
	}
}