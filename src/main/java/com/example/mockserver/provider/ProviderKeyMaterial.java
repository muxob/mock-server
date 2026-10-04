package com.example.mockserver.provider;

import com.example.mockserver.provider.dto.CertificateDto;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Component
public class ProviderKeyMaterial {

    @Value("${provider.package-public-key}")
    private Resource packagePublicKeyResource;

    @Value("${provider.client-private-key}")
    private Resource clientPrivateKeyResource;

    @Value("${provider.agent-private-key}")
    private Resource agentPrivateKeyResource;

    @Value("${provider.client-certificate}")
    private Resource clientCertificateResource;

    @Value("${provider.agent-certificate}")
    private Resource agentCertificateResource;

    private PublicKey packagePublicKey;
    private PrivateKey clientPrivateKey;
    private PrivateKey agentPrivateKey;
    private X509Certificate clientCertificate;
    private X509Certificate agentCertificate;
    private String clientCertificateBase64;
    private String agentCertificateBase64;

    @PostConstruct
    public void load() throws Exception {
        this.packagePublicKey = this.readPublicKey(this.packagePublicKeyResource);
        this.clientPrivateKey = this.readPrivateKey(this.clientPrivateKeyResource);
        this.agentPrivateKey = this.readPrivateKey(this.agentPrivateKeyResource);
        this.clientCertificateBase64 = this.readCertificateText(this.clientCertificateResource);
        this.agentCertificateBase64 = this.readCertificateText(this.agentCertificateResource);
        this.clientCertificate = this.parseCertificate(this.clientCertificateBase64);
        this.agentCertificate = this.parseCertificate(this.agentCertificateBase64);

        this.ensureKeyMatchesCertificate(this.clientPrivateKey, this.clientCertificate, "client");
        this.ensureKeyMatchesCertificate(this.agentPrivateKey, this.agentCertificate, "agent");
    }

    public PublicKey packagePublicKey() {
        return packagePublicKey;
    }

    public CertificateDto clientCertificate() {
        return this.toDto(this.clientCertificate, this.clientCertificateBase64);
    }

    public CertificateDto agentCertificate() {
        return this.toDto(this.agentCertificate, this.agentCertificateBase64);
    }

    public byte[] signHash(String role, String hashBase64) throws Exception {
        final byte[] hash = Base64.getDecoder().decode(hashBase64);
        final byte[] digestInfo = new DigestInfo(
                new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE), hash).getEncoded();
        final PrivateKey privateKey = "client".equals(role) ? this.clientPrivateKey : this.agentPrivateKey;

        // Provider receives a precomputed SHA-256 digest. Sign its DigestInfo directly
        // so the resulting value is RSA-SHA256 over the original PAdES signed attributes.
        final Signature signer = Signature.getInstance("NONEwithRSA");
        signer.initSign(privateKey);
        signer.update(digestInfo);
        return signer.sign();
    }

    private CertificateDto toDto(X509Certificate certificate, String base64) {
        return new CertificateDto(
                certificate.getSerialNumber().toString(16).toUpperCase(),
                base64,
                1,
                20000,
                certificate.getNotAfter().getTime() / 1000);
    }

    private PublicKey readPublicKey(Resource resource) throws Exception {
        try (PEMParser parser = new PEMParser(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            final Object pemObject = parser.readObject();
            if (pemObject instanceof org.bouncycastle.asn1.x509.SubjectPublicKeyInfo publicKeyInfo) {
                return new JcaPEMKeyConverter().getPublicKey(publicKeyInfo);
            }
            throw new IllegalArgumentException("Expected an X.509 SubjectPublicKeyInfo public-key PEM in " + resource.getDescription());
        }
    }

    private PrivateKey readPrivateKey(Resource resource) throws Exception {
        try (PEMParser parser = new PEMParser(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            final Object pemObject = parser.readObject();
            final JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            if (pemObject instanceof PEMKeyPair pemKeyPair) {
                return converter.getKeyPair(pemKeyPair).getPrivate();
            }
            if (pemObject instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo privateKeyInfo) {
                return converter.getPrivateKey(privateKeyInfo);
            }
            throw new IllegalArgumentException("Expected an RSA private-key PEM in " + resource.getDescription());
        }
    }

    private String readCertificateText(Resource resource) throws Exception {
        final String base64;
        try (var input = resource.getInputStream()) {
            base64 = new String(input.readAllBytes(), StandardCharsets.US_ASCII).trim();
        }
        final byte[] der = Base64.getMimeDecoder().decode(base64);
        return Base64.getEncoder().encodeToString(der);
    }

    private X509Certificate parseCertificate(String base64) throws Exception {
        final byte[] der = Base64.getDecoder().decode(base64);
        return (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new java.io.ByteArrayInputStream(der));
    }

    private void ensureKeyMatchesCertificate(PrivateKey privateKey, X509Certificate certificate, String role)
            throws Exception {
        final byte[] probe = "mock-provider-key-check".getBytes(StandardCharsets.UTF_8);
        final Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(probe);
        final byte[] signature = signer.sign();
        final Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(certificate.getPublicKey());
        verifier.update(probe);
        if (!verifier.verify(signature)) {
            throw new IllegalArgumentException("The configured " + role + " private key does not match its X.509 certificate.");
        }
    }
}
