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
import java.util.PriorityQueue;

/**
 * A single threaded GameExecutor that runs TaskTrees in the thread calling 
 * the run() method. Obviously, this does not provide any core parallelism, so 
 * it obviously has very limited use. If a computer only has a single core, then 
 * it is a good idea to use this class instead, as it does not have the
 * synchronization overhead of a threaded GameExecutor.
 */
public class SingleThreadExecutor implements GameExecutor {

    private PriorityQueue<Task> taskQueue;

    /**
     * Constructs a new single threaded GameExecutor.
     */
    public SingleThreadExecutor() {
        taskQueue = new PriorityQueue();
    }

    /**
     * Runs the given TaskTree in the thread calling run().
     * @param tree 
     */
    @Override
    public void run(TaskTree tree) {
        ArrayList<Task> rootTasks = tree.getRootTasks();
        for(int i = 0; i < rootTasks.size(); i++){
            rootTasks.get(i).addToQueue(taskQueue);
        }

        for (Task task = taskQueue.poll(); task != null; task = taskQueue.poll()) {

            task.run();

            if (task.complete()) {
                task.finish();
                ArrayList<Task> newTasks = task.getUnlockedTasks();
                for (Task newTask : newTasks) {
                    if (newTask.unlock()) {
                        newTask.addToQueue(taskQueue);
                    }
                }
            }
        }
    }
    
    /**
     * Does nothing, as this GameExecutor does not allocate any new threads.
     */
    public void close(){}
}
