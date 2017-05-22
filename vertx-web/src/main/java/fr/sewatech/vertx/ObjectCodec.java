package fr.sewatech.vertx;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.io.*;

public class ObjectCodec<T extends Object> implements MessageCodec<T, Object> {
    @Override
    public void encodeToWire(Buffer buffer, Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        buffer.appendBytes(baos.toByteArray());
    }

    @Override
    public Object decodeFromWire(int pos, Buffer buffer) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer.getBytes());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object transform(Object hello) {
        return hello;
    }

    @Override
    public String name() {
        return this.getClass().getName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
