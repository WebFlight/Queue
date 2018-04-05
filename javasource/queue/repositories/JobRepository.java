package queue.repositories;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.UserException;
import com.mendix.systemwideinterfaces.core.UserException.ExceptionCategory;

public class JobRepository {
	private static ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledJobMap = new ConcurrentHashMap<>();
	
	public void add(IMendixObject job, ScheduledFuture<?> future) {
		if(!contains(job)) {
			scheduledJobMap.put(job.getId().toLong(), future);
		}
	}
	
	public ScheduledFuture<?> get(IMendixObject job) {
		if(contains(job)) {
			return scheduledJobMap.get(job.getId().toLong());
		}
		throw new UserException(ExceptionCategory.DataValidation, "Job could not be retrieved from repository.");
	}
	
//	public void remove(IMendixObject job) {
//		if(contains(job)) {
//			scheduledJobMap.remove(job.getId().toLong());
//		}
//		throw new UserException(ExceptionCategory.DataValidation, "Job could not be removed from repository.");
//	}
	
	public boolean contains(IMendixObject job) {
		return scheduledJobMap.containsKey(job.getId().toLong());
	}

}
