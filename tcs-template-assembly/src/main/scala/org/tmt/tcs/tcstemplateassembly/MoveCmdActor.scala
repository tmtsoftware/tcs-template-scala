package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.util.Timeout
import csw.messages.commands.CommandResponse.Error
import csw.services.command.scaladsl.{CommandResponseManager, CommandService}
import csw.services.logging.scaladsl.LoggerFactory
import csw.messages.commands.{CommandName, CommandResponse, ControlCommand, Setup}
import csw.messages.params.generics.{Key, KeyType, Parameter}
import csw.messages.params.models.{Id, ObsId, Prefix}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

// Add messages here
// No sealed trait or messages for this actor.  Always accepts the Submit command message.

object MoveCmdActor {
  def behavior(commandResponseManager: CommandResponseManager,
               templateHcd: Option[CommandService],
               loggerFactory: LoggerFactory): Behavior[ControlCommand] =
    Behaviors.mutable(ctx ⇒ MoveCmdActor(ctx, commandResponseManager, templateHcd: Option[CommandService], loggerFactory))
}

case class MoveCmdActor(ctx: ActorContext[ControlCommand],
                        commandResponseManager: CommandResponseManager,
                        templateHcd: Option[CommandService],
                        loggerFactory: LoggerFactory)
    extends Behaviors.MutableBehavior[ControlCommand] {

  private val log = loggerFactory.getLogger

  override def onMessage(msg: ControlCommand): Behavior[ControlCommand] = {

    handleSubmitCommand(msg)

    this
  }

  private def handleSubmitCommand(message: ControlCommand): Unit = {

    // create Point and PointDemand messages and send to HCD

    // NOTE: we use get instead of getOrElse because we assume the command has been validated
    val axesParam = message.paramSet.find(x => x.keyName == "axes").get
    val azParam   = message.paramSet.find(x => x.keyName == "az").get
    val elParam   = message.paramSet.find(x => x.keyName == "el").get

    val response = Await.result(move(message.maybeObsId, axesParam, azParam, elParam), 3.seconds)

    log.debug(s"response = $response")
    log.debug(s"runId = $message.runId")

    commandResponseManager.addSubCommand(message.runId, response.runId)

    commandResponseManager.updateSubCommand(response.runId, response)

    log.info("move command message handled")

  }

  private val templateHcdPrefix = Prefix("tcs.tcs-templatehcd")

  implicit val timeout: Timeout = Timeout(30.seconds)

  def move(obsId: Option[ObsId],
           axesParam: Parameter[_],
           azParam: Parameter[_],
           elParam: Parameter[_]): Future[CommandResponse] = {
    templateHcd match {
      case Some(commandService) =>
        val pointSetup = Setup(templateHcdPrefix, CommandName("point"), obsId)
          .add(axesParam)

        val pointDemandSetup = Setup(templateHcdPrefix, CommandName("pointDemand"), obsId)
          .add(azParam)
          .add(elParam)

        commandService.submitAllAndGetFinalResponse(Set(pointSetup, pointDemandSetup))

      case None =>
        Future.successful(Error(Id(), "Can't locate TcsTemplateHcd"))
    }
  }

}
