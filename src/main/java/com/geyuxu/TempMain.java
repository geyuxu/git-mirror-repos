package com.geyuxu;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TempMain {
    public static final int SUCCESS = 0;            // 表示程序执行成功

    public static final String SUCCESS_MESSAGE = "程序执行成功！";

    public static final String ERROR_MESSAGE = "程序执行出错：";


    static String yamlUrl = "/Volumes/JS_W_500/bare-repo/repo.yml";
    //static String repoBasePath = "/Volumes/JS_W_500/bare-repo/";
    static String repoBasePath = "/Users/geyuxu/neo-repo/";

    static boolean isMirror = false;
    public static void main(String[] args) {

        Yaml yaml = new Yaml();
        File file = new File(yamlUrl);
        try {
            InputStream is = new FileInputStream(file);
            Map<String, Object> obj  = yaml.load(is);
            eachYaml(obj);
            System.out.println("完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static Stack<String> pathStack = new StringStack();
    static void eachYaml(Map<String, Object> obj) throws IOException{

        for(String key:obj.keySet()){
            pathStack.push(key+"/");
            String path = repoBasePath + pathStack.stream().collect(Collectors.joining());
            //System.out.println(path);
            Object value = obj.get(key);
            if(!Files.exists(Paths.get(path))){
                File dir = new File(path);
                System.out.println("mkdir "+path);
                dir.mkdir();
            }
            if(value instanceof Map) {
                eachYaml((Map) value);
            }else if(value instanceof List){
                ((List) value).forEach(a->{
                    String gitPath=((String)a);
                    String gitDir;
                    if(isMirror) {
                        gitDir = gitPath.substring(gitPath.lastIndexOf('/') + 1);
                    }else{
                        gitDir = gitPath.substring(gitPath.lastIndexOf('/') + 1,gitPath.lastIndexOf('.'));
                    }
                    String gitFullDir = path+gitDir;
                    //System.out.println("git dir:"+gitDir);
                    if(Files.exists(Paths.get(gitFullDir))){
                        //git remote update
                        try {
                            System.out.println("## IN PATH:"+gitFullDir);
                            System.out.println("git remote update");
                            Process process = Runtime.getRuntime().exec("git remote update",null,new File(gitFullDir));
                            readProcessOutput(process);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //System.out.println("fetch ed");
                    }else{
                        //git clone --mirror
                        try {
                            System.out.println("## IN PATH:"+path);

                            String command;
                            if(isMirror){
                                command = "git clone --mirror "+ gitPath;
                            }else{
                                command = "git clone "+ gitPath;
                            }

                            System.out.println(command);
                            Process process = Runtime.getRuntime().exec(command,null,new File(path));
                            readProcessOutput(process);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //System.out.println("clone ed");
                    }
                });
                //pathStack.pop();
            }
            pathStack.pop();
        }
    }

    /**
     * 打印进程输出
     *
     * @param process 进程
     */
    private static void readProcessOutput(final Process process) {
        // 将进程的正常输出在 System.out 中打印，进程的错误输出在 System.err 中打印
        read(process.getInputStream(), System.out);
        read(process.getErrorStream(), System.err);

        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (exitCode == SUCCESS) {
            System.out.println(SUCCESS_MESSAGE);
        } else {
            System.err.println(ERROR_MESSAGE + exitCode);
        }
    }

    // 读取输入流
    private static void read(InputStream inputStream, PrintStream out) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printProcessBack(Process p) {
        BufferedReader br = new BufferedReader(new InputStreamReader(p
                .getInputStream()));
        StringBuffer sb = new StringBuffer();
        String inline = null;
        while (true) {
            try {
                if (!(null != (inline = br.readLine()))) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            sb.append(inline).append("\n");
        }
        System.out.println(sb.toString());
    }
}
