package queue.usecases;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.helpers.ExponentialBackoff;
import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.QueueRepository;

public class QueueHandler implements Runnable {
	
	private IMendixIdentifier jobId;
	private final IContext context = Core.createSystemContext();
	
	public QueueHandler (IMendixIdentifier jobId) {
		this.jobId = jobId;
	}

	@Override
	public void run() {
		try {
			IMendixObject jobObject = null;
			int retries = 0;
			while (retries <= 10) {
				jobObject = Core.retrieveId(this.context, jobId);
				if (jobObject != null) {
					break;
				}
				try {
					Thread.sleep(ExponentialBackoff.getExponentialBackOff(500, retries));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				retries++;
			}
			
			Job job = Job.load(this.context, jobId);
			HashMap<String, Object> jobInput = new HashMap<>();
			jobInput.put("Job", jobObject);
			try {
				job.setStatus(this.context, ENU_JobStatus.Running);
				job.commit();
				Core.execute(this.context, job.getMicroflowName(), true, jobInput);
				job.setStatus(this.context, ENU_JobStatus.Done);
				job.commit(this.context);
			} catch (CoreException e) {
				if (job.getRetry() < job.getmaxRetries()) {
					QueueRepository
					.getQueue(job.getQueue())
					.schedule(
							new QueueHandler(this.jobId), ExponentialBackoff.getExponentialBackOff(500, job.getRetry()), TimeUnit.MILLISECONDS
							);
					job.setRetry(job.getRetry() + 1);
					job.setStatus(ENU_JobStatus.Queued);
					job.commit();
				} else {
					job.setStatus(this.context, ENU_JobStatus.Error);
					job.commit(this.context);
				}
			} finally {
			}
			
		} catch (CoreException e) {
			//TODO Implement error handling 
			e.printStackTrace();
		}
	}

}