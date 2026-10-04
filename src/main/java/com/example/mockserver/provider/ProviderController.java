package com.example.mockserver.provider;

import com.example.mockserver.provider.dto.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vendor")
public class ProviderController {

    private final ProviderService providerService;
    private final ProviderKeyMaterial keyMaterial;

    public ProviderController(ProviderService providerService, ProviderKeyMaterial keyMaterial) {
        this.providerService = providerService;
        this.keyMaterial = keyMaterial;
    }

    @PostMapping("/user/check/extended")
    public UserCheckDto checkUser(@RequestBody(required = false) String data) {
        return new UserCheckDto(true, true, false, false, false, true, true, true);
    }

    @PostMapping("/document/auth/online")
    public FollowUpDto login(@RequestBody(required = false) String data) {
        return new FollowUpDto("mock-thread", "123"); // "mock-login-transaction");
    }

    @PostMapping("/user/certificate/get")
    public CertificateDto getCertificate(@RequestBody(required = false) String data) {
        return keyMaterial.agentCertificate();
    }

    @PostMapping("/document/doc/online")
    public FollowUpGroupDto sendDeclaration(
            @RequestPart("data") String data,
            @RequestPart("document") MultipartFile doc) {
        return providerService.sendDeclaration(data, doc);
    }

    @PostMapping("/document/group/hash/online")
    public FollowUpDto signGroup(@RequestBody(required = false) String data) {
        return providerService.signGroup(data);
    }

    @PostMapping("/document/status")
    public StatusDto checkDocumentStatus(@RequestBody(required = false) String data) {
        return new StatusDto(2, false);
    }

    @PostMapping("/document/group/status")
    public StatusDto checkGroupStatus(@RequestBody(required = false) String data) {
        return new StatusDto(2, false);
    }

    @PostMapping(value = "/document/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] downloadDocument(@RequestBody(required = false) String data) {
        return providerService.downloadDeclaration(data);
    }

    @PostMapping(value = "/document/group/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] downloadGroupDocument(@RequestBody(required = false) String data) {
        return providerService.downloadGroupSignatures(data);
    }

    @PostMapping("/hash/group/certificate/check")
    public OtpCertificateStatusDto checkOtpCertificate(@RequestBody(required = false) String data) {
        return new OtpCertificateStatusDto(true, false);
    }

    @PostMapping("/hash/group/certificate/get")
    public CertificateDto getOtpCertificate(@RequestBody(required = false) String data) {
        return keyMaterial.clientCertificate();
    }

    @PostMapping("/hash/group/offline/file/init")
    public FollowUpDto initializeOtp(
            @RequestPart("data") String data,
            @RequestPart("signedfile") MultipartFile signedFile) {
        return providerService.initializeOtp(data, signedFile);
    }

    @PostMapping("/hash/group/offline/activate")
    public void activateOtp(@RequestBody(required = false) String data) {
    }

    @PostMapping("/hash/group/offline/send")
    public void signOtp(@RequestBody(required = false) String data) {
        providerService.signOtp(data);
    }

    @PostMapping("/hash/group/offline/check")
    public StatusDto checkOtpStatus(@RequestBody(required = false) String data) {
        return new StatusDto(2, false);
    }

    @PostMapping(value = "/hash/group/offline/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] downloadOtpDocument(@RequestBody(required = false) String data) {
        return providerService.downloadOtpSignatures(data);
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
