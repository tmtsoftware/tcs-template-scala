package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.logging.scaladsl.LoggerFactory

// Add messages here
sealed trait MonitorMessage

object MonitorMessage {

  case class LocationEventMessage() extends MonitorMessage

}

object MonitorActor {
  def behavior(commandResponseManager: CommandResponseManager, loggerFactory: LoggerFactory): Behavior[MonitorMessage] =
    Behaviors.mutable(ctx ⇒ MonitorActor(ctx, commandResponseManager, loggerFactory))
}

case class MonitorActor(ctx: ActorContext[MonitorMessage],
                        commandResponseManager: CommandResponseManager,
                        loggerFactory: LoggerFactory)
    extends Behaviors.MutableBehavior[MonitorMessage] {

  import org.tmt.tcs.tcstemplateassembly.MonitorMessage._

  private val log = loggerFactory.getLogger

  override def onMessage(msg: MonitorMessage): Behavior[MonitorMessage] = {
    msg match {
      case (x: LocationEventMessage) => onLocationEventMessage(x)
      case _                         => log.error(s"unhandled message in Monitor Actor onMessage: $msg")
    }
    this
  }

  private def onLocationEventMessage(message: LocationEventMessage): Unit = {

    log.info("location event message handled")

  }

}
