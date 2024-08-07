package com.docdoc.oauth2.openfeign;

import com.docdoc.oauth2.model.dto.AuthDataDTO;
import com.docdoc.oauth2.model.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "Doctor", url = "http://localhost:8080/doctor")
public interface UserRequest {

    @PostMapping("/authorize")
    ResponseEntity<UserResponseDTO> getOAuth2(@RequestBody AuthDataDTO authDataDTO);
}
