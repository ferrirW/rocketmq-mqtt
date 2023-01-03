package org.apache.rocketmq.mqtt.ds.test.meta;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.rocketmq.mqtt.common.model.Constants;
import org.apache.rocketmq.mqtt.ds.config.ServiceConf;
import org.apache.rocketmq.mqtt.ds.meta.WillMsgClient;
import org.apache.rocketmq.mqtt.ds.meta.WillMsgPersistManagerImpl;
import org.apache.rocketmq.mqtt.meta.util.IpUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WillMsgPersistManagerImplTest {
    public WillMsgPersistManagerImpl willMsgPersistManager;
    public WillMsgClient willMsgClient;
    private long checkAliveIntervalMillis = 5 * 1000;

    @Before
    public void Before() throws IOException, IllegalAccessException, InterruptedException, TimeoutException {
        willMsgClient = new WillMsgClient();
        ServiceConf serviceConf = mock(ServiceConf.class);
        when(serviceConf.getMetaAddr()).thenReturn("11.164.204.116:8080,11.164.204.117:8080,11.164.204.118:8080");
        FieldUtils.writeDeclaredField(willMsgClient, "serviceConf", serviceConf, true);

        willMsgClient.init();
        willMsgPersistManager = new WillMsgPersistManagerImpl();
        FieldUtils.writeDeclaredField(willMsgPersistManager, "willMsgClient", willMsgClient, true);
    }

    @Test
    public void put() throws ExecutionException, InterruptedException, TimeoutException {
        String ip = IpUtil.getLocalAddressCompatible();
        String csKey = Constants.CS_ALIVE + Constants.CTRL_1 + ip;
        long currentTime = System.currentTimeMillis();

        CompletableFuture<Boolean> future = willMsgPersistManager.put(csKey, String.valueOf(currentTime));
        Assert.assertTrue(future.get(3000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void get() throws ExecutionException, InterruptedException, TimeoutException {
        String ip = IpUtil.getLocalAddressCompatible();
        String csKey = Constants.CS_ALIVE + Constants.CTRL_1 + ip;
        long currentTime = System.currentTimeMillis();

        CompletableFuture<Boolean> future = willMsgPersistManager.put(csKey, String.valueOf(currentTime));
        Assert.assertTrue(future.get(3000, TimeUnit.MILLISECONDS));

        CompletableFuture<byte[]> future1 = willMsgPersistManager.get(csKey);
        Assert.assertEquals(String.valueOf(currentTime), new String(future1.get(3000, TimeUnit.MILLISECONDS)));
    }

    @Test
    public void compareAndPut() throws ExecutionException, InterruptedException, TimeoutException {
        String ip = IpUtil.getLocalAddressCompatible();
        String csKey = Constants.CS_ALIVE + Constants.CTRL_1 + ip;
        String masterKey = Constants.CS_MASTER;
        long currentTime = System.currentTimeMillis();

        willMsgPersistManager.get(masterKey).whenComplete((result, throwable) -> {
            String content = new String(result);
            if (Constants.NOT_FOUND.equals(content) || masterHasDown(content)) {
                willMsgPersistManager.compareAndPut(masterKey, content, ip + Constants.COLON + currentTime).whenComplete((rs, tb) -> {
                    if (!rs || tb != null) {
                        System.out.println("{} fail to update master" + ip);
                        return;
                    }
                    System.out.println("------------put success-------------------");
                });

            }
        });

        Thread.sleep(10000);
    }

    private boolean masterHasDown(String masterValue) {
        String[] ipTime = masterValue.split(Constants.COLON);
        if (ipTime.length < 2) {
            return true;
        }

        return System.currentTimeMillis() - Long.parseLong(ipTime[1]) > 10 * checkAliveIntervalMillis;
    }

}
