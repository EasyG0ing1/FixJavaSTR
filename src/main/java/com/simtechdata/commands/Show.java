package com.simtechdata.commands;

import com.simtechdata.arguments.Arguments;
import com.simtechdata.commands.interfaces.Command;
import com.simtechdata.enums.Option;
import com.simtechdata.log.Log;
import com.simtechdata.work.Process;

import java.io.IOException;

public class Show
        implements Command {
    private Arguments config;

    public Show(Arguments config) {
        this.config = config;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Not enough arguments for show option. Use help for more information");
            return;
        }

        Option  option      = Option.SHOW;
        String  pathString  = args[1];
        String  zipFilename = "";
        boolean allFiles    = config.getAllFiles();

        if (Process.pathValid(pathString, option)) {
            try {
                Process.files(option, pathString, zipFilename, allFiles);
            }
            catch (IOException e) {
                Log.showLn("Error processing files: \n" + e.getMessage());
            }
        }
        else {
            Log.showLn("Path given does not exist or is not a directory\n" + pathString);
        }

    }
}
