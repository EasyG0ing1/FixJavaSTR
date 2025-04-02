package com.simtechdata.commands.interfaces;

import com.simtechdata.arguments.Arguments;
import com.simtechdata.commands.*;
import com.simtechdata.log.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Executor {

    private final Map<String, Command> commandMap;
    private final Arguments            config;


    public Executor(Arguments config) {
        this.config = config;
        commandMap  = new HashMap<>();
        commandMap.put("fix", new Fix(config));
        commandMap.put("show", new Show(config));
        commandMap.put("unzip", new Unzip(config));
        commandMap.put("help", new Help(config));
        commandMap.put("version", new Version(config));
    }

    public void executeCommand(String[] args) {
        String  commandKey = args[0].replaceAll("[^a-zA-Z]+", "").toLowerCase();
        Command command    = commandMap.get(commandKey);

        if (command == null) {
            Log.showLn("\nInvalid argument: " + commandKey + "\n");
            return;
        }

        if (Arrays.asList(args).contains("all")) {
            config.setAllFiles(true);
        }

        command.execute(args);
    }
}
