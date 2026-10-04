package com.example.mockserver.provider;

import com.example.mockserver.provider.dto.*;
import com.example.mockserver.provider.ProviderPackageWriter.MockSignature;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProviderService {

    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentMap<String, DocumentState> declarations = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<String>> otpDocuments = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<MockSignature>> otpSignatures = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<MockSignature>> groupSignatures = new ConcurrentHashMap<>();
    // private final ConcurrentMap<String, byte[]> uploadedFiles = new ConcurrentHashMap<>();
    // private final ConcurrentMap<String, String> requests = new ConcurrentHashMap<>();

    private final ProviderKeyMaterial keyMaterial;
    private final ProviderPackageWriter packageWriter;

    public ProviderService(ProviderKeyMaterial keyMaterial, ProviderPackageWriter packageWriter) {
        this.keyMaterial = keyMaterial;
        this.packageWriter = packageWriter;
    }

    public FollowUpGroupDto sendDeclaration(DeclarationSpec spec, MultipartFile doc) {
        String id = nextId("mock-declaration-");
        DocumentState state = new DocumentState(fileName(doc, "declaration.pdf"), bytes(doc));
        declarations.put(id, state);
        // uploadedFiles.put("declaration", state.bytes());
        // requests.put("declaration", safe(data));
        return new FollowUpGroupDto("mock-thread", List.of(new TransactionDto(id)));
    }

    public FollowUpDto initializeOtp(InitSpec spec, MultipartFile signedFile) {
        String id = nextId("mock-otp-");
        List<String> names = spec.documents() == null ? List.of()
                : spec.documents().stream().map(InitSpec.DocumentDescription::description).toList();
        otpDocuments.put(id, names);
        // uploadedFiles.put("otp", bytes(signedFile));
        // requests.put("otp-init", safe(data));
        return new FollowUpDto("mock-thread", id);
    }

    public FollowUpDto signGroup(SignSpec spec) {
        List<MockSignature> signatures = new ArrayList<>();
        if (spec.documents() != null) {
            for (var document : spec.documents()) {
                signatures.add(sign("agent", defaultName(document.description()), document.hash()));
            }
        }
        String id = nextId("mock-group-");
        groupSignatures.put(id, List.copyOf(signatures));
        // requests.put("group-sign", safe(data));
        return new FollowUpDto("mock-thread", id);
    }

    public void signOtp(OtpSignSpec spec) {
        String id = spec.transactionID();
        List<String> names = otpDocuments.getOrDefault(id, List.of());
        List<String> hashes = spec.hash() == null ? List.of() : spec.hash();
        List<MockSignature> signatures = new ArrayList<>();
        for (int i = 0; i < hashes.size(); i++) {
            String name = i < names.size() ? names.get(i) : "document-" + (i + 1) + ".pdf";
            signatures.add(sign("client", name, hashes.get(i)));
        }
        otpSignatures.put(id, List.copyOf(signatures));
        // requests.put("otp-sign", safe(data));
    }

    public byte[] downloadDeclaration(DocumentSpec spec) {
        String id = spec.transactionID();
        DocumentState state = declarations.get(id);
        if (state == null) throw new IllegalArgumentException("Unknown declaration transaction: " + id);
        try {
            return packageWriter.directSignedDocument(id, state.fileName(), state.bytes());
        } catch (Exception e) {
            throw new IllegalStateException("Could not create mock declaration download", e);
        }
    }

    public byte[] downloadGroupSignatures(DocumentSpec spec) { return downloadHashes(spec, groupSignatures); }
    public byte[] downloadOtpSignatures(DocumentSpec spec) { return downloadHashes(spec, otpSignatures); }

    // public byte[] getUploadedFile(String name) { return uploadedFiles.get(name); }
    // public String getReceivedRequest(String name) { return requests.get(name); }

    private byte[] downloadHashes(DocumentSpec spec, ConcurrentMap<String, List<MockSignature>> store) {
        String id = spec.transactionID();
        List<MockSignature> signatures = store.get(id);
        if (signatures == null) throw new IllegalArgumentException("No mock signatures for transaction: " + id);
        try { return packageWriter.signedHashes(id, signatures); }
        catch (Exception e) { throw new IllegalStateException("Could not create mock signature download", e); }
    }

    private MockSignature sign(String role, String name, String hash) {
        try { return new MockSignature(name, keyMaterial.signHash(role, hash)); }
        catch (Exception e) { throw new IllegalArgumentException("Could not sign document hash for " + name, e); }
    }

    private String nextId(String prefix) {
        return prefix + sequence.incrementAndGet();
    }

    /*private static String safe(String value) { return value == null ? "{}" : value; }*/

    private static String defaultName(String name) { return name == null || name.isBlank() ? "document.pdf" : name; }

    private static String fileName(MultipartFile file, String fallback) {
        String name = file.getOriginalFilename();
        return name == null || name.isBlank() ? fallback : name;
    }

    private static byte[] bytes(MultipartFile file) {
        try { return file.getBytes(); }
        catch (Exception e) { throw new IllegalArgumentException("Could not read uploaded mock file", e); }
    }

    private record DocumentState(String fileName, byte[] bytes) { }
}
