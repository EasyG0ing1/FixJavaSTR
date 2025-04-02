# FixJavaSTR
## Overview
**FixJavaSTR** is a command-line tool designed to help a java developer quickly and easily replace the abandoned Java String Templates with concatenation. It automatically backs up any files it modifies into a zip file, just in case. The tool provides robust support for recursive directory traversal, and the ability to process all files in your projects folder, not just classes. 

Upon re-opening projects that I wrote back when String Templates were available, the IDE was giving me numerous errors because current versions of Java do not support String Templates, so instead of manually fixing String Template syntax, I wrote this tool to go through all of my .java files and fix that code for me to prevent errors in the future. 

### Key Features
- Recursively processes files in a given directory.
- Supports `.java` and `.jsh` file formats by default, with optional support for all file types.
- Automatically zips files before modifying them to ensure backups are always available.
- Provides complete control with arguments to view, process, and revert changes.

## Contents
- [Installing](#installation)
- [Compiling](#compiling)
  - [Compile Scripts](#compile-scripts)
- [What It Does](#what-fixjavastr-does)
  - [Command-Line Arguments](#command-line-arguments)
  - [Argument Requirements](#argument-requirements)
  - [Examples](#examples)
    - [Show Files To Be Modified](#show-files-to-be-modified)
    - [Fix Files](#fix-files)
    - [Restore Original Files](#restore-original-files)
    - [View Help](#view-help)
- [How It Works](#how-it-works)
- [Practical Notes](#practical-notes)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Installation
To obtain the program:

* If you are on MacOS, you can download the compiled native-image from the [releases](https://github.com/EasyG0ing1/FixJavaSTR/releases/latest) page and just run it from Terminal.
* Linux and Windows and MacOS users can also do this from a terminal:
``` bash
    git clone https://github.com/EasyG0ing1/FixJavaSTR.git
    cd FixJavaSTR
    mvn clean package
    java -jar target/FixJavaSTR-jar-with-dependencies.jar
```

I have tested this with Maven version `4.0.0-rc-2` and Java version 24. If you are using a different version of maven, 
go into the `pom.xml` file and under the Maven Enforcer Plugin, change this line to match your version.
``` xml
<requireMavenVersion>
    <version>4.0.0-rc-2</version>
</requireMavenVersion>
```

If you aren't sure which version of maven you're using, run `mvn --version` and you should see output similar to this:
``` bash
Apache Maven 4.0.0-rc-2 (273314404f85ec3c089e295d8b4e0cb18c287cf5)
Maven home: /Users/user/.sdkman/candidates/maven/4.0.0-rc-2
Java version: 24, vendor: Oracle Corporation, runtime: /Users/michael/.sdkman/candidates/java/24.ea.28-graal
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "15.3.1", arch: "x86_64", family: "mac"
```
The first line has the version number after the word `Maven` just copy and paste that into the pom file as stated.

I cannot guarantee that this will work with versions of Maven less than 4, but I can't think of any reason why it wouldn't work, except for compiling the project into a GraalVM native-image, which will almost certainly require Maven version 4.

If you aren't using [SDKMAN!](https://sdkman.io/) to manage your Java development environment, I highly recommend it as it
can not only download and install numerous versions of Java, then allow you to switch your current version with a simple
command, it will also manage other aspects of your Java environment like different versions of Maven or Gradle for example.
It is a robust and easy tool to use. It not only handles the downloading and installation of the various SDKs, it also 
properly manages any environment variables that need to be set for any given SDK.

## Compiling
If you're compelled to compile the program into a GraalVM native-image, the POM file is already set up for that. The
easiest way to go about it is to use SDKMAN to install GraalVM and Maven like this:

``` bash
sdk install java 24-graal
sdk install maven 4.0.0-rc-2
sdk use java 24-graal
sdk use maven 4.0.0-rc-2
```
Next, clone the repo as described above, then go into the `FixJavaSTR/compile` folder and run the script, then use the program.
***
#### MacOS / Linux
``` bash
    git clone https://github.com/EasyG0ing1/FixJavaSTR.git
    cd FixJavaSTR/compile
    chnod +x *.sh
    ./native.sh
    ./FixJavaSTR
```

#### Windows
``` bash
    git clone https://github.com/EasyG0ing1/FixJavaSTR.git
    cd FixJavaSTR\compile
    native.bat
    FixJavaSTR
```
***
GraalVM can't compile all Java projects as one might think. Because of it's limited ability to weed through reflection 
or properly figure out JNI etc. it becomes necessary to do a step before compiling the native image. That step involves
running your compiled jar file using an argument that causes Graal to record abstract uses of other objects in your
program. Doing this creates a file called `reachability-metadata.json` and that file needs to exist in the resources
folder in your project under a very specific path. 

I have already generated that file and it is included in the repo. However, if for some reason you need to re-create it,
you need to run the `dograal.sh` or `dograal.bat` file. This will compile the jar file, then execute the right commands
which will run the program and ultimately it will generate the `reachability-metadata.json` file and put it in the 
correct folder. After doing that, you simply run `native.sh` or `native.bat` to compile the native image.

### Compile Scripts

Here is a breakdown of what the different scripts do in the compile folder (there are Linux / MacOS `.sh` and Windows `.bat` versions of the scripts):

In Linux / MacOS, run `chmod +x *.sh` first to make them executable.


``` bash
  compile - Simply makes the jar file and puts it in the target folder.
  dograal - described above
  native  - compiles the native-image
  runjar  - Runs the jar file in a JVM
```

The `dograal` script must have a path to your projects folder passed into it: `./dograal.sh /path/to/java/projects` / `dograal.bat C:\\path\\to\\java\\projects`. It WILL NOT modify any of your files, it will simply run the program passively so that GraalVM can get what it needs.
## What FixJavaSTR Does

It's really quite simple. The program goes through your java project folder recursively and looks for files containing String Template code (which was
available as a preview feature in Java 21 and 22), then it converts those back into standard concatenation syntax or String.format() depending on which template you engaged. So for
example, this code would be converted into the next line:

``` java
  System.out.println(STR."\{NL}Showing Downloaded Links:\{NL}");
  System.out.println(NL + "Showing Downloaded Links:" + NL);
  
  String formattedString = FMT."Name: %s, Age: %d, Salary: %.2f\{name, age, salary}";
  String formattedString = String.format("Name: %s, Age: %d, Salary: %.2f", name, age, salary);
```
By default, the program only searches `.java` and `.jsh` files. However, if you want to process every file in a given folder,
just use the `all` argument as described below. When you use the `all` argument, the program does a best effort at identifying
binary files so that they are not included in the search for String templates. This includes `.class` files.

Once the program has obtained a list of files, it loads each file into a HashMap and uses a Regex to find String Templates. When it
finds them, they get added to a final list and once that list is created, the program copies each file into a zip file. 

After the zip file is created, the String Templates are converted back into concatenation syntax and the file is saved 
back onto itself. Meaning this program will alter your project code files. However, I have tested it quite extensively and
have found it to be flawless. That doesn't mean, however, that there might be syntax used that my RegEx won't cover. If you find such a case,
create an issue and I'll update the program as needed.

You can get a list of which files the program will be modifying before you enact any change on them if you use the `show` argument.
That argument runs the same code as the `fix` argument does, but it stops before the zip file creation step.

Using the `fix` argument will cause your files to be modified.


## Usage
FixJavaSTR is a command-line tool that accepts a variety of arguments to manage and process files within a specified directory.
The command flow is simple: `FixJavaSTR <command> <path> <option>`.

### Command-Line Arguments

| **Argument** | **Description**                                                                                                                                   |
|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `show`       | Outputs a list of files that use String templates in the specified directory.                                                                     |
| `fix`        | Converts String templates into concatenation while creating a zip file backup of the original files.                                              |
| `unzip`      | Extracts the zip file created by FixJavaSTR to restore files in their original directory structure (with an optional output directory).           |
| `all`        | Processes all files in the directory, not just `.java` and `.jsh`. Makes a best effort to isolate text-only files. Must be the **last argument**. |
| `help`       | Displays a help message with detailed explanations of usage.                                                                                      |
| `version`    | Displays the program version number.                                                                                                              |

### Argument requirements

| **Argument** | **Required**                               | **Optional**                  |
|--------------|--------------------------------------------|-------------------------------|
| `show`       | \<path to project folder\>                 | all                           |
| `fix`        | \<path to project folder\> \<ZipFileName\> | all                           |
| `unzip`      | \<Full Zip File Path\>                     | \<alternate path to unzip to> 
| `help`       | No requirements                            |                               |
| `version`    | No requirements                            |                               |

### Examples
#### **Show Files To Be Modified**
Use the `show` argument to display (`.java` and `.jsh`) files recursively discovered in the provided path that utilize String templates.
``` bash
  FixJavaSTR show /path/to/directory
  FixJavaSTR show C:\\path\\to\\directory
```
To include all files (not just `.java` and `.jsh`), add the `all` argument:
``` bash
  FixJavaSTR show /path/to/directory all
  FixJavaSTR show C:\\path\\to\\directory all
```
***
#### **Fix Files**
Use the `fix` argument to replace all String templates with concatenations while creating a backup zip file, which will be saved in the folder being searched.
``` bash
  FixJavaSTR fix /path/to/project/directory BackupFiles.zip
  FixJavaSTR fix C:\\path\\to\\project\\directory BackupFiles.zip
```
To include all files, use the `all` argument:
``` bash
  FixJavaSTR fix /path/to/project/directory BackupFiles.zip all
  FixJavaSTR fix C:\\path\\to\\project\\directory BackupFiles.zip all
```
***
#### **Restore Original Files**
Use the `unzip` argument to restore files from the backup zip file.

The original folder tree will be preserved and by default, the files are extracted into the same folder where the zip file exists (`/path/to/` and `C:\path\to\` in this example):
``` bash
  FixJavaSTR unzip /path/to/BackupFile.zip
  FixJavaSTR unzip C:\\path\\to\\BackupFile.zip
```

To extract the files into a specific directory, provide the path to unzip them to (Again, the original folder tree will be preserved). If the output folder does not exist, it will be created automatically:
``` bash
  FixJavaSTR unzip /path/to/BackupFile.zip /path/to/unzip/into
  FixJavaSTR unzip C:\\path\\to\\BackupFile.zip C:\\path\\to\\unzip\\into
```
***
#### **View Help**
To display the help message, use the `help` argument:
``` bash
  FixJavaSTR
  FixJavaSTR help
```
## How It Works
1. **File Detection**: By default, FixJavaSTR scans for `.java` and `.jsh` files in the specified directory and subdirectories.
2. **Backup**: Before applying changes, FixJavaSTR creates a zip file containing the original files for safety.
3. **String Replacement**: Replaces String templates (e.g., `STR."\{value}"` and `FMT." %d \{variable}"`) with concatenation or String.format().
4. **Restore on Demand**: Allows users to revert changes by unzipping the backup.

## Practical Notes
Use the `show` command to assess which files will be changed before proceeding with the `fix` command.
If any issues arise, simply use the `unzip` command to get your original files back.

## Troubleshooting
### Common Issues
#### 1. **"No files found with String templates"**
- Ensure the provided directory contains `.java` or `.jsh` files with templates.
- Use the `all` argument if working with alternative file types.

#### 2. **"File path not recognized"**
- Verify you provided the **full path** to the directory/file, starting from the root `/folder` for Linux / MacOS and `C:\` for Windows
- For Windows users, ensure backslashes (\) are properly escaped in the command (e.g., `C:\\Users\\user\\projects`).

#### 3. **Contacting Me**
- Create an issue here in this repo with any questions, concerns, problems or comments.

## License
This project is licensed under the [MIT License](LICENSE). Feel free to use, modify, and share the tool within the terms of the license.
## Learn More
Visit the official repository on [GitHub](https://github.com/EasyG0ing1/FixJavaSTR) for the latest updates, additional documentation, and contribution opportunities.
By following this guide you should have a complete understanding of how to use FixJavaSTR to manage and refactor String template usage in your projects. Let me know if any additional details are needed!
