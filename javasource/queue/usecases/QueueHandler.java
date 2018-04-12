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
	private boolean runFromUser;
	private IUser user;
	private JobValidator jobValidator;
	private JobToQueueAdder jobToQueueAdder;
	private ScheduledJobRepository scheduledJobRepository;
	private QueueRepository queueRepository;
	private JobRepository jobRepository;
	private int retry = 0;
	
	public QueueHandler (ILogNode logger, IUser user, boolean runFromUser, JobValidator jobValidator, JobToQueueAdder jobToQueueAdder, ScheduledJobRepository scheduledJobRepository, QueueRepository queueRepository, JobRepository jobRepository, IMendixIdentifier jobId) {
		this.jobId = jobId;
		this.logger = logger;
		this.user = user;
		this.runFromUser = runFromUser;
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
			
			IContext context = null;
			
			if(runFromUser && user != null) {
				context = queueRepository.getUserContext(this.user);
			}
			
			if(!runFromUser || user == null) {
				context = queueRepository.getSystemContext();
			}
			
			int retries = 0;
			while (retries <= 10) {
				logger.debug("Trying to retrieve job object. Attempt " + (retries + 1) + " of 10.");
				jobObject = jobRepository.getJob(context, jobId);
				
				if (jobObject != null) {
					logger.debug("Job object found.");
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
			
			Job job = Job.initialize(context, jobObject);
			this.retry = job.getRetry(context);
			
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
				Throwable t = e.getCause();
				do {
					t = t.getCause();
					if(t instanceof InterruptedException) {
						logger.warn("Microflow " + job.getMicroflowName(context) + " has been interrupted. Status will be set to Cancelled.");
						return;
					}
				} while (t.getCause() != null);
				
				logger.error("Error during execution of microflow " + job.getMicroflowName(context) + ".", e);
				if (job.getRetry(context) < job.getMaxRetries(context)) {
					logger.debug("Retry " + (job.getRetry(context) + 1) + " of " + job.getMaxRetries(context) + " will be scheduled for job with microflow " + job.getMicroflowName(context) + ".");
					jobToQueueAdder.addRetry(context, logger, queueRepository, jobRepository, scheduledJobRepository, jobValidator, job, user, runFromUser);
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