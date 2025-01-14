package com.lihao.redis.demo.github.demo;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.SortingParams;

public class SomeOperate {

	/*
	 * 2018/01/05 测试Redis Setbit命令
	 * 用于对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit)
	 */
	public static void SetBitOperate(Jedis jedis) {
		System.out.println("==================setBit======================");
		int oneDayActiveUser = uniqueCount(jedis, "play", "2018-01-06");
		System.out.println("2018-01-06: 活跃用户为：" + oneDayActiveUser);
	}

	/*
	 * 计算出一天的活跃用户数量
	 */
	public static int uniqueCount(Jedis jedis, String action, String date) {
		String key = action + ":" + date;
		System.out.println(key + " "+key.getBytes() + " " +jedis.get(key.getBytes()));
		BitSet user = BitSet.valueOf(jedis.get(key.getBytes()));
		return user.cardinality();// 返回该字符集被置为1的个数
	}

	/*
	 * 计算某几天内活跃用户的数量（某一天活跃就算，所以是取并集），相比于用set来实现更省内存
	 */
	public static int allUniqueCount(Jedis jedis, String action, String... dates) {
		BitSet all = new BitSet();
		for (String date : dates) {
			String key = action + ":" + date;
			BitSet user = BitSet.valueOf(jedis.get(key.getBytes()));
			all.or(user);
		}
		return all.cardinality();// 返回该字符集被置为1的个数
	}

	/*
	 * 2017/11/26 测试普通模式与PipeLine模式的效率： 测试方法：向redis中插入10000组数据
	 */
	public static void testPipeLineAndNormal(Jedis jedis)
			throws InterruptedException {
		Logger logger = Logger.getLogger("javasoft");
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			jedis.set(String.valueOf(i), String.valueOf(i));
		}
		long end = System.currentTimeMillis();
		logger.info("the jedis total time is:" + (end - start));

