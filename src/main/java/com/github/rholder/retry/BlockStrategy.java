/*
 * Copyright 2012-2015 Ray Holder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rholder.retry;

/**
 * 这是用于确定重试器应如何在两次重试之间阻塞的策略，通常这只是一个Thread.sleep()，但是如果需要的话，实现可能会更加复杂。
 */
public interface BlockStrategy {

    /**
     * Attempt to block for the designated amount of time. Implementations
     * that don't block or otherwise delay the processing from within this
     * method for the given sleep duration can significantly modify the behavior
     * of any configured {@link com.github.rholder.retry.WaitStrategy}. Caution
     * is advised when generating your own implementations.
     *
     * @param sleepTime the computed sleep duration in milliseconds
     * @throws InterruptedException
     */
    void block(long sleepTime) throws InterruptedException;
}