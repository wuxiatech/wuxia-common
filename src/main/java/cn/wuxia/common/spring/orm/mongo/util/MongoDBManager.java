package cn.wuxia.common.spring.orm.mongo.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import cn.wuxia.common.util.PropertiesUtils;

public class MongoDBManager {

    private MongoClient mg = null;

    private DB db = null;

    private final static Map<String, MongoDBManager> instances = new ConcurrentHashMap<String, MongoDBManager>();

    /**
     * 实例化
     * 
     * @return MongoDBManager对象
     */
    static {
        getInstance("db");// 初始化默认的MongoDB数据库
    }

    public static MongoDBManager getInstance() {

        return getInstance("db");// 配置文件默认数据库前缀为db
    }

    public static MongoDBManager getInstance(String dbName) {

        MongoDBManager mm = instances.get(dbName);
        if (mm == null) {
            mm = getNewInstance(dbName);
            if (mm != null)
                instances.put(dbName, mm);
        }

        return mm;
    }

    private static synchronized MongoDBManager getNewInstance(String dbName) {

        MongoDBManager mm = new MongoDBManager();
        try {
            Properties p = PropertiesUtils.loadProperties("classpath:mongodb.properties");
            mm.mg = new MongoClient(p.getProperty(dbName + ".host"), Integer.parseInt(p.getProperty(dbName + ".port")));
            mm.db = mm.mg.getDB(p.getProperty(dbName + ".database"));
        } catch (Exception e) {
            System.out.print("Can't connect " + dbName + " MongoDB!");
            return null;
        }

        return mm;
    }

    /**
     * 获取集合（表）
     * 
     * @param collection
     */
    public DBCollection getCollection(String collection) {

        return this.db.getCollection(collection);
    }

    /**
     * ----------------------------------分割线------------------------------------
     */

    /**
     * 插入
     * 
     * @param collection
     * @param o
     *            插入
     */
    public void insert(String collection, DBObject o) {

        getCollection(collection).insert(o);
    }

    /**
     * 批量插入
     * 
     * @param collection
     * @param list
     *            插入的列表
     */
    public void insertBatch(String collection, List<DBObject> list) {

        if (list == null || list.isEmpty()) {
            return;
        }

        getCollection(collection).insert(list);

    }

    /**
     * 删除
     * 
     * @param collection
     * @param q
     *            查询条件
     */
    public void delete(String collection, DBObject q) {

        getCollection(collection).remove(q);
    }

