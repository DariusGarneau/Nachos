package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue;
import java.util.ArrayList;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm { 

	// que for sleeping threads
	private PriorityQueue<SleepingThread> waitQueue = null;
	private static ArrayList<Long> order = new ArrayList<Long>();//used for test cases to verify threads woke in the correct order
	private static final char AlarmTestChar = 'a';

	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * <p><b>Note</b>: Nachos will not function correctly with more than one
	 * alarm.
	 */
	public Alarm() {

		waitQueue = new PriorityQueue<SleepingThread>();
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() { timerInterrupt(); }
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread
	 * that should be run.
	 */
	public void timerInterrupt() {

		//store and disable interrupts
		boolean iStatus = Machine.interrupt().disable();
		for (int i=0; i < waitQueue.size(); ++i) {
			SleepingThread s = waitQueue.peek();
			if (Machine.timer().getTime() >= + s.wakeTime){
				Lib.debug(AlarmTestChar, "Waking Thread " + s.thread.toString());
				order.add(s.wakeTime);//for test cases
				//yield the current thread
				KThread.yield();

				//put thread (in waitQueue) into readyState
				s.thread.ready();

				//remove thread from list
				waitQueue.poll();
			}else {
				Lib.debug(AlarmTestChar, "No more threads ready to wake up. Test 3 Successful!");
				//everthing in queue doesnt need to be woken up
				i = waitQueue.size();
			}

			Machine.interrupt().restore(iStatus);

		}

	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks,
	 * waking it up in the timer interrupt handler. The thread must be
	 * woken up (placed in the scheduler ready set) during the first timer
	 * interrupt where
	 *
	 * <p><blockquote>
	 * (current time) >= (WaitUntil called time)+(x)
	 * </blockquote>
	 *
	 * @param	x	the minimum number of clock ticks to wait.
	 *
	 * @see	nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {

		//wakeTime = now + x
		long wakeTime = Machine.timer().getTime() + x;

		//disable interrupts
		boolean iStatus = Machine.interrupt().disable();

		//add thread to the waitQueue, because its a priority queue it's automatically sorted
		//and put thread to sleep
		waitQueue.add(new SleepingThread(KThread.currentThread(), wakeTime));

		//System.out.println("Queue size " + getQueueSize());
		KThread.sleep();

		//restore interrupts
		Machine.interrupt().restore(iStatus);
	}


	//------------------------------------------------------------------------------

	/**
	 * Class defining sleeping thread type
	 * Implements comparable so it can be sorted in a priority queue
	 *
	 * */
	private class SleepingThread implements Comparable<SleepingThread> {
		public KThread thread;
		public long wakeTime;

		//constructor
		public SleepingThread(KThread t, long wt){
			this.thread = t;
			this.wakeTime = wt;
		}

		public int compareTo(SleepingThread other){
			return (int)(this.wakeTime - other.wakeTime);
		}

	}

	public int getQueueSize(){
		return waitQueue.size();
	}

	/**
	 *  SelfTest method
	 *  
	 */

	public static void selfTest() {

		Lib.debug(AlarmTestChar, "\n******* Alarm selfTest() Starting *******\n\nCreating alarm and test Threads");

		Alarm alarm = new Alarm();
		KThread threadA = null;
		KThread threadB = null;
		KThread threadC = null;
		KThread threadD = null;

		// Test Case 1: Put to sleep in order------------------------------------
		Lib.debug(AlarmTestChar, "Test Case 1: Put to sleep in order.\nExecuting Threads...");
		threadA = new KThread(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Thread A sleeping for 1000");
				alarm.waitUntil(1000);
			}
		});

		threadB = new KThread(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Thread B sleeping for 2000");
				alarm.waitUntil(2000);
			}
		});

		threadC = new KThread(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Thread C sleeping for 3000");
				alarm.waitUntil(3000);
			}
		});

		threadA.setName("Thread A");
		threadA.fork();

		threadB.setName("Thread B");
		threadB.fork();

		threadC.setName("Thread C");
		threadC.fork();

		threadA.join();
		threadB.join();
		threadC.join();

		threadD = new KThread(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Verifying threads woke in order...");
				if(order.get(0) < order.get(1) && order.get(1) < order.get(2))
					Lib.debug(AlarmTestChar, "Test 1 Successful! Threads work up in the correct order.\n");
				order.clear();//clear order
			}
		});

		threadD.fork();
		threadD.join();


		// Test Case 2: Put to sleep in a mixed order----------------------------

		Lib.debug(AlarmTestChar, "Test Case 2: Put to sleep in mixed order.\nExecuting Threads...");
		threadA = new KThread(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Thread A sleeping for 3000");
				alarm.waitUntil(3000);
			}
		});

		threadB = new KThread(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Thread B sleeping for 1000");
				alarm.waitUntil(1000);
			}
		});

		threadC = new KThread(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Thread C sleeping for 2000");
				alarm.waitUntil(2000);
			}
		});

		threadA.setName("Thread A");
		threadA.fork();

		threadB.setName("Thread B");
		threadB.fork();

		threadC.setName("Thread C");
		threadC.fork();

		threadA.join();
		threadB.join();
		threadC.join();

		threadD = new KThread(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Verifying threads woke in order..." + order.size());
				alarm.waitUntil(3000);//this thread should wait to make sure the test threads are all awake
				if(order.get(0) < order.get(1) && order.get(1) < order.get(2))
					Lib.debug(AlarmTestChar, "Test 2 Successful! Threads work up in the correct order.");
			}
		});

		threadD.setName("Thread D");
		threadD.fork();
		threadD.join();


		// Test Case 3: Check that timerInterrupt() terminates properly
		// 	(terminates instead of looping through the rest of the waiting threads
		Lib.debug(AlarmTestChar, "\nTest Case 3: Check that timerInterrupt() loop terminates properly.\nRun previous test with tracer code.");


		// Test Case 4: Check wait times and threads sorted properly 
		Lib.debug(AlarmTestChar, "\nTest Case 4: Check that the wait times and threads are being sorted correctly.\nRun previous test with tracer code.\n");

		KThread finish = new KThread(new Runnable(){
			public void run(){

			}
		});

		finish.fork();
		finish.join();
	}	

}


