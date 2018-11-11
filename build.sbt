name := "periodicals"
version := "1.0"
scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
	"com.beachape" %% "enumeratum" % "1.5.13",
	"javax.servlet" % "javax.servlet-api" % "3.1.0" % Provided,
	"javax.servlet" % "jstl" % "1.2" % Provided,
	"javax.servlet.jsp" % "javax.servlet.jsp-api" % "2.3.1" % Provided,
	"org.slf4j" % "slf4j-api" % "1.7.21",
	"org.apache.logging.log4j" % "log4j-api" % "2.6.2",
	"org.apache.logging.log4j" % "log4j-core" % "2.6.2",
	"org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.6.2",
	"com.google.guava" % "guava" % "17.0",
	"org.apache.commons" % "commons-dbcp2" % "2.1.1",
	"org.apache.commons" % "commons-lang3" % "3.4",
	"org.json" % "json" % "20160807",
	"mysql" % "mysql-connector-java" % "8.0.11",
	"com.h2database" % "h2" % "1.4.192",
	"org.scalatest" %% "scalatest" % "3.0.5" % Test,
	"org.mockito" %% "mockito-scala" % "1.0.0-beta.6" % Test,
)

enablePlugins(JettyPlugin)