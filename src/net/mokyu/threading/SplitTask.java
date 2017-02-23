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

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A special task with several subtasks. In this Task, you do not override
 * run(). Instead you override runSubtask(int). This method is called
 * a number of times specified by the subtask parameter, with the subtask 
 * number 0 to subtasks-1. This allows you do parallel computations within a
 * single Task.
 * 
 * For example, you want to parallelize movement updating. Instead of
 * creating lots of BasicTasks, you can simply create one SplitTask.
 * You can then do the updating on part of the objects you're updating in
 * in each subtask. Either have each subtask update every n:th object, or
 * have each subtask update a block of units.
 * 
 * SplitTasks have their logic in the runSubtask(int) function, but they
 * also have a finish() method which is called ONCE after all subtasks have
 * been completed. This allows you to merge, combine, finalize, e.t.c the
 * processing done by the different subtasks.
 * 
 * The number of subtasks can be changed when an GameExecutor is not
 * running the TaskTree which this SplitTask is added to. This allows you
 * to dynamically change the number of subtasks and adapting to the amount
 * of work that has to be done in this task. This can help reduce the
 * synchronization overhead. It is possible to override the setSubtasks(int)
 * function to allocate data for each subtask, e.t.c, when the number of
 * subtasks is changed.
 */
public abstract class SplitTask extends Task{

    private int subtasks;
    private AtomicInteger startCount, endCount;

    /**
     * Constructs a new SplitTask with the specified parameters.
     * 
     * @param id The ID number of the new SplitTask. Must be unique.
     * @param taskPriority The relative priority of the task.
     * @param subtasks The number of subtasks. Has to be more or equal to 1.
     */
    public SplitTask(int id, int taskPriority, int subtasks){
        super(id, taskPriority);
        if(subtasks < 1){
            throw new IllegalArgumentException("Number of subtasks has to be at least 1.");
        }
        this.subtasks = subtasks;
        startCount = new AtomicInteger(0);
        endCount = new AtomicInteger(0);
    }

    @Override
    void addToQueue(Queue<Task> queue) {
        for(int i = 0; i < subtasks; i++){
            queue.add(this);
        }
    }

    /**
     * Do not override this method for SplitTasks. Override runSubtask(int)
     * instead.
     */
    @Override
    protected void run(){
        runSubtask(startCount.getAndIncrement());
    }

    /**
     * This method is called once for each subtask, with a unique subtask ID for
     * each call.
     * @param subtask 
     */
    protected abstract void runSubtask(int subtask);

    @Override
    boolean complete() {
        int endID = endCount.incrementAndGet();
        if(endID == subtasks){
            startCount.set(0);
            endCount.set(0);
            return true;
        }
        return false;
    }

    /**
     * Sets the number of subtasks of this SplitTask. Do NOT call this method
     * while a GameExecutor is running on a TaskTree containing this SplitTask.
     * The result in such a case is undefined (AKA kaboom).
     * 
     * @param subtasks The new number of subtasks. Has to be more or equal to 1.
     */
    public void setSubtasks(int subtasks){
        if(subtasks < 1){
            throw new IllegalArgumentException("Number of subtasks has to be at least 1.");
        }
        this.subtasks = subtasks;
    }
    
    /**
     * Getter for the number of subtasks.
     * @return the number of subtasks.
     */
    public int getSubtasks(){
        return subtasks;
    }
}