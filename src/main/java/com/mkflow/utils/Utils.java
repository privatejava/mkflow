package com.mkflow.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private static ExecutorService service = Executors.newCachedThreadPool();

    public static ExecutorService getExecutorService() {
        if (service.isShutdown() || service.isTerminated()) {
            service = Executors.newCachedThreadPool();
        }
        return service;
    }

    public static ObjectMapper mapper() {
        return new ObjectMapper();
    }

    public static String listFiles(String file, boolean recursive) {
        StringBuilder builder = new StringBuilder();
        try (Stream<Path> walk = Files.walk(Paths.get(file), recursive ? 5 : 1)) {
            List<String> result = walk
                .map(x -> x.toString()).collect(Collectors.toList());
            result.forEach(f -> {
                builder.append(f);
                log.debug("{}", f);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static File findBobFlowFile(File dir) {
        try (Stream<Path> walk = Files.walk(dir.toPath(), 1)) {
            List<Path> result = walk
                .filter(p -> p.toFile().isFile())
                .filter(p -> p.toFile().getName().startsWith("mkflow"))
                .collect(Collectors.toList());
            log.debug("{}", result);
            if (result != null && !result.isEmpty()) {
                return result.get(0).toFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getWorkingDir() {
        String SEP = System.getProperty("file.separator");
        File f = new File(System.getProperty("java.io.tmpdir") + SEP + ".mkflow");
        if (!f.exists()) {
            boolean created = f.mkdirs();
            log.debug("Creating dir {}:{}", f.getAbsolutePath(), created);
            File ssh = new File(f.getAbsolutePath() + SEP + ".ssh");
            created = ssh.mkdirs();
            log.debug("Creating dir {}:{}", ssh.getAbsolutePath(), created);
        }
        return f;
    }

    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }

    public static void zip(String directory, String destination) {
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

//            if(password.length()>0){
//                parameters.setEncryptFiles(true);
//                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
//                parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
//                parameters.setPassword(password);
//            }

            ZipFile zipFile = new ZipFile(destination);

            File targetFile = new File(directory);
            if (targetFile.isFile()) {
                zipFile.addFile(targetFile, parameters);
            } else if (targetFile.isDirectory()) {
                for (File f : targetFile.listFiles()) {
                    if (f.isFile()) {
                        zipFile.addFile(f, parameters);
                    } else {
                        zipFile.addFolder(f, parameters);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setEnv(String name, String value) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.put(name, value);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            env.put(name, value);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.put(name, value);
                }
            }
        }
    }
//    public static void setEnv(String key, String value) {
//        try {
//            Map<String, String> env = System.getenv();
//            Class<?> cl = env.getClass();
//            Field field = cl.getDeclaredField("m");
//            field.setAccessible(true);
//            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
//            writableEnv.put(key, value);
//        } catch (Exception e) {
//            throw new IllegalStateException("Failed to set environment variable", e);
//        }
//    }

    public static <T> T getByPath(Object obj, String path,Class<T> returnType){
        String[] allPaths = path.split("\\.");
        boolean isLast = false;
        for(int i=0; i<allPaths.length; i++){
            String keyName = allPaths[i].replaceAll("\\[.*\\]","");
            if(obj instanceof Map && ((Map)obj).containsKey(keyName)){
                Object val = ((Map) obj).get(keyName);
                if (val instanceof List && allPaths[i].matches(".*\\[([0-9]+)\\]")){
                    Pattern compile = Pattern.compile(".*\\[([0-9]+)\\]");
                    Matcher m = compile.matcher(allPaths[i]);
                    if(m.matches()){
                        int index = Integer.parseInt(m.group(1));
                        val = ((List) val).get(index);
                    }else{
                        val = null;
                    }
                }

                if(i+1 == allPaths.length){
                    return (T)val;
                }else{
                    return (T)getByPath(val, Arrays.stream(allPaths).skip(i+1).collect(Collectors.joining(".")),returnType);
                }
            }else {
                return null;
            }
        }
        return null;
    }



    public static File getSshDir() {
        return getWorkingDir().toPath().resolve(".ssh").toFile();
    }


}
