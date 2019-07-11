package com.intellif.serializer;

/**
 * 序列化传输对象，目前采用最简单的json来序列，以后考虑使用类似protobuf的高效序列化方式
 *
 * @author inori
 * @create 2019-07-11 20:40
 */
public interface Serializer {

    void decode();

    void encode();

}