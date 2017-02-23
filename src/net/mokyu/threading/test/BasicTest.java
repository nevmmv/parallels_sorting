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

package net.mokyu.threading.test;

import net.mokyu.threading.BasicTask;
import net.mokyu.threading.SingleThreadExecutor;
import net.mokyu.threading.TaskTree;
import net.mokyu.threading.TaskTreeBuilder;

public class BasicTest{
    public static void main(String[] args){
        System.out.println("Building a basic TaskTree...");
        
        TaskTreeBuilder builder = new TaskTreeBuilder();
        builder.addTask(new MyTask());
        TaskTree tree = builder.build();
        
        System.out.println("Creating a single-thread GameExecutor...");
        SingleThreadExecutor executor = new SingleThreadExecutor();
        
        System.out.println("Running the TaskTree...");
        executor.run(tree);
        
        System.out.println("Done!");
    }
    
    private static class MyTask extends BasicTask{
        
        public MyTask(){
            super(0, 0);
        }

        @Override
        protected void run() {
            System.out.println("Hello! I'm a BasicTask!");
        }
    }
}
