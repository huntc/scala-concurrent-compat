package com.typesafe.scalacompat.concurrency

import java.util.concurrent.{ Executor, Callable }
import com.typesafe.scalacompat.concurrent.DeferredResult

class Future[T] {
  val future: scala.concurrent.Future[T] = ???

  def onSuccess(r: Callable[T]) {}

  def onSuccess(r: Callable[T], executor: Executor) {}

  def onFailure(r: Callable[T]) {}

  def onFailure(r: Callable[T], executor: Executor) {}

  def onComplete(r: Callable[T]) {}

  def onComplete(r: Callable[T], executor: Executor) {}

  def isCompleted: Boolean = ???
}
