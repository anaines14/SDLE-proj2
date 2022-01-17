package main.model.timelines;

import java.io.Serializable;
import java.security.*;

public class Cipher implements Serializable {
    private byte[] sign = null;

    public boolean hasSignature(){
        return sign != null;
    }

    public void addSignature(String toCipher, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withDSA");
            signature.initSign(privateKey);
            signature.update(toCipher.getBytes());
            sign = signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }

    public boolean verifySignature(String toCipher, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withDSA");
            signature.initVerify(publicKey);
            signature.update(toCipher.getBytes());
            return signature.verify(sign);
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public byte[] getSign() {
        return sign;
    }

}
