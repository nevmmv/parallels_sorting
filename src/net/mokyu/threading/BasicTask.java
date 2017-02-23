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

/**
 * A BasicTask is a simple Task with just a run() method that is called by the 
 * GameExecutor.
 */

public abstract class BasicTask extends Task{
    
    /**
     * Constructs a new BasicTask with the specified parameters.
     * 
     * @param id The ID number of the new BasicTask. Must be unique.
     * @param taskPriority The relative priority of the task.
     */
    public BasicTask(int id, int taskPriority){
        super(id, taskPriority);
    }

    @Override
    void addToQueue(Queue<Task> queue) {
        queue.add(this);
    }

    @Override
    boolean complete() {
        return true;
    }
    
    /**
     * Useless for BasicTasks (and DrawTasks). Just put your finishing code at
     * the end of run(). It is called after run for all tasks though, so you can use it if
     * you want.
     */
    public void finish(){}
}