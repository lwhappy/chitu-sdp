
package com.chitu.bigdata.sdp.flink.common.util

import com.google.common.util.concurrent.ThreadFactoryBuilder

import java.util.concurrent.{ExecutorService, ThreadFactory, TimeUnit}


object ThreadUtils {

  def threadFactory(threadName: String, isDaemon: Boolean): ThreadFactory = new ThreadFactoryBuilder().setNameFormat(threadName + "-%d").setDaemon(isDaemon).build

  def threadFactory(threadName: String): ThreadFactory = threadFactory(threadName, isDaemon = true)

  @throws[InterruptedException]
  def shutdownExecutorService(executorService: ExecutorService): Unit = {
    shutdownExecutorService(executorService, 5)
  }

  @throws[InterruptedException]
  def shutdownExecutorService(executorService: ExecutorService, timeoutS: Int): Unit = {
    if (executorService != null && !executorService.isShutdown) {
      executorService.shutdown()
      if (!executorService.awaitTermination(timeoutS, TimeUnit.SECONDS)) {
        executorService.shutdownNow
        executorService.awaitTermination(timeoutS, TimeUnit.SECONDS)
      }
    }
  }
}
