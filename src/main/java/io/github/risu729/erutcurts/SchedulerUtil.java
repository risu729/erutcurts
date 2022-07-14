/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

final class SchedulerUtil {

  public static ScheduledExecutorService newScheduledDaemonThreadPool(int corePoolSize) {
    return Executors.newScheduledThreadPool(1,
        new ThreadFactory() {
          @Override
          public Thread newThread(Runnable runnable) {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
          }
        });
  }

  private SchedulerUtil() {
    throw new AssertionError();
  }
}