package com.simtechdata.work;

import com.simtechdata.enums.Option;
import com.simtechdata.custom.CustomAtomicBoolean;
import com.simtechdata.log.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.simtechdata.enums.Option.FIX;
import static com.simtechdata.enums.Option.SHOW;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Process {

    private static final AtomicLong          counter        = new AtomicLong(0);
    private static final AtomicLong          totalFiles     = new AtomicLong(0);
    private static final CustomAtomicBoolean processing     = new CustomAtomicBoolean(false);
    private static final CustomAtomicBoolean finished       = new CustomAtomicBoolean(true);
    private static final ExecutorService     executor       = Executors.newSingleThreadExecutor();
    private static final Map<String, String> fileContentMap = new ConcurrentHashMap<>();


    public static void processFiles(Option option, String pathString, String zipFilename, boolean allFiles) throws IOException {
        Path       path          = Paths.get(pathString);
        List<Path> pathList      = new CopyOnWriteArrayList<>();
        List<File> dirtyFileList = new CopyOnWriteArrayList<>();
        Path       zipFilePath   = Paths.get(pathString, zipFilename);
        File       zipFile       = zipFilePath.toFile();
        if (zipFile.exists() && option.equals(FIX)) {
            Log.showLn("\nZip file already exists, this program will not overwrite the file. Either delete the zip file or use a different filename.\n");
            return;
        }
        Log.showLn("Getting list of files to process from: " + pathString);
        if (allFiles) {
            Files.walk(path).filter(Files::isRegularFile).forEach(pathList::addLast);
        }
        else {
            Files
                    .walk(path)
                    .filter(p -> Files.isRegularFile(p) && (p.toString().endsWith(".java") || p
                            .toString()
                            .endsWith(".jsh")))
                    .forEach(pathList::addLast);
        }
        totalFiles.set(pathList.size());
        Log.showLn("Found " + commaFormat(totalFiles.get()) + " files to search");
        processing.setTrue();
        executor.submit(showProcessing());
        Pattern TEMPLATE_PATTERN = Pattern.compile("STR\\.\"|FMT\\.\"");
        for (Path p : pathList) {
            File file = p.toFile();
            counter.incrementAndGet();
            if (isTextFile(file)) {
                String fileBlock = FileUtils.readFileToString(file, Charset.defaultCharset());
                String filePath  = file.getAbsolutePath();
                if (TEMPLATE_PATTERN.matcher(fileBlock).find()) {
                    dirtyFileList.add(file);
                    fileContentMap.put(filePath, fileBlock);
                }
            }
        }
        processing.setFalse();
        while (finished.isFalse()) {
            sleep(500, MILLISECONDS);
        }

        if (!dirtyFileList.isEmpty()) {
            dirtyFileList.sort(Comparator.comparing(File::getAbsolutePath));

            Set<String> extensions = new CopyOnWriteArraySet<>();
            for (File file : dirtyFileList) {
                Path relPath = path.relativize(file.toPath());
                extensions.add(FilenameUtils.getExtension(relPath.toString()));
                Log.showLn("\t" + relPath);
            }
            Log.showLn("\nTotal files found: " + dirtyFileList.size());
            StringBuilder sb = new StringBuilder();
            for (String ext : extensions) {
                sb.append(".").append(ext).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            Log.showLn("File extensions found: " + sb);

            if (option.equals(SHOW)) {
                return;
            }

            Log.showLn("\nZipping files to: " + zipFilePath.toAbsolutePath());
            Zipping.zipFiles(dirtyFileList, zipFile, pathString);
            if (zipFile.exists()) {
                Log.showLn(" ".repeat(18) + "**** Files zipped successfully ****");
            }
            else {
                Log.showLn("Zip file not created, exiting\n");
                return;
            }

            Log.showLn(dirtyFileList.size() + " files will be modified");
            pressEnterToContinue();

            for (File file : dirtyFileList) {
                String  filePath = file.getAbsolutePath();
                String  dirty    = fileContentMap.get(filePath);
                boolean hasSTR   = dirty.contains("STR.\"");
                boolean hasFMT   = dirty.contains("FMT.\"");
                boolean doBoth   = hasSTR && hasFMT;
                boolean doClean  = hasSTR || hasFMT;
                String  clean    = "";
                if (doClean) {
                    if (doBoth) {
                        clean = cleanSTR(dirty);
                        clean = cleanFMT(clean);
                    }
                    else if (hasSTR) {
                        clean = cleanSTR(dirty);
                    }
                    else {
                        clean = cleanFMT(dirty);
                    }
                }
                FileUtils.writeStringToFile(file, clean, Charset.defaultCharset());
            }
        }
        Log.showLn("Done!");
    }

    private static boolean isTextFile(File file) throws IOException {

        if (FilenameUtils.getExtension(file.getName()).equals("class")) return false;

        InputStream in     = new FileInputStream(file);
        byte[]      header = new byte[4];
        if (in.read(header) == 4) {
            if ((header[0] & 0xFF) == 0xCA && (header[1] & 0xFF) == 0xFE && (header[2] & 0xFF) == 0xBA && (header[3] & 0xFF) == 0xBE) {
                in.close();
                return false;
            }
        }
        in.close();
        in = new FileInputStream(file);

        int    bytesRead;
        int    total   = 0;
        int    nonText = 0;
        byte[] buffer  = new byte[1024];

        while ((bytesRead = in.read(buffer)) != -1 && total < 4096) {
            for (int i = 0; i < bytesRead; i++) {
                byte b = buffer[i];
                if (b < 0x09 || (b > 0x0D && b < 0x20) || b == 0x7F) {
                    nonText++;
                }
                total++;
            }
        }
        if (total == 0) return false; // empty files aren't "text"
        double ratio = nonText / (double) total;
        in.close();
        return ratio < 0.30;
    }

    private static Runnable showProcessing() {
        return () -> {
            int lastSize = 0;
            finished.setFalse();
            String first            = "Searching files for String template code ... (";
            String totalFilesString = " / " + commaFormat(totalFiles.get()) + ")";
            while (processing.isTrue()) {
                cleanLine(lastSize);
                String update = first + commaFormat(counter.get()) + totalFilesString;
                Log.show(update);
                lastSize = update.length();
                sleep(400, MILLISECONDS);
            }
            cleanLine(lastSize);
            Log.showLn(first + commaFormat(counter.get()) + totalFilesString);
            finished.setTrue();
        };
    }

    private static String commaFormat(long number) {
        return String.format("%,d", number);
    }

    private static void cleanLine(int lastSize) {
        Log.show("\r".repeat(lastSize));
        Log.show(" ".repeat(lastSize));
        Log.show("\r".repeat(lastSize));
    }

    private static String cleanSTR(String content) {
        Pattern STR_TEMPLATE_PATTERN = Pattern.compile("STR\\.\"([^\"]*)\"");
        Pattern EMBEDDED_VAR_PATTERN = Pattern.compile("\\\\\\{([^}]+)}");

        Matcher templateMatcher = STR_TEMPLATE_PATTERN.matcher(content);

        StringBuilder transformed = new StringBuilder();
        while (templateMatcher.find()) {
            String  inner      = templateMatcher.group(1);  // content inside "..."
            Matcher varMatcher = EMBEDDED_VAR_PATTERN.matcher(inner);

            StringBuilder newString = new StringBuilder("\"");
            int           last      = 0;
            while (varMatcher.find()) {
                newString.append(inner, last, varMatcher.start());
                newString.append("\" + ").append(varMatcher.group(1)).append(" + \"");
                last = varMatcher.end();
            }
            newString.append(inner.substring(last)).append("\"");

            String replaced = newString.toString().replaceAll("\\+ \"\"", "")  // remove empty concat
                                       .replaceAll("\"\" \\+", ""); // leading empty strings

            templateMatcher.appendReplacement(transformed, Matcher.quoteReplacement(replaced));
        }
        templateMatcher.appendTail(transformed);

        return removeWhitespaceAroundParens(transformed.toString());
    }

    private static String cleanFMT(String content) {
        Pattern fmtPattern = Pattern.compile("FMT\\.\"([^\"]*)\"");
        Matcher fmtMatcher = fmtPattern.matcher(content);
        StringBuffer result = new StringBuffer();

        while (fmtMatcher.find()) {
            String template = fmtMatcher.group(1);
            StringBuilder formatString = new StringBuilder();
            List<String> arguments = new ArrayList<>();
            int lastEnd = 0;

            Pattern exprPattern = Pattern.compile("(%[-\\d\\.]*[a-zA-Z])\\\\\\{([^}]+)\\}");
            Matcher exprMatcher = exprPattern.matcher(template);

            while (exprMatcher.find()) {
                formatString.append(template, lastEnd, exprMatcher.start());
                formatString.append(exprMatcher.group(1));
                arguments.add(exprMatcher.group(2).trim());
                lastEnd = exprMatcher.end();
            }

            formatString.append(template.substring(lastEnd));

            // Handle trailing \{...} or {...} for argument list
            Matcher trailingArgsMatcher = Pattern.compile("^(.*?)(\\\\?\\{([^}]+)})$").matcher(formatString.toString());
            if (trailingArgsMatcher.matches()) {
                formatString = new StringBuilder(trailingArgsMatcher.group(1));
                if (arguments.isEmpty()) {
                    // Only add if not already added from inline matches
                    for (String arg : trailingArgsMatcher.group(3).split(",")) {
                        arguments.add(arg.trim());
                    }
                }
            }

            String replacement = arguments.isEmpty()
                                 ? "\"" + formatString.toString().replace("\"", "\\\"") + "\""
                                 : "String.format(\"" + formatString.toString().replace("\"", "\\\"") + "\", " + String.join(", ", arguments) + ")";

            fmtMatcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        fmtMatcher.appendTail(result);
        return result.toString();
    }

    private static String removeWhitespaceAroundParens(String line) {
        StringBuilder result   = new StringBuilder();
        boolean       inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"' && (i == 0 || line.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                result.append(c);
                continue;
            }

            if (!inQuotes) {
                if (c == '(' && i + 1 < line.length() && Character.isWhitespace(line.charAt(i + 1))) {
                    result.append('(');
                    while (i + 1 < line.length() && Character.isWhitespace(line.charAt(i + 1))) i++;
                    continue;
                }
                if (c == ')' && !result.isEmpty() && Character.isWhitespace(result.charAt(result.length() - 1))) {
                    result.deleteCharAt(result.length() - 1);
                    result.append(')');
                    continue;
                }
            }

            result.append(c);
        }

        return result.toString().replaceAll("\\s+;", ";");
    }

    private static void sleep(long time, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(time);
        }
        catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public static void pressEnterToContinue() throws IOException {
        Log.show("\nPress Enter to continue...");
        System.in.read();
    }

    public static boolean pathValid(String pathString, Option option) {
        if (!pathString.isEmpty()) {
            Path path = Paths.get(pathString);
            switch (option) {
                case FIX,
                     SHOW -> {
                    return Files.exists(path) && Files.isDirectory(path);
                }
                case UNZIP -> {
                    return Files.exists(path) && Files.isRegularFile(path);
                }
            }
        }
        return false;
    }

}
