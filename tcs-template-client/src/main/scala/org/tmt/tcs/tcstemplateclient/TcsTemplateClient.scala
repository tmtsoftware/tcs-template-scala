package org.tmt.tcs.tcstemplateclient

import akka.actor.{ActorRefFactory, ActorSystem, Scheduler}
import akka.stream.ActorMaterializer
import akka.actor.typed
import akka.util.Timeout
import csw.messages.location.ComponentType.Assembly
import csw.messages.location.Connection.AkkaConnection
import csw.messages.location._
import csw.messages.params.generics.{Key, KeyType}
import csw.messages.params.models.{Id, ObsId, Prefix}
import csw.services.location.scaladsl.LocationService

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.typed.scaladsl.adapter._
import csw.messages.commands.CommandResponse.Error
import csw.messages.commands.{CommandName, CommandResponse, Setup}
import csw.services.command.scaladsl.CommandService

/**
 * A client for locating and communicating with the TcsTemplate Assembly
 *
 * @param source the client's prefix
 * @param system ActorSystem (must be created by ClusterAwareSettings.system - should be one per application)
 * @param locationService a reference to the location service created with LocationServiceFactory.withSystem(system)
 */
case class TcsTemplateClient(source: Prefix, system: ActorSystem, locationService: LocationService) {

  import system._

  implicit val timeout: Timeout                 = Timeout(30.seconds)
  implicit val scheduler: Scheduler             = system.scheduler
  implicit def actorRefFactory: ActorRefFactory = system
  implicit val mat: ActorMaterializer           = ActorMaterializer()

  private val connection = AkkaConnection(ComponentId("TcstemplateAssembly", Assembly))

  private val targetTypeKey: Key[String] = KeyType.StringKey.make("targetType")
  private val wavelengthKey: Key[Double] = KeyType.DoubleKey.make("wavelength")

  /**
   * Gets a reference to the running assembly from the location service, if found.
   */
  private def getAssembly: Future[Option[CommandService]] = {
    implicit val sys: typed.ActorSystem[Nothing] = system.toTyped
    locationService.resolve(connection, 30.seconds).map(_.map(new CommandService(_)))
  }

  /**
   * Sends a setTargetWavelength message to the Assembly and returns the response
   */
  def setTargetWavelength(obsId: Option[ObsId], targetType: String, wavelength: Double): Future[CommandResponse] = {
    getAssembly.flatMap {
      case Some(commandService) =>
        val setup = Setup(source, CommandName("setTargetWavelength"), obsId)
          .add(targetTypeKey.set(targetType))
          .add(wavelengthKey.set(wavelength))

        // FIXME: see below
        // the CSW designed way to perform immediate response commands is to do all the work in the validation
        // method, and return the command response there.  This does not work nicely with having actors
        // reponsible for commands.  For this reason, the immediate response command is implemented exactly
        // like long running commands, but the submitAndSubscribe returns faster.
        commandService.submitAndSubscribe(setup)

      case None =>
        Future.successful(Error(Id(), "Can't locate TcsTemplateAssembly"))
    }
  }

  /**
   * Sends a setTargetWavelength message to the Assembly and returns the response
   */
  def datum(obsId: Option[ObsId]): Future[CommandResponse] = {
    getAssembly.flatMap {
      case Some(commandService) =>
        val setup = Setup(source, CommandName("datum"), obsId)

        commandService.submitAndSubscribe(setup)

      case None =>
        Future.successful(Error(Id(), "Can't locate TcsTemplateAssembly"))
    }
  }

}
