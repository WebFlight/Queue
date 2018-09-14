package queue.helpers;

import java.util.concurrent.TimeUnit;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;

import queue.proxies.constants.Constants;
import queue.repositories.ConstantsRepository;

public class ClusterSupportInitializer {
	
	private ConstantsRepository constantsRepository;
	private static ILogNode logger = Core.getLogger(Constants.getLOGNODE());
	
	public ClusterSupportInitializer (ConstantsRepository constantsRepository) {
		this.constantsRepository = constantsRepository;
	}
	
	public void initialize () {
		if (constantsRepository.isClusterSupport()) {
			Core.scheduleAtFixedRate(new QueueInfoUpdater<>(Core.createSystemContext()), 10L, 5L, TimeUnit.SECONDS);
			Core.scheduleAtFixedRate(new QueueControlMessageFetcher<>(Core.createSystemContext()), 10L, 5L, TimeUnit.SECONDS);
			logger.info("Support enabled for Mendix Clustered Runtime.");
		}
	}
}