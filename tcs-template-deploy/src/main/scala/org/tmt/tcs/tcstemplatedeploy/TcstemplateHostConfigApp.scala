package org.tmt.tcs.tcstemplatedeploy

import csw.framework.deploy.hostconfig.HostConfig

object TcstemplateHostConfigApp extends App {

  HostConfig.start("tcs-template-host-config-app", args)

}
