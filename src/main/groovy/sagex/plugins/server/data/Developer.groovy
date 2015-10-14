/*
 *      Copyright 2015 Battams, Derek
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */
package sagex.plugins.server.data

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import sagex.plugins.server.DataStore

@Canonical
@EqualsAndHashCode(includes='email')
class Developer {
	final String email
	String password
	String key
	final List<Plugin> plugins = []
	private boolean pluginsLoaded = false
	
	Plugin[] getPlugins() {
		if(!pluginsLoaded) {
			DataStore.instance.getPluginsOwnedByDev(this).each {
				plugins << it
			}
			pluginsLoaded = true
		}
		plugins
	}
}
