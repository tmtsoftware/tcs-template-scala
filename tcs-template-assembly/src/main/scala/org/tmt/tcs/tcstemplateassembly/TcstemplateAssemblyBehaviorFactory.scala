package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.scaladsl.ActorContext
import csw.framework.scaladsl.{ComponentBehaviorFactory, ComponentHandlers, CurrentStatePublisher}
import csw.messages.framework.ComponentInfo
import csw.messages.scaladsl.TopLevelActorMessage
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.location.scaladsl.LocationService
import csw.services.logging.scaladsl.LoggerFactory

class TcstemplateAssemblyBehaviorFactory extends ComponentBehaviorFactory {

  override def handlers(
      ctx: ActorContext[TopLevelActorMessage],
      componentInfo: ComponentInfo,
      commandResponseManager: CommandResponseManager,
      currentStatePublisher: CurrentStatePublisher,
      locationService: LocationService,
      loggerFactory: LoggerFactory
  ): ComponentHandlers =
    new TcstemplateAssemblyHandlers(ctx,
                                    componentInfo,
                                    commandResponseManager,
                                    currentStatePublisher,
                                    locationService,
                                    loggerFactory)

}
