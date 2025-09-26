package com.bot.common.util;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SignatureED25519 {

    public static Pair<Ed25519PrivateKeyParameters, Ed25519PublicKeyParameters> getSeed(String seed) {
        String seedStr = seed.length() < 32 ? seed + seed.substring(0, 32 - seed.length()) : seed;
        byte[] seedBytes = seedStr.getBytes(StandardCharsets.UTF_8);
        Ed25519PrivateKeyParameters prk = new Ed25519PrivateKeyParameters(seedBytes, 0);
        Ed25519PublicKeyParameters puk = prk.generatePublicKey();
        return new Pair<>(prk, puk);
    }

    public static byte[] getData(String text, String time) {
        byte[] t1 = time.getBytes(StandardCharsets.UTF_8);
        byte[] t2 = text.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(t1.length + t2.length);
        buffer.put(t1);
        buffer.put(t2);

        return buffer.array();
    }

    public static String sign(String secret, byte[] data) {
        Ed25519PrivateKeyParameters prk = getSeed(secret).getFirst();
        Ed25519Signer ed = new Ed25519Signer();
        ed.init(true, prk);
        ed.update(data, 0, data.length);
        return bytesToHex(ed.generateSignature());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(Character.forDigit((b >> 4) & 0xf, 16)).append(Character.forDigit(b & 0xf, 16));
        }
        return result.toString();
    }

    public static class Pair<T, U> {
        private final T first;
        private final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() {
            return first;
        }

        public U getSecond() {
            return second;
        }
    }
}
