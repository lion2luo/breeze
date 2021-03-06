/*
 * Generated by breeze-generator (https://github.com/weibreeze/breeze-generator)
 * Schema: testmsg.breeze
 * Date: 2019/6/18
 */
package com.weibo.breeze.test.message;

import com.weibo.breeze.*;
import com.weibo.breeze.message.Message;
import com.weibo.breeze.message.Schema;
import com.weibo.breeze.type.BreezeType;

import java.util.List;
import java.util.Map;

import static com.weibo.breeze.Breeze.getBreezeType;
import static com.weibo.breeze.type.Types.TYPE_INT32;
import static com.weibo.breeze.type.Types.TYPE_STRING;

public class TestMsg implements Message {
    private static final Schema breezeSchema = new Schema();
    private static BreezeType<Map<String, TestSubMsg>> myMapBreezeType;
    private static BreezeType<List<TestSubMsg>> myArrayBreezeType;
    private static BreezeType<TestSubMsg> subMsgBreezeType;
    private static BreezeType<MyEnum> myEnumBreezeType;
    private static BreezeType<List<MyEnum>> enumArrayBreezeType;

    static {
        try {
            breezeSchema.setName("motan.TestMsg")
                    .putField(new Schema.Field(1, "myInt", "int32"))
                    .putField(new Schema.Field(2, "myString", "string"))
                    .putField(new Schema.Field(3, "myMap", "map<string, TestSubMsg>"))
                    .putField(new Schema.Field(4, "myArray", "array<TestSubMsg>"))
                    .putField(new Schema.Field(5, "subMsg", "TestSubMsg"))
                    .putField(new Schema.Field(6, "myEnum", "MyEnum"))
                    .putField(new Schema.Field(7, "enumArray", "array<MyEnum>"));
            myMapBreezeType = getBreezeType(TestMsg.class, "myMap");
            myArrayBreezeType = getBreezeType(TestMsg.class, "myArray");
            subMsgBreezeType = getBreezeType(TestMsg.class, "subMsg");
            myEnumBreezeType = getBreezeType(TestMsg.class, "myEnum");
            enumArrayBreezeType = getBreezeType(TestMsg.class, "enumArray");
        } catch (BreezeException ignore) {
        }
        Breeze.putMessageInstance(breezeSchema.getName(), new TestMsg());
    }

    private int myInt;
    private String myString;
    private Map<String, TestSubMsg> myMap;
    private List<TestSubMsg> myArray;
    private TestSubMsg subMsg;
    private MyEnum myEnum;
    private List<MyEnum> enumArray;

    @Override
    public void writeToBuf(BreezeBuffer buffer) throws BreezeException {
        BreezeWriter.writeMessage(buffer, () -> {
            TYPE_INT32.writeMessageField(buffer, 1, myInt);
            TYPE_STRING.writeMessageField(buffer, 2, myString);
            myMapBreezeType.writeMessageField(buffer, 3, myMap);
            myArrayBreezeType.writeMessageField(buffer, 4, myArray);
            subMsgBreezeType.writeMessageField(buffer, 5, subMsg);
            myEnumBreezeType.writeMessageField(buffer, 6, myEnum);
            enumArrayBreezeType.writeMessageField(buffer, 7, enumArray);
        });
    }

    @Override
    public Message readFromBuf(BreezeBuffer buffer) throws BreezeException {
        BreezeReader.readMessage(buffer, (int index) -> {
            switch (index) {
                case 1:
                    myInt = TYPE_INT32.read(buffer);
                    break;
                case 2:
                    myString = TYPE_STRING.read(buffer);
                    break;
                case 3:
                    myMap = myMapBreezeType.read(buffer);
                    break;
                case 4:
                    myArray = myArrayBreezeType.read(buffer);
                    break;
                case 5:
                    subMsg = subMsgBreezeType.read(buffer);
                    break;
                case 6:
                    myEnum = myEnumBreezeType.read(buffer);
                    break;
                case 7:
                    enumArray = enumArrayBreezeType.read(buffer);
                    break;
                default: //skip unknown field
                    BreezeReader.readObject(buffer, Object.class);
            }
        });
        return this;
    }

    @Override
    public String messageName() {
        return breezeSchema.getName();
    }

    @Override
    public String messageAlias() {
        return breezeSchema.getAlias();
    }

    @Override
    public Schema schema() {
        return breezeSchema;
    }

    @Override
    public Message defaultInstance() {
        return new TestMsg();
    }

    public int getMyInt() {
        return myInt;
    }

    public TestMsg setMyInt(int myInt) {
        this.myInt = myInt;
        return this;
    }

    public String getMyString() {
        return myString;
    }

    public TestMsg setMyString(String myString) {
        this.myString = myString;
        return this;
    }

    public Map<String, TestSubMsg> getMyMap() {
        return myMap;
    }

    public TestMsg setMyMap(Map<String, TestSubMsg> myMap) {
        this.myMap = myMap;
        return this;
    }

    public List<TestSubMsg> getMyArray() {
        return myArray;
    }

    public TestMsg setMyArray(List<TestSubMsg> myArray) {
        this.myArray = myArray;
        return this;
    }

    public TestSubMsg getSubMsg() {
        return subMsg;
    }

    public TestMsg setSubMsg(TestSubMsg subMsg) {
        this.subMsg = subMsg;
        return this;
    }

    public MyEnum getMyEnum() {
        return myEnum;
    }

    public TestMsg setMyEnum(MyEnum myEnum) {
        this.myEnum = myEnum;
        return this;
    }

    public List<MyEnum> getEnumArray() {
        return enumArray;
    }

    public TestMsg setEnumArray(List<MyEnum> enumArray) {
        this.enumArray = enumArray;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestMsg testMsg = (TestMsg) o;

        if (myInt != testMsg.myInt) return false;
        if (myString != null ? !myString.equals(testMsg.myString) : testMsg.myString != null) return false;
        if (myMap != null ? !myMap.equals(testMsg.myMap) : testMsg.myMap != null) return false;
        if (myArray != null ? !myArray.equals(testMsg.myArray) : testMsg.myArray != null) return false;
        if (subMsg != null ? !subMsg.equals(testMsg.subMsg) : testMsg.subMsg != null) return false;
        if (myEnum != testMsg.myEnum) return false;
        return enumArray != null ? enumArray.equals(testMsg.enumArray) : testMsg.enumArray == null;
    }

    @Override
    public int hashCode() {
        int result = myInt;
        result = 31 * result + (myString != null ? myString.hashCode() : 0);
        result = 31 * result + (myMap != null ? myMap.hashCode() : 0);
        result = 31 * result + (myArray != null ? myArray.hashCode() : 0);
        result = 31 * result + (subMsg != null ? subMsg.hashCode() : 0);
        result = 31 * result + (myEnum != null ? myEnum.hashCode() : 0);
        result = 31 * result + (enumArray != null ? enumArray.hashCode() : 0);
        return result;
    }
}
