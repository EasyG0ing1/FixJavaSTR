package com.simtechdata.commands;

import com.simtechdata.App;
import com.simtechdata.arguments.Arguments;
import com.simtechdata.commands.interfaces.Command;
import com.simtechdata.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class Version
        implements Command {


    private Arguments config;

    public Version(Arguments config) {
        this.config = config;
    }

    @Override
    public void execute(String[] args) {
        showVersion();
    }

    private static void showVersion() {
        Properties prop = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                Log.showLn("Could not determine current version");
            }
            else {
                prop.load(input);
                Log.showLn(prop.getProperty("version"));
            }
        }
        catch (IOException e) {
            Log.showLn(Arrays.toString(e.getStackTrace()));
        }
    }

}
