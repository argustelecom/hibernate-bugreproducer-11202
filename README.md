# hibernate-bugreproducer-11202

This is simple reproducer for https://hibernate.atlassian.net/browse/HHH-11202

Uses custom CacheKeysFactory because org.hibernate.cache.internal.DefaultCacheKeysFactory is not accessible.
Uses non-public lib hibernate-infinispan:tests for hibernate-infinispan test stuff.


To "repair" modify string in ReproducerTest:

 		settings.put("hibernate.cache.keys_factory", "ru.argustelecom.system.inf.hibernate.cache.AffectedClassicalCacheKeysFactory");


to:

 		settings.put("hibernate.cache.keys_factory", "ru.argustelecom.system.inf.hibernate.cache.ClassicalCacheKeysFactory");



