package queue.helpers;

import java.util.concurrent.ScheduledFuture;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.UserException;

import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.JobRepository;

public class JobCanceller {
	
	public boolean cancel(JobRepository jobRepository, Job job, boolean removeWhenRunning) throws CoreException {
		ScheduledFuture<?> future; 
		try {
			future = jobRepository.get(job.getMendixObject());
		} catch (UserException e) {
			return false;
		}
		
		boolean cancelled = future.cancel(removeWhenRunning);
		
		if(cancelled) {
			job.setStatus(ENU_JobStatus.Cancelled);
			job.commit();
		}
		
		return cancelled;
	}

}
