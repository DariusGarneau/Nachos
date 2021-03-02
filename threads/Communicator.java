package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */

Lock lock;
Condition2 speakerCondition;
Condition2 listenerCondition;
int word;
boolean received = false;
int listeners = 0;

public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        lock = new lock();
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
        lock.acquire();

        while(listeners == 0 || word == null){
            speakerCondition.sleep();
        }

        this.word = word;

        received = true;

        listenerCondition.wake();

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

        speakerCondition.wake();

        listenerCondition.sleep();

        listeners--;

        int transfer = word; 

        received = true;

        speaker.wake();

        lock.release();
         
	return word;
    }
}
