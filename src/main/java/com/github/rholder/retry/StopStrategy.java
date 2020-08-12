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
 * 用于确定重试器在失败尝试之后是否必须停止重试的策略。
 *
 * @author JB
 */
public interface StopStrategy {

    /**
     * 如果重试器应停止重试，则返回true
     *
     * @param failedAttempt 之前执行的失败的{@code Attempt}
     * @return 如果retryer要停止任务，则返回true，否则返回false
     */
    boolean shouldStop(Attempt failedAttempt);

}
