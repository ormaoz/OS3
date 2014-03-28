import java.util.concurrent.Semaphore;


/**
 * A synchronized bounded-size queue for multithreaded producer-consumer applications.
 * 
 * @param <T> Type of data items
 * @author ID: 029983111, ID: 038064556
 */
public class SynchronizedQueue<T> {

	private T[] buffer;
	private int producers;
	private int headOfQueue;
	private int tailOfQueue;
	private int size;
	private Semaphore addProducer;
	private Semaphore reduceProducer;
	
	/**
	 * Constructor. Allocates a buffer (an array) with the given capacity and
	 * resets pointers and counters.
	 * @param capacity Buffer capacity
	 */
	@SuppressWarnings("unchecked")
	public SynchronizedQueue(int capacity) {
		this.buffer = (T[])(new Object[capacity]);
		this.producers = 0;
		
		// indexes to the head and tail of the queue
		this.headOfQueue = 0;
		this.tailOfQueue = capacity;
		
		// Count the number of items in the queue
		this.size = 0;
		
		// Semaphores for critical sections
		addProducer = new Semaphore(1, true);
		reduceProducer = new Semaphore(1, true);
		
	}
	
	/**
	 * Dequeues the first item from the queue and returns it.
	 * If the queue is empty but producers are still registered to this queue, 
	 * this method blocks until some item is available.
	 * If the queue is empty and no more items are planned to be added to this 
	 * queue (because no producers are registered), this method returns null.
	 * 
	 * @return The first item, or null if there are no more items
	 * @see #registerProducer()
	 * @see #unregisterProducer()
	 */
	public synchronized T dequeue() {
		
		// As long as the queue is empty, prevent dequeue items.
		while (size == 0) {
			if (producers == 0) {
				return null;
			} else {
				try {
					this.wait();
				} catch (InterruptedException e) {
					System.err.println(e);
				}
			}
		}
		this.notifyAll();
		
		// Save the current item in the head of the queue
		T currentHead = buffer[headOfQueue];
		buffer[headOfQueue] = null; // Help the garbage collector
		
		// advance the head of the queue index to the next one. 
		// If reached the end of the queue, will be back to start.
		headOfQueue = (headOfQueue + 1) % buffer.length;
		size--;
		return currentHead;
	}

	/**
	 * Enqueues an item to the end of this queue. If the queue is full, this 
	 * method blocks until some space becomes available.
	 * 
	 * @param item Item to enqueue
	 */
	public synchronized void enqueue(T item) {
		
		// As long as the queue is full, prevent enqueue item into it.
		while (size == buffer.length) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		this.notifyAll();
		
		tailOfQueue = (tailOfQueue + 1) % buffer.length;
		buffer[tailOfQueue] = item;
		size++;
	}

	/**
	 * Returns the capacity of this queue
	 * @return queue capacity
	 */
	public int getCapacity() {
		return buffer.length;
	}

	/**
	 * Returns the current size of the queue (number of elements in it)
	 * @return queue size
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Registers a producer to this queue. This method actually increases the
	 * internal producers counter of this queue by 1. This counter is used to
	 * determine whether the queue is still active and to avoid blocking of
	 * consumer threads that try to dequeue elements from an empty queue, when
	 * no producer is expected to add any more items.
	 * Every producer of this queue must call this method before starting to 
	 * enqueue items, and must also call <see>{@link #unregisterProducer()}</see> when
	 * finishes to enqueue all items.
	 * 
	 * @see #dequeue()
	 * @see #unregisterProducer()
	 */
	public void registerProducer() {
		addProducer.acquireUninterruptibly();
		
		// Critical section
		this.producers++;
		// End of critical section
		
		addProducer.release();
	}

	/**
	 * Unregisters a producer from this queue. See <see>{@link #registerProducer()}</see>.
	 * 
	 * @see #dequeue()
	 * @see #registerProducer()
	 */
	public void unregisterProducer() {
		reduceProducer.acquireUninterruptibly();
		
		// Critical section
		this.producers--;
		// End of critical section
		
		reduceProducer.release();

	}
}
