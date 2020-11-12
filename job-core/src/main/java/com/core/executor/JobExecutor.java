package com.core.executor;

import com.core.bean.Admin;
import com.core.bean.Executor;
import com.core.bean.impl.ExecutorImpl;
import com.core.handler.IJobHandler;
import com.core.log.JobFileAppender;
import com.core.thread.JobLogFileCleanThread;
import com.core.thread.JobThread;
import com.core.thread.TriggerCallbackThread;
import com.cxl.rpc.remoting.invoker.RpcInvokerFactory;
import com.cxl.rpc.remoting.invoker.call.CallType;
import com.cxl.rpc.remoting.invoker.reference.RpcReferenceBean;
import com.cxl.rpc.remoting.invoker.route.LoadBalance;
import com.cxl.rpc.remoting.net.NetEnum;
import com.cxl.rpc.remoting.provider.RpcProviderFactory;
import com.cxl.rpc.serialize.Serializer;
import com.cxl.rpc.util.IpUtil;
import com.cxl.rpc.util.NetUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Data
public class JobExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobExecutor.class);

    //----------------param-----------------------
    private String adminAddresses;
    private String appName;
    private String ip;
    private int port;
    private String accessToken;
    private String logPath;
    private int logRetentionDays;

    //-------------------------start stop------------------------------
    public void start() throws Exception {
        //init logpath
        JobFileAppender.initLogPath(logPath);

        //init invoker, admin-client
        initAdminList(adminAddresses, accessToken);

        //init JobLogFileCallThread
        JobLogFileCleanThread.getIntance().start(logRetentionDays);

        //init TriggerCallbackThread
        TriggerCallbackThread.getInstance().start();

        //init executor-server
        port = port > 0 ? port : NetUtil.findAvailablePort(9999);
        ip = ip != null && ip.length() > 0 ? ip : IpUtil.getIp();
        initRpcProvider(ip, port, appName, accessToken);
    }

    public void destroy() {
        //destroy executor-server
        stopRpcProvider();

        //destory jobThreadRepository
        if (jobThreadRepository.size() > 0) {
            for (Map.Entry<Integer, JobThread> item : jobThreadRepository.entrySet()) {
                removeJobThread(item.getKey(),"web container destroy and kill the job");
            }
            jobThreadRepository.clear();
        }
        jobHandlerRepository.clear();

        //destroy JObLogFileThread
        JobLogFileCleanThread.getIntance().toStop();

        //destroy TriggerCallbackThread
        TriggerCallbackThread.getInstance().toStop();

        //destroy invoker
        stopInvokerFactory();
    }

    //----------------------------executor-server (rpc provider)---------
    private RpcProviderFactory rpcProviderFactory = null;

    private void initRpcProvider(String ip, int port, String appName, String accessToken) throws Exception {

        //init provider factory
        String address = IpUtil.getIpPort(port);
        Map<String, String> serviceRegistryParam = new HashMap<>();
        serviceRegistryParam.put("appName", appName);
        serviceRegistryParam.put("address", address);

        rpcProviderFactory = new RpcProviderFactory();
        rpcProviderFactory.initConfig(NetEnum.NETTY, Serializer.SerializerEnum.JACKSON.getSerializer(), ip, port, accessToken, ExecutorServiceRegistry.class, serviceRegistryParam);

        //add services
        rpcProviderFactory.addService(Executor.class.getName(), null, new ExecutorImpl());

        //start
        rpcProviderFactory.start();
    }

    public void stopRpcProvider() {
        //stop provider factory
        try {
            rpcProviderFactory.stop();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    // ---------------------- admin-client (rpc invoker) ----------------------
    private static List<Admin> adminList;
    private static Serializer serializer;

    public void initAdminList(String adminAddresses, String accessToken) {
        serializer = Serializer.SerializerEnum.JACKSON.getSerializer();
        if (adminAddresses != null && adminAddresses.length() > 0) {
            for (String address : adminAddresses.split(",")) {
                if (address != null && address.length() > 0) {
                    String addressUrl = address.concat(Admin.MAPPING);
                    Admin admin = (Admin) new RpcReferenceBean(NetEnum.NETTY, serializer, CallType.SYNC, LoadBalance.ROUND, Admin.class, null, 3000, addressUrl, accessToken, null, null).getObject();
                    if (adminList == null) {
                        adminList = new ArrayList<>();
                    }
                    adminList.add(admin);
                }
            }
        }
    }

    private void stopInvokerFactory() {
        //stop invoker factory
        try {
            RpcInvokerFactory.getInstance().stop();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static List<Admin> getAdminList() {
        return adminList;
    }

    public static Serializer getSerializer() {
        return serializer;
    }

    //---------------------------job handler repository-----------------------
    private static ConcurrentMap<String, IJobHandler> jobHandlerRepository = new ConcurrentHashMap<>();

    public static IJobHandler registryJobHandker(String name, IJobHandler jobHandler) {
        LOGGER.info(">>>>>>>>>>>>>>>>>job register jobHandler success , name:{}, jobHandler:{}", name, jobHandler);
        return jobHandlerRepository.put(name, jobHandler);
    }

    public static IJobHandler loadJobHandler(String name) {
        return jobHandlerRepository.get(name);
    }

    //--------------------------job thread repository--------------------------
    private static ConcurrentMap<Integer, JobThread> jobThreadRepository = new ConcurrentHashMap<>();

    public static JobThread registryJobThread(int jobId, IJobHandler handler, String removeOldReason) {
        JobThread newJobThread = new JobThread(jobId, handler);
        newJobThread.start();
        LOGGER.info(">>>>>>>>>>>>>>>job registry JobThread success , jobId:{},handler:{}", new Object[]{jobId, handler});

        JobThread oldJobThread = jobThreadRepository.put(jobId, newJobThread);
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
        }
        return newJobThread;
    }

    public static void removeJobThread(int jobId, String removeOldReason) {
        JobThread oldThread = jobThreadRepository.remove(jobId);
        if (oldThread != null) {
            oldThread.toStop(removeOldReason);
            oldThread.interrupt();
        }
    }

    public static JobThread loadJobThread(int jobId) {
        JobThread jobThread = jobThreadRepository.get(jobId);
        return jobThread;
    }
}
