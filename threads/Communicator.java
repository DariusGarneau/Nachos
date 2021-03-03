package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
	private Lock lock;
	private Condition2 speakerCondition;
	private Condition2 listenerCondition;
	private int word;
	private boolean received = false;
	private int listeners = 0;


	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
		lock = new Lock();
		speakerCondition = new Condition2(lock);
		listenerCondition = new Condition2(lock);
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 *
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 *
	 * @param	word	the integer to transfer.
	 */
	public void speak(int word) {
		Lib.assertTrue(!lock.isHeldByCurrentThread());
		lock.acquire();
		while(listeners == 0 || !received){
			speakerCondition.sleep();
			received = true;
		}
		this.word = word;
		received = true;
		listenerCondition.wakeAll();
		lock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return
	 * the <i>word</i> that thread passed to <tt>speak()</tt>.
	 *
	 * @return	the integer transferred.
	 */    
	public int listen() {
		lock.acquire();
		listeners++;
		while(!received){
			speakerCondition.wakeAll();
			listenerCondition.sleep();
		}
		listeners--;
		int transfer = word; 
		received = false;
		speakerCondition.wake();
		lock.release();
		return word;
	}

	public static void selfTest(){
		System.out.println("------------COMMUNICATOR TEST CASES---------------");

		Communicator cTest = new Communicator();

		//***************Test Case 1*******************
		KThread test1a = new KThread(new Runnable(){
			public void run(){
				System.out.println("Test Case 1: One Speaker and Listener.");
				System.out.println("Sending message 123...");
				cTest.speak(123);
			}
		});

		test1a.fork();

		KThread test1b = new KThread(new Runnable(){
			public void run(){
				int result = cTest.listen();
				System.out.println(result);
				if(result == 123)
					System.out.println("Test 1 Successful! Message " + result + " received.\n");
				else
					System.out.println("Test 1 Failed.\n");
			}
		});

		test1b.fork();
		test1b.join();
		test1a.join();


		//***************Test Case 2*******************
		//3 speakers and one listener
		
		Communicator speakers[] = new Communicator[3];
		boolean check[] = new boolean[3];

		for(int i = 0; i < speakers.length; i++)
			speakers[i] = new Communicator();

		KThread test2a = new KThread(new Runnable(){
			public void run(){
				System.out.println("Test Case 2: Multiple Speakers.\nSending message '1'");
				speakers[0].speak(1);
			}
		});

		test2a.fork();

		KThread test2b = new KThread(new Runnable(){
			public void run(){
				System.out.println("Sending message '2'");
				speakers[1].speak(2);
			}
		});

		test2b.fork();

		KThread test2c = new KThread(new Runnable(){
			public void run(){
				System.out.println("Sending message '3'");
				speakers[2].speak(3);
			}
		});

		test2c.fork();

		KThread test2d = new KThread(new Runnable(){
			public void run(){
				System.out.println("Listening for messages.");
				for(int i = 0; i < 3; i++){
					int result = speakers[i].listen();
					if(result == i+1)
						check[i] = true;//correct result receieved for each message
				}
				//check if each message was received
				if(check[0] && check[1] && check[2])
					System.out.println("Test 2 Successful! Each message was received by the listener.\n");
				else
					System.out.println("Test 2 Failed.");
			}
		});

		test2d.fork();
		test2d.join();
		test2a.join();
		test2b.join();
		test2c.join();


		//***************Test Case 3*******************
		

	}
}

