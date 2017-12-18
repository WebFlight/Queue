package queue.entities;

public class QueueConfiguration {
	
	private String name;
	private int corePoolSize;
	private int priority;
	
	public QueueConfiguration(String name, int corePoolSize, int priority) {
		this.name = name;
		this.corePoolSize = corePoolSize;
		this.priority = priority;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}
