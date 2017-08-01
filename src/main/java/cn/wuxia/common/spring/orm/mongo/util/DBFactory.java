package cn.wuxia.common.spring.orm.mongo.util;

public class DBFactory {
	
	public static MongoDBManager getMongoDB() {
		
		return MongoDBManager.getInstance();
	}

	public static MongoDBManager getMongoDB(String dbName) {
		
		return MongoDBManager.getInstance(dbName);
	}
}
