package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParallelProcessing {

    private final ArrayList<Runnable> runners;

    private final Thread[] threads;
    
    private int processed;
    
    public final List<Runnable> skiped;

    public ParallelProcessing(final Runnable[] runners, final int numThreads){
        this.runners = new ArrayList<>();
        this.runners.addAll(Arrays.asList(runners));
        threads = new Thread[numThreads];
        processed = 0;
        skiped = new ArrayList<>();
    }

    public void join() throws InterruptedException {
        for (final Thread thread : this.threads) {
            thread.join();
        }
    }

    public synchronized int numProcessed() {
        return processed;
    }

    public void start() {
        for (int i = 0; i < this.threads.length; i++) {
            this.threads[i] = new Thread() {
                @Override
                public void run() {
                    Runnable element = nextElement();
                    while (element != null) {
                    	try {
                    		element.run();
                    		addProcessed();
                    	} catch(Throwable e){
                    		addSkip(element);
                    		e.printStackTrace(System.err);
                    	}
                        element = nextElement();
                    }
                }
            };
            this.threads[i].start();
        }
    }
    
    private synchronized void addProcessed(){
    	processed++;
    }
    
    private synchronized void addSkip(final Runnable runner){
    	skiped.add(runner);
    }

    private synchronized Runnable nextElement() {
        if (this.runners.isEmpty()) {
            return null;
        }
        final Runnable element = this.runners.remove(0);
        return element;
    }
}
