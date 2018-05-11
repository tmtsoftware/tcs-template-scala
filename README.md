# TCS CSW Template

This project implements a TCS template Assembly using TMT Common Software 
([CSW](https://github.com/tmtsoftware/csw-prod)) APIs. 

## Subprojects

* tcs-template-assembly - a template assembly that implements several command types, monitors state, and loads configuration
* tcs-template-hcd - an HCD that the assembly communicates with
* tcs-template-deploy - for starting/deploying the Assembly and HCD
* tcs-template-client - a client app that sends commands to the Assembly

## Requirements

* Write a Scala and Java template for TCS use
* Template should have detailed comments and design documentation that the developers can follow.
* Add Java template as additional step

Fully/Partially Completed
1. Create typed actors for each of the internal components in the architecture doc: Lifecycle Actor, MonitorActor, Command Handler Actor, Event Handler Actor
2. Support submit messages:
a. immediate completion
b. query/subscribe for long running commands
c. command Aggregator
3. Use commands and parameters that make sense in the real world
4. Load and use configuration with the configuration service
5. Build a client app to exercise the assembly commands
6. Logging usage
7. State management

Not Started
1. Deployment should be from jars in the local ivy repo.
2. Also need to use the REPL to command components.
3. Deployment of multiple Assemblies

##  Documentation
### Understanding the Template Code

The template contains code that shows how to

1. Create typed actors for each of the internal components in the architecture doc: Lifecycle Actor, MonitorActor, Command Handler Actor, Event Handler Actor
2. Supports submit messages:
  1. immediate completion
  2. query/subscribe for long running commands
  3. how to use Command Aggregator
3. Load and use configuration with the configuration service
4. Logging usage
5. State reporting

#### Creating Typed Actors

The template code creates Typed Actors for the following assembly subcomponents:

TBD - &lt;write up which components are being created&gt;

##### Lifecycle Actor

The lifecycle actor contains all lifecycle related functions: functions that are performed at startup and shutdown.  Loading configuration and connecting to HCDs and other Assemblies as needed.

##### Monitor Actor

Health monitoring for the assembly.  Tracks dependency location changes and monitors health and state of the assembly.

##### Command Handler Actor

Directs submit commands to appropriate workers.  Handles onGoOnline and onGoOffline actions (for now, going offline means ignoring incoming commands)

##### SetTargetWavelengthCmdActor

This command demonstrates how immediate response commands are implemented.  This example command emulates the TPK Offset command.

Setup(Prefix(&quot;tcs.tcs-template&quot;), CommandName(&quot;setTargetWavelength&quot;), None).add(targetType).add(wavelength)

Parameter Types:

wavelength: double

targetType:  enum(SCIENCE|GUIDESTAR)

##### DatumCmdActor

This command demonstrates how long running commands are implemented.  This example emulates the ENC Datum command.

Setup(Prefix(&quot;tcs.tcs-template&quot;), CommandName(&quot;datum&quot;), None).add(axes)

Parameter Types:

 Axes: enum(Aximuth|Elevation|BOTH)

##### MoveCmdActor

This command demonstrates how command aggregation can be implemented.  This example emulates the MCS Move command.  The command aggregates HCD commands: Point(axes) and PointDemand(Az, El).

Setup(Prefix(&quot;tcs.tcs-template&quot;), CommandName(&quot;move&quot;), None).add(axes).add(az).add(el)

Parameter Types:

 axes: enum(Aximuth|Elevation|BOTH)

 az: double

 el: double

##### FollowCmdActor

TBD

##### Event Handler Actor

This cannot be implemented fully until CSW Event Service becomes available.  For now, events to be published are written to a log file.

### Support for immediate completion Submit Commands

TBD - &lt;write up the command, parameters and where it is in the code&gt;

### Support for long running Submit Commands

TBD - &lt;write up the command, parameters and where it is in the code&gt;

#### Support for Splitting commands into subcommands and aggregating command response

TBD

#### Using the Configuration Service

TBD

#### Support for State Reporting

TBD

## Build and Running the Template

### Downloading the template

Clone or download tmtsoftware/tcs-template-scala to a directory of choice

### Building the template

cd tcs-template

sbt stage publishLocal

### Deploying/Running the Template Assembly

#### Set up appropriate environment variables

Add the following lines to ~/.bashrc (on linux, or startup file appropriate to your linux shell):

export interfaceName=&lt;machine interface name&gt;   (The interface name of your machine can be obtained by running: ifconfig -a | sed &#39;s/[\t].\*//;/^$/d&#39;)

export clusterSeeds=&lt;machine IP&gt;:7777

#### Install csw-prod

Clone or download tmtsoftware/csw-prod project to a directory of choice

cd csw-prod

sbt stage publishLocal

#### Start the csw-prod Location Service:

cd csw-prod/target/universal/stage/bin

./csw-cluster-seed --clusterPort 7777

#### Start the csw-prod Configuration Service:

cd csw-prod/target/universal/stage/bin

./csw-config-server --initRepo

### Populate the assembly configuration

cd tcs-template-deploy/src/main/resources

./initialize-config.sh <ip address>

### Start the tcs-template Assembly

cd tcs-assembly/target/universal/stage/bin

./tcstemplate-container-cmd-app --local ../../../../src/main/resources/TcstemplateContainer.conf

### Build/Run the Client App

#### Configuring/Building

The client app is not part of the CSW template.  It was added with the following steps:

1. Add the tcs-template-client project directory to the tcs-template project
2. Add tcs-template-client to build.sbt and project/Dependencies.scala
3. Add the App object code to tcs-template-deploy/src/main/scala/org.tmt.tcs.tcstemplatedeploy as TcsTemplateClientApp.scala.  This is the same location as the container starting apps.
4. Sbt build stage on the project will create the necessary scripts with jar dependencies to target/universal/stage/bin

#### Running

TBD

### Run using the REPL

TBD







## Build Instructions

The build is based on sbt and depends on libraries published to bintray from the 
[csw-prod](https://github.com/tmtsoftware/csw-prod) project.

See [here](https://www.scala-sbt.org/1.0/docs/Setup.html) for instructions on installing sbt.

## Pre-requisites before running Components

Make sure that the necessary environment variables are set. For example:

* Set the environment variables (Replace interface name, IP address and port with your own values):
```bash
export interfaceName=enp0s31f6
export clusterSeeds=192.168.178.77:7777
```
for bash shell, or 
```csh
setenv interfaceName enp0s31f6
setenv clusterSeeds 192.168.178.77:7777
```

for csh or tcsh. The list of available network interfaces can be found using the _ifconfig -a_ command.
If you don't specify the network interface this way, a default will be chosen, which might sometimes not be
the one you expect. 

Before running any components, follow below steps:
 - Download csw-apps zip from https://github.com/tmtsoftware/csw-prod/releases.
 - Unzip the downloaded zip
 - Go to the bin directory where you will find `csw-services.sh` script.
 - Run `./csw_services.sh --help` to see more information
 - Run `./csw_services.sh start` to start location service and config server

## Running HCD and Assembly

 - Run `sbt tcs-template-deploy/universal:packageBin`, this will create self contained zip in target/universal directory
 - Unzip generate zip and enter into bin directory
 - Run container cmd script or host config app script
 