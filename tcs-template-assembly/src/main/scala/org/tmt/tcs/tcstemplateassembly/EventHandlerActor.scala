package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.logging.scaladsl.LoggerFactory

// Add messages here
sealed trait EventMessage

object EventMessage {

  case class EventPublishMessage() extends EventMessage

}

object EventHandlerActor {
  def behavior(commandResponseManager: CommandResponseManager, loggerFactory: LoggerFactory): Behavior[EventMessage] =
    Behaviors.mutable(ctx ⇒ EventHandlerActor(ctx, commandResponseManager, loggerFactory))
}

case class EventHandlerActor(ctx: ActorContext[EventMessage],
                             commandResponseManager: CommandResponseManager,
                             loggerFactory: LoggerFactory)
    extends Behaviors.MutableBehavior[EventMessage] {

  import org.tmt.tcs.tcstemplateassembly.EventMessage._

  private val log = loggerFactory.getLogger

  override def onMessage(msg: EventMessage): Behavior[EventMessage] = {
    msg match {
      case (x: EventPublishMessage) => publishEvent(x)
      case _                        => log.error(s"unhandled message in Event Handler Actor onMessage: $msg")
    }
    this
  }

  private def publishEvent(message: EventPublishMessage): Unit = {

    log.info(s"publish event message: $message")

  }
}
