/*
 * Copyright (c) 2011 Daniel Isheden
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.mokyu.threading;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A multi-threaded GameExecutor. This GameExecutor runs different Tasks in
 * different threads, allowing up to near linear scaling in optimal conditions
 * with multi-core processors. It may also run different subtasks of SplitTasks
 * in different threads.
 * 
 * This GameExecutor guarantees that all DrawTasks will be run with the thread
 * calling run(TaskTree), allowing you to make OpenGL calls in the DrawTask's
 * run() method, assuming the thread calling the GameExecutor's run(TaskTree)
 * method has the OpenGL context.
 * 
 * The number of threads used by a MultithreadedExecutor is actually the number
 * of threads specified plus 1. The last thread is the thread calling the 
 * run(TaskTree) method, which is only used for DrawTasks. This means that it
 * may be faster to use available cores-1 threads, if there are many and/or
 * processor heavy DrawTasks in the TaskTree(s) that will be run by this 
 * GameExecutor, but this is very rarely the case.
 */
public class MultithreadedExecutor implements GameExecutor {

    private PriorityBlockingQueue<Task> taskQueue;
    private PriorityBlockingQueue<Task> drawQueue;

    private int numThreads;
    private WorkerThread[] threads;
    private Thread drawThread;

    private int totalTasks;
    private AtomicInteger finishedTasks;
    private final Object counterSyncObject = new Object();
    
    private volatile boolean running = true;

    /**
     * Creates a new MultithreadedGameExecutor which has the specified number
     * of threads for logic Tasks (BasicTasks and SplitTasks). DrawTasks will be
     * run using the thread calling run(TaskTree).
     * 
     * Tip: The number of cores on the computer running the game can be queried by
     * calling Runtime.getRuntime().availableProcessors().
     * @param numThreads the number of threads to use.
     */
    public MultithreadedExecutor(int numThreads) {
        taskQueue = new PriorityBlockingQueue();
        drawQueue = new PriorityBlockingQueue();
        finishedTasks = new AtomicInteger(0);

        if (numThreads < 1) {
            throw new IllegalArgumentException("Minimum threads: 1");
        }

        this.numThreads = numThreads;
        startThreads();
    }

    private void startThreads() {
        threads = new WorkerThread[numThreads];
        for(int i = 0; i < numThreads; i++){
            threads[i] = new WorkerThread(i);
        }
    }

    @Override
    public void run(TaskTree tree) {
        if(!running){
            return;
        }
        drawThread = Thread.currentThread();
        //drawThread.setPriority(Thread.MAX_PRIORITY);
        ArrayList<Task> rootTasks = tree.getRootTasks();
        Task t;
        for (int i = 0; i < rootTasks.size(); i++) {
            t = rootTasks.get(i);
            if (t instanceof DrawTask) {
                t.addToQueue(drawQueue);
            } else {
                t.addToQueue(taskQueue);
            }
        }
        
        totalTasks = tree.getNumTasks();

        while (true) {
            Task task = null;
            while (task == null) {
                try {
                    task = drawQueue.take();
                } catch (InterruptedException ex) {
                    //All tasks are completed.
                    return;
                }
            }
            
            try{
                task.run();
            }catch(Throwable throwable){
                System.err.println("Uncaught exception in draw thread:");
                throwable.printStackTrace();
            }
            
            if (task.complete()) {
                task.finish();
                ArrayList<Task> newTasks = task.getUnlockedTasks();
                for (Task newTask : newTasks) {
                    if (newTask.unlock()) {
                        if (newTask instanceof DrawTask) {
                            newTask.addToQueue(drawQueue);
                        } else {
                            newTask.addToQueue(taskQueue);
                        }
                    }
                }
                synchronized (counterSyncObject) {
                    if (finishedTasks.incrementAndGet() == totalTasks) {
                        finishedTasks.set(0);
                        return;
                    }
                }
            }
        }
    }

    public void close() {
        running = false;
        for (WorkerThread t : threads) {
            t.interrupt();
        }
    }

    private class WorkerThread extends Thread {

        private int id;

        public WorkerThread(int id) {
            this.id = id;
            setName("Worker thread " + id);
            setDaemon(true);
            start();
        }

        public void run() {
            Task task;
            while (running) {
                try {
                    task = taskQueue.take();
                } catch (InterruptedException ex) {
                    //System.out.println(getName() + " stopped.");
                    break;
                }
                
                try{
                    task.run();
                }catch(Throwable throwable){
                    System.err.println("Uncaught exception in worker thread " + id + ":");
                    throwable.printStackTrace();
                }
                
                if (task.complete()) {
                    task.finish();
                    ArrayList<Task> newTasks = task.getUnlockedTasks();
                    for (Task newTask : newTasks) {
                        if (newTask.unlock()) {
                            if (newTask instanceof DrawTask) {
                                newTask.addToQueue(drawQueue);
                            } else {
                                newTask.addToQueue(taskQueue);
                            }
                        }
                    }
                    if (finishedTasks.incrementAndGet() == totalTasks) {
                        finishedTasks.set(0);
                        drawThread.interrupt();
                    }
                }
            }
        }
    }
}
