//package com.darkere.crashutils.CrashUtilCommands;
//
//import com.darkere.crashutils.CommandUtils;
//import com.darkere.crashutils.LogHandler;
//import com.mojang.brigadier.builder.ArgumentBuilder;
//import com.mojang.brigadier.context.CommandContext;
//import net.minecraft.commands.CommandSourceStack;
//import net.minecraft.commands.Commands;
//import net.minecraft.network.chat.MutableComponent;
//import net.minecraft.network.chat.TextComponent;
//import net.minecraft.ChatFormatting;
//
//import java.nio.file.Path;
//
//
//public class GetLogCommand {
//
//    public static ArgumentBuilder<CommandSourceStack, ?> register() {
//        return Commands.literal("log")
//            .executes(GetLogCommand::getLogOverview)
//            .then(Commands.literal("uploadCrashReport")
//                .executes(GetLogCommand::uploadCrashReport)
//            )
//            .then(Commands.literal("uploadLatestLog")
//                .executes(GetLogCommand::uploadLatestLog)
//            )
//            .then(Commands.literal("uploadArchivedLog")
//                .executes(GetLogCommand::uploadArchivedLog));
//    }
//
//    private static int uploadArchivedLog(CommandContext<CommandSourceStack> context) {
//        Path archivedLog = LogHandler.getLatestArchivedLogPath();
//        if (archivedLog != null) {
//            uploadString(context, archivedLog, LogHandler.getStringFromArchive(archivedLog));
//        } else {
//            context.getSource().sendSuccess(new TextComponent("No Archived Log found"), true);
//        }
//        return 1;
//    }
//
//    private static void uploadString(CommandContext<CommandSourceStack> context, Path logPath, String text) {
//        context.getSource().sendSuccess(new TextComponent("Uploading..."), false);
//        String description = "Log Created " + LogHandler.getRelativePathDateInMin(logPath) + "min before upload";
//        String url = LogHandler.uploadLog(description, logPath.getFileName().toString(), text);
//        if (url.startsWith("https")) {
//            context.getSource().sendSuccess(CommandUtils.createURLComponent(url, url), true);
//        } else {
//            context.getSource().sendSuccess(new TextComponent("Upload Failed: " + url), true);
//            context.getSource().sendSuccess(new TextComponent("Try again or use Copy"), true);
//        }
//    }
//
//    private static int uploadLatestLog(CommandContext<CommandSourceStack> context) {
//        Path latestLog = LogHandler.latestlog;
//        uploadString(context, latestLog, LogHandler.getFileAsStringFromPath(latestLog));
//        return 1;
//    }
//
//    private static int uploadCrashReport(CommandContext<CommandSourceStack> context) {
//        Path crashreport = LogHandler.getLatestCrashReportPath();
//        if (crashreport == null) {
//            context.getSource().sendSuccess(new TextComponent("No crash report found"), true);
//            return 1;
//        }
//        uploadString(context, crashreport, LogHandler.getFileAsStringFromPath(crashreport));
//        return 1;
//    }
//
//
//    private static int getLogOverview(CommandContext<CommandSourceStack> ctx) {
//        MutableComponent latestlogText = new TextComponent("Log from current Game").withStyle(ChatFormatting.YELLOW);
//        latestlogText.append(CommandUtils.getCommandTextComponent(" [Upload]", "/cu log uploadLatestLog"));
//        latestlogText.append(CommandUtils.createCopyComponent(" [Copy]", LogHandler.getFileAsStringFromPath(LogHandler.latestlog)));
//        ctx.getSource().sendSuccess(latestlogText, true);
//        Path crashreport = LogHandler.getLatestCrashReportPath();
//        if (crashreport != null) {
//            MutableComponent crashreporttext = new TextComponent("Latest Crash Report ").withStyle(ChatFormatting.RED).append(new TextComponent(LogHandler.getRelativePathDateInMin(crashreport) + " Minutes old").withStyle(ChatFormatting.AQUA));
//            crashreporttext.append(CommandUtils.getCommandTextComponent(" [Upload]", "/cu log uploadCrashReport"));
//            crashreporttext.append(CommandUtils.createCopyComponent(" [Copy]", LogHandler.getFileAsStringFromPath(crashreport)));
//            ctx.getSource().sendSuccess(crashreporttext, true);
//        }
//        Path archivedLog = LogHandler.getLatestArchivedLogPath();
//        if (archivedLog != null) {
//            MutableComponent archivedText = new TextComponent("Archived Latest.log ").withStyle(ChatFormatting.GREEN).append(new TextComponent(LogHandler.getRelativePathDateInMin(archivedLog) + " Minutes old").withStyle(ChatFormatting.AQUA));
//            archivedText.append(CommandUtils.getCommandTextComponent(" [Upload]", "/cu log uploadArchivedLog"));
//            archivedText.append(CommandUtils.createCopyComponent(" [Copy]", LogHandler.getStringFromArchive(archivedLog)));
//            ctx.getSource().sendSuccess(archivedText, true);
//        }
//        return 1;
//
//    }
//}
