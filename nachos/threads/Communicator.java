package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
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
    	
    	// while the listener is listening (busy), put speaker into sleep
    	while (isListening) {
    		// add speaker to the speaker waitQueue
    		// put it to sleep
    		speakerCondition.sleep();
    	}
    	
    	// else
    	// if the listener waitQueue is not empty
    	// wake up listener, place the listener in the ready queue
    	listenerCondition.wake();
    	// receiving the word
    	this.word = word;
    	
    	// update the listener in listening mode (busy)
    	isListening = true;
    		
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

    	// while the listener is not listening (free), put listener into sleep
		while (!isListening) {
			// add listener to the listener waitQueue
			// put it to sleep
			listenerCondition.sleep();
		}
		
		// else
		// if the speaker waitQueue is not empty
		// wake up speaker, place the speaker in the ready queue
		speakerCondition.wake();
		
		// update the listener in non-listening mode (free)
		isListening = false;

		lock.release();
		
		return word;
    }
    
    int word = 0;
    boolean isListening = false;
    private Lock lock = new Lock();
    private Condition2 speakerCondition = new Condition2(lock);
    private Condition2 listenerCondition = new Condition2(lock);
}
