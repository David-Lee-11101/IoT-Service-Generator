package com.applet.mpinterface.service;

import com.applet.mpinterface.framework.SpringGenerator;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@Ignore("Legacy Windows integration experiment; requires external services and local tool paths")
public class IRundetailsServiceTest {

    @Autowired
    private IRundetailsService rundetailsService;

    @Autowired
    private SpringGenerator generator;
    @Autowired
    private ISpecificGenerator specificGenerator;

    // 生成 Spring 配置
    @Test
    public void generateAppletSpring() throws TemplateException, IOException {
        generateSpring("appletA", 9090);
        generateSpring("appletB", 9091);
    }

    // 生成微服务代码
    @Test
    public void generateAppletFunction() {
        generateFunction("appletA");
        generateFunction("appletB");
    }

    // 实验次数
    private int appletNum = 1;

    @Test
    public void generateAppletTimeCountExperiment() throws IOException, TemplateException, InterruptedException {
        runExperiment(appletNum);
    }

    public void runExperiment(int appletNum) throws IOException, TemplateException, InterruptedException {
        List<Long> generateTimes = new ArrayList<>();
        List<Long> startTimes = new ArrayList<>();
        List<Long> shutdownTimes = new ArrayList<>();

        for (int j = 1; j <= 10; j++) {
            // 生成阶段
            long generateTotalTime = generateAppletServices(appletNum);
            generateTimes.add(generateTotalTime);

            // 启动阶段
            long startupTotalTime = startupAppletServices(appletNum);
            startTimes.add(startupTotalTime);

            // 下线阶段
            long shutdownTotalTime = shutdownAppletServices(appletNum);
            shutdownTimes.add(shutdownTotalTime);

            System.out.println("===== Experiment " + j + " =====");
            System.out.println("Generate total time: " + generateTotalTime + " ms");
            System.out.println("Startup total time:  " + startupTotalTime + " ms");
            System.out.println("Shutdown total time: " + shutdownTotalTime + " ms");
            System.out.println("=====================");
        }

        // 打印统计数据
        printStatistics("Generate times", generateTimes);
        printStatistics("Startup times", startTimes);
        printStatistics("Shutdown times", shutdownTimes);
    }

    // 生成微服务
    private long generateAppletServices(int appletNum) throws IOException, TemplateException {
        long generateStartTime = System.currentTimeMillis();

        for (int i = 1; i <= appletNum; i++) {
            String appletName = "applet" + i;
            int port = 33000 + i;

            // 生成 Spring 配置
            generator.generateSpring("D:\\preStage\\IoTRPC\\" + appletName, appletName, port);

            // 生成服务代码
            specificGenerator.generateAllDetailgetterCode("dWpBihQN", 1702953840,
                    "D:\\preStage\\IoTRPC\\" + appletName + "\\src\\main\\java\\com\\" + appletName + "\\mpinterface",
                    appletName);
        }

        return System.currentTimeMillis() - generateStartTime;
    }

    // 启动微服务
    private long startupAppletServices(int appletNum) throws IOException {
        long startupStartTime = System.currentTimeMillis();

        for (int i = 1; i <= appletNum; i++) {
            String appletName = "applet" + i;
            compileAndPackageAndRunJar(appletName, 33000 + i);
        }

        return System.currentTimeMillis() - startupStartTime;
    }

