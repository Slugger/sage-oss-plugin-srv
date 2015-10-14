/*
 Copyright 2015 Battams, Derek
 
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
 
		http://www.apache.org/licenses/LICENSE-2.0
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package sagex.plugins.server

import groovy.sql.Sql
import groovy.util.logging.Log4j

import java.sql.SQLException

import org.apache.commons.io.FilenameUtils

import sagex.plugins.server.data.Developer
import sagex.plugins.server.data.Plugin

@Log4j
class DataStore {
	static File getAppRoot() { return new File(System.getProperty('sops.root') ?: new File(new File(System.getProperty('user.home')), '.sops').absolutePath) }
	static private final String DB_NAME = "jdbc:derby:${Boolean.parseBoolean(System.getProperty('sops.testing')) ? 'memory:' : ''}${FilenameUtils.separatorsToUnix(FilenameUtils.getFullPath(new File(getAppRoot(), 'throwaway').absolutePath))}sops"
	static {
		if(!getAppRoot().exists() && !getAppRoot().mkdirs())
			throw new IOException("Unable to create db directory: ${getAppRoot().absolutePath}")
	}

	static private DataStore INSTANCE = null
	synchronized static DataStore getInstance() {
		if(!INSTANCE)
			INSTANCE = new DataStore()
		INSTANCE
	}
	
	synchronized static void shutdown() {
		if(INSTANCE?.sql) {
			try { INSTANCE.sql.close() } catch(Throwable t) {}
			try {
				Sql.newInstance("${DB_NAME};shutdown=true")
			} catch(SQLException e) {
				if(e.SQLState != '08006') {
					log.error 'SQLError shutting down db!', e
					throw e
				}
			}
		}
		INSTANCE = null
		log.info 'Database shutdown completed.'
	}
	
	static void main(args) {
		def d = DataStore.instance.getDeveloper('derek@battams.ca')
		println d
		println d.plugins
		DataStore.shutdown()
	}

	private Sql sql = null
	
	private DataStore() {
		def jdbcStr = "${DB_NAME};create=true"
		log.info "Connecting to database: $jdbcStr"
		sql = Sql.newInstance(jdbcStr, 'org.apache.derby.jdbc.EmbeddedDriver')
		def metadata = sql.connection.metaData.getTables(null, null, 'SETTINGS', null)
		if(!metadata.next()) {
			createTables()
			setDbVersion()
			log.info 'New database created'
		} else
			log.info 'Connected to existing database'
		try { metadata?.close() } catch(Throwable t) { log.error 'SQLError', t }
	}

	void saveDeveloper(Developer d) {
		def qry
		if(getDeveloper(d.email))
			qry = "UPDATE developer SET password = $d.password, api_key = $d.key WHERE email = $d.email"
		else
			qry = "INSERT INTO developer (email, password, api_key) VALUES ($d.email, $d.password, $d.key)"
		sql.execute(qry)
	}
	
	Developer getDeveloper(String email) {
		def qry = "SELECT * FROM developer WHERE email = $email"
		def row = sql.firstRow(qry)
		row ? new Developer(email, row.password, row.api_key) : null
	}
	
	Plugin[] getPluginsOwnedByDev(Developer d) {
		def plugins = []
		def qry = "SELECT * FROM owns WHERE email = $d.email"
		sql.eachRow(qry) {
			plugins << new Plugin(it.name, it.version)
		}
		plugins
	}
	
	private void setDbVersion() {
		def qry = "INSERT INTO settings (name, value) VALUES ('dbVersion', '0')"
		log.trace qry
		sql.execute qry
	}
	
	private void createTables() {
		sql.withTransaction {
			sql.execute '''
				CREATE TABLE developer (
					email VARCHAR(128) NOT NULL PRIMARY KEY,
					password VARCHAR(32) NOT NULL,
					api_key CHAR(16) NOT NULL
				)
			'''

			sql.execute '''
				CREATE TABLE plugin (
					name VARCHAR(32) NOT NULL,
					version VARCHAR(32) NOT NULL,
					manifest CLOB NOT NULL,
					PRIMARY KEY (name, version)
				)
			'''
			
			sql.execute '''
				CREATE TABLE owns (
					email VARCHAR(128) NOT NULL,
					name VARCHAR(32) NOT NULL,
					version VARCHAR(32) NOT NULL,
					CONSTRAINT owns_email_fk FOREIGN KEY (email) REFERENCES developer(email) ON DELETE CASCADE,
					CONSTRAINT owns_plugin_fk FOREIGN KEY (name, version) REFERENCES plugin(name, version) ON DELETE CASCADE
				)
			'''
			
			sql.execute '''
				CREATE TABLE settings (
					name VARCHAR(64) NOT NULL PRIMARY KEY,
					value VARCHAR(128)
				)
			'''
		}
	}
}