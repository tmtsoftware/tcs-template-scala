package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import csw.services.command.scaladsl.{CommandResponseManager, CommandService}
import csw.services.logging.scaladsl.LoggerFactory
import csw.messages.commands.ControlCommand

// Add messages here
sealed trait CommandMessage

object CommandMessage {

  case class SubmitCommandMessage(controlCommand: ControlCommand)           extends CommandMessage
  case class GoOnlineMessage()                                              extends CommandMessage
  case class GoOfflineMessage()                                             extends CommandMessage
  case class UpdateTemplateHcdLocation(templateHcd: Option[CommandService]) extends CommandMessage

}

object CommandHandlerActor {
  def behavior(commandResponseManager: CommandResponseManager,
               online: Boolean,
               templateHcd: Option[CommandService],
               loggerFactory: LoggerFactory): Behavior[CommandMessage] =
    Behaviors.mutable(
      ctx ⇒ CommandHandlerActor(ctx, commandResponseManager, online: Boolean, templateHcd: Option[CommandService], loggerFactory)
    )
}

case class CommandHandlerActor(ctx: ActorContext[CommandMessage],
                               commandResponseManager: CommandResponseManager,
                               online: Boolean,
                               templateHcd: Option[CommandService],
                               loggerFactory: LoggerFactory)
    extends Behaviors.MutableBehavior[CommandMessage] {

  import org.tmt.tcs.tcstemplateassembly.CommandMessage._
  import org.tmt.tcs.tcstemplateassembly.CommandHandlerActor._

  private val log = loggerFactory.getLogger

  override def onMessage(msg: CommandMessage): Behavior[CommandMessage] = {
    msg match {
      case (x: SubmitCommandMessage) =>
        handleSubmitCommand(x)
        // maintain actor state
        this

      case (x: GoOfflineMessage) =>
        // change state of the actor to "online = false"
        behavior(commandResponseManager, false, templateHcd, loggerFactory)

      case (x: GoOnlineMessage) =>
        // change state of the actor to "online = true"
        behavior(commandResponseManager, true, templateHcd, loggerFactory)

      case (x: UpdateTemplateHcdLocation) =>
        // change state of the actor to "online = true"
        behavior(commandResponseManager, true, x.templateHcd, loggerFactory)

      case _ =>
        log.error(s"unhandled message in Monitor Actor onMessage: $msg")
        // maintain actor state
        this

    }

  }

  private def handleSubmitCommand(message: SubmitCommandMessage): Unit = {

    if (online) {
      // TODO: handle messages here

      message.controlCommand.commandName.name match {

        case "setTargetWavelength" =>
          log.debug(s"handling setTargetWavelength command: ${message.controlCommand}")
          val setTargetWavelengthCmdActor: ActorRef[ControlCommand] =
            ctx.spawnAnonymous(SetTargetWavelengthCmdActor.behavior(commandResponseManager, loggerFactory))

          setTargetWavelengthCmdActor ! message.controlCommand

        // TODO: when the command is complete, kill the actor
        // ctx.stop(setTargetWavelengthCmdActor)

        case "datum" =>
          log.debug(s"handling datum command: ${message.controlCommand}")
          val datumCmdActor: ActorRef[ControlCommand] =
            ctx.spawnAnonymous(DatumCmdActor.behavior(commandResponseManager, loggerFactory))

          datumCmdActor ! message.controlCommand

        case "move" =>
          log.debug(s"handling move command: ${message.controlCommand}")
          val moveCmdActor: ActorRef[ControlCommand] =
            ctx.spawnAnonymous(MoveCmdActor.behavior(commandResponseManager, templateHcd, loggerFactory))

          moveCmdActor ! message.controlCommand

        case _ =>
          log.error(s"unhandled message in Monitor Actor onMessage: ${message.controlCommand}")
        // maintain actor state

      }

    }

    log.info("command message handled")

  }

}
