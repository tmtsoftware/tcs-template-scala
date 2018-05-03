lazy val `tcs-template-assembly` = project
  .settings(
    libraryDependencies ++= Dependencies.TcstemplateAssembly
  )

lazy val `tcs-template-hcd` = project
  .settings(
    libraryDependencies ++= Dependencies.TcstemplateHcd
  )

lazy val `tcs-template-client` = project
  .settings(
    libraryDependencies ++= Dependencies.TcstemplateClient
  )

lazy val `tcs-template-deploy` = project
  .dependsOn(
    `tcs-template-assembly`,
    `tcs-template-hcd`,
    `tcs-template-client`
  )
  .enablePlugins(JavaAppPackaging, CswBuildInfo)
  .settings(
    libraryDependencies ++= Dependencies.TcstemplateDeploy
  )
