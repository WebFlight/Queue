package queue.repositories;

import java.util.HashMap;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.proxies.Job;

public class JobRepository {
	public IMendixObject getJob(IContext context, IMendixIdentifier jobId) throws CoreException {
		return Core.retrieveId(context, jobId);
	}
	
	public void executeJob(IContext context, String microflowName, boolean inTransaction, HashMap<String, Object> jobInput) throws CoreException {
		Core.execute(context, microflowName, inTransaction, jobInput);
	}
	
	public Job initialize(IContext context, IMendixObject jobObject) {
		return Job.initialize(context, jobObject);
	}
	
	public void sleep(long millis) throws InterruptedException {
		Thread.sleep(millis);
	}
}