		Pipeline pipe = jedis.pipelined();// 先创建一个pipeline的链接对象
		long start_pipe = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			pipe.set(String.valueOf(i), String.valueOf(i));
		}
		pipe.sync();// 获取所有的response
		long end_pipe = System.currentTimeMillis();
		logger.info("the pipe total time is:" + (end_pipe - start_pipe));

		BlockingQueue<String> logQueue = new LinkedBlockingQueue<String>();
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			logQueue.put("i=" + i);
		}
		long stop = System.currentTimeMillis();
		logger.info("the BlockingQueue total time is:" + (stop - begin));
	}

	/*
	 * 2017/11/25 新增pipeline及redis的事务特性
	 */
	public static void PipelineTransactions(Jedis jedis, JedisPool jedisPool) {
		try {
			Pipeline pipeLine = jedis.pipelined();
			pipeLine.set("value", "100");
			pipeLine.watch("value");
			pipeLine.multi();// 开启事务
			pipeLine.incrBy("value", 10);// 递增10
			// 对错误的数据类型使用了不支持的操作
			// pipeLine.lpush("value", "error");//执行错误的操作lpush
			pipeLine.incrBy("value", 10);// 再次递增10
			// 执行exec命令,获取"未来"的返回结果
			Response<List<Object>> listResponse = pipeLine.exec();
			pipeLine.sync();// 开启pipeling
			List<Object> result = listResponse.get();
			if (result != null && result.size() > 0) {
				for (Object o : result)
					System.out.println(o.toString());
			}
			// 虽然事务中第二个操作失败了,但不影响value的值
			System.out.println("\nvalue is " + jedis.get("value"));
		} catch (Exception e) {
			// jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/*
	 * 常用的键操作
	 */
	public static void KeyOperate(Jedis jedis, ShardedJedis shardedJedis) {

		System.out.println("==================key======================");

		// flushDB():删除当前选择数据库中的所有key
		// flushall()：删除所有数据库中的所有key
		System.out.println("清空库中所有数据：" + jedis.flushDB());

		// exists(key):判断key否存在
		System.out.println("判断key999键是否存在：" + shardedJedis.exists("key999"));
		System.out.println("新增键值对：" + shardedJedis.set("key001", "value001"));
		System.out.println("判断key001是否存在：" + shardedJedis.exists("key001"));
		System.out.println("新增键值对：" + shardedJedis.set("key002", "value002"));

		System.out.println("系统中所有键如下：");
		// keys(pattern)：返回满足给定pattern的所有key
		Set<String> keys = jedis.keys("*");
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String key = it.next();
			System.out.println(key);
		}

		// del(key):删除某个key,若key不存在，则忽略该命令。
		// Problem1：为什么删除的Jedis的，而shardedJedis的也删除了？？？？？？？？？？？？？？？
		System.out.println("系统中删除key002: " + jedis.del("key002"));
		System.out.println("判断key002是否存在：" + shardedJedis.exists("key002"));

		// expire(key,time)：设定一个key的活动时间（s）
		System.out.println("设置 key001的过期时间为5秒:" + jedis.expire("key001", 5));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}

		// ttl(key):查看某个key的剩余生存时间,单位【秒】永久生存或者不存在的都返回-1
		System.out.println("查看key001的剩余生存时间：" + jedis.ttl("key001"));
		// 移除某个key的生存时间
		System.out.println("移除key001的生存时间：" + jedis.persist("key001"));
		System.out.println("查看key001的剩余生存时间：" + jedis.ttl("key001"));

		// Problem2：如果一个键过期了，它还存在？？？？？？？？
		System.out.println("！！！！：" + jedis.get("key001"));
		// rename(oldname, newname)：重命名key
		System.out.println("重命名：" + jedis.rename("key001", "newkey001"));

		// type(key)查看key所储存的值的类型
		System.out.println("查看key所储存的值的类型：" + jedis.type("newkey001"));
	}

	/*
	 * 常用的String类型操作
	 */
	public static void StringOperate(Jedis jedis, ShardedJedis shardedJedis) {

		System.out.println("==================String_1======================");
		System.out.println("清空库中所有数据：" + jedis.flushDB());

		System.out.println("=============增=============");
		jedis.set("key001", "value001");
		jedis.set("key002", "value002");
		jedis.set("key003", "value003");
		System.out.println("已新增的3个键值对如下：");
		System.out.println("key001 -> " + jedis.get("key001"));
		System.out.println("key002 -> " + jedis.get("key002"));
		System.out.println("key003 -> " + jedis.get("key003"));

		System.out.println("=============删=============");
		System.out.println("删除key003键值对：" + jedis.del("key003"));
		System.out.println("获取key003键对应的值：" + jedis.get("key003"));

		System.out.println("=============改=============");
		// 1、直接覆盖原来的数据
		System.out.println("覆盖key001原来的数据："
				+ jedis.set("key001", "value001-update"));
		System.out.println("获取key001对应的新值：" + jedis.get("key001"));
		// 2、append(key, value)：名称为key的string的值附加value
		System.out.println("在key002原来值后面追加："
				+ jedis.append("key002", "+appendString"));
		System.out.println("获取key002对应的新值" + jedis.get("key002"));

		System.out.println("=============增，删，查（多个）=============");
		/**
		 * mset(key N, value N)：批量设置多个string的值 mget(key1, key2,…,
		 * keyN)：返回库中多个string的value
		 */
		System.out.println("一次性新增key201,key202,key203,key204及其对应值："
				+ jedis.mset("key201", "value201", "key202", "value202",
				"key203", "value203", "key204", "1"));

		System.out.println("一次性获取key201,key202,key203,key204各自对应的值："
				+ jedis.mget("key201", "key202", "key203", "key204"));

		System.out.println("一次性删除key201,key202："
				+ jedis.del(new String[] { "key201", "key202" }));

		System.out.println("一次性获取key201,key202,key203,key204各自对应的值："
				+ jedis.mget("key201", "key202", "key203", "key204"));
		// incr(key)：名称为key的string增1操作; decr(key)：名称为key的string减1操作
		// incrby(key, integer)：名称为key的string增加integer
		System.out.println("key204新值：" + jedis.incr("key204"));
		System.out.println();

		// jedis具备的功能shardedJedis中也可直接使用
		System.out.println("==================String_2=====================");
		System.out.println("清空库中所有数据：" + jedis.flushDB());

		System.out.println("=============新增键值对时防止覆盖原先值============");
		//setnx:如果key已经存在，返回0，nx是not exist的意思。
		System.out.println("原先key301不存在时，新增key301："
				+ shardedJedis.setnx("key301", "value301"));
		System.out.println("原先key302不存在时，新增key302："
				+ shardedJedis.setnx("key302", "value302"));
		System.out.println("当key302存在时，尝试新增key302："
				+ shardedJedis.setnx("key302", "value302_new"));
		System.out.println("获取key301对应的值：" + shardedJedis.get("key301"));
		System.out.println("获取key302对应的值：" + shardedJedis.get("key302"));

		System.out.println("=============超过有效期键值对被删除=============");
		// setex(key, time, value)：向库中添加string，设定过期时间time
		System.out.println("新增key303，并指定过期时间为2秒"
				+ shardedJedis.setex("key303", 2, "key303-2second"));
		System.out.println("获取key303对应的值：" + shardedJedis.get("key303"));
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		System.out.println("3秒之后，获取key303对应的值：" + shardedJedis.get("key303"));

		System.out.println("===========获取原值，更新为新值一步完成============");
		System.out.println("key302原值："
				+ shardedJedis.getSet("key302", "value302-after-getset"));
		System.out.println("key302新值：" + shardedJedis.get("key302"));

		System.out.println("=============获取子串=============");
		System.out.println("获取key302对应值中的子串："
				+ shardedJedis.getrange("key302", 5, 7));
		System.out.println("获取key302对应值中的子串："
				+ shardedJedis.getrange("key302", -5, -1));// 字符串右面下标是从-1开始的
	}

	/*
	 * 常用的List类型操作
	 * 场景：LIST可以作为消息队列，LPUSH链表头作为生产者插入消息，RPOP作为消费者取得消息。
	 */
	public static void ListOperate(Jedis jedis, ShardedJedis shardedJedis) {

		System.out.println("===================list=======================");
		System.out.println("清空库中所有数据：" + jedis.flushDB());

		System.out.println("=============增=============");
		// rpush(key, value)：在名称为key的list尾添加一个值为value的元素
		// lpush(key, value)：在名称为key的list头添加一个值为value的元素
		shardedJedis.lpush("stringlists", "vector");
		shardedJedis.lpush("stringlists", "ArrayList");
		shardedJedis.lpush("stringlists", "vector");
		shardedJedis.lpush("stringlists", "vector");
		shardedJedis.lpush("stringlists", "LinkedList");
		shardedJedis.lpush("stringlists", "MapList");
		shardedJedis.lpush("stringlists", "SerialList");
		shardedJedis.lpush("stringlists", "HashList");
		shardedJedis.rpush("stringlists", "TestList");
		shardedJedis.lpush("numberlists", "3");
		shardedJedis.lpush("numberlists", "1");
		shardedJedis.lpush("numberlists", "5");
		shardedJedis.lpush("numberlists", "2");

		// lrange(key, start, end)：返回名称为key的list中start至end之间的元素
		System.out.println("所有元素-stringlists："
				+ shardedJedis.lrange("stringlists", 0, -1));
		System.out.println("所有元素-numberlists："
				+ shardedJedis.lrange("numberlists", 0, -1));
		System.out.println("=============删=============");
		// lrem(key, count, value)：删除count个key的list中值为value的元素
		// ，第二个参数为删除的个数,（有重复时）若count>0时按从头到尾的顺序删除，类似于出栈；
		// 若count<0时，按从尾到头的顺序删除；count=0时，删除全部
		System.out.println("成功删除指定元素个数-stringlists："
				+ shardedJedis.lrem("stringlists", 2, "vector"));
		System.out.println("删除指定元素之后-stringlists："
				+ shardedJedis.lrange("stringlists", 0, -1));
		// 截取指定区间的数据
		System.out.println("删除下标0-3区间之外的元素："
				+ shardedJedis.ltrim("stringlists", 0, 3));
		System.out.println("删除指定区间之外元素后-stringlists："
				+ shardedJedis.lrange("stringlists", 0, -1));
		// lpop(key)：返回并删除名称为key的list中的首元素
		// rpop(key)：返回并删除名称为key的list中的尾元素
		System.out.println("出栈元素：" + shardedJedis.lpop("stringlists"));
		System.out.println("元素出栈后-stringlists："
				+ shardedJedis.lrange("stringlists", 0, -1));

		System.out.println("=============改=============");
		// 修改列表中指定下标的值
		shardedJedis.lset("stringlists", 0, "hello list!");
		System.out.println("下标为0的值修改后-stringlists："
				+ shardedJedis.lrange("stringlists", 0, -1));

		System.out.println("=============查=============");
		// llen(key)：返回名称为key的list的长度
		System.out
				.println("长度-stringlists：" + shardedJedis.llen("stringlists"));
		System.out
				.println("长度-numberlists：" + shardedJedis.llen("numberlists"));
		// lindex(key, index)：返回名称为key的list中index位置的元素
		System.out.println("stringlists中第三个元素： "
				+ shardedJedis.lindex("stringlists", 2));

		// 排序
		/*
		 * list中存字符串时必须指定参数为alpha，如果不使用SortingParams，而是直接使用sort("list")，
		 * 会出现"ERR One or more scores can't be converted into double"
		 */
		SortingParams sortingParameters = new SortingParams();
		sortingParameters.alpha();// 按字典序排序
		// limit(int start, int count) 限制返回元素的个数
		sortingParameters.limit(0, 3);
		System.out.println("返回排序后的结果-stringlists："
				+ shardedJedis.sort("stringlists", sortingParameters));
		System.out.println("返回排序后的结果-numberlists："
				+ shardedJedis.sort("numberlists"));
		// 子串： start为元素下标，end也为元素下标；-1代表倒数一个元素，-2代表倒数第二个元素
		// 注意：虽然先进行了排序操作，但此处仍然输出的是原来的存储顺序
		System.out.println("子串-第二个开始到结束："
				+ shardedJedis.lrange("stringlists", 1, -1) + "\n");
	}

	/*
	 * 常用的Set类型操作
	 */
	public static void SetOperate(Jedis jedis, ShardedJedis shardedJedis) {
		System.out.println("================set=================");
		System.out.println("清空库中所有数据：" + jedis.flushDB());

		System.out.println("=============增=============");
		System.out.println("向sets集合中加入元素element001："
				+ jedis.sadd("sets", "element001"));
		System.out.println("向sets集合中加入元素element002："
				+ jedis.sadd("sets", "element002"));
		System.out.println("向sets集合中加入元素element003："
				+ jedis.sadd("sets", "element003"));
		System.out.println("向sets集合中加入元素element004："
				+ jedis.sadd("sets", "element004"));
		System.out.println("向sets集合中加入元素element001："
				+ jedis.sadd("sets", "element001"));
		System.out.println("查看sets集合中的所有元素:" + jedis.smembers("sets"));
		System.out.println();

		System.out.println("=============删=============");
		System.out.println("集合sets中删除元素element003："
				+ jedis.srem("sets", "element003"));
		// smembers(key) ：返回名称为key的set的所有元素
		System.out.println("查看sets集合中的所有元素:" + jedis.smembers("sets"));
		/*
		 * System.out.println("sets集合中任意位置的元素出栈："+jedis.spop("sets")); --无实际意义
		 * System.out.println("查看sets集合中的所有元素:"+jedis.smembers("sets"));
		 */
		System.out.println();

		System.out.println("=============查=============");
		// sismember(key, member) ：member是否是名称为key的set的元素
		System.out.println("判断element001是否在集合sets中："
				+ jedis.sismember("sets", "element001"));
		// scard(key) ：返回名称为key的set的基数
		System.out.println("基数： " + jedis.scard("sets"));
		System.out.println("循环查询获取sets中的每个元素：");
		Set<String> set = jedis.smembers("sets");
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			System.out.println(obj);
		}
		System.out.println();

		System.out.println("=============集合运算=============");
		System.out.println("sets1中添加元素element001："
				+ jedis.sadd("sets1", "element001"));
		System.out.println("sets1中添加元素element002："
				+ jedis.sadd("sets1", "element002"));
		System.out.println("sets1中添加元素element003："
				+ jedis.sadd("sets1", "element003"));

		System.out.println("sets2中添加元素element002："
				+ jedis.sadd("sets2", "element002"));
		System.out.println("sets2中添加元素element003："
				+ jedis.sadd("sets2", "element003"));
		System.out.println("sets2中添加元素element004："
				+ jedis.sadd("sets2", "element004"));

		System.out.println("查看sets1集合中的所有元素:" + jedis.smembers("sets1"));
		System.out.println("查看sets2集合中的所有元素:" + jedis.smembers("sets2"));
		System.out.println("sets1和sets2交集：" + jedis.sinter("sets1", "sets2"));
		System.out.println("sets1和sets2并集：" + jedis.sunion("sets1", "sets2"));
		// 记A，B是两个集合，则所有属于A且不属于B的元素构成的集合,为“差集”
		System.out.println("sets1和sets2差集：" + jedis.sdiff("sets1", "sets2"));
	}

	/*
	 * 常用的SortedSet类型操作
	 * 每个元素都会关联一个double类型的分数。redis正是通过分数来为集合中的成员进行从小到大的排序
	 * 集合是通过哈希表实现的，所以添加，删除，查找的复杂度都是O(1)
	 */
	public static void SortedSetOperate(Jedis jedis, ShardedJedis shardedJedis) {
		System.out.println("====================zset======================");
		System.out.println(jedis.flushDB());

		System.out.println("=============增=============");
		System.out.println("zset中添加元素element001："
				+ shardedJedis.zadd("zset", 7.0, "element001"));
		System.out.println("zset中添加元素element002："
				+ shardedJedis.zadd("zset", 8.0, "element002"));
		System.out.println("zset中添加元素element003："
				+ shardedJedis.zadd("zset", 2.0, "element003"));
		//element004被设置了2次，那么将以最后一次的设置为准
		System.out.println("zset中添加元素element004："
				+ shardedJedis.zadd("zset", 3.0, "element004"));
		System.out.println("zset中添加元素element004："
				+ shardedJedis.zadd("zset", 1.0, "element004"));
		System.out
				.println("zset集合中的所有元素：" + shardedJedis.zrange("zset", 0, -1));// 按照权重值排序
		System.out.println();

		System.out.println("=============删=============");
		System.out.println("zset中删除元素element002："
				+ shardedJedis.zrem("zset", "element002"));
		System.out
				.println("zset集合中的所有元素：" + shardedJedis.zrange("zset", 0, -1));
		System.out.println();

		System.out.println("=============查=============");
		System.out.println("统计zset集合中的元素中个数：" + shardedJedis.zcard("zset"));
		System.out.println("统计zset集合中权重某个范围内（1.0——5.0），元素的个数："
				+ shardedJedis.zcount("zset", 1.0, 5.0));
		System.out.println("查看zset集合中element004的权重："
				+ shardedJedis.zscore("zset", "element004"));
		System.out.println("查看下标1到2范围内的元素值："
				+ shardedJedis.zrange("zset", 1, 2));
	}

	/*
	 * 常用的Hash类型操作
	 */
	public static void HashOperate(Jedis jedis, ShardedJedis shardedJedis) {

		System.out.println("================hash===================");
		System.out.println(jedis.flushDB());

		System.out.println("=============增=============");
		// hset(key, field, value)：向名称为key的hash中添加元素field
		System.out.println("hashs中添加key001和value001键值对："
				+ shardedJedis.hset("hashs", "key001", "value001"));
		System.out.println("hashs中添加key002和value002键值对："
				+ shardedJedis.hset("hashs", "key002", "value002"));
		System.out.println("hashs中添加key003和value003键值对："
				+ shardedJedis.hset("hashs", "key003", "value003"));
		// hincrby(key,field,integer)：将名称为key的hash中field的value增加integer
		System.out.println("新增key004和4的整型键值对："
				+ shardedJedis.hincrBy("hashs", "key004", 4l));
		// hvals(key)：返回名称为key的hash中所有键对应的value
		System.out.println("hashs中的所有值：" + shardedJedis.hvals("hashs"));
		System.out.println();

		System.out.println("=============删=============");
		System.out.println("hashs中删除key002键值对："
				+ shardedJedis.hdel("hashs", "key002"));
		System.out.println("hashs中的所有值：" + shardedJedis.hvals("hashs"));
		System.out.println();

		System.out.println("=============改=============");
		System.out.println("key004整型键值的值增加100："
				+ shardedJedis.hincrBy("hashs", "key004", 100l));
		System.out.println("hashs中的所有值：" + shardedJedis.hvals("hashs"));
		System.out.println();

		System.out.println("=============查=============");
		System.out.println("判断key003是否存在："
				+ shardedJedis.hexists("hashs", "key003"));
		System.out.println("获取key004对应的值："
				+ shardedJedis.hget("hashs", "key004"));
		// hlen(key)：返回名称为key的hash中元素个数
		System.out.println("hashs中元素个数：" + shardedJedis.hlen("hashs"));
		System.out.println("批量获取key001和key003对应的值："
				+ shardedJedis.hmget("hashs", "key001", "key003"));
		System.out.println("获取hashs中所有的key：" + shardedJedis.hkeys("hashs"));
		System.out.println("获取hashs中所有的value：" + shardedJedis.hvals("hashs"));
		// hgetAll(key)：返回名称为key的hash中所有的键（field）及其对应的value
		System.out.println("获取hashs中所有的key-value："
				+ shardedJedis.hgetAll("hashs"));
		System.out.println();
	}
	// 在redis2.8.9版本以上即可运行，否则没有对应的方法。
//	public static void HyperLogLogOperate(Jedis jedis){
//		//HyperLogLog
//		for(int i =0; i <10; i++)
//		{
//		System.out.println("pfadd :"+ jedis.pfadd("HyperLogLog", UUID.randomUUID().toString()));
//		}
//		System.out.println("pfcount :"+ jedis.pfcount("HyperLogLog"));
//	}
}
