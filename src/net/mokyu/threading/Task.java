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
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the base class of the 3 task types BasicTask, DrawTask and SplitTask.
 * There shouldn't be any reason for you to use this class directly.
 */
public abstract class Task implements Comparable<Task>{

    private static final int DEFAULT_ID_CAPACITY = 3;
    private static final int DEFAULT_TASK_CAPACITY = 3;

    private int id;
    private ArrayList<Integer> requiredIDs;
    private ArrayList<Task> unlockedTasks;
    private int neededUnlocks;
    private AtomicInteger unlockCounter;

    private int priority;

    //Setup

    /**
     * Constructs a new Task with the specified parameters. This is the base
     * class of the 3 task types BasicTask, DrawTask and SplitTask. There
     * shouldn't be any reason for you to use this class directly.
     * 
     * @param id The ID number of the new Task. Must be unique.
     * @param taskPriority The relative priority of the task.
     */
    public Task(int id, int taskPriority) {
        this.id = id;
        this.priority = taskPriority;
        requiredIDs = new ArrayList(DEFAULT_ID_CAPACITY);
        unlockedTasks = new ArrayList(DEFAULT_TASK_CAPACITY);
        unlockCounter = new AtomicInteger(0);
    }
    
    /**
     * Adds the specified Task to the tasks that has to be completed before this
     * Task can be run. Equal to calling addRequiredTask(task.getID());
     * @param task The Task required by this Task
     */
    public void addRequiredTask(Task task){
        addRequiredTask(task.getID());
    }

    /**
     * Adds the specified task ID to the tasks that has to be completed before
     * this Task can be run. addRequiredTask(Task) is the preferred method of 
     * doing this. Use this if the required Task has not been created yet.
     * 
     * @param id 
     */
    public void addRequiredTask(int id) {
        requiredIDs.add(id);
    }


    //Internal

    ArrayList<Integer> getRequiredIDs(){
        return requiredIDs;
    }

    void addUnlockedTask(Task task) {
        unlockedTasks.add(task);
    }

    void update(){
        neededUnlocks = requiredIDs.size();
    }

    boolean unlock(){
        int i = unlockCounter.incrementAndGet();
        if(i == neededUnlocks){
            unlockCounter.set(0);
            return true;
        }
        
        return false;
    }

    abstract void addToQueue(Queue<Task> queue);
    
    /**
     * This is the method that is called by the GameExecutor. This is where
     * you put your logic.
     */
    protected abstract void run();
    
    abstract boolean complete();
    
    /**
     * This method is called when the Task is finished. Useless for BasicTasks
     * and DrawTasks, but very useful for SplitTasks. It is called once when all
     * subtasks has finished in the case of SplitTasks, allowing you to finalize
     * the work done by the multiple threads.
     */
    public abstract void finish();

    ArrayList<Task> getUnlockedTasks(){
        return unlockedTasks;
    }


    //Useful
    /**
     * Used for sorting the Tasks based on priority.
     * 
     * @param task The Task to compare this Task with.
     * @return the difference between the given Task's priority and this Task's
     * priority.
     */
    public int compareTo(Task task) {
        return task.getPriority() - priority;
    }

    /**
     * Getter for ID.
     * @return this Task's ID.
     */
    public int getID(){
        return id;
    }

    public int getPriority(){
        return priority;
    }
}