package com.simtechdata;

import com.simtechdata.arguments.Arguments;
import com.simtechdata.commands.CommandExecutor;
import com.simtechdata.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;


/**
 * The App class serves as the entry point for the application.
 * It processes command-line arguments and delegates command execution
 * to the appropriate components.
 */
public class App {

    public static void main(String[] args) throws Exception {
        processArguments(args);
    }

    /**
     * Processes the command-line arguments, determines the user-specified command,
     * and executes the appropriate functionality based on the input.
     * <p>
     * If no arguments are provided, it defaults to the "help" command. Otherwise,
     * it validates, parses, and executes the appropriate command (e.g., fix, show, unzip, help)
     * using the {@link CommandExecutor}.
     * </p>
     *
     * @param args The array of command-line arguments provided by the user.
     *             This could include a command (e.g., "fix", "unzip", etc.)
     *             and additional parameters, such as file paths or options.
     *
     *             <p>
     *             The method performs the following steps:
     *             <ul>
     *               <li>Defaults the command to "help" if no arguments are supplied.</li>
     *               <li>Initializes an {@link Arguments} object and a {@link CommandExecutor} instance to manage commands.</li>
     *               <li>Delegates the command execution to {@link CommandExecutor#executeCommand(String[])}.</li>
     *               <li>Terminates the application using {@link System#exit(int)} after command execution.</li>
     *             </ul>
     *             </p>
     *
     *             <b>Example Usage:</b>
     *             <pre>
     *             java MyApplication fix /path/to/files archive.zip
     *             java MyApplication unzip /path/to/archive.zip
     *             java MyApplication show /path/to/files
     *             java MyApplication help
     *             </pre>
     */
    private static void processArguments(String[] args) {

        String[] arguments;

        if (args.length == 0) {
            arguments = new String[] {"help"};
        }
        else {
            arguments = args;
        }

        Arguments       config   = new Arguments();
        CommandExecutor executor = new CommandExecutor(config);

        executor.executeCommand(arguments);

        System.exit(0);
    }

}
