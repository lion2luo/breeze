/*
 * Generated by breeze-generator (https://github.com/weibreeze/breeze-generator)
 * Schema: testmsg.breeze
 * Date: 2019/7/9
 */
package com.weibo.breeze.test.message;

import com.weibo.breeze.*;
import com.weibo.breeze.serializer.Serializer;

import static com.weibo.breeze.type.Types.TYPE_INT32;

public enum MyEnum {
    E1(1),
    E2(2),
    E3(3);

    static {
        try {
            Breeze.registerSerializer(new MyEnumSerializer());
        } catch (BreezeException ignore) {
        }
    }

    private int number;

    MyEnum(int number) {
        this.number = number;
    }

    public static class MyEnumSerializer implements Serializer<MyEnum> {
        private static final String[] names = new String[]{"motan.MyEnum", MyEnum.class.getName()};

        @Override
        public void writeToBuf(MyEnum obj, BreezeBuffer buffer) throws BreezeException {
            BreezeWriter.writeMessage(buffer, () -> {
                TYPE_INT32.writeMessageField(buffer, 1, obj.number);
            });
        }

        @Override
        public MyEnum readFromBuf(BreezeBuffer buffer) throws BreezeException {
            int[] number = new int[]{-1};
            BreezeReader.readMessage(buffer, (int index) -> {
                switch (index) {
                    case 1:
                        number[0] = TYPE_INT32.read(buffer);
                        break;
                    default:
                        BreezeReader.readObject(buffer, Object.class);
                }
            });
            switch (number[0]) {
                case 1:
                    return E1;
                case 2:
                    return E2;
                case 3:
                    return E3;
            }
            throw new BreezeException("unknown enum number:" + number[0]);
        }

        @Override
        public String[] getNames() {
            return names;
        }
    }
}
