package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import csw.messages.commands.{CommandResponse, ControlCommand}
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.logging.scaladsl.LoggerFactory

// Add messages here
// No sealed trait or messages for this actor.  Always accepts the Submit command message.

object SetTargetWavelengthCmdActor {
  def behavior(commandResponseManager: CommandResponseManager, loggerFactory: LoggerFactory): Behavior[ControlCommand] =
    Behaviors.mutable(ctx ⇒ SetTargetWavelengthCmdActor(ctx, commandResponseManager, loggerFactory))
}

case class SetTargetWavelengthCmdActor(ctx: ActorContext[ControlCommand],
                                       commandResponseManager: CommandResponseManager,
                                       loggerFactory: LoggerFactory)
    extends Behaviors.MutableBehavior[ControlCommand] {

  private val log = loggerFactory.getLogger

  override def onMessage(msg: ControlCommand): Behavior[ControlCommand] = {

    handleSubmitCommand(msg)

    this
  }

  private def handleSubmitCommand(message: ControlCommand): Unit = {

    // complete the command by sending a command response "Completed"

    commandResponseManager.addOrUpdateCommand(message.runId, CommandResponse.Completed(message.runId))

    log.info("command message handled")

  }

}
