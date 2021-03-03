package nachos.threads;
import nachos.machine.*;
public class ReactWater{

    Lock lock;
    Condition2 hCondition;
    Condition2 oCondition;

    private static int hCount;
    private static int oCount;

    public ReactWater(){
        Lock = new Lock();
        hCondition = new Condition2(lock);
        oCondition = new Condition2(lock);
        hCount = 0;
        oCount = 0;
    }

    public void hReady(){
        lock.acquire();
        hCount++;

        if(oCount < 1 && hCount < 2){
            hCondition.sleep();
        }

        hCondition.wake();
        makeWater();
        lock.release();
    }

    public void oReady(){
        lock.acquire();
        oCount++;

        if(oCount < 1 && hCount < 2){
            oCondition.sleep();
        }

        oCondition.wake();
        makeWater();
        lock.release();
    }

    public void makeWater(){
        hCount = hCount - 2;
        oCount = oCount - 1;
        System.out.println("Water was made!");
    }




}