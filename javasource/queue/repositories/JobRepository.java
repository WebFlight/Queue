package queue.repositories;

import java.util.HashMap;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;

import queue.proxies.Job;

public class JobRepository {
	public Job getJob(IContext context, IMendixIdentifier jobId) throws CoreException {
		return Job.load(context, jobId);
	}
	
	public void executeJob(IContext context, String microflowName, boolean inTransaction, HashMap<String, Object> jobInput) throws CoreException {
		Core.execute(context, microflowName, inTransaction, jobInput);
	}
}
