package com.example.mockserver.provider;

import com.example.mockserver.provider.dto.FollowUpDto;
import com.example.mockserver.provider.dto.FollowUpGroupDto;
import com.example.mockserver.provider.dto.TransactionDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper;
    private final ProviderKeyMaterial keyMaterial;
    private final ProviderPackageWriter packageWriter;

    public ProviderService(
            ObjectMapper objectMapper,
            ProviderKeyMaterial keyMaterial,
            ProviderPackageWriter packageWriter)
    {
        this.objectMapper = objectMapper;
        this.keyMaterial = keyMaterial;
        this.packageWriter = packageWriter;
    }

    public FollowUpGroupDto sendDeclaration(String data, MultipartFile doc) {
        String id = nextId("mock-declaration-");
        DocumentState state = new DocumentState(fileName(doc, "declaration.pdf"), bytes(doc));
        declarations.put(id, state);
        // uploadedFiles.put("declaration", state.bytes());
        // requests.put("declaration", safe(data));
        return new FollowUpGroupDto("mock-thread", List.of(new TransactionDto(id)));
    }

    public FollowUpDto initializeOtp(String data, MultipartFile signedFile) {
        String id = nextId("mock-otp-");
        JsonNode body = parse(data);
        List<String> names = descriptions(body);
        otpDocuments.put(id, names);
        // uploadedFiles.put("otp", bytes(signedFile));
        // requests.put("otp-init", safe(data));
        return new FollowUpDto("mock-thread", id);
    }

    public FollowUpDto signGroup(String data) {
        JsonNode body = parse(data);
        List<MockSignature> signatures = new ArrayList<>();
        for (JsonNode document : body.path("documents")) {
            signatures.add(sign("agent", document.path("description").asText("document.pdf"), document.path("hash").asText()));
        }
        String id = nextId("mock-group-");
        groupSignatures.put(id, List.copyOf(signatures));
        // requests.put("group-sign", safe(data));
        return new FollowUpDto("mock-thread", id);
    }

    public void signOtp(String data) {
        JsonNode body = parse(data);
        String id = body.path("transactionID").asText();
        List<String> names = otpDocuments.getOrDefault(id, List.of());
        List<String> hashes = new ArrayList<>();
        body.path("hash").forEach(node -> hashes.add(node.asText()));
        List<MockSignature> signatures = new ArrayList<>();
        for (int i = 0; i < hashes.size(); i++) {
            String name = i < names.size() ? names.get(i) : "document-" + (i + 1) + ".pdf";
            signatures.add(sign("client", name, hashes.get(i)));
        }
        otpSignatures.put(id, List.copyOf(signatures));
        // requests.put("otp-sign", safe(data));
    }

    public byte[] downloadDeclaration(String data) {
        JsonNode body = parse(data);
        String id = body.path("transactionID").asText();
        DocumentState state = declarations.get(id);
        if (state == null) throw new IllegalArgumentException("Unknown declaration transaction: " + id);
        try {
            return packageWriter.directSignedDocument(id, state.fileName(), state.bytes());
        } catch (Exception e) {
            throw new IllegalStateException("Could not create mock declaration download", e);
        }
    }

    public byte[] downloadGroupSignatures(String data) {
        return downloadHashes(data, groupSignatures);
    }

    public byte[] downloadOtpSignatures(String data) {
        return downloadHashes(data, otpSignatures);
    }

    // public byte[] getUploadedFile(String name) { return uploadedFiles.get(name); }
    // public String getReceivedRequest(String name) { return requests.get(name); }

    private byte[] downloadHashes(String data, ConcurrentMap<String, List<MockSignature>> store) {
        String id = parse(data).path("transactionID").asText();
        List<MockSignature> signatures = store.get(id);
        if (signatures == null) throw new IllegalArgumentException("No mock signatures for transaction: " + id);
        try { return packageWriter.signedHashes(id, signatures); }
        catch (Exception e) { throw new IllegalStateException("Could not create mock signature download", e); }
    }

    private MockSignature sign(String role, String name, String hash) {
        try { return new MockSignature(name, keyMaterial.signHash(role, hash)); }
        catch (Exception e) { throw new IllegalArgumentException("Could not sign document hash for " + name, e); }
    }

    private List<String> descriptions(JsonNode body) {
        List<String> names = new ArrayList<>();
        body.path("documents").forEach(node -> names.add(node.path("description").asText("document.pdf")));
        return names;
    }

    private JsonNode parse(String json) {
        try { return objectMapper.readTree(json == null ? "{}" : json); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid Provider mock request JSON", e); }
    }

    private String nextId(String prefix) {
        return prefix + sequence.incrementAndGet();
    }

    /*private static String safe(String value) { return value == null ? "{}" : value; }*/

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
