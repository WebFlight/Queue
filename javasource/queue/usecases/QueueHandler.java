package queue.usecases;

import java.util.HashMap;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IUser;

import queue.helpers.ExponentialBackoff;
import queue.helpers.JobToQueueAdder;
import queue.helpers.JobValidator;
import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.JobRepository;
import queue.repositories.QueueRepository;
import queue.repositories.ScheduledJobRepository;

public class QueueHandler implements Runnable {
	
	private IMendixIdentifier jobId;
	private ILogNode logger;
	private IUser user;
	private JobValidator jobValidator;
	private JobToQueueAdder jobToQueueAdder;
	private ScheduledJobRepository scheduledJobRepository;
	private QueueRepository queueRepository;
	private JobRepository jobRepository;
	private int retry = 0;
	
	public QueueHandler (ILogNode logger, IUser user, JobValidator jobValidator, JobToQueueAdder jobToQueueAdder, ScheduledJobRepository scheduledJobRepository, QueueRepository queueRepository, JobRepository jobRepository, IMendixIdentifier jobId) {
		this.jobId = jobId;
		this.logger = logger;
		this.user = user;
		this.jobValidator = jobValidator;
		this.jobToQueueAdder = jobToQueueAdder;
		this.scheduledJobRepository = scheduledJobRepository;
		this.queueRepository = queueRepository;
		this.jobRepository = jobRepository;
	}

	@Override
	public void run() {
		
		try {
			IMendixObject jobObject = null;
			Job job = null;
			
			IContext context = null;
			
			if(user != null) {
				context = queueRepository.getUserContext(this.user);
			}
			
			if(user == null) {
				context = queueRepository.getSystemContext();
			}
			
			int retries = 0;
			while (retries <= 10) {
				logger.debug("Trying to retrieve job object. Attempt " + (retries + 1) + " of 10.");
				job = jobRepository.getJob(context, jobId);
				jobObject = job.getMendixObject();
				
				if (jobObject != null) {
					logger.debug("Job object found.");
					this.retry = job.getRetry(context);
					break;
				}
				logger.debug("Job object not found.");
				
				try {
					Thread.sleep(ExponentialBackoff.getExponentialBackOff(200, retries));
				} catch (InterruptedException e) {
					logger.error("While executing job, could bring Thread to sleep when retrieving job object.");
				}
				retries++;
			}
			
			HashMap<String, Object> jobInput = new HashMap<>();
			jobInput.put("Job", jobObject);
			
			try {
				job.setStatus(context, ENU_JobStatus.Running);
				job.commit(context);
				logger.debug("Job status set to Running.");
				logger.debug("Starting execution of microflow " + job.getMicroflowName(context) + ".");
				jobRepository.executeJob(context, job.getMicroflowName(context), true, jobInput);
				logger.debug("Finished execution of microflow " + job.getMicroflowName(context) + ".");
				job.setStatus(context, ENU_JobStatus.Done);
				job.commit(context);
				logger.debug("Job status set to Done.");
			} catch (CoreException e) {
				logger.error("Error during execution of microflow " + job.getMicroflowName(context) + ".", e);
				if (job.getRetry(context) < job.getmaxRetries(context)) {
					logger.debug("Retry " + (job.getRetry(context) + 1) + " of " + job.getmaxRetries(context) + " will be scheduled for job with microflow " + job.getMicroflowName(context) + ".");
					jobToQueueAdder.addRetry(context, logger, queueRepository, jobRepository, scheduledJobRepository, jobValidator, job, user);
					logger.debug("Job rescheduled and status set to Queued.");
				} else {
					job.setStatus(context, ENU_JobStatus.Error);
					job.commit(context);
					logger.debug("Max retries reached, status is set to Error.");
				}
			} finally {
				scheduledJobRepository.remove(context, jobObject, retry);
			}
			
		} catch (CoreException e) {
			logger.error("Could not retrieve job object. Job will not be executed.");
		}
	}

}