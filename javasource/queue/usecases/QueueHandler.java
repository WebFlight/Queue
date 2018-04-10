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
				jobObject = Core.retrieveId(context, jobId);
				if (jobObject != null) {
					break;
				}
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
				Core.execute(context, job.getMicroflowName(), true, jobInput);
				job.setStatus(context, ENU_JobStatus.Done);
				job.commit(context);
			} catch (CoreException e) {
				if (job.getRetry() < job.getmaxRetries()) {
					queueRepository
					.getQueue(job.getQueue())
					.schedule(
							queueRepository.getQueueHandler(logger, queueRepository, jobId), 
							ExponentialBackoff.getExponentialBackOff(500, job.getRetry()), TimeUnit.MILLISECONDS
							);
					job.setRetry(job.getRetry() + 1);
					job.setStatus(ENU_JobStatus.Queued);
					job.commit();
				} else {
					job.setStatus(context, ENU_JobStatus.Error);
					job.commit(context);
				}
			} finally {
			}
			
		} catch (CoreException e) {
			logger.error("Could not retrieve job object. Job will not be executed.");
		}
	}

}