package com.simtechdata.commands;

import com.simtechdata.arguments.Arguments;
import com.simtechdata.commands.interfaces.Command;
import com.simtechdata.enums.Option;
import com.simtechdata.log.Log;
import com.simtechdata.work.Process;
import com.simtechdata.work.Zip;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Paths;

public class Unzip
        implements Command {
    private Arguments config;

    public Unzip(Arguments config) {
        this.config = config;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Not enough arguments for unzip. Use help for more information");
            return;
        }

        Option option            = Option.UNZIP;
        String zipFilePathString = args[1];
        String zipFilename       = FilenameUtils.getName(zipFilePathString);
        String unzipPathString;

        if (args.length == 3) {
            unzipPathString = args[2];
        }
        else {
            unzipPathString = Paths.get(zipFilePathString).getParent().toString();
        }

        if (Process.pathValid(zipFilePathString, option)) {
            Log.showLn("Unzipping:      " + zipFilename);
            Log.showLn("Into directory: " + unzipPathString);
            try {
                Process.pressEnterToContinue();
                Zip.unzip(zipFilePathString, unzipPathString);
            }
            catch (IOException e) {
                Log.showLn("Error unzipping files: \n" + e.getMessage());
            }
        }
        else {
            Log.showLn("Zip file path given does not exist or is not a file.\n" + zipFilePathString);
        }

    }
}
