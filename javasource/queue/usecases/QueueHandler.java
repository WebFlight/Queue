package queue.usecases;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.helpers.ExponentialBackoff;
import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.QueueRepository;

public class QueueHandler implements Runnable {
	
	private IMendixIdentifier jobId;
	private ILogNode logger;
	private QueueRepository queueRepository;
	
	public QueueHandler (ILogNode logger, QueueRepository queueRepository, IMendixIdentifier jobId) {
		this.jobId = jobId;
		this.logger = logger;
		this.queueRepository = queueRepository;
	}

	@Override
	public void run() {
		IContext context = queueRepository.getSystemContext();
		try {
			IMendixObject jobObject = null;
			int retries = 0;
			while (retries <= 10) {
				logger.debug("Trying to retrieve job object. Attempt " + (retries + 1) + " of 10.");
				jobObject = Core.retrieveId(context, jobId);
				if (jobObject != null) {
					logger.debug("Job object found.");
					break;
				}
				logger.debug("Job object not found.");
				try {
					Thread.sleep(ExponentialBackoff.getExponentialBackOff(500, retries));
				} catch (InterruptedException e) {
					logger.error("While executing job, could bring Thread to sleep when retrieving job object.");
				}
				retries++;
			}
			
			Job job = Job.load(context, jobId);
			HashMap<String, Object> jobInput = new HashMap<>();
			jobInput.put("Job", jobObject);
			
			try {
				job.setStatus(context, ENU_JobStatus.Running);
				job.commit();
				logger.debug("Job status set to Running.");
				logger.debug("Starting execution of microflow " + job.getMicroflowName() + ".");
				Core.execute(context, job.getMicroflowName(), true, jobInput);
				logger.debug("Finished execution of microflow " + job.getMicroflowName() + ".");
				job.setStatus(context, ENU_JobStatus.Done);
				job.commit(context);
				logger.debug("Job status set to Done.");
			} catch (CoreException e) {
				logger.error("Error during execution of microflow " + job.getMicroflowName() + ".");
				if (job.getRetry() < job.getmaxRetries()) {
					logger.debug("Retry " + (job.getRetry() + 1) + " of " + job.getmaxRetries() + " will be scheduled for job with microflow " + job.getMicroflowName() + ".");
					queueRepository
					.getQueue(job.getQueue())
					.schedule(
							queueRepository.getQueueHandler(logger, queueRepository, jobId), 
							ExponentialBackoff.getExponentialBackOff(500, job.getRetry()), TimeUnit.MILLISECONDS
							);
					job.setRetry(job.getRetry() + 1);
					job.setStatus(ENU_JobStatus.Queued);
					job.commit();
					logger.debug("Job rescheduled and status set to Queued.");
				} else {
					job.setStatus(context, ENU_JobStatus.Error);
					job.commit(context);
					logger.debug("Max retries reached, status is set to Error.");
				}
			} finally {
			}
			
		} catch (CoreException e) {
			logger.error("Could not retrieve job object. Job will not be executed.");
		}
	}

}