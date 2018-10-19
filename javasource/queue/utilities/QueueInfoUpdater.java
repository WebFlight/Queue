package queue.utilities;

import com.mendix.core.actionmanagement.CoreAction;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import queue.helpers.QueueInfoUpdaterExecutor;
import queue.repositories.QueueRepository;

public class QueueInfoUpdater<R> extends CoreAction<R> {
	
	private ILogNode logger;
	private CoreUtility coreUtility;
	private QueueRepository queueRepository;
	private QueueInfoUpdaterExecutor queueInfoUpdaterExecutor;
	
	public QueueInfoUpdater(ILogNode logger, IContext arg0, CoreUtility coreUtility, QueueRepository queueRepository, QueueInfoUpdaterExecutor queueInfoUpdaterExecutor) {
		super(arg0);
		this.logger = logger;
		this.coreUtility = coreUtility;
		this.queueRepository = queueRepository;
		this.queueInfoUpdaterExecutor = queueInfoUpdaterExecutor;
	}

	@Override
	public R execute() {
		queueInfoUpdaterExecutor.execute(this.getContext(), logger, queueRepository, coreUtility);
		return null;
	}

}
