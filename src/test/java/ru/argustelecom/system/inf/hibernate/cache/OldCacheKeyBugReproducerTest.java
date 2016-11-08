package ru.argustelecom.system.inf.hibernate.cache;

import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.test.cache.infinispan.functional.cluster.ClusterAwareRegionFactory;
import org.hibernate.test.cache.infinispan.functional.cluster.DualNodeJtaTransactionManagerImpl;
import org.hibernate.test.cache.infinispan.functional.cluster.DualNodeTest;
import org.hibernate.test.cache.infinispan.functional.entities.Account;
import org.hibernate.test.cache.infinispan.functional.entities.Report;
import org.hibernate.test.cache.infinispan.functional.entities.ReportPk;
import org.hibernate.test.cache.infinispan.util.TxUtil;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.infinispan.test.fwk.TestResourceTracker;
import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;

public class OldCacheKeyBugReproducerTest extends DualNodeTest{

	public OldCacheKeyBugReproducerTest() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public List<Object[]> getParameters() {
		return getParameters(true, false, false, false);
	}
	
	@Override
	protected void cleanupTransactionManagement() {
		// Don't clean up the managers, just the transactions
		// Managers are still needed by the long-lived caches
		DualNodeJtaTransactionManagerImpl.cleanupTransactions();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void applyStandardSettings(Map settings) {
		super.applyStandardSettings(settings);
		// applying our CacheKeysFactory, that uses hibernate type metadata in cache-key
		// this factory like DefaultCacheKeyFactory
		settings.put("hibernate.cache.keys_factory", "ru.argustelecom.system.inf.hibernate.cache.ClassicalCacheKeysFactory");
	}
	
	
	@Override
	public String[] getMappings() {
		return new String[] {"cache/infinispan/functional/entities/report.hbm.xml"};
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void test () throws Exception{
		SessionFactory localFactory = sessionFactory();

		// Second session factory doesn't; just needs a transaction manager
		SessionFactory remoteFactory = secondNodeEnvironment().getSessionFactory();

		Date date = new Date();
		String reason = "reason1";

		TxUtil.withTxSession(useJta, localFactory, session -> {
			Report report = new Report(new ReportPk(date, reason), 7);
			session.persist(report);
		});
		
		Report cachedReport = remoteFactory.openSession().get(Report.class, new ReportPk(date, reason));
		
		assertNotNull(cachedReport);
		
	}


}
