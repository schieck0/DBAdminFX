package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javafx.beans.property.StringProperty;

public class SystemRuntime {

    private SystemRuntime() {
    }

    private static boolean exec(String command, boolean waitfor, boolean abortOnError, StringBuilder retorno, Integer timeOutSec) {
        try {
            Process proc = java.lang.Runtime.getRuntime().exec(command);

            StreamGobbler errorGobbler = new StreamGobbler(
                    proc.getErrorStream(), retorno != null ? retorno : System.out);
            StreamGobbler outputGobbler = new StreamGobbler(
                    proc.getInputStream(), retorno != null ? retorno : System.out);
            new Thread(errorGobbler).start();
            new Thread(outputGobbler).start();

            if (waitfor || retorno != null) {
                int exitVal = (timeOutSec == null ? proc.waitFor() : (proc.waitFor(timeOutSec, TimeUnit.SECONDS) ? 0 : 1));
                if (exitVal == 1 && abortOnError) {
                    throw new RuntimeException("ERRO durante a execução do comando: \n" + command);
                }
                return exitVal == 0;
            } else {
                return true;
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static boolean exec(String command, StringProperty retorno, String... envVars) {
        try {
            Process proc = java.lang.Runtime.getRuntime().exec(command, envVars);
            StreamGobbler errorGobbler = new StreamGobbler(
                    proc.getErrorStream(), retorno != null ? retorno : System.out);
            StreamGobbler outputGobbler = new StreamGobbler(
                    proc.getInputStream(), retorno != null ? retorno : System.out);
            new Thread(errorGobbler).start();
            new Thread(outputGobbler).start();

            if (retorno != null) {
                int exitVal = proc.waitFor();
                if (exitVal == 1) {
                    if (retorno != null) {
                        retorno.setValue(retorno.get() + "\nERRO durante a execução do comando: \n" + command + "\n");
                    } else {
                        throw new RuntimeException("ERRO durante a execução do comando: \n" + command);
                    }
                }
                return exitVal == 0;
            } else {
                return true;
            }
        } catch (InterruptedException | IOException e) {
            if (retorno != null) {
                retorno.setValue(retorno.get() + e.getMessage() + "\n");
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    public static boolean exec(String command, StringBuilder retorno, Charset charset, String... envVars) {
        try {
            Process proc = java.lang.Runtime.getRuntime().exec(command, envVars);
            StreamGobbler errorGobbler = new StreamGobbler(
                    proc.getErrorStream(), retorno != null ? retorno : System.out, charset);
            StreamGobbler outputGobbler = new StreamGobbler(
                    proc.getInputStream(), retorno != null ? retorno : System.out, charset);
            new Thread(errorGobbler).start();
            new Thread(outputGobbler).start();

            if (retorno != null) {
                int exitVal = proc.waitFor();
                if (exitVal == 1) {
                    if (retorno != null) {
                        retorno.append("\nERRO durante a execução do comando: \n" + command + "\n");
                    } else {
                        throw new RuntimeException("ERRO durante a execução do comando: \n" + command);
                    }
                }
                return exitVal == 0;
            } else {
                return true;
            }
        } catch (InterruptedException | IOException e) {
            if (retorno != null) {
                retorno.append(e.getMessage() + "\n");
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String exec(String command, File location) throws RuntimeException {
        try {
            Process proc = null;
            if (location != null) {
                proc = java.lang.Runtime.getRuntime().exec(command, null, location);
            } else {
                proc = java.lang.Runtime.getRuntime().exec(command);
            }
            StringBuilder retorno = new StringBuilder();
            StreamGobbler errorGobbler = new StreamGobbler(
                    proc.getErrorStream(), retorno);
            StreamGobbler outputGobbler = new StreamGobbler(
                    proc.getInputStream(), retorno);
            new Thread(errorGobbler).start();
            new Thread(outputGobbler).start();

            int exitVal = proc.waitFor();
            if (exitVal == 1) {
                throw new RuntimeException("ERRO durante a execução do comando: " + command + "\nDetalhes:\n" + retorno.toString());
            } else {
                return retorno.toString();
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String exec(String command) throws RuntimeException {
        return exec(command, (File) null);
    }

    public static boolean exec(String command, boolean abortOnError, StringBuilder retorno) throws RuntimeException {
        return exec(command, true, abortOnError, retorno, null);
    }

    public static boolean exec(String command, StringBuilder retorno, Integer timeOutSec) throws RuntimeException {
        return exec(command, true, false, retorno, timeOutSec);
    }

    public static boolean exec(String command, boolean waitfor, boolean abortOnError) throws RuntimeException {
        return exec(command, waitfor, abortOnError, null, null);
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getJavaArch() {
        return System.getProperty("os.arch").contains("64") ? "64" : "32";
    }

    public static String getOSArch() throws RuntimeException {
        try {
            String cmd = "";
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
//            cmd = System.getenv("SYSTEMROOT") + "\\System32\\wbem\\WMIC.exe os get osarchitecture";//n funciona em XP e Server 2003
                Process proc = Runtime.getRuntime().exec("systeminfo");
                BufferedReader reader = null;
                reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                proc.waitFor();

                if (sb.toString().toLowerCase().contains("x64-based")) {
                    return "64";
                } else if (sb.toString().toLowerCase().contains("x86-based")) {
                    return "32";
                } else {
                    throw new RuntimeException("Nao foi possivel capturar a arquitetura do SO.");
                }
            } else {
//            cmd = "file /bin/bash | cut -d' ' -f3";
                cmd = "file /bin/bash";
                Process proc = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = null;
                reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                proc.waitFor();

                if (sb.toString().replaceAll("(.*)((\\d{2})-bit)(.*)", "$2").contains("64")) {
                    return "64";
                } else if (sb.toString().replaceAll("(.*)((\\d{2})-bit)(.*)", "$2").contains("32")) {
                    return "32";
                } else {
                    throw new RuntimeException("Nao foi possivel capturar a arquitetura do SO.");
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isLocalAddress(String host) {
        try {
            String localhostName = InetAddress.getLocalHost().getHostName();

            if (localhostName.trim().toLowerCase().equals(host.trim().toLowerCase())) {
                return true;
            }

            Set<String> ips = new HashSet<>();
            for (InetAddress a : InetAddress.getAllByName(localhostName)) {
                ips.add(a.getHostAddress());
            }
            return ips.contains(host);
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    public static String getIPAddress() throws RuntimeException {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                return InetAddress.getLocalHost().getHostAddress();
            } else {
                NetworkInterface ni = NetworkInterface.getByName("eth0");
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                InetAddress iaddress;
                do {
                    iaddress = ias.nextElement();
                } while (!(iaddress instanceof Inet4Address));

                return iaddress.getHostAddress();
            }
        } catch (UnknownHostException | SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getCurrentPID() throws RuntimeException {
        try {
            java.lang.management.RuntimeMXBean runtime
                    = java.lang.management.ManagementFactory.getRuntimeMXBean();
            java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
            jvm.setAccessible(true);
            sun.management.VMManagement mgmt
                    = (sun.management.VMManagement) jvm.get(runtime);
            java.lang.reflect.Method pid_method
                    = mgmt.getClass().getDeclaredMethod("getProcessId");
            pid_method.setAccessible(true);
            return (Integer) pid_method.invoke(mgmt);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static int getPID() {
        return Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    }
}

class StreamGobbler implements Runnable {

    private InputStream inputStream;
    private Object out;
    private Charset charset;

    public StreamGobbler(InputStream is, Object os, Charset charset) {
        inputStream = is;
        out = os;
        this.charset = charset;
    }

    public StreamGobbler(InputStream is, Object os) {
        inputStream = is;
        out = os;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(SystemRuntime.isWindows()
                ? new InputStreamReader(inputStream, charset != null ? charset : StandardCharsets.ISO_8859_1) : new InputStreamReader(inputStream, charset != null ? charset : null))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (out instanceof PrintStream) {
                    PrintStream outputStream = (PrintStream) out;
                    outputStream.println(line + "\n");
                } else if (out instanceof StringBuilder) {
                    StringBuilder sBuilder = (StringBuilder) out;
                    sBuilder.append(line + "\n");
                } else if (out instanceof StringProperty) {
                    StringProperty s = (StringProperty) out;
                    s.set(line);
                }
            }
        } catch (IOException ioe) {
        }
    }
}
