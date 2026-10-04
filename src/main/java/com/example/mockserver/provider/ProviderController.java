package com.example.mockserver.provider;

import com.example.mockserver.provider.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vendor")
public class ProviderController {

    private final ObjectMapper objectMapper;
    private final ProviderService providerService;
    private final ProviderKeyMaterial keyMaterial;

    public ProviderController(
            ObjectMapper objectMapper,
            ProviderService providerService,
            ProviderKeyMaterial keyMaterial
    ) {
        this.objectMapper = objectMapper;
        this.providerService = providerService;
        this.keyMaterial = keyMaterial;
    }

    @PostMapping("/user/check/extended")
    public UserCheckDto checkUser(@RequestBody BaseSpec spec) {
        return new UserCheckDto(true, true, false, false, false, true, true, true);
    }

    @PostMapping("/document/auth/online")
    public FollowUpDto login(@RequestBody LoginSpec spec) {
        return new FollowUpDto("mock-thread", "123");
    }

    @PostMapping("/user/certificate/get")
    public CertificateDto getCertificate(@RequestBody CertificateSpec spec) {
        return keyMaterial.agentCertificate();
    }

    @PostMapping("/document/doc/online")
    public FollowUpGroupDto sendDeclaration(
            @RequestPart("data") String data,
            @RequestPart("document") MultipartFile doc
    ) throws JsonProcessingException {
        DeclarationSpec spec = objectMapper.readValue(data, DeclarationSpec.class);
        return providerService.sendDeclaration(spec, doc);
    }

    @PostMapping("/document/group/hash/online")
    public FollowUpDto signGroup(@RequestBody SignSpec spec) {
        return providerService.signGroup(spec);
    }

    @PostMapping("/document/status")
    public StatusDto checkDocumentStatus(@RequestBody DocumentSpec spec) {
        return new StatusDto(2, false);
    }

    @PostMapping("/document/group/status")
    public StatusDto checkGroupStatus(@RequestBody DocumentSpec spec) {
        return new StatusDto(2, false);
    }

    @PostMapping(value = "/document/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] downloadDocument(@RequestBody DocumentSpec spec) {
        return providerService.downloadDeclaration(spec);
    }

    @PostMapping(value = "/document/group/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] downloadGroupDocument(@RequestBody DocumentSpec spec) {
        return providerService.downloadGroupSignatures(spec);
    }

    @PostMapping("/hash/group/certificate/check")
    public OtpCertificateStatusDto checkOtpCertificate(@RequestBody DocumentSpec spec) {
        return new OtpCertificateStatusDto(true, false);
    }

    @PostMapping("/hash/group/certificate/get")
    public CertificateDto getOtpCertificate(@RequestBody DocumentSpec spec) {
        return keyMaterial.clientCertificate();
    }

    @PostMapping("/hash/group/offline/file/init")
    public FollowUpDto initializeOtp(
            @RequestPart("data") String data,
            @RequestPart("signedfile") MultipartFile signedFile
    ) throws JsonProcessingException {
        InitSpec spec = objectMapper.readValue(data, InitSpec.class);
        return providerService.initializeOtp(spec, signedFile);
    }

    @PostMapping("/hash/group/offline/activate")
    public void activateOtp(@RequestBody ActivationSpec spec) {
    }

    @PostMapping("/hash/group/offline/send")
    public void signOtp(@RequestBody OtpSignSpec spec) {
        providerService.signOtp(spec);
    }

    @PostMapping("/hash/group/offline/check")
    public StatusDto checkOtpStatus(@RequestBody DocumentSpec spec) {
        return new StatusDto(2, false);
    }

    @PostMapping(value = "/hash/group/offline/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] downloadOtpDocument(@RequestBody DocumentSpec spec) {
        return providerService.downloadOtpSignatures(spec);
    }

    /*@GetMapping("/mock-data/files/{name}")
    public byte[] getUploadedFile(@PathVariable String name) {
        final byte[] file = mockService.getUploadedFile(name);
        if (file == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No mock upload named '" + name + "'.");
        }
        return file;
    }*/

    /*@GetMapping("/mock-data/requests/{name}")
    public String getReceivedRequest(@PathVariable String name) {
        final String request = mockService.getReceivedRequest(name);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No mock request named '" + name + "'.");
        }
        return request;
    }*/
}
