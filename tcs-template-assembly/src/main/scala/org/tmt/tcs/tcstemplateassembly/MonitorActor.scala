package org.tmt.tcs.tcstemplateassembly

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.logging.scaladsl.LoggerFactory

// Add messages here
sealed trait MonitorMessage

object MonitorMessage {

  case class LocationEventMessage() extends MonitorMessage

  case class AssemblyStateChangeMessage(assemblyState: AssemblyState.AssemblyState)                         extends MonitorMessage
  case class AssemblyMotionStateChangeMessage(assemblyMotionState: AssemblyMotionState.AssemblyMotionState) extends MonitorMessage

}

object AssemblyState extends Enumeration {
  type AssemblyState = Value
  val Ready, Degraded, Disconnected, Faulted = Value
}

object AssemblyMotionState extends Enumeration {
  type AssemblyMotionState = Value
  val Idle, Slewing, Tracking, InPosition, Halted = Value
}

/**
 * MonitorActor maintains the state of the assembly.  It can do this by accepting messages from other assembly
 * components to change the state or through monitoring location event messages for components this assembly
 * depends on.
 */
object MonitorActor {
  def behavior(assemblyState: AssemblyState.AssemblyState,
               assemblyMotionState: AssemblyMotionState.AssemblyMotionState,
               loggerFactory: LoggerFactory): Behavior[MonitorMessage] =
    Behaviors.mutable(ctx ⇒ MonitorActor(ctx, assemblyState, assemblyMotionState, loggerFactory))
}

case class MonitorActor(ctx: ActorContext[MonitorMessage],
                        assemblyState: AssemblyState.AssemblyState,
                        assemblyMotionState: AssemblyMotionState.AssemblyMotionState,
                        loggerFactory: LoggerFactory)
    extends Behaviors.MutableBehavior[MonitorMessage] {

  import org.tmt.tcs.tcstemplateassembly.MonitorMessage._

  private val log = loggerFactory.getLogger

  override def onMessage(msg: MonitorMessage): Behavior[MonitorMessage] = {
    msg match {
      case (x: LocationEventMessage)             => onLocationEventMessage(x)
      case (x: AssemblyStateChangeMessage)       => onAssemblyStateChangeMessage(x)
      case (x: AssemblyMotionStateChangeMessage) => onAssemblyMotionStateChangeMessage(x)
      case _                                     => log.error(s"unhandled message in Monitor Actor onMessage: $msg")
    }
    this
  }

  private def onLocationEventMessage(message: LocationEventMessage): Unit = {

    log.info("location event message handled")

  }

  private def onAssemblyStateChangeMessage(message: AssemblyStateChangeMessage): Unit = {

    log.info("assembly state change event message handled")

    MonitorActor.behavior(message.assemblyState, assemblyMotionState, loggerFactory)

  }

  private def onAssemblyMotionStateChangeMessage(message: AssemblyMotionStateChangeMessage): Unit = {

    log.info("assembly motion state change event message handled")

    MonitorActor.behavior(assemblyState, message.assemblyMotionState, loggerFactory)

  }

}
