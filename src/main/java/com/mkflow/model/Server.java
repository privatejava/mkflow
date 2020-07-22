/*
 * Copyright 2020 Mkflow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.mkflow.model;


import com.jcraft.jsch.*;
import com.mkflow.utils.JschFileTransfer;
import com.mkflow.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Server<T> implements ProvisionerFactory<T> {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    protected List<BuildSpecParser> buildspecParsers;

    private Cloud cloud;

    private Container container;

    private Build build;

    private Codebase codebase;

    private Buildspec buildspec;

    private File outputFile;

    private File sourceFile;

    private Path workDir;

    private List<Channel> channels;

    private String uniqueId;

    private Long created;

    protected Server() {
        addBuildspecParser(new BuildSpecParser());
        channels = new ArrayList<>();
        created = System.currentTimeMillis();
    }

    public void init(String key) {
        uniqueId = key;
        workDir = Path.of(System.getProperty("java.io.tmpdir") + "/" + key);
        sourceFile = workDir.resolve("codebase").toFile();
        sourceFile.mkdir();
        outputFile = workDir.resolve("output.log").toFile();
    }

    public void init() throws IOException {
        uniqueId = UUID.randomUUID().toString();
        File f = new File(System.getProperty("java.io.tmpdir") + "/" + uniqueId);
        f.mkdir();
        workDir = f.toPath();
        sourceFile = workDir.resolve("codebase").toFile();
        sourceFile.mkdir();
        outputFile = workDir.resolve("output.log").toFile();
        outputFile.createNewFile();
    }

    public List<BuildSpecParser> getBuildspecParser() {
        if (buildspecParsers == null) {
            buildspecParsers = new ArrayList<>();
        }
        return buildspecParsers;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public File getSourceFile() {
        return sourceFile;
    }


    public abstract ProvisionerFactory<T> getProvisioner();

    public abstract void connect() throws JSchException;

    public abstract Session getSession() throws JSchException;

    public abstract Session getCurrentSession();

    public Cloud getCloud() {
        return cloud;
    }

    public void setCloud(Cloud cloud) {
        this.cloud = cloud;
    }

    public Buildspec getBuildspec() {
        return buildspec;
    }


    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }


    public void addBuildspecParser(BuildSpecParser parser) {
        getBuildspecParser().add(parser);
    }

    public Logger getLog() {
        return log;
    }

    public Path getWorkDir() {
        return workDir;
    }

    public Codebase getCodebase() {
        return codebase;
    }

    public void setCodebase(Codebase codebase) {
        this.codebase = codebase;
    }

    public File getOutputFile() {
        return outputFile;
    }


    public void loadBuildspec(String file) throws Exception {


//         if (codebase != null && (codebase.getUri().toString().startsWith("git")
//            || codebase.getUri().toString().startsWith("http") || codebase.getUri().toString().startsWith("ftp")
//        )) {
//            Git git = Git.cloneRepository()
//                .setURI(codebase.getUri().toString())
//                .setDirectory(sourceFile)
//                .setBranchesToClone(Arrays.asList("refs/heads/develop"))
//                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(codebase.getParam().getDetail().getUsername(), codebase.getParam().getDetail().getPassword()))
//                .call();
//
//            Utils.listFiles(sourceFile.getAbsolutePath(),false);
//            for (BuildSpecParser parser : getBuildspecParser()) {
//                try {
//                    this.buildspec = parser.parse(sourceFile.toPath().resolve(file).toString());
//                } catch (Exception ex) {
//
//                }
//                if (getBuildspec() != null) {
//                    break;
//                }
//            }
//        }else if (file != null && (file.startsWith("./") || file.startsWith("/") || file.startsWith("file://") || file.matches("[a-zA-Z]\\:"))) {
//             for (BuildSpecParser parser : getBuildspecParser()) {
//                 try {
//                     this.buildspec = parser.parse(file);
//                 } catch (Exception ex) {
//
//                 }
//                 if (getBuildspec() != null) {
//                     break;
//                 }
//             }
//         }
    }

    //    public void loadBuildspec(InputStream is) {
//        for (BuildSpecParser parser : getBuildspecParser()) {
//            try {
//                this.buildspec = parser.parse(is);
//            } catch (Exception ex) {
//
//            }
//            if (getBuildspec() != null) {
//                break;
//            }
//        }
//    }
    public void loadBuildspec(Buildspec buildspec) {
        this.buildspec = buildspec;
    }

    public void loadBuildspec() {
        if (getBuild().getCommands() != null) {
            if (getBuild().getCommands().size() == 1) {
                String file = getBuild().getCommands().get(0);
                for (BuildSpecParser parser : getBuildspecParser()) {
                    try {
                        this.buildspec = parser.parse(sourceFile.toPath().resolve(file).toString());
                    } catch (Exception ex) {
//                        ex.printStackTrace();
                    }
                    if (getBuildspec() != null) {
                        break;
                    }
                }
            } else {
                this.buildspec = getBuildspecParser().get(0).parse(getBuild().getCommands());
            }
        }
    }

    public CompletableFuture<T> provision() throws Exception {
        loadBuildspec();
        if (this.getBuildspec() == null) {
            throw new Exception("Invalid buildspec data provided");
        }
        if (getContainer() == null || getContainer().getAuth() == null || getContainer().getAuth().getType() == null) {
            throw new Exception("Invalid auth method provided data provided");
        }
        if (getCloud() == null || getCloud().getProvision() == null || getCloud().getProvision().getType() == null) {
            throw new Exception("Invalid cloud provision type provided");
        }
        switch (getCloud().getProvision().getType()) {
            case DEMAND:
                return onDemandProvision(this);
            case MARKET:
                return marketProvision(this);
            case ON_PREMISE:
                return onPremiseProvision(this);
            default:
                throw new UnsupportedOperationException();
        }
    }


    public void waitUntilChannelTimeout(Channel channel) throws InterruptedException {
        long timeout = 300000L;
        while (timeout > 0L) {
            if (channel.isClosed()) {
                break;
            }
            Thread.sleep(1000L);
            timeout -= 1000L;
        }
    }

    /**
     * Executes the Commands in sequential order of the Phase.
     *
     * @param phase
     * @return
     * @throws JSchException
     * @throws InterruptedException
     */
    protected boolean executeComands(Phase phase) throws JSchException, InterruptedException {
        boolean success = true;
        if (phase == null || phase.getCommands() == null || phase.getCommands().isEmpty()) {
            return success;
        }
        for (Command command : phase.getCommands()) {
            ChannelExec channel = (ChannelExec) getSession().openChannel("exec");
            Utils.getExecutorService().submit(() -> {
                PipedOutputStream out = new PipedOutputStream();
                try (PipedInputStream is = new PipedInputStream(out)) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    channel.setOutputStream(out);
                    channel.setErrStream(out);

                    String line = null;
                    while ((line = br.readLine()) != null) {
                        writeLog(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            ByteArrayInputStream is = new ByteArrayInputStream(command.getCommand().getBytes());
            channel.setPty(true);
            String s = "'\"'\"'";
            channel.setCommand("sudo su -c '" + command.getCommand().replace("'", s) + "'");
            channel.setInputStream(is);


            writeLog("#>" + command.getCommand());

            channel.connect(300000);

            waitUntilChannelTimeout(channel);
            if (channel.getExitStatus() != 0) {
                success = false;
                break;
            }
            channel.disconnect();
        }
        return success;
    }

    private void buildstats() {
        Duration diff = Duration.of((System.currentTimeMillis() - created), ChronoUnit.MILLIS);
        writeLog("Build Started:\t\t" + new Date(System.currentTimeMillis()));
        writeLog("Total Build Time:\t\t" + String.format("%02d:%02d", diff.toMinutesPart(), diff.toSecondsPart()));
    }

    private void writeLog(String msg) {
        String formattedMsg = String.format("[%s:%d]:%s",uniqueId, new Date().getTime(), msg);
        log.debug("{}", formattedMsg);
        try {
            Files.write(outputFile.toPath(), (formattedMsg + "\n").getBytes("utf-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the commands in separate execution which does not remembers the last state of the commands.
     *
     * @throws JSchException
     * @throws InterruptedException
     * @throws IOException
     */
    protected CompletableFuture<Boolean> executeCommands() throws JSchException, InterruptedException, IOException {
        CompletableFuture<Boolean> completed = new CompletableFuture<>();
        if (getBuildspec() == null) {
            completed.completeExceptionally(new Exception("Invalid buildspec provided"));
            return completed;
        }
        Utils.getExecutorService().submit(() -> {
            Exception reason = null;
            boolean success = true;
            try {
                writeLog("SOURCE =========");
                success = downloadCache();
                success = copyCodebase();
                if (success) {
                    writeLog("INSTALL =========");
                    success = executeComands(getBuildspec().getInstall());
                }
                if (success) {
                    writeLog("PRE BUILD =========");
                    success = executeComands(getBuildspec().getPreBuild());
                }
                if (success) {
                    writeLog("BUILD =========");
                    success = executeComands(getBuildspec().getBuild());
                }
                if (success) {
                    writeLog("POST BUILD =========");
                    success = executeComands(getBuildspec().getPostBuild());
                }

//                if (success) {
                writeLog("UPLOAD ARTIFACTS =========");
                success = uploadCache();
//                }


            } catch (Exception e) {
                reason = e;
                e.printStackTrace();
                completed.completeExceptionally(e);
            } finally {
                if (success) {
                    writeLog("Build Succedded");
                } else {
                    StringWriter trace = new StringWriter();
                    PrintWriter pw = new PrintWriter(trace);
                    reason.printStackTrace(pw);
                    writeLog("Build Failed: " + reason.getMessage());
                    writeLog(trace.toString());
                }
                buildstats();
                completed.complete(success);

            }

        });
        return completed;
    }

    public CompletableFuture<Boolean> execute(boolean isolate) throws JSchException, InterruptedException, IOException {
        if (isolate) {
            return executeCommands();
        } else {
            return executeWithTTY();
        }
    }

    protected boolean downloadCache() throws Exception {
        if (getBuild().getCache() != null && getBuildspec().getCache() != null &&
            getBuildspec().getCache().getPaths() != null && getBuildspec().getCache().getPaths().size() > 0) {
            switch (getBuild().getCache().getType()) {
                case S3:
                    String source = getBuild().getCache().getLocation();
                    S3CacheProvider provider = new S3CacheProvider(getSession());
                    List<String> paths = getBuildspec().getCache().getPaths();
                    provider.download(source, paths.get(0));
            }
        }
        return true;

    }

    protected boolean uploadCache() throws Exception {
        if (getBuild().getCache() != null && getBuildspec().getCache() != null &&
            getBuildspec().getCache().getPaths() != null && getBuildspec().getCache().getPaths().size() > 0) {
            switch (getBuild().getCache().getType()) {
                case S3:
                    String target = getBuild().getCache().getLocation();
                    S3CacheProvider provider = new S3CacheProvider(getSession());
                    List<String> paths = getBuildspec().getCache().getPaths();
                    provider.upload(paths.get(0), target);
            }
        }
        if (getBuild().getCache() != null && getBuildspec().getArtifacts() != null &&
                getBuildspec().getArtifacts().getFiles() != null && getBuildspec().getArtifacts().getFiles().size() > 0) {
            switch (getBuild().getCache().getType()) {
                case S3:
                    String target = getBuild().getCache().getLocation();
                    S3CacheProvider provider = new S3CacheProvider(getSession());
                    List<String> paths = getBuildspec().getArtifacts().getFiles();
                    provider.upload(paths.get(0), target);
            }
        }
        return true;

    }

    protected boolean copyCodebase() throws JSchException {
        boolean success = false;

        try {
            ChannelExec execChannel = (ChannelExec) getSession().openChannel("exec");
            File zipFile = workDir.resolve("codebase.zip").toFile();
            if (zipFile.exists()) {
                Utils.zip(sourceFile.getAbsolutePath(), zipFile.getAbsolutePath());
                log.debug("Copying files {} -> {}", getSession(), sourceFile, "server");
                JschFileTransfer.uploadFile(getSession(), zipFile, ".");
                log.debug("Copying complete");
                execChannel.setCommand("unzip -qo codebase.zip && rm  -rf codebase.zip");
                execChannel.connect(300000);
                waitUntilChannelTimeout(execChannel);
                if (execChannel.getExitStatus() != 0) {
                    success = false;
                } else {
                    success = true;
                }
            } else {
                success = true;
            }
            execChannel.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            throw new JSchException(e.toString());
        }
        return success;


    }

    protected boolean executeTTYCommands(Channel ch, PrintStream commander, BufferedReader reader, Phase phase) throws InterruptedException, ExecutionException {
        if (phase == null || phase.getCommands() == null || phase.getCommands().isEmpty()) {
            return true;
        }
        for (Command c : phase.getCommands()) {
            CommandResult result = getResult(ch, c, commander, reader).get();
//            writeLog(result.getResult());
            if (result.getReturnCode() != 0) {
                return false;
            }
            writeLog("----------------------");
        }
        return true;
    }

    protected CompletableFuture<CommandResult> getResult(Channel channel, Command c, PrintStream commander, BufferedReader reader) {
        CompletableFuture<CommandResult> completableFuture = new CompletableFuture<>();
        final StringBuilder builder = new StringBuilder();
        final StringBuilder line = new StringBuilder();
        final boolean[] isPrompt = new boolean[1];
        Utils.getExecutorService().submit(() -> {
//            String line = null;

            try {
                char toAppend = ' ';
                int i = 0;
                commander.println(c.getCommand());
                while ((i = reader.read()) != -1 && !completableFuture.isDone()) {
                    toAppend = (char) i;
                    if (toAppend == '\n' || toAppend == '\r') {
//                        log.debug("LN: {}",line.toString());
                        if (line.toString().startsWith("RET:")) {
                            CommandResult result = new CommandResult();
                            String[] lines = builder.toString().split("\n");
                            if (lines.length > 1) {
                                result.setResult(Stream.of(lines).limit(lines.length - 1).collect(Collectors.joining("\n")));
                            } else {
                                result.setResult(builder.toString());
                            }
                            result.setReturnCode(Integer.parseInt(line.toString().replace("RET:", "")));
                            completableFuture.complete(result);
                            break;
                        } else if (line.length() > 0) {
                            builder.append(line.toString() + "\n");
                            if (!isPrompt[0]) {
                                writeLog(line.toString());
                            }
                        }
                        line.setLength(0);

                    } else {
//                        log.debug("Appending.. {}",toAppend);
                        line.append(toAppend);
//                        log.debug("LN:{}",line.toString());
                    }
                }
            } catch (Exception ex) {
//                ex.printStackTrace();
                completableFuture.completeExceptionally(ex);
            }

        });
        Utils.getExecutorService().submit(() -> {
            isPrompt[0] = false;
            List<String> lastTwoCommands = new ArrayList<>(2);
            while (!completableFuture.isDone()) {
                try {
                    Thread.sleep(500);
                    lastTwoCommands.add(line.toString());
                    if (lastTwoCommands.size() == 2) {
                        if (lastTwoCommands.get(0).equalsIgnoreCase(lastTwoCommands.get(1)) &&
                            lastTwoCommands.get(0).length() > 0 &&
                            lastTwoCommands.get(0).charAt(lastTwoCommands.get(0).length() - 1) == 32) {
                            isPrompt[0] = true;
                            commander.println("echo RET:$?");
                        } else {
                            lastTwoCommands.clear();
                        }
                    }
                    if (channel.getExitStatus() > 0) {
                        log.debug("Completing...");
                        CommandResult result = new CommandResult();
                        String[] lines = builder.toString().split("\n");
                        if (lines.length > 2) {
                            result.setResult(Stream.of(lines).limit(lines.length - 2).collect(Collectors.joining("\n")));
                        } else {
                            result.setResult(builder.toString());
                        }
                        result.setReturnCode(channel.getExitStatus());
                        completableFuture.complete(result);
                    }
                    if (channel.isClosed()) {
                        log.debug("Closing Streams...");
                        commander.close();
                        reader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        return completableFuture;

    }

    protected CompletableFuture<Boolean> executeWithTTY() throws JSchException, InterruptedException, IOException {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        ChannelShell channel = (ChannelShell) getSession().openChannel("shell");
        channel.setPtySize(1000, 10000, 1000, 10000);
        channel.setPtyType("dumb");
        channel.setPty(true);
        channels.add(channel);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();


        PipedOutputStream stream = new PipedOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new PipedInputStream(stream)));
        OutputStream inputstream_for_the_channel = channel.getOutputStream();
        PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
        channel.setOutputStream(stream, true);
        if (!channel.isConnected()) {
            channel.connect(300000);
        }

        Utils.getExecutorService().submit(new Thread() {
            public void run() {
                boolean success = true;
                Exception reason = null;
                try {
                    writeLog("SOURCE =========");
                    success = downloadCache();
                    success = copyCodebase();
                    if (success) {
                        writeLog("INSTALL =========");
                        success = executeTTYCommands(channel, commander, reader, buildspec.getInstall());
                    }
                    if (success) {
                        writeLog("PRE BUILD =========");
                        success = executeTTYCommands(channel, commander, reader, buildspec.getPreBuild());
                    }
                    if (success) {
                        writeLog("BUILD =========");
                        success = executeTTYCommands(channel, commander, reader, buildspec.getBuild());
                    }
                    if (success) {
                        writeLog("POST BUILD =========");
                        success = executeTTYCommands(channel, commander, reader, buildspec.getPostBuild());
                    }

                    writeLog("UPLOAD ARTIFACTS =========");
                    success = uploadCache();
                } catch (Exception e) {
                    reason = e;
                    e.printStackTrace();
                } finally {
                    if (success) {
                        writeLog("Build Succedded");
                    } else {
                        StringWriter trace = new StringWriter();
                        PrintWriter pw = new PrintWriter(trace);
                        reason.printStackTrace(pw);
                        writeLog("Build Failed: " + reason.getMessage());
                        writeLog(trace.toString());
                    }
                    buildstats();
                    completableFuture.complete(success);
                    System.out.println("Completing..");
                }
            }
        });
        Utils.getExecutorService().submit(new Thread() {
            public void run() {
                while (true && channel.isConnected()) {
//                    int i = -1;
//                    try {
//                        if ((i = inputStream.read()) != -1) {
//                            System.out.print((char)i);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    if ((out.size() > 0)) {
                        System.out.println(new String(out.toByteArray()) + new String(err.toByteArray()));
                        out.reset();
                        err.reset();

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
                log.debug("{}", channel.getExitStatus());

            }
        });
        return completableFuture;
    }
}
