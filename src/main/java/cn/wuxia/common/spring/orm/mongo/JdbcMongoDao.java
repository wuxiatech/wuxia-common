package cn.wuxia.common.spring.orm.mongo;

public class JdbcMongoDao {
//    protected static Logger logger = LoggerFactory.getLogger(JdbcMongoDao.class);
//
//    private static final int DEFAULT_SKIP = 0;
//
//    private static final int DEFAULT_LIMIT = 200;
//
//    /**
//     * 如非实体存储的时候需要制定collection name
//     * 
//     * @return
//     */
//    protected String collectionName;
//
//    protected MongoCollection mongoCollection;
//
//    public JdbcMongoDao(String collectionName) {
//        this.collectionName = collectionName;
//        Properties p = PropertiesUtils.loadProperties("classpath:mongodb.properties");
//        MongoClient mg = new MongoClient(p.getProperty("mongo.host"), Integer.parseInt(p.getProperty("mongo.port")));
//        MongoDatabase db = mg.getDatabase(p.getProperty("mongo.database"));
//        mongoCollection = db.getCollection(collectionName);
//    }
//
//    public JdbcMongoDao(String host, String port, String database, String collectionName) {
//        this.collectionName = collectionName;
//        MongoClient mg = new MongoClient(host, Integer.valueOf(port));
//        MongoDatabase db = mg.getDatabase(database);
//        mongoCollection = db.getCollection(collectionName);
//    }
//
//    /**
//     * ----------------------------------分割线------------------------------------
//     */
//
//    /**
//     * 插入
//     * 
//     * @param collection
//     * @param o
//     *            插入
//     */
//    public void save(Document o) {
//        mongoCollection.insertOne(o);
//    }
//
//    /**
//     * 批量插入
//     * 
//     * @param collection
//     * @param list
//     *            插入的列表
//     */
//    public void batchSave(List<Document> list) {
//
//        if (list == null || list.isEmpty()) {
//            return;
//        }
//
//        mongoCollection.insertMany(list);
//
//    }
//
//    /**
//     * 删除
//     * 
//     * @param collection
//     * @param q
//     *            查询条件
//     */
//    public void delete(Document q) {
//
//        mongoCollection.deleteOne(q);
//    }
//
//    /**
//     * 批量删除
//     * 
//     * @param collection
//     * @param list
//     *            删除条件列表
//     */
//    public void deleteBatch(Document list) {
//
//        if (list == null || list.isEmpty()) {
//            return;
//        }
//
//        mongoCollection.deleteMany(list);
//    }
//
//    /**
//     * 计算集合总条数
//     * 
//     * @param collection
//     */
//    public long getCount(String collection) {
//        return mongoCollection.count();
//    }
//
//    /**
//     * 计算满足条件条数
//     * 
//     * @param collection
//     * @param q
//     *            查询条件
//     */
//
//    public long getCount(Document q) {
//        return mongoCollection.count(q);
//    }
//
//    /**
//     * 更新
//     * 
//     * @param collection
//     * @param q
//     *            查询条件
//     * @param setFields
//     *            更新对象
//     */
//    public void update(Document q, Document setFields) {
//        mongoCollection.updateMany(q, new Document("$set", setFields));
//    }
//
//    /**
//     * 查找集合所有对象
//     * 
//     * @param collection
//     */
//    public List<Document> findAll(String collection) {
//        return IteratorUtils.toList(mongoCollection.find().iterator());
//    }
//
//    /**
//     * 按顺序查找集合所有对象
//     * 
//     * @param collection
//     *            数据集
//     * @param orderBy
//     *            排序
//     */
//    public List<Document> findAll(Document orderBy) {
//        return IteratorUtils.toList(mongoCollection.find().sort(orderBy).iterator());
//    }
//
//    /**
//     * 查找（返回一个对象）
//     * 
//     * @param collection
//     * @param q
//     *            查询条件
//     */
//    public Document findOne(Document q) {
//        return (Document) mongoCollection.find(q).first();
//    }
//
//    /**
//     * 分页查找集合对象，返回特定字段
//     * 
//     * @param collection
//     * @param q
//     *            查询条件
//     * @param pages  每页记录数
//     */
//    public Pages findPage(Document q, Pages pages) {
//        com.mongodb.client.FindIterable ita = mongoCollection.find(q).skip(pages.getFirst()).limit(pages.getPageSize());
//        pages.setTotalCount(mongoCollection.count(q));
//        pages.setResult(IteratorUtils.toList(ita.iterator()));
//        return pages;
//    }
//
//    /**
//     * 查找（返回一个List<DBObject>）
//     * 
//     * @param collection
//     * @param q
//     *            查询条件
//     */
//    public List<Document> find(Document q) {
//
//        FindIterable c = mongoCollection.find(q);
//        if (c != null)
//            return IteratorUtils.toList(c.iterator());
//        else
//            return null;
//    }
//
//    /**
//     * 获取需要操作的实体类class
//     * 
//     * @return
//     */
//    private Class<T> getEntityClass() {
//        return ReflectionUtil.getSuperClassGenricType(getClass());
//    }

}
