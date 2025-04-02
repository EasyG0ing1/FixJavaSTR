package com.simtechdata;

import com.simtechdata.arguments.Arguments;
import com.simtechdata.commands.interfaces.Executor;

public class App {

    public static void main(String[] args) throws Exception {
        processArguments(args);
    }

    private static void processArguments(String[] args) {

        String[] arguments;

        if (args.length == 0) {
            arguments = new String[] {"help"};
        }
        else {
            arguments = args;
        }

        Arguments config   = new Arguments();
        Executor  executor = new Executor(config);

        executor.executeCommand(arguments);

        System.exit(0);
    }

}
