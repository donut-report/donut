package report.donut

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import report.donut.log.Log
import scopt.OptionParser

object Boot extends App with Log {

  case class DonutArguments(resultSources: String = "",
                            outputPath: String = "donut",
                            prefix: String = "",
                            datetime: String = DateTimeFormat.forPattern("yyyy-MM-dd-HHmm").print(DateTime.now),
                            template: String = "default",
                            countSkippedAsFailure: Boolean = false,
                            countPendingAsFailure: Boolean = false,
                            countUndefinedAsFailure: Boolean = false,
                            countMissingAsFailure: Boolean = false,
                            // DynamicBinding behavior of com.gilt handlebars does not match JavaScript truth evaluation for empty string.
                            projectName: String = null,
                            projectVersion: String = null,
                            customAttributes: Map[String, String] = Map())

  val optionParser = new OptionParser[DonutArguments]("Donut reports") {

    head("\nDonut help")
    head("----------")

    opt[String]('s', "resultSources").required() action { (arg, config) =>
      config.copy(resultSources = arg)
    } text "Required -> Use --resultSources cucumber:/my/path/cucumber-reports,cucumber:/my/adapted/nunit-reports"

    opt[String]('n', "projectName").required() action { (arg, config) =>
      config.copy(projectName = arg)
    } text "Required -> Use --projectName myProject"

    opt[String]('o', "outputPath") action { (arg, config) =>
      config.copy(outputPath = arg)
    } text "Use --outputPath /my/path/output/donut"

    opt[String]('p', "prefix") action { (arg, config) =>
      config.copy(prefix = arg)
    } text "Use --prefix fileNamePrefix"

    opt[String]('d', "datetime") action { (arg, config) =>
      config.copy(datetime = arg)
    } text "Use --datetime yyyy-MM-dd-HHmm"

    opt[String]('t', "template") action { (arg, config) =>
      config.copy(template = arg)
    } text "Use --template default/light"

    opt[String]('v', "projectVersion") action { (arg, config) =>
      config.copy(projectVersion = arg)
    } text "Use --projectVersion 1.0"

    opt[Map[String, String]]('c', "customAttributes") action { (arg, config) =>
      config.copy(customAttributes = arg)
    } text "Use --customAttributes k1=v1,k2=v2..."

    opt[Boolean]("skippedFails") action { (arg, config) =>
      config.copy(countSkippedAsFailure = arg)
    } text "Use --skippedFails true/false"

    opt[Boolean]("pendingFails") action { (arg, config) =>
      config.copy(countPendingAsFailure = arg)
    } text "Use --pendingFails true/false"

    opt[Boolean]("undefinedFails") action { (arg, config) =>
      config.copy(countUndefinedAsFailure = arg)
    } text "Use --undefinedFails true/false"

    opt[Boolean]("missingFails") action { (arg, config) =>
      config.copy(countMissingAsFailure = arg)
    } text "Use --missingFails true/false"

    checkConfig { c =>
      if (c.resultSources == "") failure("Missing result sources.") else success
    }
  }

  optionParser parse(args, DonutArguments()) match {
    case Some(appargs) =>
      try {
        Generator(appargs.resultSources,
          appargs.outputPath,
          appargs.prefix,
          appargs.datetime,
          appargs.template,
          appargs.countSkippedAsFailure,
          appargs.countPendingAsFailure,
          appargs.countUndefinedAsFailure,
          appargs.countMissingAsFailure,
          appargs.projectName,
          appargs.projectVersion,
          collection.mutable.Map[String, String]() ++= appargs.customAttributes)
      } catch {
        case e: DonutException => {
          println(e.mgs)
          System.exit(-1)
        }
      }
    case None =>
      // error message will have first been displayed
      System.exit(-1)
  }
}