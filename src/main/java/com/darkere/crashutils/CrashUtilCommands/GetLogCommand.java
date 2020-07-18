package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.LogHandler;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.nio.file.Path;


public class GetLogCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("getLog")
            .executes(GetLogCommand::getLogOverview)
            .then(Commands.literal("uploadCrashReport")
                .executes(GetLogCommand::uploadCrashReport)
            )
            .then(Commands.literal("uploadLatestLog")
                .executes(GetLogCommand::uploadLatestLog)
            )
            .then(Commands.literal("uploadArchivedLog")
                .executes(GetLogCommand::uploadArchivedLog));
    }

    private static int uploadArchivedLog(CommandContext<CommandSource> context) {
        Path archivedLog = LogHandler.getLatestArchivedLogPath();
        if (archivedLog != null) {
            uploadString(context, archivedLog, LogHandler.getStringFromArchive(archivedLog));
        } else {
            context.getSource().sendFeedback(new StringTextComponent("No Archived Log found"), true);
        }
        return 1;
    }

    private static void uploadString(CommandContext<CommandSource> context, Path logPath, String text) {
        context.getSource().sendFeedback(new StringTextComponent("Uploading..."), false);
        String description = "Log Created " + LogHandler.getRelativePathDateInMin(logPath) + "min before upload";
        String url = LogHandler.uploadLog(description, logPath.getFileName().toString(), text);
        if (url.startsWith("https")) {
            context.getSource().sendFeedback(CommandUtils.createURLComponent(url, url), true);
        } else {
            context.getSource().sendFeedback(new StringTextComponent("Upload Failed: " + url), true);
            context.getSource().sendFeedback(new StringTextComponent("Try again or use Copy"), true);
        }
    }

    private static int uploadLatestLog(CommandContext<CommandSource> context) {
        Path latestLog = LogHandler.latestlog;
        uploadString(context, latestLog, LogHandler.getFileAsStringFromPath(latestLog));
        return 1;
    }

    private static int uploadCrashReport(CommandContext<CommandSource> context) {
        Path crashreport = LogHandler.getLatestCrashReportPath();
        if (crashreport == null) {
            context.getSource().sendFeedback(new StringTextComponent("No crash report found"), true);
            return 1;
        }
        uploadString(context, crashreport, LogHandler.getFileAsStringFromPath(crashreport));
        return 1;
    }


    private static int getLogOverview(CommandContext<CommandSource> ctx) {
        IFormattableTextComponent latestlogText = new StringTextComponent("Log from current Game").func_240701_a_(TextFormatting.YELLOW);
        latestlogText.func_230529_a_(CommandUtils.getCommandTextComponent(" [Upload]", "/cu getLog uploadLatestLog"));
        latestlogText.func_230529_a_(CommandUtils.createCopyComponent(" [Copy]", LogHandler.getFileAsStringFromPath(LogHandler.latestlog)));
        ctx.getSource().sendFeedback(latestlogText, true);
        Path crashreport = LogHandler.getLatestCrashReportPath();
        if (crashreport != null) {
            IFormattableTextComponent crashreporttext = new StringTextComponent("Latest Crash Report ").func_240701_a_(TextFormatting.RED).func_230529_a_(new StringTextComponent(LogHandler.getRelativePathDateInMin(crashreport) + " Minutes old").func_240701_a_(TextFormatting.AQUA));
            crashreporttext.func_230529_a_(CommandUtils.getCommandTextComponent(" [Upload]", "/cu getLog uploadCrashReport"));
            crashreporttext.func_230529_a_(CommandUtils.createCopyComponent(" [Copy]", LogHandler.getFileAsStringFromPath(crashreport)));
            ctx.getSource().sendFeedback(crashreporttext, true);
        }
        Path archivedLog = LogHandler.getLatestArchivedLogPath();
        if (archivedLog != null) {
            IFormattableTextComponent archivedText = new StringTextComponent("Archived Latest.log ").func_240701_a_(TextFormatting.GREEN).func_230529_a_(new StringTextComponent(LogHandler.getRelativePathDateInMin(archivedLog) + " Minutes old").func_240701_a_(TextFormatting.AQUA));
            archivedText.func_230529_a_(CommandUtils.getCommandTextComponent(" [Upload]", "/cu getLog uploadArchivedLog"));
            archivedText.func_230529_a_(CommandUtils.createCopyComponent(" [Copy]", LogHandler.getStringFromArchive(archivedLog)));
            ctx.getSource().sendFeedback(archivedText, true);
        }
        return 1;

    }
}
