/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.grpc.client;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.DisposableBean;

import io.grpc.ForwardingChannelBuilder2;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class DefaultGrpcChannelFactory implements GrpcChannelFactory, DisposableBean {

	private Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();

	@Override
	public ManagedChannelBuilder<?> createChannel(String authority) {
		ManagedChannelBuilder<?> target = Grpc.newChannelBuilder(authority, InsecureChannelCredentials.create());
		return new DisposableChannelBuilder(authority, target);
	}

	@Override
	public void destroy() throws Exception {
		for (ManagedChannel channel : channels.values()) {
			channel.shutdown();
		}
	}

	class DisposableChannelBuilder extends ForwardingChannelBuilder2<DisposableChannelBuilder> {

		private final ManagedChannelBuilder<?> delegate;

		private final String authority;

		public DisposableChannelBuilder(String authority, ManagedChannelBuilder<?> delegate) {
			this.authority = authority;
			this.delegate = delegate;
		}

		@Override
		protected ManagedChannelBuilder<?> delegate() {
			return delegate;
		}

		@Override
		public ManagedChannel build() {
			ManagedChannel channel = channels.computeIfAbsent(authority, name -> super.build());
			return channel;
		}

	}

}
