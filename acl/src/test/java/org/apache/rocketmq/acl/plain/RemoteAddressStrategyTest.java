/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.acl.plain;

import org.junit.Assert;
import org.junit.Test;

public class RemoteAddressStrategyTest {

    RemoteAddressStrategyFactory remoteAddressStrategyFactory = new RemoteAddressStrategyFactory();

    @Test
    public void NetaddressStrategyFactoryTest() {
        PlainAccessResource plainAccessResource = new PlainAccessResource();
        RemoteAddressStrategy remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        Assert.assertEquals(remoteAddressStrategy, RemoteAddressStrategyFactory.NULL_NET_ADDRESS_STRATEGY);

        plainAccessResource.setRemoteAddr("*");
        remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        Assert.assertEquals(remoteAddressStrategy, RemoteAddressStrategyFactory.NULL_NET_ADDRESS_STRATEGY);

        plainAccessResource.setRemoteAddr("127.0.0.1");
        remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        Assert.assertEquals(remoteAddressStrategy.getClass(), RemoteAddressStrategyFactory.OneRemoteAddressStrategy.class);

        plainAccessResource.setRemoteAddr("127.0.0.1,127.0.0.2,127.0.0.3");
        remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        Assert.assertEquals(remoteAddressStrategy.getClass(), RemoteAddressStrategyFactory.MultipleRemoteAddressStrategy.class);

        plainAccessResource.setRemoteAddr("127.0.0.{1,2,3}");
        remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        Assert.assertEquals(remoteAddressStrategy.getClass(), RemoteAddressStrategyFactory.MultipleRemoteAddressStrategy.class);

        plainAccessResource.setRemoteAddr("127.0.0.1-200");
        remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        Assert.assertEquals(remoteAddressStrategy.getClass(), RemoteAddressStrategyFactory.RangeRemoteAddressStrategy.class);

        plainAccessResource.setRemoteAddr("127.0.0.*");
        remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        Assert.assertEquals(remoteAddressStrategy.getClass(), RemoteAddressStrategyFactory.RangeRemoteAddressStrategy.class);

        plainAccessResource.setRemoteAddr("127.0.1-20.*");
        remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        Assert.assertEquals(remoteAddressStrategy.getClass(), RemoteAddressStrategyFactory.RangeRemoteAddressStrategy.class);
    }

    @Test(expected = AclPlugRuntimeException.class)
    public void verifyTest() {
        PlainAccessResource plainAccessResource = new PlainAccessResource();
        plainAccessResource.setRemoteAddr("127.0.0.1");
        remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        plainAccessResource.setRemoteAddr("256.0.0.1");
        remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
    }

    @Test
    public void nullNetaddressStrategyTest() {
        boolean isMatch = RemoteAddressStrategyFactory.NULL_NET_ADDRESS_STRATEGY.match(new PlainAccessResource());
        Assert.assertTrue(isMatch);
    }

    public void oneNetaddressStrategyTest() {
        PlainAccessResource plainAccessResource = new PlainAccessResource();
        plainAccessResource.setRemoteAddr("127.0.0.1");
        RemoteAddressStrategy remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        plainAccessResource.setRemoteAddr("");
        boolean match = remoteAddressStrategy.match(plainAccessResource);
        Assert.assertFalse(match);

        plainAccessResource.setRemoteAddr("127.0.0.2");
        match = remoteAddressStrategy.match(plainAccessResource);
        Assert.assertFalse(match);

        plainAccessResource.setRemoteAddr("127.0.0.1");
        match = remoteAddressStrategy.match(plainAccessResource);
        Assert.assertTrue(match);
    }

    @Test
    public void multipleNetaddressStrategyTest() {
        PlainAccessResource plainAccessResource = new PlainAccessResource();
        plainAccessResource.setRemoteAddr("127.0.0.1,127.0.0.2,127.0.0.3");
        RemoteAddressStrategy remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        multipleNetaddressStrategyTest(remoteAddressStrategy);

        plainAccessResource.setRemoteAddr("127.0.0.{1,2,3}");
        remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        multipleNetaddressStrategyTest(remoteAddressStrategy);

    }

