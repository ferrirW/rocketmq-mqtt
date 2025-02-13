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

package org.apache.rocketmq.mqtt.cs.session.loop;


import io.netty.channel.Channel;
import org.apache.rocketmq.mqtt.common.model.Queue;
import org.apache.rocketmq.mqtt.common.model.Subscription;
import org.apache.rocketmq.mqtt.common.model.WillMessage;
import org.apache.rocketmq.mqtt.cs.channel.ChannelManager;
import org.apache.rocketmq.mqtt.cs.session.Session;

import java.util.List;
import java.util.Set;


public interface SessionLoop {

    /**
     * set ChannelManager
     *
     * @param channelManager
     */
    void setChannelManager(ChannelManager channelManager);

    /**
     * load one mqtt session
     *
     * @param clientId
     * @param channel
     */
    void loadSession(String clientId, Channel channel);

    /**
     * unload one mqtt session
     *
     * @param clientId
     * @param channelId
     * @return
     */
    Session unloadSession(String clientId, String channelId);

    /**
     * get the session by channelId
     *
     * @param channelId
     * @return
     */
    Session getSession(String channelId);

    /**
     * get session list by clientId
     *
     * @param clientId
     * @return
     */
    List<Session> getSessionList(String clientId);

    /**
     * add subscription
     *
     * @param channelId
     * @param subscriptions
     */
    void addSubscription(String channelId, Set<Subscription> subscriptions);

    /**
     * remove subscription
     *
     * @param channelId
     * @param subscriptions
     */
    void removeSubscription(String channelId, Set<Subscription> subscriptions);

    /**
     * notify to pull message from queue
     *
     * @param session
     * @param subscription
     * @param queue
     */
    void notifyPullMessage(Session session, Subscription subscription, Queue queue);

    void addWillMessage(Channel channel, WillMessage willMessage);
}
