package org.tmt.tcs.tcstemplatedeploy

import csw.framework.deploy.containercmd.ContainerCmd

object TcstemplateContainerCmdApp extends App {

  ContainerCmd.start("tcs-template-container-cmd-app", args)

}
