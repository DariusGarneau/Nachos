package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
	/**
	 * Allocate a new condition variable.
	 *
	 * @param	conditionLock	the lock associated with this condition
	 *				variable. The current thread must hold this
	 *				lock whenever it uses <tt>sleep()</tt>,
	 *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;
		waitQueue = new LinkedList<KThread>();
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The
	 * current thread must hold the associated lock. The thread will
	 * automatically reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		//Lib.assertTrue(conditionLock.isHeldByCurrentThread())
		if(conditionLock.isHeldByCurrentThread()){
			boolean intStatus = Machine.interrupt().disable();
			conditionLock.release();
			KThread t = KThread.currentThread();
			waitQueue.add(t);
			KThread.sleep();
			conditionLock.acquire();
			Machine.interrupt().restore(intStatus);
		}
		}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread(), "Lock not held...");
		boolean intStatus = Machine.interrupt().disable();
		if(waitQueue.size() != 0){
			KThread t = waitQueue.removeFirst();
			t.ready();
		}
		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		while(waitQueue.size() != 0)
			wake();
	}

	public static void selfTest(){
		System.out.println("---------CONDITION2 TEST CASES----------");
		Lock lock = new Lock();
		Condition2 cTest = new Condition2(lock);


		//***************TEST CASE 1**********************
		KThread test1 = new KThread(new Runnable(){
			public void run(){
				System.out.println("\n\nTest Case 1: The lock is not present on the thread.");
				lock.acquire();
				lock.release();
				System.out.println("Attempting to sleep without lock...");
				cTest.sleep();
				//verify cTest is not sleeping
				KThread t = KThread.currentThread();
				if(t.getStatus() != 3)
					System.out.println("Test 1 Successful! This thread did not fall asleep.\n");
			}
		});

		test1.setName("Condition2 test 1");
		test1.fork();
		test1.join();


		//***************TEST CASE 2**********************
		//Put the thread to sleep on one thread. Check the queue size and wake it on another.
		KThread test2a = new KThread(new Runnable(){
			public void run(){
				System.out.println("Test Case 2: The thread is added to the queue.");
				System.out.println("Current queue size: " + cTest.getQueueSize() + "\nAcquiring lock and putting thread to sleep...");
				lock.acquire();
				cTest.sleep();
				lock.release();
			}
		});
		test2a.setName("Condition2 test 2");
		test2a.fork();

		KThread test2b = new KThread(new Runnable(){
			public void run(){
				System.out.println("Thread asleep. Queue size: " + cTest.getQueueSize());
				lock.acquire();
				if(cTest.getQueueSize() > 0)
					System.out.println("Test 2 Successful! Sleeping Thread was added to the queue.\n");

				cTest.wakeAll();
				lock.release();
			}
		});


		test2b.fork();
		test2b.join();
		test2a.join();


		//***************TEST CASE 3**********************
		KThread test3 = new KThread(new Runnable(){
			public void run(){
				
			}
		});


		//***************TEST CASE 4**********************
		KThread test4a = new KThread(new Runnable(){
			public void run(){
				System.out.println("Test Case 4: The thread is added to the queue.");
				lock.acquire();
				cTest.sleep();
				lock.release();
			}
		});

		test4a.setName("Test4");
		test4a.fork();

		KThread test4b = new KThread(new Runnable(){
			public void run(){
				lock.acquire();
				if(test4a.getStatus() == 3)
					System.out.println("Status of thread " + test4a.toString() + " is blocked.");	
				if(cTest.getQueueSize() > 0){
					System.out.println("Waking next thread on queue.");
					cTest.wakeAll();
				}
				if(test4a.getStatus() == 1)//status ready
					System.out.println("Test 4 Successful! Thread " + test4a.toString() + " is awake.\n");
				lock.release();
			}
		});

		test4b.fork();
		test4b.join();
		test4a.join();
	}

	private int getQueueSize(){
		return waitQueue.size();
	}

	private Lock conditionLock;
	private LinkedList<KThread> waitQueue = null;
	}
