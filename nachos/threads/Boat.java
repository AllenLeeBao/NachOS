package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    static final int origin = 0;
    static final int destination = 1;

    static int childAtOrigin;
    static int childAtDestination;
    static int adultAtOrigin;
    static int adultAtDestination;
    static int boatLocation;
    
    static int childCount;
    
    static boolean needPilot;
    static boolean firstChild;

    static Lock boatLock = new Lock();
    static Condition2 childsWaitingAtOrigin = new Condition2(boatLock);
    static Condition2 adultsWaitingAtOrigin = new Condition2(boatLock);
    static Condition2 childsWaitingAtDestination = new Condition2(boatLock);
    
    public static void selfTest()
    {
        BoatGrader b = new BoatGrader();

		//Cancel the following codes to test
		
        //System.out.println("\n ***Testing Boats with only 2 children***");
        //begin(0, 2, b);

        //System.out.println("\n ***Testing Boats with only 2 children***");
        //begin(0, 1, b);
        
        //System.out.println("\n ***Testing Boats with only 2 children***");
        //begin(1, 0, b);
        
        //System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
        //begin(1, 2, b);

        //System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
        //begin(3, 3, b);
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
        childCount = children;

        needPilot = true;
        firstChild = false;
        
        Runnable childRunnable= new Runnable() {
            public void run() {
                ChildItinerary();
            }
        };

        for (int i = 0; i < adults; ++i) {
            KThread adult = new KThread(new Runnable() {
                public void run() {
                    AdultItinerary();
                }
            });
            adult.setName("Adult #" + i).fork();
        }

        for (int i = 0; i < children; ++i) {
            KThread child = new KThread(new Runnable() {
                public void run() {
                    if (childCount > 1) {
                        firstChild = false;
                        childCount--;
                    } else
                        firstChild = true;
                    ChildItinerary();
                }
            });
            child.setName("Child #" + i).fork();
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

        boatLock.acquire();

        while (boatLocation != origin || (childAtDestination == 0 && childAtOrigin > 0))
            adultsWaitingAtOrigin.sleep();
        
        bg.AdultRowToMolokai();
        adultAtOrigin --;
        adultAtDestination++;
        boatLocation = destination;

        childsWaitingAtDestination.wake();

        boatLock.release();
    }

    static void ChildItinerary()
    {
        bg.initializeChild(); //Required for autograder interface. Must be the first thing called.

        //DO NOT PUT ANYTHING ABOVE THIS LINE.

        int thisLocation = origin;
        boatLock.acquire();

        while (adultAtOrigin + childAtOrigin > 0) {
            //System.out.println("child go " + adultAtOrigin + " " + childAtDestination);
            if (thisLocation == origin) {
                // if boat is not at origin or an adult can go, let this child sleep
                if (boatLocation != origin || (adultAtOrigin > 0 && childAtDestination > 0) || !firstChild)
                    childsWaitingAtOrigin.sleep();
                firstChild = true;
                //System.out.println("not sleep");

                if (needPilot) {
                    bg.ChildRowToMolokai();
                    thisLocation = destination;
                    if (childAtOrigin > 1) {
                        //System.out.println("take a passenger");
                        //System.out.println(adultAtOrigin + " " + childAtDestination);
                        needPilot = false;
                        childsWaitingAtOrigin.wake();
                    } else {
                        boatLocation = destination;
                        childAtDestination++;
                        childAtOrigin--;
                    }
                    childsWaitingAtDestination.sleep();
                }
                else {
                    bg.ChildRideToMolokai();
                    thisLocation = destination;
                    boatLocation = destination;
                    childAtDestination += 2;
                    childAtOrigin -= 2;
                    needPilot = true;
                    if (childAtOrigin + adultAtOrigin > 0)
                        childsWaitingAtDestination.wake();
                    childsWaitingAtDestination.sleep();
                }
            }

            else {
                //System.out.println("child at destination: "+childAtDestination);
                bg.ChildRowToOahu();
                childAtDestination--;
                //System.out.println("child at destination: "+childAtDestination);
                childAtOrigin++;
                boatLocation = origin;
                thisLocation = origin;

                if (adultAtOrigin == 0)
                    childsWaitingAtOrigin.wake();
                else if (childAtDestination > 0)
                    adultsWaitingAtOrigin.wake();
                else
                    childsWaitingAtOrigin.wake();
                childsWaitingAtOrigin.sleep();
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