    /**
     * 批量删除
     * 
     * @param collection
     * @param list
     *            删除条件列表
     */
    public void deleteBatch(String collection, List<DBObject> list) {

        if (list == null || list.isEmpty()) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            getCollection(collection).remove(list.get(i));
        }
    }

    /**
     * 计算集合总条数
     * 
     * @param collection
     */
    public long getCount(String collection) {

        return getCollection(collection).find().count();
    }

    /**
     * 计算满足条件条数
     * 
     * @param collection
     * @param q
     *            查询条件
     */

    public long getCount(String collection, DBObject q) {

        return getCollection(collection).getCount(q);
    }

    /**
     * 更新
     * 
     * @param collection
     * @param q
     *            查询条件
     * @param setFields
     *            更新对象
     */
    public void update(String collection, DBObject q, DBObject setFields) {

        getCollection(collection).updateMulti(q, new BasicDBObject("$set", setFields));
    }

    /**
     * 查找集合所有对象
     * 
     * @param collection
     */
    public List<DBObject> findAll(String collection) {

        return getCollection(collection).find().toArray();
    }

    /**
     * 按顺序查找集合所有对象
     * 
     * @param collection
     *            数据集
     * @param orderBy
     *            排序
     */
    public List<DBObject> findAll(String collection, DBObject orderBy) {

        return getCollection(collection).find().sort(orderBy).toArray();
    }

    /**
     * 查找（返回一个对象）
     * 
     * @param collection
     * @param q
     *            查询条件
     */
    public DBObject findOne(String collection, DBObject q) {

        return getCollection(collection).findOne(q);
    }

    /**
     * 查找（返回一个对象）
     * 
     * @param collection
     * @param q
     *            查询条件
     * @param fileds
     *            返回字段
     */
    public DBObject findOne(String collection, DBObject q, DBObject fileds) {

        return getCollection(collection).findOne(q, fileds);
    }

    /**
     * 查找返回特定字段（返回一个List<DBObject>）
     * 
     * @param collection
     * @param q
     *            查询条件
     * @param fileds
     *            返回字段
     */
    public List<DBObject> findLess(String collection, DBObject q, DBObject fileds) {

        DBCursor c = getCollection(collection).find(q, fileds);
        if (c != null)
            return c.toArray();
        else
            return null;
    }

    /**
     * 查找返回特定字段（返回一个List<DBObject>）
     * 
     * @param collection
     * @param q
     *            查询条件
     * @param fileds
     *            返回字段
     * @param orderBy
     *            排序
     */
    public List<DBObject> findLess(String collection, DBObject q, DBObject fileds, DBObject orderBy) {

        DBCursor c = getCollection(collection).find(q, fileds).sort(orderBy);
        if (c != null)
            return c.toArray();
        else
            return null;
    }

    /**
     * 分页查找集合对象，返回特定字段
     * 
     * @param collection
     * @param q
     *            查询条件
     * @param fileds
     *            返回字段
     * @pageNo 第n页
     * @perPageCount 每页记录数
     */
    public List<DBObject> findLess(String collection, DBObject q, DBObject fileds, int pageNo, int perPageCount) {

        return getCollection(collection).find(q, fileds).skip((pageNo - 1) * perPageCount).limit(perPageCount).toArray();
    }

    /**
     * 按顺序分页查找集合对象，返回特定字段
     * 
     * @param collection
     *            集合
     * @param q
     *            查询条件
     * @param fileds
     *            返回字段
     * @param orderBy
     *            排序
     * @param pageNo
     *            第n页
     * @param perPageCount
     *            每页记录数
     */
    public List<DBObject> findLess(String collection, DBObject q, DBObject fileds, DBObject orderBy, int pageNo, int perPageCount) {

        return getCollection(collection).find(q, fileds).sort(orderBy).skip((pageNo - 1) * perPageCount).limit(perPageCount).toArray();
    }

    /**
     * 查找（返回一个List<DBObject>）
     * 
     * @param collection
     * @param q
     *            查询条件
     */
    public List<DBObject> find(String collection, DBObject q) {

        DBCursor c = getCollection(collection).find(q);
        if (c != null)
            return c.toArray();
        else
            return null;
    }

    /**
     * 按顺序查找（返回一个List<DBObject>）
     * 
     * @param collection
     * @param q
     *            查询条件
     * @param orderBy
     *            排序
     */
    public List<DBObject> find(String collection, DBObject q, DBObject orderBy) {

        DBCursor c = getCollection(collection).find(q).sort(orderBy);
        if (c != null)
            return c.toArray();
        else
            return null;
    }

    /**
     * 分页查找集合对象
     * 
     * @param collection
     * @param q
     *            查询条件
     * @pageNo 第n页
     * @perPageCount 每页记录数
     */
    public List<DBObject> find(String collection, DBObject q, int pageNo, int perPageCount) {

        return getCollection(collection).find(q).skip((pageNo - 1) * perPageCount).limit(perPageCount).toArray();
    }

    /**
     * 按顺序分页查找集合对象
     * 
     * @param collection
     *            集合
     * @param q
     *            查询条件
     * @param orderBy
     *            排序
     * @param pageNo
     *            第n页
     * @param perPageCount
     *            每页记录数
     */
    public List<DBObject> find(String collection, DBObject q, DBObject orderBy, int pageNo, int perPageCount) {

        return getCollection(collection).find(q).sort(orderBy).skip((pageNo - 1) * perPageCount).limit(perPageCount).toArray();
    }

    /**
     * distinct操作
     * 
     * @param collection
     *            集合
     * @param field
     *            distinct字段名称
     */
    public Object[] distinct(String collection, String field) {

        return getCollection(collection).distinct(field).toArray();
    }

    /**
     * distinct操作
     * 
     * @param collection
     *            集合
     * @param field
     *            distinct字段名称
     * @param q
     *            查询条件
     */
    public Object[] distinct(String collection, String field, DBObject q) {

        return getCollection(collection).distinct(field, q).toArray();
    }

    /**
     * group分组查询操作，返回结果少于10,000keys时可以使用
     * 
     * @param collection
     *            集合
     * @param key
     *            分组查询字段
     * @param q
     *            查询条件
     * @param reduce
     *            reduce Javascript函数，如：function(obj, out){out.count++;out.csum=obj.c;}
     * @param finalize
     *            reduce function返回结果处理Javascript函数，如：function(out){out.avg=out.csum/out.count;}
     */
    public BasicDBList group(String collection, DBObject key, DBObject q, DBObject initial, String reduce, String finalize) {

        return ((BasicDBList) getCollection(collection).group(key, q, initial, reduce, finalize));
    }

    /**
     * group分组查询操作，返回结果大于10,000keys时可以使用
     * 
     * @param collection
     *            集合
     * @param map
     *            映射javascript函数字符串，如：function(){ for(var key in this) { emit(key,{count:1}) } }
     * @param reduce
     *            reduce Javascript函数字符串，如：function(key,emits){ total=0; for(var i in emits){ total+=emits[i].count;  } return {count:total}; }
     * @param q
     *            分组查询条件
     * @param orderBy
     *            分组查询排序
     */
    //	public Iterable<DBObject> mapReduce(String collection, String map, String reduce, 
    //			DBObject q, DBObject orderBy) {
    //
    ////		DBCollection coll = db.getCollection(collection);
    ////		MapReduceCommand cmd = new MapReduceCommand(coll, map, reduce, null, MapReduceCommand.OutputType.INLINE, q);
    ////		return coll.mapReduce(cmd).results();
    //		MapReduceOutput out = getCollection(collection).mapReduce(map, reduce, null, q);
    //		return out.getOutputCollection().find().sort(orderBy).toArray();
    //	}

    /**
     * group分组分页查询操作，返回结果大于10,000keys时可以使用
     * 
     * @param collection
     *            集合
     * @param map
     *            映射javascript函数字符串，如：function(){ for(var key in this) { emit(key,{count:1}) } }
     * @param reduce
     *            reduce Javascript函数字符串，如：function(key,emits){ total=0; for(var i in emits){ total+=emits[i].count;  } return {count:total}; }
     * @param q
     *            分组查询条件
     * @param orderBy
     *            分组查询排序
     * @param pageNo
     *            第n页
     * @param perPageCount
     *            每页记录数
     */
    //	public List<DBObject> mapReduce(String collection, String map, String reduce, 
    //			DBObject q, DBObject orderBy, int pageNo, int perPageCount) {
    //
    //		MapReduceOutput out = getCollection(collection).mapReduce(map, reduce, null, q);
    //		return out.getOutputCollection().find().sort(orderBy).skip((pageNo - 1) * perPageCount)
    //				.limit(perPageCount).toArray();
    //	}

    /**
     * group分组查询操作，返回结果大于10,000keys时可以使用
     * 
     * @param collection
     *            集合
     * @param map
     *            映射javascript函数字符串，如：function(){ for(var key in this) { emit(key,{count:1}) } }
     * @param reduce
     *            reduce Javascript函数字符串，如：function(key,emits){ total=0; for(var i in emits){ total+=emits[i].count;  } return {count:total}; }
     * @param outputCollectionName
     *            输出结果表名称
     * @param q
     *            分组查询条件
     * @param orderBy
     *            分组查询排序
     */
    public List<DBObject> mapReduce(String collection, String map, String reduce, String outputCollectionName, DBObject q, DBObject orderBy) {

        if (!db.collectionExists(outputCollectionName)) {
            getCollection(collection).mapReduce(map, reduce, outputCollectionName, q);
        }

        return getCollection(outputCollectionName).find(null, new BasicDBObject("_id", false)).sort(orderBy).toArray();
    }

    /**
     * group分组分页查询操作，返回结果大于10,000keys时可以使用
     * 
     * @param collection
     *            集合
     * @param map
     *            映射javascript函数字符串，如：function(){ for(var key in this) { emit(key,{count:1}) } }
     * @param reduce
     *            reduce Javascript函数字符串，如：function(key,emits){ total=0; for(var i in emits){ total+=emits[i].count;  } return {count:total}; }
     * @param outputCollectionName
     *            输出结果表名称
     * @param q
     *            分组查询条件
     * @param orderBy
     *            分组查询排序
     * @param pageNo
     *            第n页
     * @param perPageCount
     *            每页记录数
     */
    public List<DBObject> mapReduce(String collection, String map, String reduce, String outputCollectionName, DBObject q, DBObject orderBy,
            int pageNo, int perPageCount) {

        if (!db.collectionExists(outputCollectionName)) {
            getCollection(collection).mapReduce(map, reduce, outputCollectionName, q);
        }

        return getCollection(outputCollectionName).find(null, new BasicDBObject("_id", false)).sort(orderBy).skip((pageNo - 1) * perPageCount)
                .limit(perPageCount).toArray();
    }

    public static void main(String[] args) {
        try {
            //			getInstance().insert("user",
            //				new BasicDBObject().append("name", "admin3").append("type", "2").append("score", 70)
            //					 .append("level", 2).append("inputTime", new Date().getTime()));
            //			getInstance().update("user", new BasicDBObject().append("status", 1), new BasicDBObject().append("status", 2));
            //=== group start =============
            //			StringBuilder sb = new StringBuilder(100); 
            //            sb.append("function(obj, out){out.count++;out.") 
            //                    .append("scoreSum").append("+=obj.") 
            //                    .append("score").append(";out.") 
            //                    .append("levelSum").append("+=obj.") 
            //                    .append("level").append('}'); 
            //            String reduce = sb.toString();
            //            BasicDBList list = getInstance().group("user", new BasicDBObject("type", true), 
            //					new BasicDBObject(), 
            //					new BasicDBObject().append("count", 0).append("scoreSum", 0).append("levelSum", 0).append("levelAvg", (Double) 0.0),
            //					reduce,
            //					"function(out){ out.levelAvg = out.levelSum / out.count }");
            //            for (Object o : list) {
            //            	DBObject obj = (DBObject)o;
            //    			System.out.println(obj);
            //            }
            //======= group end=========
            //=== mapreduce start =============
            //			Iterable<DBObject> list2 = getInstance().mapReduce("user", 
            //					"function(){emit( {type:this.type}, {score:this.score, level:this.level} );}", 
            //					"function(key,values){var result={score:0,level:0};var count = 0;values.forEach(function(value){result.score += value.score;result.level += value.level;count++});result.level = result.level / count;return result;}", 
            //					new BasicDBObject(), 
            //					new BasicDBObject("score",1));
            //			for (DBObject o : list2) {
            //				System.out.println(o);
            //			}

            //			List<DBObject> list3 = getInstance().mapReduce("user", 
            //					"function(){emit({type:this.type},{type:this.type,score:this.score,level:this.level});}", 
            //					"function(key,values){var result={type:key.type,score:0,level:0};var count=0;values.forEach(function(value){result.score+=value.score;result.level+=value.level;count++});result.level=result.level/count;return result;}", 
            //					"group_temp_user", 
            //					new BasicDBObject(), 
            //					new BasicDBObject("score",1));
            //			for (DBObject o : list3) {
            //				System.out.println(o);
            //			}
            //======= mapreduce end=========
            //			System.out.print(getInstance().findAll("user"));
            //			 System.out.print(getInstance().find("user",
            //					 new BasicDBObject("inputTime", new BasicDBObject("$gt",1348020002890L)),
            //					 new BasicDBObject().append("_id","-1"), 1, 2));
            //			getInstance().delete("user", new BasicDBObject());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
