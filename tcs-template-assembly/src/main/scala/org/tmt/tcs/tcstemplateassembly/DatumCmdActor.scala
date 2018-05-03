package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import csw.messages.commands.ControlCommand
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.logging.scaladsl.LoggerFactory

// Add messages here
// No sealed trait or messages for this actor.  Always accepts the Submit command message.

object DatumCmdActor {
  def behavior(commandResponseManager: CommandResponseManager, loggerFactory: LoggerFactory): Behavior[ControlCommand] =
    Behaviors.mutable(ctx ⇒ DatumCmdActor(ctx, commandResponseManager, loggerFactory))
}

case class DatumCmdActor(ctx: ActorContext[ControlCommand],
                         commandResponseManager: CommandResponseManager,
                         loggerFactory: LoggerFactory)
    extends Behaviors.MutableBehavior[ControlCommand] {

  private val log = loggerFactory.getLogger

  override def onMessage(msg: ControlCommand): Behavior[ControlCommand] = {

    handleSubmitCommand(msg)

    this
  }

  private def handleSubmitCommand(message: ControlCommand): Unit = {

    // TODO: implement long running message

    log.info("command message handled")

  }

}
