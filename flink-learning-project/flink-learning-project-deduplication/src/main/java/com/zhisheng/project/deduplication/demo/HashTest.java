package com.zhisheng.project.deduplication.demo;

import com.google.common.hash.Hashing;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * Created on 2019-11-16
 *
 * @author :hao.li
 */
public class HashTest {

    public static String md5Test(String primaryKey) {
        return DigestUtils.md5Hex(primaryKey).substring(0, 4) + "_" + primaryKey;
    }

    public static String murmur3Test(String primaryKey) {
        return Hashing.murmur3_32().hashString(primaryKey, StandardCharsets.UTF_8).toString() +
                "_" + primaryKey;
    }

    public static void main(String[] args) {
        String lihao = murmur3Test("lihao");
        System.out.println(lihao);
        long lihao1 = Hashing.murmur3_128(5).hashUnencodedChars("lihao").asLong();
        System.out.println(lihao1);
    }
}

