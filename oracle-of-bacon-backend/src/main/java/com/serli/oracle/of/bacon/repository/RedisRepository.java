package com.serli.oracle.of.bacon.repository;

import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisRepository {
    private final Jedis jedis;

    public static final String INDEX = "oob:queried";

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }

    public List<String> getLastTenSearches() {
        return jedis.lrange(INDEX, 0, 10);
    }

    public void setLastSearched(String actorName) {
        if (jedis.lrange(INDEX, 0, 10).size() == 10) {
            jedis.rpop(INDEX);
        }

        jedis.lpushx(INDEX, actorName);
    }
}
