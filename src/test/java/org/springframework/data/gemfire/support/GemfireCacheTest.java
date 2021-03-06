/*
 * Copyright 2010-104 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire.support;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.cache.Cache;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.DistributedSystem;

/**
 * @author Costin Leau
 * @author John Blum
 * @author Oliver Gierke
 */
// TODO avoid using actual GemFire Cache and Region instances, thereby creating a distributed system, for this test!
// TODO Use Mocks!
public class GemfireCacheTest extends AbstractNativeCacheTest<Region<Object, Object>> {

	@Rule public ExpectedException exception = ExpectedException.none();

	@Override
	protected Cache createCache(Region<Object, Object> nativeCache) {
		return new GemfireCache(nativeCache);
	}

	@Override
	@SuppressWarnings({ "deprecation", "unchecked" })
	protected Region<Object, Object> createNativeCache() throws Exception {
		com.gemstone.gemfire.cache.Cache instance = null;

		try {
			instance = CacheFactory.getAnyInstance();
		}
		catch (Exception ignore) {
		}

		if (instance == null) {
			DistributedSystem ds = DistributedSystem.connect(new Properties());
			instance = CacheFactory.create(ds);
		}

		Region region = instance.getRegion(CACHE_NAME);

		if (region == null) {
			region = instance.createRegion(CACHE_NAME, new com.gemstone.gemfire.cache.AttributesFactory().create());
		}

		return region;
	}

	/**
	 * @see SGF-317
	 */
	@Test
	public void findsTypedValue() throws Exception {

		GemfireCache cache = new GemfireCache(createNativeCache());
		cache.put("key", "value");

		assertThat(cache.get("key", String.class), is("value"));
	}

	/**
	 * @see SGF-317
	 */
	@Test
	public void skipTypeChecksIfTargetTypeIsNull() throws Exception {

		GemfireCache cache = new GemfireCache(createNativeCache());
		cache.put("key", "value");

		assertThat(cache.get("key", null), is((Object) "value"));
	}

	/**
	 * @see SGF-317
	 */
	@Test
	public void throwsIllegalStateExceptionIfTypedAccessDoesntFindMatchingType() throws Exception {

		GemfireCache cache = new GemfireCache(createNativeCache());
		cache.put("key", "value");

		exception.expect(IllegalStateException.class);
		exception.expectMessage(Integer.class.getName());
		exception.expectMessage("value");

		cache.get("key", Integer.class);
	}
}
