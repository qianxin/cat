package com.dianping.cat.system.config;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.monitorrules.entity.MetricItem;
import com.dianping.cat.home.monitorrules.entity.MonitorRules;
import com.dianping.cat.home.monitorrules.entity.Rule;
import com.dianping.cat.home.monitorrules.transform.DefaultSaxParser;

public class NetworkMetricRuleConfigManager extends BaseMetricRuleConfigManager implements Initializable {

	private static final String CONFIG_NAME = "networkRulesConfig";

	@Override
   protected String getConfigName() {
	   return CONFIG_NAME;
   }

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_config = DefaultSaxParser.parse(content);
			m_configId = config.getId();
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-network-metric-rule-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_config = DefaultSaxParser.parse(content);
				m_configId = config.getId();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new MonitorRules();
		}
	}

	public boolean insert(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);
			setSumTrueWhenAllFalse(m_config);

			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	private void setSumTrueWhenAllFalse(MonitorRules config) {
		for (Rule rule : m_config.getRules()) {
			for (MetricItem item : rule.getMetricItems()) {
				if (!item.isMonitorAvg() && !item.isMonitorCount() && !item.isMonitorSum()) {
					item.setMonitorSum(true);
				}
			}
		}
	}

}