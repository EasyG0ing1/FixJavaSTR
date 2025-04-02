package com.simtechdata.commands;

import com.simtechdata.arguments.Arguments;
import com.simtechdata.enums.Option;
import com.simtechdata.log.Log;
import com.simtechdata.work.Process;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Fix
        implements Command {
    private final Arguments config;

    public Fix(Arguments config) {
        this.config = config;
    }

    @Override
    public void execute(String[] args) {
        boolean allFiles = config.getAllFiles();

        if ((args.length < 3 && !allFiles) || (args.length < 4 && allFiles)) {
            Log.showLn("Not enough arguments for fix - use help for more information");
            return;
        }

        Option option = Option.FIX;
        String pathString = args[1];
        String zipFilename = args[2];

        if (Process.pathValid(pathString, option) && !zipFilename.isEmpty()) {
            Path zipFilePath = Paths.get(pathString, zipFilename);
            Log.showLn("Files under the path: " + pathString);
            Log.showLn("Will be zipped to:    " + zipFilePath);
            Log.showLn("Then, all String template code in those files will be converted back to concatenation");
            if(allFiles) {
                Log.showLn("(ALL files in the directory will be searched for String templates)");
            }
            else {
                Log.showLn("(Only .java and .jsh files will be processed)");
            }
            try {
                Process.pressEnterToContinue();
                Process.processFiles(option, pathString, zipFilename, allFiles);
            }
            catch (IOException e) {
                Log.showLn("Error processing files: \n" + e.getMessage());
            }
        }
        else {
            Log.showLn("Path given does not exist or is not a directory:\n" + pathString);
        }

    }
}
