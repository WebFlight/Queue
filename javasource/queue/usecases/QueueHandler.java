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
import queue.repositories.QueueRepository;
import queue.repositories.ScheduledJobRepository;

public class QueueHandler implements Runnable {
	
	private IMendixIdentifier jobId;
	private ILogNode logger;
	private JobToQueueAdder jobToQueueAdder;
	private ScheduledJobRepository scheduledJobRepository;
	private QueueRepository queueRepository;
	private JobRepository jobRepository;
	private int retry = 0;
	
	public QueueHandler (ILogNode logger, JobToQueueAdder jobToQueueAdder, ScheduledJobRepository scheduledJobRepository, QueueRepository queueRepository, JobRepository jobRepository, IMendixIdentifier jobId) {
		this.jobId = jobId;
		this.logger = logger;
		this.jobToQueueAdder = jobToQueueAdder;
		this.scheduledJobRepository = scheduledJobRepository;
		this.queueRepository = queueRepository;
		this.jobRepository = jobRepository;
	}

	@Override
	public void run() {
		
		try {
			IMendixObject jobObject = null;
			
			IContext context = queueRepository.getSystemContext();
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
			
			Job job = jobRepository.initialize(context, jobObject);
			this.retry = job.getRetry(context);
			String microflowName = job.getMicroflowName(context);
			
			HashMap<String, Object> jobInput = jobRepository.getJobInput(jobObject);
			
			try {
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
			} catch (CoreException e) {
				scheduledJobRepository.remove(context, jobObject, retry);
				Throwable t = e.getCause();
				while(true) {
					t = t.getCause();
					if (t == null) {
						break;
					}
					
					if(t instanceof InterruptedException) {
						logger.warn("Microflow " + microflowName + " has been interrupted. Status will be set to Cancelled.");
						job.setStatus(context, ENU_JobStatus.Cancelled);
						job.commit(context);
						return;
					}
				}
				
				logger.error("Error during execution of microflow " + microflowName + ".", e);
				if (this.retry < job.getMaxRetries(context)) {
					logger.debug("Retry " + (this.retry + 1) + " of " + job.getMaxRetries(context) + " will be scheduled for job with microflow " + job.getMicroflowName(context) + ".");
					jobToQueueAdder.addRetry(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
					logger.debug("Job rescheduled and status set to Queued.");
				} else {
					job.setStatus(context, ENU_JobStatus.Error);
					job.commit(context);
					logger.debug("Max retries reached, status is set to Error.");
				}
			} 
		} catch (CoreException e) {
			logger.error("Could not retrieve job object. Job will not be executed.");
		}
	}
}