    @Test(expected = AclPlugRuntimeException.class)
    public void multipleNetaddressStrategyExceptionTest() {
        PlainAccessResource plainAccessResource = new PlainAccessResource();
        plainAccessResource.setRemoteAddr("127.0.0.1,2,3}");
        remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
    }

    private void multipleNetaddressStrategyTest(RemoteAddressStrategy remoteAddressStrategy) {
        PlainAccessResource plainAccessResource = new PlainAccessResource();
        plainAccessResource.setRemoteAddr("127.0.0.1");
        boolean match = remoteAddressStrategy.match(plainAccessResource);
        Assert.assertTrue(match);

        plainAccessResource.setRemoteAddr("127.0.0.2");
        match = remoteAddressStrategy.match(plainAccessResource);
        Assert.assertTrue(match);

        plainAccessResource.setRemoteAddr("127.0.0.3");
        match = remoteAddressStrategy.match(plainAccessResource);
        Assert.assertTrue(match);

        plainAccessResource.setRemoteAddr("127.0.0.4");
        match = remoteAddressStrategy.match(plainAccessResource);
        Assert.assertFalse(match);

        plainAccessResource.setRemoteAddr("127.0.0.0");
        match = remoteAddressStrategy.match(plainAccessResource);
        Assert.assertFalse(match);

    }

    @Test
    public void rangeNetaddressStrategyTest() {
        String head = "127.0.0.";
        PlainAccessResource plainAccessResource = new PlainAccessResource();
        plainAccessResource.setRemoteAddr("127.0.0.1-200");
        RemoteAddressStrategy remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        rangeNetaddressStrategyTest(remoteAddressStrategy, head, 1, 200, true);
        plainAccessResource.setRemoteAddr("127.0.0.*");
        remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        rangeNetaddressStrategyTest(remoteAddressStrategy, head, 0, 255, true);

        plainAccessResource.setRemoteAddr("127.0.1-200.*");
        remoteAddressStrategy = remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
        rangeNetaddressStrategyThirdlyTest(remoteAddressStrategy, head, 1, 200);
    }

    private void rangeNetaddressStrategyTest(RemoteAddressStrategy remoteAddressStrategy, String head, int start, int end,
        boolean isFalse) {
        PlainAccessResource plainAccessResource = new PlainAccessResource();
        for (int i = -10; i < 300; i++) {
            plainAccessResource.setRemoteAddr(head + i);
            boolean match = remoteAddressStrategy.match(plainAccessResource);
            if (isFalse && i >= start && i <= end) {
                Assert.assertTrue(match);
                continue;
            }
            Assert.assertFalse(match);

        }
    }

    private void rangeNetaddressStrategyThirdlyTest(RemoteAddressStrategy remoteAddressStrategy, String head, int start,
        int end) {
        String newHead;
        for (int i = -10; i < 300; i++) {
            newHead = head + i;
            if (i >= start && i <= end) {
                rangeNetaddressStrategyTest(remoteAddressStrategy, newHead, 0, 255, false);
            }
        }
    }

    @Test(expected = AclPlugRuntimeException.class)
    public void rangeNetaddressStrategyExceptionStartGreaterEndTest() {
        rangeNetaddressStrategyExceptionTest("127.0.0.2-1");
    }

    @Test(expected = AclPlugRuntimeException.class)
    public void rangeNetaddressStrategyExceptionScopeTest() {
        rangeNetaddressStrategyExceptionTest("127.0.0.-1-200");
    }

    @Test(expected = AclPlugRuntimeException.class)
    public void rangeNetaddressStrategyExceptionScopeTwoTest() {
        rangeNetaddressStrategyExceptionTest("127.0.0.0-256");
    }

    private void rangeNetaddressStrategyExceptionTest(String netaddress) {
        PlainAccessResource plainAccessResource = new PlainAccessResource();
        plainAccessResource.setRemoteAddr(netaddress);
        remoteAddressStrategyFactory.getNetaddressStrategy(plainAccessResource);
    }

}