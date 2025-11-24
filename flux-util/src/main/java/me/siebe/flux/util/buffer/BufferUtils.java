package me.siebe.flux.util.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utility class for reading binary data from ByteBuffers.
 * <p>
 * Provides methods to read various data types from buffers, handling
 * byte order and type conversions.
 */
public final class BufferUtils {
    private BufferUtils() {
        // Utility class
    }

    /**
     * Reads a 32-bit unsigned integer from a ByteBuffer.
     *
     * @param buffer the buffer to read from
     * @return the unsigned integer value as a long
     */
    public static long readUInt32(ByteBuffer buffer) {
        return buffer.getInt() & 0xFFFFFFFFL;
    }

    /**
     * Reads a 16-bit unsigned integer from a ByteBuffer.
     *
     * @param buffer the buffer to read from
     * @return the unsigned integer value as an int
     */
    public static int readUInt16(ByteBuffer buffer) {
        return buffer.getShort() & 0xFFFF;
    }

    /**
     * Reads a 8-bit unsigned integer from a ByteBuffer.
     *
     * @param buffer the buffer to read from
     * @return the unsigned integer value as an int
     */
    public static int readUInt8(ByteBuffer buffer) {
        return buffer.get() & 0xFF;
    }

    /**
     * Reads a float array from a ByteBuffer.
     *
     * @param buffer the buffer to read from
     * @param count  number of floats to read
     * @return array of floats
     */
    public static float[] readFloats(ByteBuffer buffer, int count) {
        float[] result = new float[count];
        for (int i = 0; i < count; i++) {
            result[i] = buffer.getFloat();
        }
        return result;
    }

    /**
     * Reads an integer array from a ByteBuffer.
     *
     * @param buffer the buffer to read from
     * @param count  number of integers to read
     * @return array of integers
     */
    public static int[] readInts(ByteBuffer buffer, int count) {
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = buffer.getInt();
        }
        return result;
    }

    /**
     * Reads a short array from a ByteBuffer.
     *
     * @param buffer the buffer to read from
     * @param count  number of shorts to read
     * @return array of shorts
     */
    public static short[] readShorts(ByteBuffer buffer, int count) {
        short[] result = new short[count];
        for (int i = 0; i < count; i++) {
            result[i] = buffer.getShort();
        }
        return result;
    }

    /**
     * Reads a byte array from a ByteBuffer.
     *
     * @param buffer the buffer to read from
     * @param count  number of bytes to read
     * @return array of bytes
     */
    public static byte[] readBytes(ByteBuffer buffer, int count) {
        byte[] result = new byte[count];
        buffer.get(result);
        return result;
    }

    /**
     * Creates a view of a ByteBuffer with the specified byte order.
     *
     * @param buffer    the buffer to create a view from
     * @param byteOrder the desired byte order
     * @return a new buffer with the specified byte order (shares the same backing data)
     */
    public static ByteBuffer withByteOrder(ByteBuffer buffer, ByteOrder byteOrder) {
        ByteBuffer view = buffer.duplicate();
        view.order(byteOrder);
        return view;
    }

    /**
     * Creates a slice of a ByteBuffer starting at the given offset with the given length.
     * Creates a direct buffer copy for better compatibility with native libraries like STBImage.
     *
     * @param buffer the buffer to slice
     * @param offset byte offset from the start
     * @param length length of the slice
     * @return a new direct ByteBuffer containing the slice
     */
    public static ByteBuffer slice(ByteBuffer buffer, int offset, int length) {
        if (offset < 0 || length < 0 || offset + length > buffer.capacity()) {
            throw new IllegalArgumentException("Invalid slice parameters: offset=" + offset + ", length=" + length + ", capacity=" + buffer.capacity());
        }

        int oldPosition = buffer.position();
        int oldLimit = buffer.limit();
        try {
            buffer.position(offset);
            buffer.limit(offset + length);

            // Create a direct buffer copy (STBImage requires direct buffers)
            ByteBuffer slice = org.lwjgl.BufferUtils.createByteBuffer(length);
            slice.put(buffer);
            slice.flip();

            return slice;
        } finally {
            buffer.position(oldPosition);
            buffer.limit(oldLimit);
        }
    }

    /**
     * Creates a ByteBuffer from a byte array.
     * Creates a direct buffer for better compatibility with native libraries like STBImage.
     *
     * @param bytes the byte array
     * @return a new direct ByteBuffer containing the bytes
     */
    public static ByteBuffer createByteBuffer(byte[] bytes) {
        ByteBuffer buffer = org.lwjgl.BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }
}

