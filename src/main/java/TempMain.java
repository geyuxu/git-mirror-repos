import com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.URIish;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TempMain {
    public static void main(String[] args) {
        String ymlUrl = "/Volumes/JS_W_500/bare-repo/repo.yml";

        Yaml yaml = new Yaml();
        File file = new File(ymlUrl);
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
            String path = "/Volumes/JS_W_500/bare-repo/"+pathStack.stream().collect(Collectors.joining());
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
                    String gitDir = gitPath.substring(gitPath.lastIndexOf('/')+1,gitPath.length());
                    String gitFullDir = path+gitDir;
                    //System.out.println("git dir:"+gitDir);
                    if(Files.exists(Paths.get(gitFullDir))){
                        //git remote update
                        try {
                            System.out.println("## IN PATH:"+gitFullDir);
                            System.out.println("git remote update");
                            Process p = Runtime.getRuntime().exec("git remote update",null,new File(gitFullDir));
                            printProcessBack(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //System.out.println("fetch ed");
                    }else{
                        //git clone --mirror
                        try {
                            System.out.println("## IN PATH:"+path);
                            System.out.println("git clone --mirror "+gitPath);
                            Process p = Runtime.getRuntime().exec("git clone --mirror "+gitPath,null,new File(path));
                            printProcessBack(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //System.out.println("clone ed");
                    }
                });
                pathStack.pop();
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
