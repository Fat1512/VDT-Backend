package com.example.demo.controller;

import com.example.demo.dto.APIResponse;
import com.example.demo.dto.APIResponseMessage;
import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@CrossOrigin
public class StudentController {

    private final StudentRepository studentRepository;

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<APIResponse> deleteStudent(@PathVariable Integer id) {
        studentRepository.deleteById(id);
        APIResponse response = APIResponse.builder()
                .status(HttpStatus.NO_CONTENT)
                .message(APIResponseMessage.SUCCESSFULLY_DELETED.name())
                .data(null)
                .build();
        return ResponseEntity.ok(response);

    }
}
