package com.github.mertunctuncer.playervaults.util;

import com.github.mertunctuncer.playervaults.model.PlayerVault;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;

public class VaultSerializer {
    public static String serialize(PlayerVault vault) {
        try (ByteArrayOutputStream outputstream = new ByteArrayOutputStream()) {
            DataOutput output = new DataOutputStream(outputstream);

            ItemStack[] items = vault.getContents();
            output.writeInt(items.length);

            for(ItemStack item : items) {
                if(item == null) {
                    output.writeInt(0);
                    continue;
                }

                byte[] serializedItem = item.serializeAsBytes();
                output.writeInt(serializedItem.length);
                output.write(serializedItem);
            }

            return Base64Coder.encodeLines(outputstream.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Could not serialize PlayerVault", e);
        }
    }

    public static ItemStack[] deserialize(String base64) {
        byte[] data = Base64Coder.decodeLines(base64);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            DataInputStream input = new DataInputStream(inputStream);
            int itemCount = input.readInt();
            ItemStack[] items = new ItemStack[itemCount];

            for (int i = 0; i < itemCount ; i++) {
                int byteLength = input.readInt();
                if(byteLength == 0) {
                    continue;
                }

                byte[] itemBytes = new byte[byteLength];
                int readCount = input.read(itemBytes);

                if(readCount != byteLength) {
                    throw new RuntimeException("Invalid item length read into the buffer, expected " + byteLength + " but read " + readCount);
                }

                items[i] = ItemStack.deserializeBytes(itemBytes);
            }

            return items;
        } catch (IOException e) {
            throw new RuntimeException("Could not deserialize PlayerVault", e);
        }
    }
}
