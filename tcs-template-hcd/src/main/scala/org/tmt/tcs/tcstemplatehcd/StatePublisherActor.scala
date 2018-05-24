package org.tmt.tcs.tcstemplatehcd

import java.time.Instant
import java.util.concurrent.TimeUnit

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import csw.framework.scaladsl.CurrentStatePublisher
import csw.messages.params.generics.KeyType
import csw.messages.params.models.Prefix
import csw.messages.params.models.Units.degree
import csw.messages.params.states.CurrentState
import csw.services.logging.scaladsl.LoggerFactory

import scala.concurrent.duration.{Duration}

// Add messages here
sealed trait StatePublisherMessage

object StatePublisherMessage {

  case class StartMessage()   extends StatePublisherMessage
  case class StopMessage()    extends StatePublisherMessage
  case class PublishMessage() extends StatePublisherMessage

}

private case object TimerKey

object StatePublisherActor {
  def behavior(currentStatePublisher: CurrentStatePublisher, loggerFactory: LoggerFactory): Behavior[StatePublisherMessage] =
    Behaviors.withTimers(timers ⇒ StatePublisherActor(timers, currentStatePublisher, loggerFactory))
}

case class StatePublisherActor(timer: TimerScheduler[StatePublisherMessage],
                               currentStatePublisher: CurrentStatePublisher,
                               loggerFactory: LoggerFactory)
    extends Behaviors.MutableBehavior[StatePublisherMessage] {

  import org.tmt.tcs.tcstemplatehcd.StatePublisherMessage._

  //prefix
  val prefix = Prefix("tcs.test")

  //keys
  val timestampKey = KeyType.TimestampKey.make("timestampKey")

  val azPosKey        = KeyType.DoubleKey.make("azPosKey")
  val azPosErrorKey   = KeyType.DoubleKey.make("azPosErrorKey")
  val elPosKey        = KeyType.DoubleKey.make("elPosKey")
  val elPosErrorKey   = KeyType.DoubleKey.make("elPosErrorKey")
  val azInPositionKey = KeyType.BooleanKey.make("azInPositionKey")
  val elInPositionKey = KeyType.BooleanKey.make("elInPositionKey")

  private val log = loggerFactory.getLogger

  override def onMessage(msg: StatePublisherMessage): Behavior[StatePublisherMessage] = {
    msg match {
      case (x: StartMessage)   => doStart(x)
      case (y: StopMessage)    => doStop(y)
      case (y: PublishMessage) => doPublishMessage(y)
      case _                   => log.error(s"unhandled message in StatePublisher Actor onMessage: $msg")
    }
    this
  }

  private def doStart(message: StartMessage): Unit = {
    log.info("start message received")

    timer.startPeriodicTimer(TimerKey, PublishMessage(), Duration.create(1, TimeUnit.SECONDS))

    log.info("start message completed")

  }

  private def doStop(message: StopMessage): Unit = {

    log.info("stop message completed")

  }

  private def doPublishMessage(message: PublishMessage): Unit = {

    log.debug("publishMessage received")

    // example parameters for a current state

    val azPosParam        = azPosKey.set(35.34).withUnits(degree)
    val azPosErrorParam   = azPosErrorKey.set(0.34).withUnits(degree)
    val elPosParam        = elPosKey.set(46.7).withUnits(degree)
    val elPosErrorParam   = elPosErrorKey.set(0.03).withUnits(degree)
    val azInPositionParam = azInPositionKey.set(false)
    val elInPositionParam = elInPositionKey.set(true)

    val timestamp = timestampKey.set(Instant.now)

    //create CurrentState and use sequential add
    val currentState = CurrentState(prefix)
      .add(azPosParam)
      .add(elPosParam)
      .add(azPosErrorParam)
      .add(elPosErrorParam)
      .add(azInPositionParam)
      .add(elInPositionParam)
      .add(timestamp)

    currentStatePublisher.publish(currentState)

  }

}
