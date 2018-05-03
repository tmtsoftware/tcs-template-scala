package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.scaladsl.ActorContext
import csw.framework.scaladsl.{ComponentHandlers, CurrentStatePublisher}
import csw.messages.commands.{CommandResponse, ControlCommand}
import csw.messages.framework.ComponentInfo
import csw.messages.location.TrackingEvent
import csw.messages.scaladsl.TopLevelActorMessage
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.location.scaladsl.LocationService
import csw.services.logging.scaladsl.LoggerFactory

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
    ctx.spawnAnonymous(MonitorActor.behavior(commandResponseManager, loggerFactory))

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
    CommandResponse.Accepted(controlCommand.runId)
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
