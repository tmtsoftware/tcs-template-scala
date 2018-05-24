package org.tmt.tcs.tcstemplatehcd

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import csw.framework.scaladsl.{ComponentHandlers, CurrentStatePublisher}
import csw.messages.commands.{CommandResponse, ControlCommand}
import csw.messages.framework.ComponentInfo
import csw.messages.location.TrackingEvent
import csw.messages.scaladsl.TopLevelActorMessage
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.location.scaladsl.LocationService
import csw.services.logging.scaladsl.LoggerFactory
import org.tmt.tcs.tcstemplatehcd.StatePublisherMessage.StartMessage

import scala.async.Async.async
import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * Domain specific logic should be written in below handlers.
 * This handlers gets invoked when component receives messages/commands from other component/entity.
 * For example, if one component sends Submit(Setup(args)) command to TcstemplateHcd,
 * This will be first validated in the supervisor and then forwarded to Component TLA which first invokes validateCommand hook
 * and if validation is successful, then onSubmit hook gets invoked.
 * You can find more information on this here : https://tmtsoftware.github.io/csw-prod/framework.html
 */
class TcstemplateHcdHandlers(
    ctx: ActorContext[TopLevelActorMessage],
    componentInfo: ComponentInfo,
    commandResponseManager: CommandResponseManager,
    currentStatePublisher: CurrentStatePublisher,
    locationService: LocationService,
    loggerFactory: LoggerFactory
) extends ComponentHandlers(ctx, componentInfo, commandResponseManager, currentStatePublisher, locationService, loggerFactory) {

  implicit val ec: ExecutionContextExecutor = ctx.executionContext
  private val log                           = loggerFactory.getLogger

  // create the assembly's components
  val statePublisherActor: ActorRef[StatePublisherMessage] =
    ctx.spawnAnonymous(StatePublisherActor.behavior(currentStatePublisher, loggerFactory))

  statePublisherActor ! StartMessage()

  override def initialize(): Future[Unit] = async {
    log.debug("initialize called")
  }

  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit = {}

  override def validateCommand(controlCommand: ControlCommand): CommandResponse = {
    CommandResponse.Accepted(controlCommand.runId)
  }

  override def onSubmit(controlCommand: ControlCommand): Unit = {

    controlCommand.commandName.name match {

      case "point" =>
        log.debug(s"handling point command: ${controlCommand}")

        Thread.sleep(500)

        commandResponseManager.addOrUpdateCommand(controlCommand.runId, CommandResponse.Completed(controlCommand.runId))

      case "pointDemand" =>
        log.debug(s"handling pointDemand command: ${controlCommand}")

        Thread.sleep(1000)

        commandResponseManager.addOrUpdateCommand(controlCommand.runId, CommandResponse.Completed(controlCommand.runId))

      case _ =>
        log.error(s"unhandled message in Monitor Actor onMessage: ${controlCommand}")
      // maintain actor state

    }

  }

  override def onOneway(controlCommand: ControlCommand): Unit = {}

  override def onShutdown(): Future[Unit] = async {
    log.debug("initialize called")
  }

  override def onGoOffline(): Unit = {}

  override def onGoOnline(): Unit = {}
}
