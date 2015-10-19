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
package sagex.plugins.server.servlets

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.shiro.SecurityUtils

import sagex.plugins.server.DataStore
import sagex.plugins.server.data.Plugin
import freemarker.template.Configuration

@WebServlet(name='manifestServlet', urlPatterns=['/manifest'])
class ManifestServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		def xml = new XmlParser().parseText('<PluginRepository />')
		DataStore.instance.plugins.each {
			def node = new XmlParser().parseText(it.manifest)
			xml.append node
		}
		resp.contentType = 'application/xml'
		new XmlNodePrinter(new IndentPrinter(resp.writer)).print(xml)
	}
}
