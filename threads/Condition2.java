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
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		boolean intStatus = Machine.interrupt().disable();

		KThread t = KThread.currentThread();
		waitQueue.add(t);
		conditionLock.release();
		KThread.sleep();
		conditionLock.acquire();
		Machine.interrupt().restore(intStatus);
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

		new KThread(new Runnable(){
			public void run(){
				System.out.println("Test Case 1: The lock is not present on the thread.");
				lock.acquire();
				cTest.sleep();
			}
		}).fork();

	}

	private Lock conditionLock;
	private LinkedList<KThread> waitQueue = null;
}
