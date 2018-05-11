package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import com.typesafe.config.Config
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.config.api.models.ConfigData
import csw.services.logging.scaladsl.LoggerFactory

// Add messages here
sealed trait LifecycleMessage

object LifecycleMessage {

  case class InitializeMessage() extends LifecycleMessage
  case class ShutdownMessage()   extends LifecycleMessage

}

object LifecycleActor {
  def behavior(commandResponseManager: CommandResponseManager,
               assemblyConfig: Config,
               loggerFactory: LoggerFactory): Behavior[LifecycleMessage] =
    Behaviors.mutable(ctx ⇒ LifecycleActor(ctx, commandResponseManager, assemblyConfig: Config, loggerFactory))
}

case class LifecycleActor(ctx: ActorContext[LifecycleMessage],
                          commandResponseManager: CommandResponseManager,
                          assemblyConfig: Config,
                          loggerFactory: LoggerFactory)
    extends Behaviors.MutableBehavior[LifecycleMessage] {

  import org.tmt.tcs.tcstemplateassembly.LifecycleMessage._

  private val log = loggerFactory.getLogger

  override def onMessage(msg: LifecycleMessage): Behavior[LifecycleMessage] = {
    msg match {
      case (x: InitializeMessage) => doInitialize(x)
      case (y: ShutdownMessage)   => doShutdown(y)
      case _                      => log.error(s"unhandled message in Lifecycle Actor onMessage: $msg")
    }
    this
  }

  private def doInitialize(message: InitializeMessage): Unit = {

    // example of working with Config
    val bazValue: Int = assemblyConfig.getInt("foo.bar.baz")
    log.debug(s"foo.bar.baz config element value is: $bazValue")

    log.info("initialize command completed")

  }

  private def doShutdown(message: ShutdownMessage): Unit = {

    log.info("shutdown command completed")

  }

}