    // 下线微服务
    private long shutdownAppletServices(int appletNum) throws IOException, InterruptedException {
        long shutdownStartTime = System.currentTimeMillis();

        for (int i = 1; i <= appletNum; i++) {
            String appletName = "applet" + i;

            String batFilePath = "D:\\preStage\\IoTRPC\\" + appletName + "\\kill-" + appletName + ".bat"; // 批处理文件路径
            generateKillBatchFile(batFilePath, appletName);

            try {
                String[] cmd = {"C:\\Windows\\System32\\cmd.exe", "/c", "start", "", batFilePath};
                Runtime.getRuntime().exec(cmd);
                System.out.println("通过 cmd.exe /c kill 删除jar进程。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return System.currentTimeMillis() - shutdownStartTime;
    }

    // 编译并启动 JAR 文件
    public long compileAndPackageAndRunJar(String appletName, int port) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("D:\\apache-maven-3.9.0\\bin\\mvn.cmd", "clean", "install");
        processBuilder.directory(new File("D:\\preStage\\IoTRPC\\" + appletName));

        long startTime = System.currentTimeMillis();
        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Maven build and package successful for " + appletName);
            } else {
                System.err.println("Maven build failed with exit code " + exitCode + " for " + appletName);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String batFilePath = "D:\\preStage\\IoTRPC\\" + appletName + "\\start-" + appletName + ".bat";
        generateBatchFile(batFilePath, appletName);

        try {
            String[] cmd = {"C:\\Windows\\System32\\cmd.exe", "/c", "start", "", batFilePath};
            Runtime.getRuntime().exec(cmd);
            System.out.println("通过 cmd.exe /c start 启动批处理文件，模拟桌面点击。");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!isServiceHealthy("127.0.0.1", port)) {
            System.out.println("等待服务启动...");
        }
        System.out.println("Java process start successful " + appletName);

        return System.currentTimeMillis() - startTime;
    }

    // 生成健康检查方法
    public boolean isServiceHealthy(String host, int port) {
        try {
            URL url = new URL("http://" + host + ":" + port + "/actuator/health");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);

            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (IOException e) {
            return false;
        }
    }

    // 打印统计数据
    public void printStatistics(String label, List<Long> times) {
        long minTime = times.stream().mapToLong(Long::longValue).min().orElse(0);
        long maxTime = times.stream().mapToLong(Long::longValue).max().orElse(0);
        double avgTime = times.stream().mapToLong(Long::longValue).average().orElse(0);

        System.out.println(label + " - Minimum time: " + minTime + " ms");
        System.out.println(label + " - Maximum time: " + maxTime + " ms");
        System.out.println(label + " - Average time: " + avgTime + " ms");
    }

    // 生成批处理文件内容
    private void generateBatchFile(String batFilePath, String appletName) throws IOException {
        File batFile = new File(batFilePath);
        try (FileWriter writer = new FileWriter(batFile)) {
            String command = "D:\\Java8\\bin\\java.exe -jar D:\\preStage\\IoTRPC\\" + appletName + "\\target\\" + appletName + "-0.0.1-SNAPSHOT.jar";
            writer.write(command);
        }
    }

    // 生成批处理文件内容（用于 kill 进程）
    private void generateKillBatchFile(String batFilePath, String appletName) throws IOException {
        File batFile = new File(batFilePath);
        String jarName = appletName + "-0.0.1-SNAPSHOT.jar";
        String command = "@echo off\r\n"
                + "for /f \"tokens=1\" %%i in ('jps -l ^| find \"" + jarName + "\"') do taskkill /F /PID %%i\r\n";
        try (FileWriter writer = new FileWriter(batFile)) {
            writer.write(command);
        }
    }

    // 生成 Spring 配置文件
    private void generateSpring(String appletName, int port) throws TemplateException, IOException {
        generator.generateSpring("D:\\preStage\\IoTRPC\\" + appletName, appletName, port);
    }

    // 生成服务代码
    private void generateFunction(String appletName) {
        specificGenerator.generateAllDetailgetterCode("dWpBihQN", 1702953840,
                "D:\\preStage\\IoTRPC\\" + appletName + "\\src\\main\\java\\com\\" + appletName + "\\mpinterface", appletName);
    }

    // 生成消费者代码
    private String generateConsumerCode() {
        return "@RemoteCallReference\n" +
                "private IappletAService appletAService;\n" +
                "\n" +
                "@GetMapping(\"SwitchName\")\n" +
                "public String SwitchName() {\n" +
                "return appletAService.SwitchName(\"dWpBihQN\", 1702953840);\n" +
                "}";
    }

    // ----------------- 新增：内存占用实验（预生成、预编译） -----------------

    /**
     * 新的内存占用实验：
     * 1. 一次性生成 50 个微服务（applet1～applet50）
     * 2. 编译生成所有微服务的 jar 文件（预编译，一次性完成）
     * 3. 根据指定数量启动微服务（采用最小内存参数： -Xms16m -Xmx64m ），并统计内存占用
     * 4. 下线操作不在实验中自动调用，由人工关闭
     */
    @Test
    public void testMemoryUsageExperimentPrecompiled() throws IOException, TemplateException, InterruptedException {
//        int totalMicroservices = 50;
//        // 第一步：生成 50 个微服务
//        System.out.println("开始生成 " + totalMicroservices + " 个微服务...");
//        generateAppletServices(totalMicroservices);
//        System.out.println("生成完成。");
//
//        // 第二步：预编译所有微服务（一次性编译生成 jar 文件）
//        for (int i = 1; i <= totalMicroservices; i++) {
//            String appletName = "applet" + i;
//            compileAndPackageJarMinMemory(appletName);
//        }
//        System.out.println("所有微服务编译完成。");

        // 第三步：启动指定数量的微服务（例如：启动 20 个）
        int microservicesToStart = 20; // 可根据需要调整
        System.out.println("启动 " + microservicesToStart + " 个微服务（最小内存模式）...");
        for (int i = 1; i <= microservicesToStart; i++) {
            String appletName = "applet" + i;
            startExistingJarMinMemory(appletName, 33000 + i);
        }

        // 等待所有服务启动并稳定
        for (int i = 1; i <= microservicesToStart; i++) {
            int port = 33000 + i;
            while (!isServiceHealthy("127.0.0.1", port)) {
                System.out.println("等待 applet" + i + " 启动...");
                Thread.sleep(1000);
            }
        }
        System.out.println("所有服务已启动，等待稳定...");
        Thread.sleep(10000);

        // 第四步：统计内存占用（单位：MB）
        long totalBytes = getTotalMicroserviceMemoryUsage();
        double totalMB = totalBytes / (1024.0 * 1024.0);
        System.out.printf("启动 %d 个微服务时，总内存占用：%.2f MB%n", microservicesToStart, totalMB);

        /// 自动关闭所有启动的微服务
        System.out.println("开始自动关闭所有微服务...");
        shutdownAppletServices(microservicesToStart);
        System.out.println("所有微服务已关闭。");
    }

    /**
     * 预编译微服务：仅编译并生成 jar 文件，不启动服务
     */
    private void compileAndPackageJarMinMemory(String appletName) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("D:\\apache-maven-3.9.0\\bin\\mvn.cmd", "clean", "install");
        processBuilder.directory(new File("D:\\preStage\\IoTRPC\\" + appletName));
        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("预编译成功：" + appletName);
            } else {
                System.err.println("预编译失败，exitCode=" + exitCode + "：" + appletName);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动已预编译好的微服务（最小内存模式）：仅启动 jar 文件，不再编译
     * 启动命令中增加 -Xms16m -Xmx64m 参数
     */
    private void startExistingJarMinMemory(String appletName, int port) throws IOException {
        String batFilePath = "D:\\preStage\\IoTRPC\\" + appletName + "\\start-" + appletName + ".bat";
        generateMinMemoryBatchFile(batFilePath, appletName);
        try {
            String[] cmd = {"C:\\Windows\\System32\\cmd.exe", "/c", "start", "", batFilePath};
            Runtime.getRuntime().exec(cmd);
            System.out.println("启动 " + appletName + "（最小内存模式）。");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 等待当前服务启动
        while (!isServiceHealthy("127.0.0.1", port)) {
            System.out.println("等待 " + appletName + " 启动...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println(appletName + " 启动成功。");
    }

    /**
     * 生成最小内存模式下的启动批处理文件（启动脚本）
     * 启动命令中增加参数：-Xms16m -Xmx64m
     */
    private void generateMinMemoryBatchFile(String batFilePath, String appletName) throws IOException {
        File batFile = new File(batFilePath);
        try (FileWriter writer = new FileWriter(batFile)) {
            String command = "D:\\Java8\\bin\\java.exe -Xms8m -Xmx32m -jar D:\\preStage\\IoTRPC\\"
                    + appletName + "\\target\\" + appletName + "-0.0.1-SNAPSHOT.jar";
            writer.write(command);
        }
    }

    /**
     * 使用 WMIC 查询所有匹配微服务的内存占用（WorkingSetSize，单位为字节），并汇总返回
     */
    private long getTotalMicroserviceMemoryUsage() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("wmic", "process", "where",
                "CommandLine like '%applet%-0.0.1-SNAPSHOT.jar%'", "get", "WorkingSetSize");
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        long totalBytes = 0;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.equalsIgnoreCase("WorkingSetSize")) continue;
            try {
                long bytes = Long.parseLong(line);
                totalBytes += bytes;
            } catch (NumberFormatException e) {
                // 忽略无法解析的行
            }
        }
        reader.close();
        return totalBytes;
    }

}
