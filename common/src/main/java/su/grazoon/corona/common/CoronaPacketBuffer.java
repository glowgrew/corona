package su.grazoon.corona.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public class CoronaPacketBuffer extends ByteBuf {

    private final ByteBuf delegate;

    public CoronaPacketBuffer(ByteBuf buffer) {
        this.delegate = buffer;
    }

    public static int getVarIntSize(int input) {
        for (int i = 1; i < 5; ++i) {
            if ((input & -1 << i * 7) == 0x0) {
                return i;
            }
        }
        return 5;
    }

    public CoronaPacketBuffer writeByteArray(byte[] array) {
        writeVarInt(array.length);
        writeBytes(array);
        return this;
    }

    public byte[] readByteArray() {
        return readByteArray(readableBytes());
    }

    public byte[] readByteArray(int maxLength) {
        int i = readVarInt();
        if (i > maxLength) {
            throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + maxLength);
        }
        byte[] bytes = new byte[i];
        readBytes(bytes);
        return bytes;
    }

    public CoronaPacketBuffer writeVarIntArray(int[] array) {
        writeVarInt(array.length);
        for (int i : array) {
            writeVarInt(i);
        }
        return this;
    }

    public int[] readVarIntArray() {
        return readVarIntArray(readableBytes());
    }

    public int[] readVarIntArray(int maxLength) {
        int i = readVarInt();
        if (i > maxLength) {
            throw new DecoderException("VarIntArray with size " + i + " is bigger than allowed " + maxLength);
        }
        int[] ints = new int[i];
        for (int j = 0; j < ints.length; ++j) {
            ints[j] = readVarInt();
        }
        return ints;
    }

    public CoronaPacketBuffer writeLongArray(long[] array) {
        this.writeVarInt(array.length);
        for (long i : array) {
            writeLong(i);
        }
        return this;
    }

    public long[] readLongArray(long[] array) {
        return readLongArray(array, this.readableBytes() / 8);
    }

    public long[] readLongArray(long[] array, int maxLength) {
        int i = readVarInt();
        if (array == null || array.length != i) {
            if (i > maxLength) {
                throw new DecoderException("LongArray with size " + i + " is bigger than allowed " + maxLength);
            }
            array = new long[i];
        }
        for (int j = 0; j < array.length; ++j) {
            array[j] = readLong();
        }
        return array;
    }

    public <T extends Enum<T>> T readEnumValue(Class<T> enumClass) {
        return enumClass.getEnumConstants()[readVarInt()];
    }

    public CoronaPacketBuffer writeEnumValue(Enum<?> value) {
        return writeVarInt(value.ordinal());
    }

    public int readVarInt() {
        int i = 0;
        int j = 0;
        while (true) {
            byte b0 = this.readByte();
            i |= (b0 & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((b0 & 0x80) != 0x80) {
                return i;
            }
        }
    }

    public long readVarLong() {
        long i = 0L;
        int j = 0;
        while (true) {
            byte b0 = this.readByte();
            i |= (b0 & 0x7F) << j++ * 7;
            if (j > 10) {
                throw new RuntimeException("VarLong too big");
            }
            if ((b0 & 0x80) != 0x80) {
                return i;
            }
        }
    }

    public CoronaPacketBuffer writeUniqueId(UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
        return this;
    }

    public UUID readUniqueId() {
        return new UUID(readLong(), readLong());
    }

    public CoronaPacketBuffer writeVarInt(int input) {
        while ((input & 0xFFFFFF80) != 0x0) {
            writeByte((input & 0x7F) | 0x80);
            input >>>= 7;
        }
        writeByte(input);
        return this;
    }

    public CoronaPacketBuffer writeVarLong(long value) {
        while ((value & 0xFFFFFFFFFFFFFF80L) != 0x0L) {
            writeByte((int) (value & 0x7FL) | 0x80);
            value >>>= 7;
        }
        writeByte((int) value);
        return this;
    }

    public String readString(int maxLength) {
        int i = readVarInt();
        if (i > maxLength * 4) {
            throw new DecoderException(
                    "The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")");
        }
        if (i < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        }
        String s = toString(readerIndex(), i, StandardCharsets.UTF_8);
        readerIndex(readerIndex() + i);
        if (s.length() > maxLength) {
            throw new DecoderException(
                    "The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")");
        }
        return s;
    }

    public CoronaPacketBuffer writeString(String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 32767) {
            throw new EncoderException("String too big (was " + bytes.length + " bytes encoded, max " + 32767 + ")");
        }
        writeVarInt(bytes.length);
        writeBytes(bytes);
        return this;
    }

    public Date readTime() {
        return new Date(readLong());
    }

    public CoronaPacketBuffer writeTime(Date time) {
        writeLong(time.getTime());
        return this;
    }

    public int capacity() {
        return delegate.capacity();
    }

    public ByteBuf capacity(int p_capacity_1_) {
        return delegate.capacity(p_capacity_1_);
    }

    public int maxCapacity() {
        return delegate.maxCapacity();
    }

    public ByteBufAllocator alloc() {
        return delegate.alloc();
    }

    public ByteOrder order() {
        return delegate.order();
    }

    public ByteBuf order(ByteOrder p_order_1_) {
        return delegate.order(p_order_1_);
    }

    public ByteBuf unwrap() {
        return delegate.unwrap();
    }

    public boolean isDirect() {
        return delegate.isDirect();
    }

    public boolean isReadOnly() {
        return delegate.isReadOnly();
    }

    public ByteBuf asReadOnly() {
        return delegate.asReadOnly();
    }

    public int readerIndex() {
        return delegate.readerIndex();
    }

    public ByteBuf readerIndex(int p_readerIndex_1_) {
        return delegate.readerIndex(p_readerIndex_1_);
    }

    public int writerIndex() {
        return delegate.writerIndex();
    }

    public ByteBuf writerIndex(int p_writerIndex_1_) {
        return delegate.writerIndex(p_writerIndex_1_);
    }

    public ByteBuf setIndex(int p_setIndex_1_, int p_setIndex_2_) {
        return delegate.setIndex(p_setIndex_1_, p_setIndex_2_);
    }

    public int readableBytes() {
        return delegate.readableBytes();
    }

    public int writableBytes() {
        return delegate.writableBytes();
    }

    public int maxWritableBytes() {
        return delegate.maxWritableBytes();
    }

    public boolean isReadable() {
        return delegate.isReadable();
    }

    public boolean isReadable(int p_isReadable_1_) {
        return delegate.isReadable(p_isReadable_1_);
    }

    public boolean isWritable() {
        return delegate.isWritable();
    }

    public boolean isWritable(int p_isWritable_1_) {
        return delegate.isWritable(p_isWritable_1_);
    }

    public ByteBuf clear() {
        return delegate.clear();
    }

    public ByteBuf markReaderIndex() {
        return delegate.markReaderIndex();
    }

    public ByteBuf resetReaderIndex() {
        return delegate.resetReaderIndex();
    }

    public ByteBuf markWriterIndex() {
        return delegate.markWriterIndex();
    }

    public ByteBuf resetWriterIndex() {
        return delegate.resetWriterIndex();
    }

    public ByteBuf discardReadBytes() {
        return delegate.discardReadBytes();
    }

    public ByteBuf discardSomeReadBytes() {
        return delegate.discardSomeReadBytes();
    }

    public ByteBuf ensureWritable(int p_ensureWritable_1_) {
        return delegate.ensureWritable(p_ensureWritable_1_);
    }

    public int ensureWritable(int p_ensureWritable_1_, boolean p_ensureWritable_2_) {
        return delegate.ensureWritable(p_ensureWritable_1_, p_ensureWritable_2_);
    }

    public boolean getBoolean(int p_getBoolean_1_) {
        return delegate.getBoolean(p_getBoolean_1_);
    }

    public byte getByte(int p_getByte_1_) {
        return delegate.getByte(p_getByte_1_);
    }

    public short getUnsignedByte(int p_getUnsignedByte_1_) {
        return delegate.getUnsignedByte(p_getUnsignedByte_1_);
    }

    public short getShort(int p_getShort_1_) {
        return delegate.getShort(p_getShort_1_);
    }

    public short getShortLE(int p_getShortLE_1_) {
        return delegate.getShortLE(p_getShortLE_1_);
    }

    public int getUnsignedShort(int p_getUnsignedShort_1_) {
        return delegate.getUnsignedShort(p_getUnsignedShort_1_);
    }

    public int getUnsignedShortLE(int p_getUnsignedShortLE_1_) {
        return delegate.getUnsignedShortLE(p_getUnsignedShortLE_1_);
    }

    public int getMedium(int p_getMedium_1_) {
        return delegate.getMedium(p_getMedium_1_);
    }

    public int getMediumLE(int p_getMediumLE_1_) {
        return delegate.getMediumLE(p_getMediumLE_1_);
    }

    public int getUnsignedMedium(int p_getUnsignedMedium_1_) {
        return delegate.getUnsignedMedium(p_getUnsignedMedium_1_);
    }

    public int getUnsignedMediumLE(int p_getUnsignedMediumLE_1_) {
        return delegate.getUnsignedMediumLE(p_getUnsignedMediumLE_1_);
    }

    public int getInt(int p_getInt_1_) {
        return delegate.getInt(p_getInt_1_);
    }

    public int getIntLE(int p_getIntLE_1_) {
        return delegate.getIntLE(p_getIntLE_1_);
    }

    public long getUnsignedInt(int p_getUnsignedInt_1_) {
        return delegate.getUnsignedInt(p_getUnsignedInt_1_);
    }

    public long getUnsignedIntLE(int p_getUnsignedIntLE_1_) {
        return delegate.getUnsignedIntLE(p_getUnsignedIntLE_1_);
    }

    public long getLong(int p_getLong_1_) {
        return delegate.getLong(p_getLong_1_);
    }

    public long getLongLE(int p_getLongLE_1_) {
        return delegate.getLongLE(p_getLongLE_1_);
    }

    public char getChar(int p_getChar_1_) {
        return delegate.getChar(p_getChar_1_);
    }

    public float getFloat(int p_getFloat_1_) {
        return delegate.getFloat(p_getFloat_1_);
    }

    public double getDouble(int p_getDouble_1_) {
        return delegate.getDouble(p_getDouble_1_);
    }

    public ByteBuf getBytes(int p_getBytes_1_, ByteBuf p_getBytes_2_) {
        return delegate.getBytes(p_getBytes_1_, p_getBytes_2_);
    }

    public ByteBuf getBytes(int p_getBytes_1_, ByteBuf p_getBytes_2_, int p_getBytes_3_) {
        return delegate.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_);
    }

    public ByteBuf getBytes(int p_getBytes_1_, ByteBuf p_getBytes_2_, int p_getBytes_3_, int p_getBytes_4_) {
        return delegate.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_, p_getBytes_4_);
    }

    public ByteBuf getBytes(int p_getBytes_1_, byte[] p_getBytes_2_) {
        return delegate.getBytes(p_getBytes_1_, p_getBytes_2_);
    }

    public ByteBuf getBytes(int p_getBytes_1_, byte[] p_getBytes_2_, int p_getBytes_3_, int p_getBytes_4_) {
        return delegate.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_, p_getBytes_4_);
    }

    public ByteBuf getBytes(int p_getBytes_1_, ByteBuffer p_getBytes_2_) {
        return delegate.getBytes(p_getBytes_1_, p_getBytes_2_);
    }

    public ByteBuf getBytes(int p_getBytes_1_, OutputStream p_getBytes_2_, int p_getBytes_3_) throws IOException {
        return delegate.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_);
    }

    public int getBytes(int p_getBytes_1_, GatheringByteChannel p_getBytes_2_, int p_getBytes_3_) throws IOException {
        return delegate.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_);
    }

    public int getBytes(int p_getBytes_1_, FileChannel p_getBytes_2_, long p_getBytes_3_, int p_getBytes_5_) throws IOException {
        return delegate.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_, p_getBytes_5_);
    }

    public CharSequence getCharSequence(int p_getCharSequence_1_, int p_getCharSequence_2_, Charset p_getCharSequence_3_) {
        return delegate.getCharSequence(p_getCharSequence_1_, p_getCharSequence_2_, p_getCharSequence_3_);
    }

    public ByteBuf setBoolean(int p_setBoolean_1_, boolean p_setBoolean_2_) {
        return delegate.setBoolean(p_setBoolean_1_, p_setBoolean_2_);
    }

    public ByteBuf setByte(int p_setByte_1_, int p_setByte_2_) {
        return delegate.setByte(p_setByte_1_, p_setByte_2_);
    }

    public ByteBuf setShort(int p_setShort_1_, int p_setShort_2_) {
        return delegate.setShort(p_setShort_1_, p_setShort_2_);
    }

    public ByteBuf setShortLE(int p_setShortLE_1_, int p_setShortLE_2_) {
        return delegate.setShortLE(p_setShortLE_1_, p_setShortLE_2_);
    }

    public ByteBuf setMedium(int p_setMedium_1_, int p_setMedium_2_) {
        return delegate.setMedium(p_setMedium_1_, p_setMedium_2_);
    }

    public ByteBuf setMediumLE(int p_setMediumLE_1_, int p_setMediumLE_2_) {
        return delegate.setMediumLE(p_setMediumLE_1_, p_setMediumLE_2_);
    }

    public ByteBuf setInt(int p_setInt_1_, int p_setInt_2_) {
        return delegate.setInt(p_setInt_1_, p_setInt_2_);
    }

    public ByteBuf setIntLE(int p_setIntLE_1_, int p_setIntLE_2_) {
        return delegate.setIntLE(p_setIntLE_1_, p_setIntLE_2_);
    }

    public ByteBuf setLong(int p_setLong_1_, long p_setLong_2_) {
        return delegate.setLong(p_setLong_1_, p_setLong_2_);
    }

    public ByteBuf setLongLE(int p_setLongLE_1_, long p_setLongLE_2_) {
        return delegate.setLongLE(p_setLongLE_1_, p_setLongLE_2_);
    }

    public ByteBuf setChar(int p_setChar_1_, int p_setChar_2_) {
        return delegate.setChar(p_setChar_1_, p_setChar_2_);
    }

    public ByteBuf setFloat(int p_setFloat_1_, float p_setFloat_2_) {
        return delegate.setFloat(p_setFloat_1_, p_setFloat_2_);
    }

    public ByteBuf setDouble(int p_setDouble_1_, double p_setDouble_2_) {
        return delegate.setDouble(p_setDouble_1_, p_setDouble_2_);
    }

    public ByteBuf setBytes(int p_setBytes_1_, ByteBuf p_setBytes_2_) {
        return delegate.setBytes(p_setBytes_1_, p_setBytes_2_);
    }

    public ByteBuf setBytes(int p_setBytes_1_, ByteBuf p_setBytes_2_, int p_setBytes_3_) {
        return delegate.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_);
    }

    public ByteBuf setBytes(int p_setBytes_1_, ByteBuf p_setBytes_2_, int p_setBytes_3_, int p_setBytes_4_) {
        return delegate.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_, p_setBytes_4_);
    }

    public ByteBuf setBytes(int p_setBytes_1_, byte[] p_setBytes_2_) {
        return delegate.setBytes(p_setBytes_1_, p_setBytes_2_);
    }

    public ByteBuf setBytes(int p_setBytes_1_, byte[] p_setBytes_2_, int p_setBytes_3_, int p_setBytes_4_) {
        return delegate.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_, p_setBytes_4_);
    }

    public ByteBuf setBytes(int p_setBytes_1_, ByteBuffer p_setBytes_2_) {
        return delegate.setBytes(p_setBytes_1_, p_setBytes_2_);
    }

    public int setBytes(int p_setBytes_1_, InputStream p_setBytes_2_, int p_setBytes_3_) throws IOException {
        return delegate.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_);
    }

    public int setBytes(int p_setBytes_1_, ScatteringByteChannel p_setBytes_2_, int p_setBytes_3_) throws IOException {
        return delegate.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_);
    }

    public int setBytes(int p_setBytes_1_, FileChannel p_setBytes_2_, long p_setBytes_3_, int p_setBytes_5_) throws IOException {
        return delegate.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_, p_setBytes_5_);
    }

    public ByteBuf setZero(int p_setZero_1_, int p_setZero_2_) {
        return delegate.setZero(p_setZero_1_, p_setZero_2_);
    }

    public int setCharSequence(int p_setCharSequence_1_, CharSequence p_setCharSequence_2_, Charset p_setCharSequence_3_) {
        return delegate.setCharSequence(p_setCharSequence_1_, p_setCharSequence_2_, p_setCharSequence_3_);
    }

    public boolean readBoolean() {
        return delegate.readBoolean();
    }

    public byte readByte() {
        return delegate.readByte();
    }

    public short readUnsignedByte() {
        return delegate.readUnsignedByte();
    }

    public short readShort() {
        return delegate.readShort();
    }

    public short readShortLE() {
        return delegate.readShortLE();
    }

    public int readUnsignedShort() {
        return delegate.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return delegate.readUnsignedShortLE();
    }

    public int readMedium() {
        return delegate.readMedium();
    }

    public int readMediumLE() {
        return delegate.readMediumLE();
    }

    public int readUnsignedMedium() {
        return delegate.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return delegate.readUnsignedMediumLE();
    }

    public int readInt() {
        return delegate.readInt();
    }

    public int readIntLE() {
        return delegate.readIntLE();
    }

    public long readUnsignedInt() {
        return delegate.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return delegate.readUnsignedIntLE();
    }

    public long readLong() {
        return delegate.readLong();
    }

    public long readLongLE() {
        return delegate.readLongLE();
    }

    public char readChar() {
        return delegate.readChar();
    }

    public float readFloat() {
        return delegate.readFloat();
    }

    public double readDouble() {
        return delegate.readDouble();
    }

    public ByteBuf readBytes(int p_readBytes_1_) {
        return delegate.readBytes(p_readBytes_1_);
    }

    public ByteBuf readSlice(int p_readSlice_1_) {
        return delegate.readSlice(p_readSlice_1_);
    }

    public ByteBuf readRetainedSlice(int p_readRetainedSlice_1_) {
        return delegate.readRetainedSlice(p_readRetainedSlice_1_);
    }

    public ByteBuf readBytes(ByteBuf p_readBytes_1_) {
        return delegate.readBytes(p_readBytes_1_);
    }

    public ByteBuf readBytes(ByteBuf p_readBytes_1_, int p_readBytes_2_) {
        return delegate.readBytes(p_readBytes_1_, p_readBytes_2_);
    }

    public ByteBuf readBytes(ByteBuf p_readBytes_1_, int p_readBytes_2_, int p_readBytes_3_) {
        return delegate.readBytes(p_readBytes_1_, p_readBytes_2_, p_readBytes_3_);
    }

    public ByteBuf readBytes(byte[] p_readBytes_1_) {
        return delegate.readBytes(p_readBytes_1_);
    }

    public ByteBuf readBytes(byte[] p_readBytes_1_, int p_readBytes_2_, int p_readBytes_3_) {
        return delegate.readBytes(p_readBytes_1_, p_readBytes_2_, p_readBytes_3_);
    }

    public ByteBuf readBytes(ByteBuffer p_readBytes_1_) {
        return delegate.readBytes(p_readBytes_1_);
    }

    public ByteBuf readBytes(OutputStream p_readBytes_1_, int p_readBytes_2_) throws IOException {
        return delegate.readBytes(p_readBytes_1_, p_readBytes_2_);
    }

    public int readBytes(GatheringByteChannel p_readBytes_1_, int p_readBytes_2_) throws IOException {
        return delegate.readBytes(p_readBytes_1_, p_readBytes_2_);
    }

    public CharSequence readCharSequence(int p_readCharSequence_1_, Charset p_readCharSequence_2_) {
        return delegate.readCharSequence(p_readCharSequence_1_, p_readCharSequence_2_);
    }

    public int readBytes(FileChannel p_readBytes_1_, long p_readBytes_2_, int p_readBytes_4_) throws IOException {
        return delegate.readBytes(p_readBytes_1_, p_readBytes_2_, p_readBytes_4_);
    }

    public ByteBuf skipBytes(int p_skipBytes_1_) {
        return delegate.skipBytes(p_skipBytes_1_);
    }

    public ByteBuf writeBoolean(boolean p_writeBoolean_1_) {
        return delegate.writeBoolean(p_writeBoolean_1_);
    }

    public ByteBuf writeByte(int p_writeByte_1_) {
        return delegate.writeByte(p_writeByte_1_);
    }

    public ByteBuf writeShort(int p_writeShort_1_) {
        return delegate.writeShort(p_writeShort_1_);
    }

    public ByteBuf writeShortLE(int p_writeShortLE_1_) {
        return delegate.writeShortLE(p_writeShortLE_1_);
    }

    public ByteBuf writeMedium(int p_writeMedium_1_) {
        return delegate.writeMedium(p_writeMedium_1_);
    }

    public ByteBuf writeMediumLE(int p_writeMediumLE_1_) {
        return delegate.writeMediumLE(p_writeMediumLE_1_);
    }

    public ByteBuf writeInt(int p_writeInt_1_) {
        return delegate.writeInt(p_writeInt_1_);
    }

    public ByteBuf writeIntLE(int p_writeIntLE_1_) {
        return delegate.writeIntLE(p_writeIntLE_1_);
    }

    public ByteBuf writeLong(long p_writeLong_1_) {
        return delegate.writeLong(p_writeLong_1_);
    }

    public ByteBuf writeLongLE(long p_writeLongLE_1_) {
        return delegate.writeLongLE(p_writeLongLE_1_);
    }

    public ByteBuf writeChar(int p_writeChar_1_) {
        return delegate.writeChar(p_writeChar_1_);
    }

    public ByteBuf writeFloat(float p_writeFloat_1_) {
        return delegate.writeFloat(p_writeFloat_1_);
    }

    public ByteBuf writeDouble(double p_writeDouble_1_) {
        return delegate.writeDouble(p_writeDouble_1_);
    }

    public ByteBuf writeBytes(ByteBuf p_writeBytes_1_) {
        return delegate.writeBytes(p_writeBytes_1_);
    }

    public ByteBuf writeBytes(ByteBuf p_writeBytes_1_, int p_writeBytes_2_) {
        return delegate.writeBytes(p_writeBytes_1_, p_writeBytes_2_);
    }

    public ByteBuf writeBytes(ByteBuf p_writeBytes_1_, int p_writeBytes_2_, int p_writeBytes_3_) {
        return delegate.writeBytes(p_writeBytes_1_, p_writeBytes_2_, p_writeBytes_3_);
    }

    public ByteBuf writeBytes(byte[] p_writeBytes_1_) {
        return delegate.writeBytes(p_writeBytes_1_);
    }

    public ByteBuf writeBytes(byte[] p_writeBytes_1_, int p_writeBytes_2_, int p_writeBytes_3_) {
        return delegate.writeBytes(p_writeBytes_1_, p_writeBytes_2_, p_writeBytes_3_);
    }

    public ByteBuf writeBytes(ByteBuffer p_writeBytes_1_) {
        return delegate.writeBytes(p_writeBytes_1_);
    }

    public int writeBytes(InputStream p_writeBytes_1_, int p_writeBytes_2_) throws IOException {
        return delegate.writeBytes(p_writeBytes_1_, p_writeBytes_2_);
    }

    public int writeBytes(ScatteringByteChannel p_writeBytes_1_, int p_writeBytes_2_) throws IOException {
        return delegate.writeBytes(p_writeBytes_1_, p_writeBytes_2_);
    }

    public int writeBytes(FileChannel p_writeBytes_1_, long p_writeBytes_2_, int p_writeBytes_4_) throws IOException {
        return delegate.writeBytes(p_writeBytes_1_, p_writeBytes_2_, p_writeBytes_4_);
    }

    public ByteBuf writeZero(int p_writeZero_1_) {
        return delegate.writeZero(p_writeZero_1_);
    }

    public int writeCharSequence(CharSequence p_writeCharSequence_1_, Charset p_writeCharSequence_2_) {
        return delegate.writeCharSequence(p_writeCharSequence_1_, p_writeCharSequence_2_);
    }

    public int indexOf(int p_indexOf_1_, int p_indexOf_2_, byte p_indexOf_3_) {
        return delegate.indexOf(p_indexOf_1_, p_indexOf_2_, p_indexOf_3_);
    }

    public int bytesBefore(byte p_bytesBefore_1_) {
        return delegate.bytesBefore(p_bytesBefore_1_);
    }

    public int bytesBefore(int p_bytesBefore_1_, byte p_bytesBefore_2_) {
        return delegate.bytesBefore(p_bytesBefore_1_, p_bytesBefore_2_);
    }

    public int bytesBefore(int p_bytesBefore_1_, int p_bytesBefore_2_, byte p_bytesBefore_3_) {
        return delegate.bytesBefore(p_bytesBefore_1_, p_bytesBefore_2_, p_bytesBefore_3_);
    }

    public int forEachByte(ByteProcessor p_forEachByte_1_) {
        return delegate.forEachByte(p_forEachByte_1_);
    }

    public int forEachByte(int p_forEachByte_1_, int p_forEachByte_2_, ByteProcessor p_forEachByte_3_) {
        return delegate.forEachByte(p_forEachByte_1_, p_forEachByte_2_, p_forEachByte_3_);
    }

    public int forEachByteDesc(ByteProcessor p_forEachByteDesc_1_) {
        return delegate.forEachByteDesc(p_forEachByteDesc_1_);
    }

    public int forEachByteDesc(int p_forEachByteDesc_1_, int p_forEachByteDesc_2_, ByteProcessor p_forEachByteDesc_3_) {
        return delegate.forEachByteDesc(p_forEachByteDesc_1_, p_forEachByteDesc_2_, p_forEachByteDesc_3_);
    }

    public ByteBuf copy() {
        return delegate.copy();
    }

    public ByteBuf copy(int p_copy_1_, int p_copy_2_) {
        return delegate.copy(p_copy_1_, p_copy_2_);
    }

    public ByteBuf slice() {
        return delegate.slice();
    }

    public ByteBuf retainedSlice() {
        return delegate.retainedSlice();
    }

    public ByteBuf slice(int p_slice_1_, int p_slice_2_) {
        return delegate.slice(p_slice_1_, p_slice_2_);
    }

    public ByteBuf retainedSlice(int p_retainedSlice_1_, int p_retainedSlice_2_) {
        return delegate.retainedSlice(p_retainedSlice_1_, p_retainedSlice_2_);
    }

    public ByteBuf duplicate() {
        return delegate.duplicate();
    }

    public ByteBuf retainedDuplicate() {
        return delegate.retainedDuplicate();
    }

    public int nioBufferCount() {
        return delegate.nioBufferCount();
    }

    public ByteBuffer nioBuffer() {
        return delegate.nioBuffer();
    }

    public ByteBuffer nioBuffer(int p_nioBuffer_1_, int p_nioBuffer_2_) {
        return delegate.nioBuffer(p_nioBuffer_1_, p_nioBuffer_2_);
    }

    public ByteBuffer internalNioBuffer(int p_internalNioBuffer_1_, int p_internalNioBuffer_2_) {
        return delegate.internalNioBuffer(p_internalNioBuffer_1_, p_internalNioBuffer_2_);
    }

    public ByteBuffer[] nioBuffers() {
        return delegate.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int p_nioBuffers_1_, int p_nioBuffers_2_) {
        return delegate.nioBuffers(p_nioBuffers_1_, p_nioBuffers_2_);
    }

    public boolean hasArray() {
        return delegate.hasArray();
    }

    public byte[] array() {
        return delegate.array();
    }

    public int arrayOffset() {
        return delegate.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return delegate.hasMemoryAddress();
    }

    public long memoryAddress() {
        return delegate.memoryAddress();
    }

    public String toString(Charset p_toString_1_) {
        return delegate.toString(p_toString_1_);
    }

    public String toString(int p_toString_1_, int p_toString_2_, Charset p_toString_3_) {
        return delegate.toString(p_toString_1_, p_toString_2_, p_toString_3_);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean equals(Object p_equals_1_) {
        return delegate.equals(p_equals_1_);
    }

    public int compareTo(ByteBuf p_compareTo_1_) {
        return delegate.compareTo(p_compareTo_1_);
    }

    public String toString() {
        return delegate.toString();
    }

    public ByteBuf retain(int p_retain_1_) {
        return delegate.retain(p_retain_1_);
    }

    public ByteBuf retain() {
        return delegate.retain();
    }

    public ByteBuf touch() {
        return delegate.touch();
    }

    public ByteBuf touch(Object p_touch_1_) {
        return delegate.touch(p_touch_1_);
    }

    public int refCnt() {
        return delegate.refCnt();
    }

    public boolean release() {
        return delegate.release();
    }

    public boolean release(int p_release_1_) {
        return delegate.release(p_release_1_);
    }
}
