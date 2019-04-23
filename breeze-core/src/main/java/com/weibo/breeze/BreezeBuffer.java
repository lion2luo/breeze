/*
 *
 *   Copyright 2009-2016 Weibo, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.weibo.breeze;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanglei28 on 2017/6/27.
 */
@SuppressWarnings("all")
public class BreezeBuffer {

    public static int encodeZigzag32(int value) {
        return (value << 1) ^ (value >> 31);
    }

    public static long encodeZigzag64(long value) {
        return (value << 1) ^ (value >> 63);
    }

    public static int decodeZigzag32(int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    public static long decodeZigzag64(long n) {
        return (n >>> 1) ^ -(n & 1);
    }

    private ByteBuffer buf;
    private WriteCountor writeCountor;

    public BreezeBuffer(int initSize) {
        this.buf = ByteBuffer.allocate(initSize);
    }

    public BreezeBuffer(ByteBuffer buf) {
        this.buf = buf;
    }

    public BreezeBuffer(byte[] bytes){
        this.buf = ByteBuffer.wrap(bytes);
    }

    public void put(byte b) {
        ensureBufferEnough(1);
        buf.put(b);
    }

    public void put(int index, byte b) {
        buf.put(index, b);
    }

    public void put(byte[] b) {
        ensureBufferEnough(b.length);
        buf.put(b);
    }

    public void putShort(short value) {
        ensureBufferEnough(2);
        buf.putShort(value);
    }

    public void putShort(int index, short value) {
        buf.putShort(index, value);
    }

    public void putInt(int value) {
        ensureBufferEnough(4);
        buf.putInt(value);
    }

    public void putInt(int index, int value) {
        buf.putInt(index, value);
    }

    public void putLong(long value) {
        ensureBufferEnough(8);
        buf.putLong(value);
    }

    public void putLong(int index, long value) {
        buf.putLong(index, value);
    }

    public void putFloat(float value) {
        ensureBufferEnough(4);
        buf.putFloat(value);
    }

    public void putFloat(int index, float value) {
        buf.putFloat(index, value);
    }

    public void putDouble(double value) {
        ensureBufferEnough(8);
        buf.putDouble(value);
    }

    public void putDouble(int index, double value) {
        buf.putDouble(index, value);
    }

    public int putZigzag32(int value) {
        return putVarint(encodeZigzag32(value));
    }

    public int putZigzag64(long value) {
        return putVarint(encodeZigzag64(value));
    }

    public int putVarint(long value) {
        int count = 0;
        while (true) {
            count++;
            if ((value & ~0x7fL) == 0) {
                put((byte) value);
                break;
            } else {
                put((byte) ((value & 0x7f) | 0x80));
                value >>>= 7;
            }
        }
        return count;
    }

    public byte get() {
        return buf.get();
    }

    public byte get(int index) {
        return buf.get(index);
    }

    public void get(byte[] dst) {
        buf.get(dst);
    }

    /**
     * get bytes remained in buf.
     * this method always return a new copy of buf bytes.
     * @return
     */
    public byte[] getBytes(){
        byte[] result = new byte[buf.remaining()];
        buf.get(result);
        return result;
    }

    public short getShort() {
        return buf.getShort();
    }

    public short getShort(int index) {
        return buf.getShort(index);
    }

    public int getInt() {
        return buf.getInt();
    }

    public int getInt(int index) {
        return buf.getInt(index);
    }

    public long getLong() {
        return buf.getLong();
    }

    public long getLong(int index) {
        return buf.getLong(index);
    }

    public float getFloat() {
        return buf.getFloat();
    }

    public float getFloat(int index) {
        return buf.getFloat(index);
    }

    public double getDouble() {
        return buf.getDouble();
    }

    public double getDouble(int index) {
        return buf.getDouble(index);
    }

    public int getZigzag32() {
        return decodeZigzag32((int) getVarint());
    }

    public long getZigzag64() {
        return decodeZigzag64(getVarint());
    }

    public long getVarint() {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            final byte b = buf.get();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw new RuntimeException("Integer overflow");
    }

    public void flip() {
        buf.flip();
    }

    public int position() {
        return buf.position();
    }

    public void position(int newPosition) {
        ensureBufferEnough(newPosition - buf.position());
        buf.position(newPosition);
    }

    public int limit() {
        return buf.limit();
    }

    public void limit(int newLimit) {
        buf.limit(newLimit);
    }

    public int capacity() {
        return buf.capacity();
    }

    public int remaining() {
        return buf.remaining();
    }

    public void clear() {
        buf.clear();
    }

    public int writeCount(int hash){
        if (writeCountor == null){
            writeCountor = new WriteCountor();
        }
        return writeCountor.put(hash);
    }

    private ByteBuffer grow(int size) {
        ByteBuffer newbuf = ByteBuffer.allocate(size);
        newbuf.put(buf.array());
        newbuf.position(buf.position());
        return newbuf;
    }

    private void ensureBufferEnough(int need) {
        int expandSize = buf.position() + need;
        if (buf.capacity() < expandSize) {
            int size = buf.capacity() * 2;
            while (size < expandSize) {
                size = size * 2;
            }
            buf = grow(size);
        }
    }

    public static class WriteCountor{
        private Map<Integer, Integer> map = new HashMap<>();
        public int put(int hash){
            Integer count = map.get(hash);
            if (count == null){
               count = 0;
            }
            count = count +1;
            map.put(hash, count);
            return count;
        }
    }
}
