package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.logging.scaladsl.LoggerFactory

// Add messages here
sealed trait LifecycleMessage

object LifecycleMessage {

  case class InitializeMessage() extends LifecycleMessage
  case class ShutdownMessage()   extends LifecycleMessage

}

object LifecycleActor {
  def behavior(commandResponseManager: CommandResponseManager, loggerFactory: LoggerFactory): Behavior[LifecycleMessage] =
    Behaviors.mutable(ctx ⇒ LifecycleActor(ctx, commandResponseManager, loggerFactory))
}

case class LifecycleActor(ctx: ActorContext[LifecycleMessage],
                          commandResponseManager: CommandResponseManager,
                          loggerFactory: LoggerFactory)
    extends Behaviors.MutableBehavior[LifecycleMessage] {

  import org.tmt.tcs.tcstemplateassembly.LifecycleMessage._

  private val log = loggerFactory.getLogger

  override def onMessage(msg: LifecycleMessage): Behavior[LifecycleMessage] = {
    msg match {
      case (x: InitializeMessage) => doInitialize(x)
      case (y: ShutdownMessage)   => doShutdown(y)
      case _                      => log.error(s"unhandled message in Lifecycle Actor onMessage: $msg")
    }
    this
  }

  private def doInitialize(message: InitializeMessage): Unit = {

    // TODO: load configuration
    // how do other components get their config?  Send a message?

    log.info("initialize command completed")

  }

  private def doShutdown(message: ShutdownMessage): Unit = {

    log.info("shutdown command completed")

  }

}
