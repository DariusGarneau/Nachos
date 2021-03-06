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
	private static final char Condition2TestChar = 'o';
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
		//Lib.assertTrue(conditionLock.isHeldByCurrentThread()) this crashes the program during test case 1
		if(conditionLock.isHeldByCurrentThread()){
			boolean intStatus = Machine.interrupt().disable();
			KThread t = KThread.currentThread();
			waitQueue.add(t);
			conditionLock.release();
			KThread.sleep();
			conditionLock.acquire();
			Machine.interrupt().restore(intStatus);
		}
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 * boolean debug: used for test cases
	 */
	private void wake(boolean debug) {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread(), "Lock not held...");
		boolean intStatus = Machine.interrupt().disable();
		if(debug && Machine.interrupt().disabled()){
			Lib.debug(Condition2TestChar, "Interrupts are disabled, waking next thread.");
		}
		if(waitQueue.size() != 0){
			KThread t = waitQueue.removeFirst();
			t.ready();
			if(debug && t.getStatus() == 1)
				wakeTestFlag = true;
			else
				wakeTestFlag = false;
		}

		Machine.interrupt().restore(intStatus);
	}

	public void wake(){
		wake(false);
	}



	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	private void wakeAll(boolean debug) {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		while(waitQueue.size() != 0)
			wake(debug);
	}

	public void wakeAll(){
		wakeAll(false);
	}

	public static void selfTest(){
		Lib.debug(Condition2TestChar, "---------CONDITION2 TEST CASES----------");
		Lock lock = new Lock();
		Condition2 cTest = new Condition2(lock);


		//***************TEST CASE 1**********************
		KThread test1 = new KThread(new Runnable(){
			public void run(){
				Lib.debug(Condition2TestChar, "\nTest Case 1: The lock is not present on the thread.");
				lock.acquire();
				lock.release();
				Lib.debug(Condition2TestChar, "Attempting to sleep without lock...");
				cTest.sleep();
				//verify cTest is not sleeping
				KThread t = KThread.currentThread();
				if(t.getStatus() != 3)
					Lib.debug(Condition2TestChar, "Test 1 Successful! This thread did not fall asleep.\n");
			}
		});

		test1.setName("Condition2 test 1");
		test1.fork();
		test1.join();


		//***************TEST CASE 2**********************
		//Put the thread to sleep on one thread. Check the queue size and wake it on another.
		KThread test2a = new KThread(new Runnable(){
			public void run(){
				Lib.debug(Condition2TestChar, "Test Case 2: The thread is added to the queue.");
				Lib.debug(Condition2TestChar, "Current queue size: " + cTest.getQueueSize() + "\nAcquiring lock and putting thread to sleep...");
				lock.acquire();
				cTest.sleep();
				lock.release();
			}
		});
		test2a.setName("Condition2 test 2");
		test2a.fork();

		KThread test2b = new KThread(new Runnable(){
			public void run(){
				Lib.debug(Condition2TestChar, "Thread asleep. Queue size: " + cTest.getQueueSize());
				lock.acquire();
				if(cTest.getQueueSize() > 0)
					Lib.debug(Condition2TestChar, "Test 2 Successful! Sleeping Thread was added to the queue.\n");
				else
					Lib.debug(Condition2TestChar, "Test 2 failed. Thread was not added to the queue.");

				cTest.wakeAll();
				lock.release();
			}
		});


		test2b.fork();
		test2b.join();
		test2a.join();


		//***************TEST CASE 3**********************
		KThread test3a = new KThread(new Runnable(){
			public void run(){
				lock.acquire();
				Lib.debug(Condition2TestChar, "Test Case 3: The thread is added to the queue.");
				cTest.sleep();
				lock.release();
			}
		});

		test3a.setName("Test 3");
		test3a.fork();

		KThread test3b = new KThread(new Runnable(){
			public void run(){
				lock.acquire();	
				if(test3a.getStatus() == 3){//first thread asleep
					Lib.debug(Condition2TestChar, "Thread " + test3a.toString() + " is sleeping.");
					cTest.wake(true);
					if(!Machine.interrupt().disabled()  && wakeTestFlag)
						Lib.debug(Condition2TestChar, "Test 3 Successful! Interrupts are enabled & thread has woken up.\n");
					else
						Lib.debug(Condition2TestChar, "Test 3 Failed.");

				}
				lock.release();
			}
		});

		test3b.fork();
		test3b.join();
		test3a.join();

		//***************TEST CASE 4**********************
		KThread test4a = new KThread(new Runnable(){
			public void run(){
				lock.acquire();
				Lib.debug(Condition2TestChar, "Test Case 4: The thread is added to the queue.");
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
					Lib.debug(Condition2TestChar, "Status of thread " + test4a.toString() + " is blocked.");	
				if(cTest.getQueueSize() > 0){
					Lib.debug(Condition2TestChar, "Waking next thread on queue.");
					cTest.wake();
				}
				if(test4a.getStatus() == 1)//status ready
					Lib.debug(Condition2TestChar, "Test 4 Successful! Thread " + test4a.toString() + " is awake.\n");
				else
					Lib.debug(Condition2TestChar, "Test 4 Failed. Thread did not wake up.");
				lock.release();
			}
		});

		test4b.fork();
		test4b.join();
		test4a.join();


		//***************TEST CASE 5**********************	
		//Put two threads to sleep and wake with wakeAll. Ensure interrupts are disabled and restored.
		KThread test5a = new KThread(new Runnable(){
			public void run(){
				lock.acquire();
				Lib.debug(Condition2TestChar, "Test Case 5: The thread is added to the queue.");
				cTest.sleep();
				lock.release();
			}
		});

		KThread test5b = new KThread(new Runnable(){
			public void run(){
				lock.acquire();
				cTest.sleep();
				lock.release();
			}
		});

		KThread.yield();
		test5a.setName("Test 5a");
		test5a.fork();
		test5b.setName("Test 5b");
		test5b.fork();

		KThread test5c = new KThread(new Runnable(){
			public void run(){
				lock.acquire();
				if(test5a.getStatus() == 3 && test5b.getStatus() == 3){//threads asleep
					Lib.debug(Condition2TestChar, "Threads " + test5a.toString() +  " and " + test5b.toString() + " are sleeping.");
					cTest.wakeAll(true);
					if(!Machine.interrupt().disabled()  && wakeTestFlag)
						Lib.debug(Condition2TestChar, "Test 5 Successful! Interrupts are enabled & threads have woken up.\n");
					else
						Lib.debug(Condition2TestChar, "Test 5 Failed.");

				}
				lock.release();
			}
		});

		test5c.fork();
		test5c.join();
		test5a.join();
		test5b.join();

		//***************TEST CASE 6**********************
		//Put two threads to sleep and use a third to wake them
		KThread test6a = new KThread(new Runnable(){
			public void run(){
				lock.acquire();
				Lib.debug(Condition2TestChar, "Test Case 6: The thread is added to the queue.");
				cTest.sleep();
				lock.release();
			}
		});

		KThread test6b = new KThread(new Runnable(){
			public void run(){
				lock.acquire();
				cTest.sleep();
				lock.release();
			}
		});

		KThread.yield();
		test6b.setName("Test6b");
		test6b.fork();
		test6a.setName("Test6a");
		test6a.fork();           		

		KThread test6c = new KThread(new Runnable(){
			public void run(){
				lock.acquire();
				Lib.debug(Condition2TestChar, "Queue Size: " + cTest.getQueueSize());
				if(test6a.getStatus() == 3 && test6b.getStatus() == 3){
					Lib.debug(Condition2TestChar, "Status of threads " + test6a.toString() + " and " + test6b.toString() +  " is blocked.");	
					if(cTest.getQueueSize() > 0){
						Lib.debug(Condition2TestChar, "Waking threads.");
						cTest.wakeAll();
					}
					if(test6a.getStatus() == 1 && test6b.getStatus() == 1)//status ready
						Lib.debug(Condition2TestChar, "Test 6 Successful! Threads " + test6a.toString() + " and " +  test6b.toString() + " are awake.\n");

				}else{
					Lib.debug(Condition2TestChar, "Test 6 Failed.");
					Lib.debug(Condition2TestChar, "Queue Size: " + cTest.getQueueSize());
				}
				lock.release();
			}
		});

		test6c.setName("Test6c");
		test6c.fork();
		test6c.join();
		test6b.join();
		test6a.join();

	}

	//Return the size of the waitQueue
	private int getQueueSize(){
		return waitQueue.size();
	}
	private static boolean wakeTestFlag = false;
	private Lock conditionLock;
	private LinkedList<KThread> waitQueue = null;
}
