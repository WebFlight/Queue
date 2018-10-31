package queue.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.factories.XASInstanceFactory;
import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.ScheduledJobRepository;
import queue.utilities.CoreUtility;
import queue.repositories.ConstantsRepository;
import queue.repositories.JobRepository;
import queue.repositories.MicroflowRepository;
import queue.repositories.QueueRepository;

public class JobToQueueAdder {
	
	private JobValidator jobValidator;
	private ExponentialBackoffCalculator exponentialBackoffCalculator;
	private TimeUnitConverter timeUnitConverter;
	private ConstantsRepository constantsRepository;
	private CoreUtility coreUtility;
	private XASInstanceFactory xasInstanceFactory;
	private MicroflowRepository microflowRepository;
	
	public JobToQueueAdder(JobValidator jobValidator, ExponentialBackoffCalculator exponentialBackoffCalculator, TimeUnitConverter timeUnitConverter, ConstantsRepository constantsRepository, CoreUtility coreUtility, XASInstanceFactory xasInstanceFactory, MicroflowRepository microflowRepository) {
		this.jobValidator = jobValidator;
		this.exponentialBackoffCalculator = exponentialBackoffCalculator;
		this.timeUnitConverter = timeUnitConverter;
		this.constantsRepository = constantsRepository;
		this.coreUtility = coreUtility;
		this.xasInstanceFactory = xasInstanceFactory;
		this.microflowRepository = microflowRepository;
	}
	
	public void add(IContext context, ILogNode logger, QueueRepository queueRepository, JobRepository jobRepository, ScheduledJobRepository scheduledJobRepository, Job job) throws CoreException {
		boolean valid = this.jobValidator.isValid(context, queueRepository, job);
		
		if (valid == false) {
			throw new CoreException("Job is not added, because it could not be validated.");
		}
		
		ScheduledExecutorService executor = queueRepository.getQueue(job.getQueue(context));
		
		if(executor == null) {
			throw new CoreException("Queue with name " + job.getQueue(context) + " could not be found. Job has not been added.");
		}
		
		if(executor.isShutdown() || executor.isTerminated()) {
			throw new CoreException("Queue with name " + job.getQueue(context) + " has already been shut down or terminated. Job has not been added.");
		}
		
		
		if(constantsRepository.isClusterSupport()) {
			List<IMendixObject> xasInstances = null;
			
			try {
				xasInstances = coreUtility.retrieveXPathQuery(context, "//System.XASInstance[XASId='" + coreUtility.getXASId() + "']");
			} catch (CoreException e) {
				throw new CoreException("Could not retrieve XAS Instance from database.", e);
			}
			IMendixObject xasInstance = xasInstances.get(0);
			
			job.setJob_XASInstance(context, xasInstanceFactory.load(context, xasInstance.getId()));
		}
	
		job.setStatus(context, ENU_JobStatus.Queued);
		
		try {
			job.commit(context);
		} catch (Exception e) {
			throw new CoreException("Could not commit job.", e);
		}
		
		IMendixObject jobObject = job.getMendixObject();
				
		ScheduledFuture<?> future =	executor.schedule(
					queueRepository.getQueueHandler(logger, this, scheduledJobRepository, queueRepository, jobRepository, microflowRepository, jobObject.getId()), 
					job.getCurrentDelay(context), 
					timeUnitConverter.getTimeUnit(job.getDelayUnit(context).getCaption("en_US"))
					);
		
		scheduledJobRepository.add(context, jobObject, future);
	}
	
	public void addWithMicroflow(IContext context, ILogNode logger, QueueRepository queueRepository, JobRepository jobRepository, ScheduledJobRepository scheduledJobRepository, Job job, String microflow) throws CoreException {
		job.setMicroflowName(context, microflow);
		add(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
	}
	
	public void addRetry(IContext context, ILogNode logger, QueueRepository queueRepository, JobRepository jobRepository, ScheduledJobRepository scheduledJobRepository, Job job) throws CoreException {
		int retry = job.getRetry(context);
		int baseDelay = job.getBaseDelay(context);
		int newDelay= this.exponentialBackoffCalculator.calculate(baseDelay, retry);
		job.setCurrentDelay(context, newDelay);
		job.setRetry(context, retry + 1);
		add(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
	}
	
	public ExponentialBackoffCalculator getExponentialBackoffCalculator() {
		return this.exponentialBackoffCalculator;
	}
	
	public void setTimeZone(IContext context, ILogNode logger) {
		String timeZoneID = constantsRepository.getTimeZoneID();
		
		if (timeZoneID == null || timeZoneID.equals("")) {
			return;
		}
		
		List<String> timeZoneList = Arrays.asList(TimeZone.getAvailableIDs());
		boolean timeZoneExists = timeZoneList.stream().anyMatch(tz -> tz.equals(timeZoneID));
		
		if (timeZoneExists) {
			context.getSession().setTimeZone(-TimeZone.getTimeZone(timeZoneID).getRawOffset()/1000/60);
			return;
		}
		
		logger.warn("TimeZoneID " + timeZoneID + " is not valid. No time zone will be configured.");
	}
}