package org.tmt.tcs.tcstemplatedeploy

import java.net.InetAddress

import akka.actor.{typed, ActorRefFactory, ActorSystem, Scheduler}
import csw.messages.params.models.Prefix
import org.tmt.tcs.tcstemplateclient.TcsTemplateClient
import csw.services.location.commons.ClusterAwareSettings
import csw.services.location.scaladsl.LocationServiceFactory
import csw.services.logging.scaladsl.LoggingSystemFactory

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
 * A demo client to test locating and communicating with the Galil HCD
 */
object TcsTemplateClientApp extends App {

  private val system: ActorSystem = ClusterAwareSettings.system
  private val locationService     = LocationServiceFactory.withSystem(system)
  private val tcsTemplateClient   = TcsTemplateClient(Prefix("tcs.tcs-template"), system, locationService)
  private val maybeObsId          = None
  private val host                = InetAddress.getLocalHost.getHostName
  LoggingSystemFactory.start("TcsTemplateClientApp", "0.1", host, system)

  val resp1 = Await.result(tcsTemplateClient.setTargetWavelength(maybeObsId, "GUIDESTAR", 867.4), 3.seconds)
  println(s"setTargetWavelength: $resp1")

}
