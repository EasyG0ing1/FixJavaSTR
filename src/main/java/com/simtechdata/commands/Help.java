package com.simtechdata.commands;

import com.simtechdata.arguments.Arguments;
import com.simtechdata.log.Log;

public class Help
        implements Command {

    private Arguments config;

    public Help(Arguments config) {
        this.config = config;
    }

    @Override
    public void execute(String[] args) {
        showHelp();
    }

    private static void showHelp() {
        String help = """
                      
                      FixJavaSTR is a tool that will process files in a directory and replace String templates with concatenation.
                      It will recursively search the path you give it.
                      
                      It will only process files ending in '.java' or '.jsh' unless you use the 'all' argument, in which case it will
                      process all files in the directory, making a best effort to isolate files that consist of text only. If you use
                      the 'all' argument, it would be best to first use the 'show' argument and look at the list it generates to make sure
                      it will be processing only files you intend on fixing. See the examples below for proper syntax.
                      
                      The program will always zip the files it processes before processing them, so that you can still get to the
                      original files if you need to.
                      
                      The following arguments are supported:
                      
                        show    - Kicks out a list of found files that use a String template
                        fix     - Reverts String templates in found files back to concatenation after first zipping up the original files
                        unzip   - Unzips files from the zip file into their original directory tree (option to change output folder - see below)
                        all     - Will process all files in the directory, not just .java and .jsh files. Must be the last argument
                        help    - Displays this help message
                        version - Displays the version of the program
                      
                      For the fix and Log.show arguments, you must follow it with the FULL PATH to the directory containing the files to process
                            ex: show /home/java/projects
                            ex: show /home/java/projects all
                            ex: show C:\\Users\\user\\java\\projects
                            ex: show C:\\Users\\user\\java\\projects all
                      
                      The fix argument additionally needs the name of the zip file
                            ex: fix /home/java/projects MyZipFile.zip
                            ex: fix /home/java/projects MyZipFile.zip all
                            ex: fix C:\\Users\\user\\java\\projects MyZipFile.zip
                            ex: fix C:\\Users\\user\\java\\projects MyZipFile.zip all
                      
                      For the unzip argument, you must follow it with the FULL PATH to the zip file.
                            ex: unzip /home/java/projects/MyZipFile.zip
                            ex: unzip C:\\Users\\user\\java\\projects\\MyZipFile.zip
                      
                      Unzipping the zip file will create the same folder tree in the same directory where the zip file exists.
                      If you need to unzip the files into a different directory, simply add the desired directory to the end
                      of the argument.
                            ex: unzip /home/java/projects/MyZipFile.zip /home/java/projects/unzipped
                            ex: unzip C:\\Users\\user\\java\\projects\\MyZipFile.zip C:\\Users\\user\\java\\projects\\unzipped
                      
                      go to https://github.com/EasyG0ing1/FixJavaSTR for more information
                      """;
        Log.showLn(help);
    }

}
