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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A TaskTreeBuilder is a factory for TaskTrees. The TaskTrees constructed can
 * then be run by a GameExecutor as many times as needed.
 * 
 * A TaskTree is constructed by creating Task objects, setting their required
 * Tasks, adding them to the TaskTreeBuilder and then calling build(), which
 * returns a TaskTree.
 * 
 * The same TaskTreeBuilder can be reused. The TaskTreeBuilder is completely
 * cleared when build() is called.
 */
public class TaskTreeBuilder {

    private HashMap<Integer, Task> taskTable;

    /**
     * Constructs a new TaskTreeBuilder.
     */
    public TaskTreeBuilder(){
        taskTable = new HashMap();
    }

    /**
     * Adds a Task to this TaskTreeBuilder so that will be included in the 
     * produced TaskTree. If a Task with the same ID as the Task provided has 
     * already been added, an exception is thrown. All Task IDs have to be
     * unique.
     * @param task The Task to add.
     */
    public void addTask(Task task){
        if(taskTable.put(task.getID(), task) != null){
            throw new IllegalArgumentException("Task ID collision: " + task.getID());
        }
    }

    /**
     * Getter for the current number of Tasks currently in this TaskTreeBuilder.
     * Note that the TaskTreeBuilder is cleared after a call to build().
     * @return the current number of Tasks in this TaskTreeBuilder.
     */
    public int getNumTasks(){
        return taskTable.size();
    }

    /**
     * Constructs a TaskTree from the Tasks added to this TaskTreeBuilder. This
     * TaskTree can then be run any number of times by a GameExecutor. This
     * method throws an exception if a Task requires a Task ID that has not 
     * been added to this TaskTreeBuilder. An exception is also thrown if there
     * are no immediately runnable Tasks in this TaskTree, e.g. all Tasks
     * require at least one other Task to be run before them, meaning there is 
     * nowhere to start. No further checks are made on the constructed TaskTree.
     * 
     * If the Tasks in this TaskTreeBuilder contains a requirement loop (task A 
     * requires task B, which in turn requires task A), then the GameExecutor 
     * running the produced TaskTree may either freeze or return after running 
     * what it can, leaving the TaskTree in an inconsistent state. No checking 
     * is done by the GameExecutor due to the high synchronization cost this
     * would require.
     * 
     * This TaskTreeBuilder is reset and reusable after this method returns.
     * @return the TaskTree constructed.
     */
    public TaskTree build(){
        Collection tasks = taskTable.values();
        if(tasks.isEmpty()){
            return null;
        }

        ArrayList<Task> rootTasks = new ArrayList();

        Iterator<Task> iterator = tasks.iterator();
        Task task;
        while(iterator.hasNext()){
            task = iterator.next();
            ArrayList<Integer> requiredIDs = task.getRequiredIDs();

            if(requiredIDs.isEmpty()){
                rootTasks.add(task);
            }

            for(int i = 0; i < requiredIDs.size(); i++){
                Task temp = taskTable.get(requiredIDs.get(i));
                if(temp == null){
                    throw new IllegalStateException("A task is depending on a task that doesn't exist. Missing ID: " + requiredIDs.get(i));
                }
                temp.addUnlockedTask(task);
            }
        }
        
        if(rootTasks.isEmpty()){
            throw new IllegalStateException("There are no immediately runnable Tasks in the produced TaskTree. (Requirement loop)");
        }

        iterator = tasks.iterator();
        while(iterator.hasNext()){
            task = iterator.next();
            task.update();
            Collections.sort(task.getUnlockedTasks());
        }

        int totalTasks = taskTable.size();
        taskTable.clear();

        Collections.sort(rootTasks);
        return new TaskTree(rootTasks, totalTasks);
    }
}