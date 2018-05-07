package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.scaladsl.ActorContext
import csw.framework.scaladsl.{ComponentHandlers, CurrentStatePublisher}
import csw.messages.commands.{CommandResponse, ControlCommand}
import csw.messages.framework.ComponentInfo
import csw.messages.location.TrackingEvent
import csw.messages.scaladsl.TopLevelActorMessage
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.util.Timeout
import csw.messages.commands.CommandIssue.UnsupportedCommandIssue
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.location.scaladsl.LocationService
import csw.services.logging.scaladsl.LoggerFactory

import scala.async.Async.async
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

/**
 * Domain specific logic should be written in below handlers.
 * This handlers gets invoked when component receives messages/commands from other component/entity.
 * For example, if one component sends Submit(Setup(args)) command to TcstemplateHcd,
 * This will be first validated in the supervisor and then forwarded to Component TLA which first invokes validateCommand hook
 * and if validation is successful, then onSubmit hook gets invoked.
 * You can find more information on this here : https://tmtsoftware.github.io/csw-prod/framework.html
 */
class TcstemplateAssemblyHandlers(
    ctx: ActorContext[TopLevelActorMessage],
    componentInfo: ComponentInfo,
    commandResponseManager: CommandResponseManager,
    currentStatePublisher: CurrentStatePublisher,
    locationService: LocationService,
    loggerFactory: LoggerFactory
) extends ComponentHandlers(ctx, componentInfo, commandResponseManager, currentStatePublisher, locationService, loggerFactory) {

  implicit val ec: ExecutionContextExecutor = ctx.executionContext
  private val log                           = loggerFactory.getLogger

  import org.tmt.tcs.tcstemplateassembly.LifecycleMessage._
  import org.tmt.tcs.tcstemplateassembly.CommandMessage._

  // create the assembly's components
  val lifecycleActor: ActorRef[LifecycleMessage] =
    ctx.spawnAnonymous(LifecycleActor.behavior(commandResponseManager, loggerFactory))

  val monitorActor: ActorRef[MonitorMessage] =
    ctx.spawnAnonymous(MonitorActor.behavior(AssemblyState.Ready, AssemblyMotionState.Idle, loggerFactory))

  val eventHandlerActor: ActorRef[EventMessage] =
    ctx.spawnAnonymous(EventHandlerActor.behavior(commandResponseManager, loggerFactory))

  val commandHandlerActor: ActorRef[CommandMessage] =
    ctx.spawnAnonymous(CommandHandlerActor.behavior(commandResponseManager, true, loggerFactory))

  override def initialize(): Future[Unit] = async {
    log.debug("initialize called")

    // tell the lifecycle actor to perform its initialize functions
    // lifecycle actor will accept messages of type LifecycleMessage: InitializeMessage and ShutdownMessage

    lifecycleActor ! InitializeMessage()

  }

  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit = {
    // pass tracking event to the MonitorActor
  }

  override def validateCommand(controlCommand: ControlCommand): CommandResponse = {

    implicit val timeout: Timeout = Timeout(3.seconds)

    controlCommand.commandName.name match {

      case "setTargetWavelength" =>
        log.debug(s"handling setTargetWavelength validation: ${controlCommand}")
        // TODO: validate command

        // as an immediate reponse command, the 'Accepted' response is not given
        // FIXME: this is pretty clunky.  Having to return a command result in the validate method breaks
        // the model of having an actor for each command, since the validate command must be the code block
        // that returns the result.  Being forced to execute the command in the validation method seems wrong anyway.
        //commandHandlerActor ! SubmitCommandMessage(controlCommand)

        // query the command response manager to find the reponse
        //val eventualResponse: Future[CommandResponse] = commandResponseManager.query(controlCommand.runId)

        //val response = Await.result(eventualResponse, 3.seconds)

        //log.debug(s"response = ${response}")

        //response

        CommandResponse.Accepted(controlCommand.runId)

      case "datum" =>
        log.debug(s"handling datum validation: ${controlCommand}")
        // TODO: validate command

        // as an long running command, the 'Accepted' response is given
        CommandResponse.Accepted(controlCommand.runId)

      case "move" =>
        log.debug(s"handling move validation: ${controlCommand}")
        // TODO: validate command

        // as an long running command, the 'Accepted' response is given
        CommandResponse.Accepted(controlCommand.runId)

      case _ =>
        log.error(s"unhandled message in Monitor Actor onMessage: ${controlCommand}")
        CommandResponse.Invalid(controlCommand.runId,
                                UnsupportedCommandIssue(s"Command name: ${controlCommand.commandName.name} not supported"))

    }

  }

  override def onSubmit(controlCommand: ControlCommand): Unit = {
    // pass command to CommandHandler
    log.debug("onSubmit called")

    commandHandlerActor ! SubmitCommandMessage(controlCommand)
  }

  override def onOneway(controlCommand: ControlCommand): Unit = {
    // pass command to CommandHandler
  }

  override def onShutdown(): Future[Unit] = async {
    log.debug("onShutdown called")

    // tell the lifecycle actor to perform its shutdown functions
    lifecycleActor ! ShutdownMessage()

  }

  override def onGoOffline(): Unit = {
    // send GoOffline Message to CommandHandler
    commandHandlerActor ! GoOfflineMessage()
  }

  override def onGoOnline(): Unit = {
    // send GoOnline Message to CommandHandler
    commandHandlerActor ! GoOnlineMessage()
  }

}
