package nachos.threads;
import nachos.machine.*;

public class ReactWater{

	Lock lock;
	Condition2 hCondition, oCondition;
	private static int hCount, oCount;
	private static final char ReactTestChar = 'r';

	/**
	 * Constructor for ReactWater
	 * initalizes lock conditions and counters
	 */
	public ReactWater(){
		lock = new Lock();
		hCondition = new Condition2(lock);
		oCondition = new Condition2(lock);
		hCount = 0;
		oCount = 0;
	} //end of constructor

	/**
	 * When H element comes, if there already exist another H element and an O element,
	 * then call makeWater()
	 * or let H element wait in line
	 */
	public void hReady(){
		lock.acquire();
		++hCount;

		while(oCount < 1 && hCount < 2)
			hCondition.sleep();

		hCondition.wake();
		makeWater();
		lock.release();
	}// end of hReady()

	/**
	 * When an O element comes, if there already exists another two H elements,
	 * then call makeWater()
	 * or let O element wait in line
	 */
	public void oReady(){
		lock.acquire();
		++oCount;

		while(oCount < 1 && hCount < 2)
			oCondition.sleep();

		oCondition.wake();
		makeWater();
		lock.release();
	}//end of oReady()

	/**
	 * Print out the message "Water was made!" when water is made.
	 */
	public void makeWater(){
		hCount -= 2;
		--oCount;
		Lib.debug(ReactTestChar, "Water was made!");
	}//end of makeWater()

	/**
	 * Self testing method for the class ReactWater()
	 */
	public static void selfTest(){

		Lib.debug(ReactTestChar, "\n\n******* Testing: Task 5 (ReactWater) *******\n");
		Lib.debug(ReactTestChar, "\nCreating hydrogen and oxygen...\n");


		//Create class to test functionality and some threads
		ReactWater motherNature = new ReactWater();
		KThread h1, h2, h3, h4, h5, o1, o2, o3, o4, o5;

		//Test Case 1: 2h and 1o
		Lib.debug(ReactTestChar, "TestCase1: Enough materials for 1 water molecule\nSuccessful if one water is made.");

		//initalise
		h1 = new KThread();
		h2 = new KThread();
		o1 = new KThread();

		h1.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		h2.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		o1.setTarget(new Runnable() {
			public void run() {
				motherNature.oReady();
			}
		});


		h1.fork();
		h2.fork();
		o1.fork();

		o1.join();

		//Test Case 2: 1o multiple (more than 4)h
		Lib.debug(ReactTestChar, "\nTestCase2: Abundance of Hydrogen\nSuccessful if one water is made.");

		//initalise
		h1 = new KThread();
		h2 = new KThread();
		h3 = new KThread();
		h4 = new KThread();
		h5 = new KThread();

		o1 = new KThread();

		h1.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		h2.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		h3.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		h4.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		h5.setTarget(new Runnable() {
			public void run() {
				motherNature.hReady();
			}
		});


		o1.setTarget(new Runnable() {
			public void run() {
				motherNature.oReady();
			}
		});


		h1.fork();
		h2.fork();
		h3.fork();
		h4.fork();
		h5.fork();

		o1.fork();

		o1.join();


		//Test Case 3: 2h multiple (more than2)o
		Lib.debug(ReactTestChar, "\nTestCase3: Abundance of Oxygen\nSuccessful if one water is made.");

		//initalise
		o1 = new KThread();
		o2 = new KThread();
		o3 = new KThread();
		o4 = new KThread();
		o5 = new KThread();

		h1 = new KThread();
		h2 = new KThread();

		h1.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		h2.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		o1.setTarget(new Runnable() {
			public void run(){
				motherNature.oReady();
			}
		});

		o2.setTarget(new Runnable() {
			public void run(){
				motherNature.oReady();
			}
		});

		o3.setTarget(new Runnable() {
			public void run() {
				motherNature.oReady();
			}
		});


		o4.setTarget(new Runnable() {
			public void run() {
				motherNature.oReady();
			}
		});

		o5.setTarget(new Runnable() {
			public void run() {
				motherNature.oReady();
			}
		});

		h1.fork();
		h2.fork();

		o1.fork();
		o2.fork();
		o3.fork();
		o4.fork();
		o5.fork();

		o1.join();


		//Test Case 4: many of both o and h
		Lib.debug(ReactTestChar, "\nTestCase4: Abundacne of both\nSuccessful if two waters are made.");

		//initalise
		h1 = new KThread();
		h2 = new KThread();
		h3 = new KThread();
		h4 = new KThread();
		h5 = new KThread();

		o1 = new KThread();
		o2 = new KThread();
		o3 = new KThread();
		o4 = new KThread();
		o5 = new KThread();

		h1.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		h2.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		h3.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		h4.setTarget(new Runnable() {
			public void run(){
				motherNature.hReady();
			}
		});

		h5.setTarget(new Runnable() {
			public void run() {
				motherNature.hReady();
			}
		});

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		o1.setTarget(new Runnable() {
			public void run(){
				motherNature.oReady();
			}
		});

		o2.setTarget(new Runnable() {
			public void run(){
				motherNature.oReady();
			}
		});

		o3.setTarget(new Runnable() {
			public void run() {
				motherNature.oReady();
			}
		});


		o4.setTarget(new Runnable() {
			public void run() {
				motherNature.oReady();
			}
		});

		o5.setTarget(new Runnable() {
			public void run() {
				motherNature.oReady();
			}
		});

		o1.fork();
		o2.fork();
		o3.fork();
		o4.fork();
		o5.fork();

		h1.fork();
		h2.fork();
		h3.fork();
		h4.fork();
		h5.fork();

		o1.join();


	}// end of selfTest()

}//end of class ReactWater()

