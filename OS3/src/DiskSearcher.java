import java.io.File;


public class DiskSearcher {
	
	// Capacity of the queue that holds the directories to be searched
	static final int DIRECTORY_QUEUE_CAPACITY = 100;
	
	// Capacity of the queue that holds the files found
	static final int RESULTS_QUEUE_CAPACITY = 100; 
	
	/**
	 * Main method. Reads arguments from command line and starts the search.
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 5) {
			System.err.println("Usage: java DiskSearcher <extension> <root directory> <destination directory> <# of searchers> <# of copiers>");
		}
		String extension = args[0];
		File root = new File(args[1]);
		File dest = new File(args[2]);
		
		//CHECKING
		System.out.println(root.canRead());
		
		int numOfSearchers = Integer.parseInt(args[3]);
		int numOfCopiers = Integer.parseInt(args[4]);
		SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>(DIRECTORY_QUEUE_CAPACITY);
		SynchronizedQueue<File> resultQueue = new SynchronizedQueue<>(RESULTS_QUEUE_CAPACITY);
		
		// Initializes Scouter thread
		Thread scouter = new Thread(new Scouter(directoryQueue, root));
		scouter.start();
		
		
		// Initializes an array that holds all searchers threads
		Thread[] searchThreadsArray = new Thread[numOfSearchers];
		for (int i = 0; i < numOfSearchers; i++) {
			searchThreadsArray[i] = new Thread(new Searcher(extension, directoryQueue, resultQueue));
			searchThreadsArray[i].start();
		}
		// Initializes an array that holds all copiers threads
		Thread[] copierThreadsArray = new Thread[numOfCopiers];
		for (int i = 0; i < numOfCopiers; i++) {
			copierThreadsArray[i] = new Thread(new Copier(dest, resultQueue));
			copierThreadsArray[i].start();
		}
		
		try {
			scouter.join();
		} catch (InterruptedException e) {
			System.err.println(e);
		}
		try {
			for (int i = 0; i < numOfSearchers; i++) {
				searchThreadsArray[i].join();
			}
			for (int i = 0; i < numOfCopiers; i++) {
				copierThreadsArray[i].join();
			}
		} catch (InterruptedException e) {
			System.err.println(e);
		}
		
		
	}

}
