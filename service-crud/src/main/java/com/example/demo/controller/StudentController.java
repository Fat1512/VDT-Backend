package com.example.demo.controller;

import com.example.demo.dto.APIResponse;
import com.example.demo.dto.APIResponseMessage;
import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepository;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@CrossOrigin
@RequiredArgsConstructor
public class StudentController {

    private final Bucket bucket;

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final StudentRepository studentRepository;

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> deleteStudent(@PathVariable Integer id) {
        studentRepository.deleteById(id);
        APIResponse response = APIResponse.builder()
                .status(HttpStatus.NO_CONTENT)
                .message(APIResponseMessage.SUCCESSFULLY_DELETED.name())
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<APIResponse> update(@RequestBody Student student) {
        student.setRole("USER");
        studentRepository.save(student);

        APIResponse response = APIResponse.builder()
                .status(HttpStatus.CREATED)
                .message(APIResponseMessage.SUCCESSFULLY_CREATED.name())
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("{studentId}")
    public ResponseEntity<APIResponse> getStudent(@PathVariable Integer studentId) {
        Student student = studentRepository.findById(studentId).get();
        APIResponse response = APIResponse.builder()
                .status(HttpStatus.OK)
                .message(APIResponseMessage.SUCCESSFULLY_RETRIEVED.name())
                .data(student)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<APIResponse> getStudents() {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            logger.info("Allowed - remaining requests: {}", probe.getRemainingTokens());
            List<Student> students = studentRepository.findAll();
            APIResponse response = APIResponse.builder()
                    .status(HttpStatus.OK)
                    .message(APIResponseMessage.SUCCESSFULLY_RETRIEVED.name())
                    .data(students)
                    .build();
            return ResponseEntity.ok(response);
        }
        logger.info("Over limit");

        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }


    @PostMapping
    public ResponseEntity<APIResponse> createStudent(@RequestBody Student student) {
        student.setRole("USER");
        studentRepository.save(student) ;

        APIResponse response = APIResponse.builder()
                .status(HttpStatus.CREATED)
                .message(APIResponseMessage.SUCCESSFULLY_CREATED.name())
                .data(null)
                .build();
        return ResponseEntity.ok(response);

    }
}
