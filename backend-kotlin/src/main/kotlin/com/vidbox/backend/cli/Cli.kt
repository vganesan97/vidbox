package com.vidbox.backend.cli

import org.apache.commons.cli.*

fun MainCli(args: Array<String>) {
    // Create Options object and define the CLI options
    val options = Options()
    options.addOption("h", "help", false, "print this message")
    options.addOption("t", "time", true, "set the time")

    try {
        // Parse the command-line arguments
        val parser: CommandLineParser = DefaultParser()
        val cmd: CommandLine = parser.parse(options, args)

        // Print help information if help option is provided
        if (cmd.hasOption("h")) {
            val formatter = HelpFormatter()
            formatter.printHelp("MyCLI", options)
        }

        // Access the value of the "time" option
        if (cmd.hasOption("t")) {
            val timeValue = cmd.getOptionValue("t")
            println("Time set to $timeValue")
        }
    } catch (e: ParseException) {
        println("Failed to parse command line arguments: ${e.message}")
    }
}
