package managers;

import java.io.*;
import java.nio.ByteBuffer;

public class SerializationManager {
    public synchronized static ByteBuffer serialize(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)){

            oos.writeObject(obj);

            ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
            return bb;
        }
        catch (IOException e) {
            System.out.println("Ошибка сериализации!");
            throw new RuntimeException();
        }
    }

    public synchronized static Object deserialize(ByteBuffer bb) throws IOException, ClassNotFoundException {
        byte[] arr = bb.array();
        ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        ObjectInputStream ois = new ObjectInputStream(bais);

        return ois.readObject();
    }

    public synchronized static boolean isEmpty(byte[] arr){
        for (byte n : arr)
            if (n != 0)
                return false;
        return true;
    }
}
