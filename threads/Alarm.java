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

//		System.out.println("Start of timerInterrupt()");

		// Yeild the current thread !!!!!!!!!!!!!!!!!!!!!! in the design doc we had this here
		// but shouldnt it be in the for loop? or after a check to see if a switch is needed?

		//KThread.yield();

		//store and disable interrupts
		boolean iStatus = Machine.interrupt().disable();
		for (int i=0; i < waitQueue.size(); ++i) {
		//	System.out.println("in timerInt for loop " + i);

			SleepingThread s = waitQueue.peek();
			if (Machine.timer().getTime() >= s.wakeTime){
				System.out.println("Waking Thread " + s.thread.toString());
				order.add(s.wakeTime);
				//yeild the current thread
				KThread.currentThread().yield();

				//put thread (in waitQueue) into readyState
				s.thread.ready();

				//remove thread from list
				waitQueue.poll();
			}else {
			//	System.out.println("in timerInt else");
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
	 *  TO-DO: complete it
	 *  	   replace print staments with debug stuff
	 *  
	 */

	public static void selfTest() {

		System.out.println("\n******* Alarm selfTest() Starting *******\n\nCreating alarm and test Threads");

		Alarm alarm = new Alarm();
		KThread threadA = null;
		KThread threadB = null;
		KThread threadC = null;
		KThread threadD = null;

		// Test Case 1: Put to sleep in order------------------------------------
		System.out.println("Test Case 1: Put to sleep in order.\nExecuting Threads...");
		threadA = new KThread(new Runnable(){
			public void run(){
				System.out.println("Thread A sleeping for 1000");
				alarm.waitUntil(1000);
			}
		});

		threadB = new KThread(new Runnable(){
			public void run(){
				System.out.println("Thread B sleeping for 2000");
				alarm.waitUntil(2000);
			}
		});

		threadC = new KThread(new Runnable(){
			public void run(){
				System.out.println("Thread B sleeping for 2000");
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
				System.out.println("Verifying threads woke in order...");
				if(order.get(0) < order.get(1) && order.get(1) < order.get(2))
					System.out.println("Test 1 Successful! Threads work up in the correct order.");
			}
		});

		threadD.fork();
		threadD.join();

//		Sleeper s1 = new Sleeper(10000);
//		KThread test1a = new KThread(s1);
//
//		Sleeper s2 = new Sleeper(20000);
//		KThread test1b = new KThread(s2);
//
//		test1a.fork();
//		test1b.fork();
//
//		test1a.join();
//		test1b.join();
		// Test Case 2: Put to sleep in a mixed order----------------------------



		// Test Case 3: Check that timerInterrupt() terminates properly
		// 	(terminates instead of looping through the rest of the waiting threads


		// Test Case 4: tba
	}	

	//	protected static class Sleeper implements Runnable {
	//
	//		private long time;
	//	//	Alarm alarm = null;
	//
	//		public Sleeper(long time){
	//			this.time = time;
	//	//`		alarm = new Alarm();
	//		}
	//
	//		@Override
	//		public void run() {
	//			System.out.println("Inside testThread");
	//			alarm.waitUntil(time);	
	//		}
	//
	//		public void test(){
	//			System.out.println("testing...");
	//		}
	//	}	
	//
	//
}


