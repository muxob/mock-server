package com.example.mockserver.provider;

import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ProviderPackageWriter {

    private static final int RIJNDAEL_BLOCK_SIZE_BYTES = 32;
    private static final int DIRECT_DOWNLOAD_CHUNK_CHARS = 7296;
    private static final int DIRECT_DOWNLOAD_CHUNK_BYTES = DIRECT_DOWNLOAD_CHUNK_CHARS * 3 / 4;

    private final ProviderKeyMaterial keyMaterial;
    private final SecureRandom secureRandom = new SecureRandom();

    public ProviderPackageWriter(ProviderKeyMaterial keyMaterial) {
        this.keyMaterial = keyMaterial;
    }

    public byte[] directSignedDocument(String transactionId, String fileName, byte[] pdfBytes) throws Exception {
        final ByteArrayOutputStream innerZipBytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(innerZipBytes)) {
            final String signedName = stripPdfExtension(fileName) + "_OUT_SIGNED.pdf";
            writeEntry(zip, signedName, pdfBytes);
        }

        final byte[] rijndaelKey = randomBytes(RIJNDAEL_BLOCK_SIZE_BYTES);
        final byte[] iv = randomBytes(RIJNDAEL_BLOCK_SIZE_BYTES);
        final String encryptedContent = encryptDirectPayload(innerZipBytes.toByteArray(), rijndaelKey, iv);
        return outerPackage(transactionId, fileName, encryptedContent, rijndaelKey, iv);
    }

    public byte[] signedHashes(String rootTransactionId, List<MockSignature> signatures) throws Exception {
        if (signatures.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an Provider signature package without documents.");
        }

        final ByteArrayOutputStream outerZipBytes = new ByteArrayOutputStream();
        try (ZipOutputStream outerZip = new ZipOutputStream(outerZipBytes)) {
            int index = 0;
            for (MockSignature signature : signatures) {
                index++;
                final String transactionId = rootTransactionId + "-file-" + index;
                final ByteArrayOutputStream innerZipBytes = new ByteArrayOutputStream();
                try (ZipOutputStream innerZip = new ZipOutputStream(innerZipBytes)) {
                    writeEntry(innerZip,
                            stripPdfExtension(signature.fileName()) + "_OUT_SIGNED_HASH.xml",
                            signature.signatureBytes());
                }

                final byte[] rijndaelKey = randomBytes(RIJNDAEL_BLOCK_SIZE_BYTES);
                final byte[] iv = randomBytes(RIJNDAEL_BLOCK_SIZE_BYTES);
                final String encryptedContent = encryptHashPayload(innerZipBytes.toByteArray(), rijndaelKey, iv);
                writeOuterEnvelope(outerZip, transactionId, signature.fileName(), encryptedContent, rijndaelKey, iv);
            }
        }
        return outerZipBytes.toByteArray();
    }

    private byte[] outerPackage(String transactionId,
                                String fileName,
                                String encryptedContent,
                                byte[] rijndaelKey,
                                byte[] iv) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            writeOuterEnvelope(zip, transactionId, fileName, encryptedContent, rijndaelKey, iv);
        }
        return output.toByteArray();
    }

    private void writeOuterEnvelope(ZipOutputStream zip,
                                    String transactionId,
                                    String fileName,
                                    String encryptedContent,
                                    byte[] rijndaelKey,
                                    byte[] iv) throws Exception {
        final PublicKey publicKey = this.keyMaterial.packagePublicKey();
        final Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsa.init(Cipher.ENCRYPT_MODE, publicKey);
        final byte[] encryptedKey = rsa.doFinal(rijndaelKey);

        writeEntry(zip, transactionId + "_OUT.enc.key",
                Base64.getEncoder().encode(encryptedKey));
        writeEntry(zip, transactionId + "_OUT.enc.iv", iv);
        writeEntry(zip, transactionId + "_OUT.enc", encryptedContent.getBytes(StandardCharsets.US_ASCII));
        writeEntry(zip, transactionId + "_OUT.enc.filename", fileName.getBytes(StandardCharsets.UTF_8));
    }

    private String encryptHashPayload(byte[] innerZip, byte[] key, byte[] iv) throws Exception {
        final byte[] base64InnerZip = Base64.getEncoder().encode(innerZip);
        return Base64.getEncoder().encodeToString(encryptChunk(base64InnerZip, key, iv));
    }

    private String encryptDirectPayload(byte[] innerZip, byte[] key, byte[] iv) throws Exception {
        final StringBuilder chunks = new StringBuilder();
        final byte[] base64Zip = Base64.getEncoder().encode(innerZip);
        for (int offset = 0; offset < base64Zip.length; offset += DIRECT_DOWNLOAD_CHUNK_BYTES) {
            final int length = Math.min(DIRECT_DOWNLOAD_CHUNK_BYTES, base64Zip.length - offset);
            final byte[] base64ZipChunk = java.util.Arrays.copyOfRange(base64Zip, offset, offset + length);
            final byte[] encryptedChunk = encryptChunk(base64ZipChunk, key, iv);
            chunks.append(Base64.getEncoder().encodeToString(encryptedChunk));
        }
        return chunks.toString();
    }

    private byte[] encryptChunk(byte[] clearText, byte[] key, byte[] iv) throws Exception {
        final PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                CBCBlockCipher.newInstance(new RijndaelEngine(256)), new ZeroBytePadding());
        cipher.init(true, new ParametersWithIV(new KeyParameter(key), iv));

        final byte[] output = new byte[cipher.getOutputSize(clearText.length)];
        int length = cipher.processBytes(clearText, 0, clearText.length, output, 0);
        length += cipher.doFinal(output, length);
        return java.util.Arrays.copyOf(output, length);
    }

    private byte[] randomBytes(int length) {
        final byte[] bytes = new byte[length];
        this.secureRandom.nextBytes(bytes);
        return bytes;
    }

    private void writeEntry(ZipOutputStream zip, String name, byte[] bytes) throws Exception {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(bytes);
        zip.closeEntry();
    }

    private String stripPdfExtension(String fileName) {
        return fileName.toLowerCase().endsWith(".pdf")
                ? fileName.substring(0, fileName.length() - 4)
                : fileName;
    }

    public record MockSignature(String fileName, byte[] signatureBytes) {
    }
}
