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
package sagex.plugins.server.security

import org.apache.log4j.Logger
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.authc.credential.CredentialsMatcher
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.cache.CacheManager
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection

import sagex.plugins.server.DataStore

class SopsRealm extends AuthorizingRealm {
	static private final Logger LOG = Logger.getLogger(SopsRealm)
	static private final String REALM_NAME = 'SOPS Realm'
	
	public SopsRealm() { super(new SimpleCredentialsMatcher()) }

	public SopsRealm(CacheManager cacheManager) { super(cacheManager, new SimpleCredentialsMatcher()) }

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		def authInfo = new SimpleAccount()
		authInfo.setPrincipals(principals)
		def dev = DataStore.instance.getDeveloper(principals.primaryPrincipal.toString())
		if(dev)
			dev.plugins.collect { it.name }.unique().each {	authInfo.addRole(it) }
		authInfo
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		def dev = DataStore.instance.getDeveloper(token.username)
		def authInfo = dev ? new SimpleAccount(new SimplePrincipalCollection(dev.email, REALM_NAME), dev.password) : null
		if(!dev || !credentialsMatcher.doCredentialsMatch(token, authInfo))
			throw new AuthenticationException('Invalid id or password')
		authInfo
	}
}
