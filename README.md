# tcs-template

This project implements a TCS template Assembly using TMT Common Software 
([CSW](https://github.com/tmtsoftware/csw-prod)) APIs. 

## Subprojects

* tcs-template-assembly - a template assembly that implements several command types, monitors state, and loads configuration
* tcs-template-hcd - an HCD (to be implemented)
* tcs-template-deploy - for starting/deploying the Assembly and HCD
* tcs-template-client - a client app that sends commands to the Assembly

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
 

# TCS CSW Template

## 1 Requirements

* Write a Scala and Java template for TCS use

* Call the prototype TcsTemplate

* Project in GitHub should be called tcs-template-scala

* Add HCD as second step

* Add Java template as additional step

### Template Features:

1. Create typed actors for each of the internal components in the architecture doc: Lifecycle Actor, MonitorActor, Command Handler Actor, Event Handler Actor
2. Supports submit messages:

a. immediate completion
b. query/subscribe for long running commands
c. command Aggregator
3. Try to use commands and parameters that make sense in the real world
4. Load and use configuration with the configuration service
5. Logging usage
6. State reporting

Template should have detailed comments and design documentation that the developers can follow.

Deployment will be from jars in the local ivy repo.

Include TcsTemplateAssemblyClientApp that can command the assembly

Also need to use the REPL to command components.

## 2 Tasks

1. Set up a client app to exercise the assembly
2. Need a better definition of the requirements for each subcomponent.
3. Implement the commands
4. Monitor actor can manage state using the behavior mechanism.

## 3 Questions

Do we want our component actors to live in the assembly package?  Do we want them to have simple names and be qualified by package or have fully qualified names?  Do we expect any reuse?

What is the best practice for creating/destroying actors whose lifecycle is aligned with the lifecycle of the enclosing assembly?  In the GalilHCD, the GalilIOActor is spawned as part of the GalilHCD class constructor.  What happens when the GalilHCD is destroyed (and/or recreated by the supervisor)?

How and when should we stop command actors?

## 4 Documentation
### 4.1 Understanding the Template Code

The template contains code that shows how to

1. Create typed actors for each of the internal components in the architecture doc: Lifecycle Actor, MonitorActor, Command Handler Actor, Event Handler Actor
2. Supports submit messages:
  1. immediate completion
  2. query/subscribe for long running commands
  3. how to use Command Aggregator
3. Load and use configuration with the configuration service
4. Logging usage
5. State reporting

#### 4.1.1  Creating Typed Actors

The template code creates Typed Actors for the following assembly subcomponents:

TBD - &lt;write up which components are being created&gt;

##### 4.1.1.1  Lifecycle Actor

The lifecycle actor contains all lifecycle related functions: functions that are performed at startup and shutdown.  Loading configuration and connecting to HCDs and other Assemblies as needed.

##### 4.1.1.2  Monitor Actor

Health monitoring for the assembly.  Tracks dependency location changes and monitors health and state of the assembly.

##### 4.1.1.3  Command Handler Actor

Directs submit commands to appropriate workers.  Handles onGoOnline and onGoOffline actions (for now, going offline means ignoring incoming commands)

##### 4.1.1.4 SetTargetWavelengthCmdActor

This command demonstrates how immediate response commands are implemented.  This example command emulates the TPK Offset command.

Setup(Prefix(&quot;tcs.tcs-template&quot;), CommandName(&quot;setTargetWavelength&quot;), None).add(targetType).add(wavelength)

Parameter Types:

wavelength: double

targetType:  enum(SCIENCE|GUIDESTAR)

##### 4.1.1.5  DatumCmdActor

This command demonstrates how long running commands are implemented.  This example emulates the ENC Datum command.

Setup(Prefix(&quot;tcs.tcs-template&quot;), CommandName(&quot;datum&quot;), None).add(axes)

Parameter Types:

 Axes: enum(Aximuth|Elevation|BOTH)

##### 4.1.1.6  MoveCmdActor

This command demonstrates how command aggregation can be implemented.  This example emulates the MCS Move command.  The command aggregates HCD commands: Point(axes) and PointDemand(Az, El).

Setup(Prefix(&quot;tcs.tcs-template&quot;), CommandName(&quot;move&quot;), None).add(axes).add(az).add(el)

Parameter Types:

 axes: enum(Aximuth|Elevation|BOTH)

 az: double

 el: double

##### 4.1.1.7  FollowCmdActor

TBD

##### 4.1.1.8  Event Handler Actor

This cannot be implemented fully until CSW Event Service becomes available.  For now, events to be published are written to a log file.

### 4.2 Support for immediate completion Submit Commands

TBD - &lt;write up the command, parameters and where it is in the code&gt;

### 4.3 Support for long running Submit Commands

TBD - &lt;write up the command, parameters and where it is in the code&gt;

#### 4.3.1  Support for Splitting commands into subcommands and aggregating command response

TBD

#### 4.3.2  Using the Configuration Service

TBD

#### 4.3.3  Support for State Reporting

TBD

### 4.4 Downloading the template

Clone or download tmtsoftware/tcs-template-scala to a directory of choice

### 4.5 Building the template

cd tcs-template

sbt stage publishLocal

### 4.6 Deploying/Running the Template Assembly

#### 4.6.1  Set up appropriate environment variables

Add the following lines to ~/.bashrc (on linux, or startup file appropriate to your linux shell):

export interfaceName=&lt;machine interface name&gt;   (The interface name of your machine can be obtained by running: ifconfig -a | sed &#39;s/[\t].\*//;/^$/d&#39;)

export clusterSeeds=&lt;machine IP&gt;:7777

#### 4.6.2  Install csw-prod

Clone or download tmtsoftware/csw-prod project to a directory of choice

cd csw-prod

sbt stage publishLocal

#### 4.6.3  Start the csw-prod Location Service:

cd csw-prod/target/universal/stage/bin

./csw-cluster-seed --clusterPort 7777

#### 4.6.4 Start the csw-prod Configuration Service:

cd csw-prod/target/universal/stage/bin

./csw-config-server --initRepo

### 4.7 Start the tcs-template Assembly

cd tcs-assembly/target/universal/stage/bin

./tcstemplate-container-cmd-app --local ../../../../src/main/resources/TcstemplateContainer.conf

### 4.8 Build/Run the Client App

#### 4.8.1  Configuring/Building

The client app is not part of the CSW template.  It was added with the following steps:

1. Add the tcs-template-client project directory to the tcs-template project
2. Add tcs-template-client to build.sbt and project/Dependencies.scala
3. Add the App object code to tcs-template-deploy/src/main/scala/org.tmt.tcs.tcstemplatedeploy as TcsTemplateClientApp.scala.  This is the same location as the container starting apps.
4. Sbt build stage on the project will create the necessary scripts with jar dependencies to target/universal/stage/bin

#### 4.8.2  Running

TBD

### 4.9  Run using the REPL

TBD

