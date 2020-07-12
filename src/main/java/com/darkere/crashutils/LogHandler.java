package com.darkere.crashutils;

import fr.minuskube.pastee.JPastee;
import fr.minuskube.pastee.data.Paste;
import fr.minuskube.pastee.data.Section;
import fr.minuskube.pastee.response.SubmitResponse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class LogHandler {
    private static final JPastee pastee = new JPastee("aleMUU8QFQiuh9q9UnK737FoM5v08v2cZOkl1f1p7");
    public static final Path latestlog = Paths.get("logs/latest.log");

    public static String uploadLog(String description, String name, String content) {

        Paste.Builder paste = Paste.builder();
        paste.description(description);
        paste.addSection(Section.builder()
            .name(name)
            .contents(content)
            .build());

        SubmitResponse resp = pastee.submit(paste.build());
        if (resp.isSuccess()) {
            return resp.getLink();
        } else {
            return resp.getErrorString();
        }
    }

    public static String getRelativePathDateInMin(Path path) {
        long last = path.toFile().lastModified();
        last = Instant.now().getEpochSecond() - (last / 1000);
        return Long.toString(last / 60);
    }

    public static Path getLatestCrashReportPath() {
        Optional<Path> lastFilePath = Optional.empty();
        try {
            lastFilePath = Files.list(Paths.get("crash-reports/"))   // here we get the stream with full directory listing
                .filter(f -> !Files.isDirectory(f))  // exclude subdirectories from listing
                .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
        } catch (IOException e) {
            return null;
        }
        return lastFilePath.orElse(null);
    }

    public static String getFileAsStringFromPath(Path path) {
        List<String> strings = new ArrayList<>(Collections.singletonList(""));
        try {
            strings = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.join(System.lineSeparator(), strings);
    }

    public static String getStringFromArchive(Path path) {
        GZIPInputStream gzip = null;
        try {
            gzip = new GZIPInputStream(new FileInputStream(String.valueOf(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
        List<String> strings = br.lines().collect(Collectors.toList());
        return String.join(System.lineSeparator(), strings);
    }

    public static Path getLatestArchivedLogPath() {
        Optional<Path> lastFilePath = Optional.empty();
        try {
            lastFilePath = Files.list(Paths.get("logs/"))   // here we get the stream with full directory listing
                .filter(f -> !Files.isDirectory(f) && f.toFile().getName().endsWith(".gz"))  // exclude subdirectories from listing
                .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
        } catch (IOException e) {
            return null;
        }
        return lastFilePath.orElse(null);
    }
}