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

/**
 * A GameExecutor is a class that can execute TaskTrees. First, the GameExecutor
 * one or more Tasks which have no requirements. It then "unlocks" Tasks that 
 * required the Task that was run, until all Tasks have been run. GameExecutors
 * make no further guarantees of the actual order in which Tasks (and subtasks)
 * are run, but will attempt to obey the priority of Tasks.
 * 
 * For example, you run a TaskTree which contains two Tasks, task A and task B.
 * These two Tasks have no requirements. Depending on the GameExecutor, they may
 * be run either sequentially or in parallel to each other, but there is no
 * guarantee which one will be run or completed first.
 * 
 * However, if task B requires task A, the TaskTree is handled very differently.
 * Task B will not be run before task A has been completed, regardless of if 
 * there are free threads to run both A and B at the same time.
 */
public interface GameExecutor {
    
    /**
     * Runs the specified TaskTree with this GameExecutor.
     * @param tree The tree to run.
     */
    public void run(TaskTree tree);
    
    /**
     * Closes this GameExecutor. This terminates all threads that the
     * GameExecutor may have allocated. It is NOT safe to call this method
     * while the GameExecutor is running a TaskTree.
     */
    public void close();
}