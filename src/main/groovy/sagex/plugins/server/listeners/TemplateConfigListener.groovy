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
package sagex.plugins.server.listeners

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

import sagex.plugins.server.DataStore
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler

@WebListener
class TemplateConfigListener implements ServletContextListener {
	static private final String FTL_ENGN_PROP = 'FTL_ENGN_SET'
	
	@Override
	void contextInitialized(ServletContextEvent sce) {
		if(!sce.servletContext.getAttribute(FTL_ENGN_PROP)) {
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_23)
			cfg.defaultEncoding = 'UTF-8'
			cfg.templateExceptionHandler = TemplateExceptionHandler.HTML_DEBUG_HANDLER
			cfg.setServletContextForTemplateLoading(sce.servletContext, 'WEB-INF/templates')
			sce.servletContext.setAttribute(FTL_ENGN_PROP, true)
			sce.servletContext.metaClass.sopsFtlCfg = cfg
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if(sce.servletContext.getAttribute(FTL_ENGN_PROP)) {
			sce.servletContext.removeAttribute(FTL_ENGN_PROP)
			DataStore.shutdown()
		}
	}
}
