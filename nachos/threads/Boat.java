package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    static final int origin = 0;
    static final int destination = 1;

    static int childAtOrigin = 0;
    static int childAtDestination = 0;
    static int adultAtOrigin = 0;
    static int adultAtDestination = 0;
    static int boatLocation = origin;
    static int passengerCount = 0;

    static Lock boatLock = new Lock();
    static Condition2 waitingAtOrigin = new Condition2(boatLock);
    static Condition2 waitingAtDestination = new Condition2(boatLock);
    static Condition2 waitingForBoatFull = new Condition2(boatLock);
    
    public static void selfTest()
    {
        BoatGrader b = new BoatGrader();

        System.out.println("\n ***Testing Boats with only 2 children***");
        begin(0, 2, b);
/*
        System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
        begin(1, 2, b);

        System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
        begin(3, 3, b);*/
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
        // Store the externally generated autograder in a class
        // variable to be accessible by children.
        bg = b;

        // Instantiate global variables here

        // Create threads here. See section 3.4 of the Nachos for Java
        // Walkthrough linked from the projects page.

	    /*Runnable r = new Runnable() {
	        public void run() {
                SampleItinerary();
            }
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork();*/

	    childAtDestination = 0;
	    childAtOrigin = children;
	    adultAtDestination = 0;
	    adultAtOrigin = adults;
	    boatLocation = origin;
	    passengerCount = 0;

        Runnable adultRunnable = new Runnable() {
            public void run() {
                AdultItinerary();
            }
        };

        Runnable childRunnable= new Runnable() {
            public void run() {
                ChildItinerary();
            }
        };

        for (int i = 0; i < adults; ++i) {
            KThread adult = new KThread(adultRunnable);
            adult.setName("Adult #" + i);
            adult.fork();
        }

        for (int i = 0; i < children; ++i) {
            KThread child = new KThread(childRunnable);
            child.setName("Child #" + i);
            child.fork();
        }

    }

    static void AdultItinerary()
    {
        bg.initializeAdult(); //Required for autograder interface. Must be the first thing called.
        //DO NOT PUT ANYTHING ABOVE THIS LINE.

        /* This is where you should put your solutions. Make calls
           to the BoatGrader to show that it is synchronized. For
           example:
               bg.AdultRowToMolokai();
           indicates that an adult has rowed the boat across to Molokai
        */

        int thisLocation = origin; //start at origin;
        boatLock.acquire();

        while (true) {
            if (thisLocation == origin) {
                while (boatLocation != origin || childAtOrigin > 1 || passengerCount > 0) {
                    waitingAtOrigin.sleep();
                }
                bg.AdultRowToMolokai();
                adultAtOrigin --;
                adultAtDestination++;
                boatLocation = destination;
                thisLocation = destination;

                waitingAtDestination.wakeAll();
                waitingAtDestination.sleep();
            }// end if (start at origin)

            else if (thisLocation == destination) {
                waitingAtDestination.sleep();
            }
            else {
                break;
            }
        }

        boatLock.release();
    }

    static void ChildItinerary()
    {
        bg.initializeChild(); //Required for autograder interface. Must be the first thing called.

        //DO NOT PUT ANYTHING ABOVE THIS LINE.

        int thisLocation = origin;
        boatLock.acquire();

        while (true) {
            if (thisLocation == origin) {
                while (boatLocation != origin || passengerCount >= 2 ||
                        (adultAtOrigin >= 1 && childAtOrigin == 1)) {
                    waitingAtDestination.sleep();
                }
                //ready to go: the boat is at origin, the boat isn't full yet,
                // (there can't be exactly 1 adult and 1 child at origin)
                waitingAtOrigin.wakeAll();
                //If at least two child at origin, they go
                //If exactly one child and no adult at origin, child go

                if (childAtOrigin == 1 && adultAtOrigin == 0) {
                    ++passengerCount;
                    childAtOrigin--;
                    childAtDestination++;
                    boatLocation = destination;
                    thisLocation = destination;

                    if (passengerCount == 1)
                        bg.ChildRowToMolokai();
                    else
                        bg.ChildRideToMolokai();
                    passengerCount = 0;

                    waitingAtDestination.sleep();
                }

                else if (childAtOrigin >= 2) {
                    ++passengerCount;

                    if (passengerCount == 1) {
                        waitingForBoatFull.sleep();

                        //when woke by the second child
                        bg.ChildRowToMolokai();
                        childAtOrigin --;
                        childAtDestination ++;
                        thisLocation = destination;

                        waitingForBoatFull.wake();
                        waitingAtDestination.sleep();
                    }

                    else if (passengerCount == 2) {
                        waitingForBoatFull.wake();
                        waitingForBoatFull.sleep();

                        bg.ChildRideToMolokai();
                        boatLocation = destination;
                        thisLocation = destination;
                        childAtOrigin--;
                        childAtDestination ++;

                        passengerCount = 0;

                        waitingAtDestination.wakeAll();
                        waitingAtDestination.sleep();
                    }
                } // if childAtOrigin >= 2
            }// if thisLocation == origin

            else if (thisLocation == destination) {
                while (boatLocation != destination && (childAtOrigin + adultAtOrigin == 0)) {
                    waitingAtDestination.sleep();
                }
                childAtDestination--;
                childAtOrigin++;
                bg.ChildRowToOahu();
                boatLocation = origin;
                thisLocation = origin;

                waitingAtOrigin.wakeAll();
                waitingAtOrigin.sleep();
            }

            else {
                break;
            }
        }
        boatLock.release();
    }

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    
}
