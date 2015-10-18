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

@WebServlet(name='pluginServlet', urlPatterns=['/plugin'])
class PluginServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		Configuration cfg = servletContext.sopsFtlCfg
		def t = cfg.getTemplate('plugin/submit.ftl')
		def model = [req: req, user: SecurityUtils.subject]
		t.process(model, resp.writer)
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		def xml = new XmlSlurper().parseText(req.getParameter('xml'))
		def p = new Plugin(xml.Identifier.text(), xml.Version.text(), req.getParameter('xml'))
		def d = DataStore.instance.getDeveloper(req.getParameter('email'))
		DataStore.instance.savePlugin(p, d)
		doGet(req, resp)
	}
}
